package day.cloudy.apps.assistant;

import android.annotation.SuppressLint;
import android.app.WallpaperManager;
import android.app.assist.AssistContent;
import android.app.assist.AssistStructure;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.service.voice.VoiceInteractionSession;
import android.support.annotation.RequiresApi;
import android.support.design.widget.AppBarLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.orm.query.Condition;
import com.orm.query.Select;

import java.util.List;

import butterknife.BindView;
import butterknife.OnTextChanged;
import day.cloudy.apps.assistant.activity.SettingsActivity;
import day.cloudy.apps.assistant.model.ApplicationItem;
import day.cloudy.apps.assistant.model.RecentApplication;
import day.cloudy.apps.assistant.recycler.ListSpacingDecoration;
import day.cloudy.apps.assistant.recycler.OnItemClickListener;
import day.cloudy.apps.assistant.recycler.OnItemLongClickListener;
import day.cloudy.apps.assistant.recycler.UnderlineFirstRowDecoration;
import day.cloudy.apps.assistant.recycler.adapter.ApplicationAdapter;
import day.cloudy.apps.assistant.shortcut.ActionItem;
import day.cloudy.apps.assistant.shortcut.PopupWindows;
import day.cloudy.apps.assistant.shortcut.QuickAction;
import day.cloudy.apps.assistant.task.LoadApplicationsTask;
import day.cloudy.apps.assistant.util.DispUtils;
import day.cloudy.apps.assistant.util.ShortCutUtils;

import static butterknife.ButterKnife.bind;
import static butterknife.ButterKnife.findById;

/**
 * Created by Gaelan Bolger on 12/15/2016.
 * Assist service
 */
@SuppressWarnings("WeakerAccess")
public class AssistInteractionSession extends VoiceInteractionSession {

    private static final String TAG = AssistInteractionSession.class.getSimpleName();

    @BindView(R.id.app_bar_layout)
    AppBarLayout mAppBarLayout;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;

    private boolean mQueriedOnHide;
    private View mContent;
    private ApplicationAdapter mAdapter;
    private AsyncTask mGetApplicationsTask;
    private RecyclerView.AdapterDataObserver mEmptyObserver = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onChanged() {
            if (TextUtils.isEmpty(mAdapter.getFilterText())) {
                findById(mContent, R.id.progress_bar)
                        .setVisibility(mAdapter.getItemCount() > 0 ? View.GONE : View.VISIBLE);
                findById(mContent, R.id.text_view_empty).setVisibility(View.GONE);
            } else {
                findById(mContent, R.id.text_view_empty)
                        .setVisibility(mAdapter.getItemCount() > 0 ? View.GONE : View.VISIBLE);
                findById(mContent, R.id.progress_bar).setVisibility(View.GONE);
            }
        }
    };

    public AssistInteractionSession(Context context) {
        super(context);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mAdapter = new ApplicationAdapter(getContext());
        mAdapter.setOnItemClickListener(new OnItemClickListener<ApplicationItem>() {
            @Override
            public void onItemClick(RecyclerView.ViewHolder holder, ApplicationItem applicationItem) {
                Log.d(TAG, "onItemClick: ");
                holder.itemView.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                startApplication(applicationItem);
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1)
            mAdapter.setOnItemLongClickListener(new OnItemLongClickListener<ApplicationItem>() {
                @Override
                public boolean onItemLongClick(RecyclerView.ViewHolder holder, ApplicationItem item) {
                    List<ShortcutInfo> shortcuts = item.shortcuts;
                    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1
                            && null != shortcuts && shortcuts.size() > 0
                            && showAppShortcutsPopupWindow(holder, shortcuts);
                }
            });
    }

    @SuppressLint("InflateParams")
    @Override
    public View onCreateContentView() {
        WallpaperManager wm = WallpaperManager.getInstance(getContext());
        Drawable wall = wm.getFastDrawable();
        mContent = getLayoutInflater().inflate(R.layout.service_assist_interaction, null, false);
        mContent.setBackground(wall);
        bind(this, mContent);

        mToolbar.inflateMenu(R.menu.service_assist_interaction);
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.item_settings:
                        Intent intent = new Intent(getContext(), SettingsActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        getContext().startActivity(intent);
                        hide();
                        return true;
                }
                return false;
            }
        });

        mRecyclerView.setHasFixedSize(true);
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 6);
        mRecyclerView.setLayoutManager(layoutManager);
        final int itemSpacing = DispUtils.dp(16);
        mRecyclerView.addItemDecoration(new ListSpacingDecoration(itemSpacing));
        mRecyclerView.addItemDecoration(new UnderlineFirstRowDecoration(layoutManager, itemSpacing / 2));
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            final int elevation = (int) (Resources.getSystem().getDisplayMetrics().density * 8);
            final float threshold = 60f;
            int ydy = 0;

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                ydy += dy;
                float percent = (float) (Math.min(60, ydy)) / threshold;
                mAppBarLayout.setElevation(elevation * percent);
            }
        });
        mRecyclerView.setAdapter(mAdapter);
        return mContent;
    }

    @Override
    public void onShow(Bundle args, int showFlags) {
        super.onShow(args, showFlags);
        Log.d(TAG, "onShow: ");
        mAdapter.registerAdapterDataObserver(mEmptyObserver);
        if (!mQueriedOnHide)
            queryApplicationPackages();
        mQueriedOnHide = false;
    }

    @Override
    public void onHide() {
        super.onHide();
        Log.d(TAG, "onHide: ");
        PopupWindows.dismiss();
        mRecyclerView.smoothScrollToPosition(0);
        mAdapter.unregisterAdapterDataObserver(mEmptyObserver);
        mQueriedOnHide = true;
        queryApplicationPackages();
    }

    @Override
    public void onDestroy() {
        cancelTasks();
        super.onDestroy();
    }

    @Override
    public void onHandleAssist(Bundle data, AssistStructure structure, AssistContent content) {
        super.onHandleAssist(data, structure, content);
        Log.d(TAG, "onHandleAssist: ");
    }

    @Override
    public void onHandleScreenshot(Bitmap screenshot) {
        super.onHandleScreenshot(screenshot);
        Log.d(TAG, "onHandleScreenshot: ");
    }

    @OnTextChanged(R.id.edit_text_filter)
    public void onFilterTextChanged(CharSequence text) {
        mAdapter.setFilterText(text);
    }

    private void queryApplicationPackages() {
        cancelTasks();
        mGetApplicationsTask = new LoadApplicationsTask(getContext(), new LoadApplicationsTask.OnCompleteListener() {
            @Override
            public void onComplete(List<ApplicationItem> applicationItems) {
                mAdapter.setApplications(applicationItems);
            }
        }).execute(getPackageManager());
    }

    private void cancelTasks() {
        if (null != mGetApplicationsTask) {
            mGetApplicationsTask.cancel(true);
            mGetApplicationsTask = null;
        }
    }

    private void startApplication(ApplicationItem applicationItem) {
        ApplicationInfo applicationInfo = applicationItem.applicationInfo;
        String packageName = applicationInfo.packageName;
        Intent intent = getPackageManager().getLaunchIntentForPackage(packageName);
        if (null != intent) {
            updateLaunchRecord(packageName, intent.getComponent().getClassName());
            try {
                getContext().startActivity(intent);
                hide();
            } catch (ActivityNotFoundException e) {
                Log.e(TAG, String.format("onItemClick: Activity not found for package, %s", applicationItem.label));
                Toast.makeText(getContext(), "Application not found", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        Log.w(TAG, String.format("onItemClick: Launch intent not found for package, %s", applicationItem.label));
        Toast.makeText(getContext(), "Application not found", Toast.LENGTH_SHORT).show();
    }

    private void updateLaunchRecord(String packageName, String activityName) {
        RecentApplication record = Select.from(RecentApplication.class)
                .where(Condition.prop(RecentApplication.COLUMN_PACKAGE_NAME).eq(packageName))
                .where(Condition.prop(RecentApplication.COLUMN_ACTIVITY_NAME).eq(activityName))
                .first();
        if (null == record)
            record = new RecentApplication(packageName, activityName);
        record.incrementLaunchCount();
        record.save();
    }

    @RequiresApi(api = Build.VERSION_CODES.N_MR1)
    private boolean showAppShortcutsPopupWindow(RecyclerView.ViewHolder holder, final List<ShortcutInfo> shortcuts) {
        PopupWindows.dismiss();
        if (!ShortCutUtils.getLauncherApps(getContext()).hasShortcutHostPermission()) {
            Toast.makeText(getContext(), "Not set as default launcher", Toast.LENGTH_SHORT).show();
            return false;
        }
        QuickAction quickAction = new QuickAction(getContext());
        for (int i = 0; i < shortcuts.size(); i++) {
            ShortcutInfo shortcut = shortcuts.get(i);
            ActionItem actionItem = new ActionItem(i,
                    shortcut.getShortLabel().toString(),
                    ShortCutUtils.getShortcutIcon(getContext(), shortcut, DisplayMetrics.DENSITY_DEFAULT));
            quickAction.addActionItem(actionItem);
        }
        quickAction.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {
            @Override
            public void onItemClick(QuickAction source, int pos, int actionId) {
                if (ShortCutUtils.startShortcut(getContext(), shortcuts.get(pos)))
                    hide();
                else
                    Toast.makeText(getContext(), "Error launching shortcut", Toast.LENGTH_SHORT).show();
                PopupWindows.dismiss();
            }
        });
        quickAction.setFocusable(true);
        quickAction.show(holder.itemView);
        return true;
    }

    private PackageManager getPackageManager() {
        return getContext().getPackageManager();
    }

}
