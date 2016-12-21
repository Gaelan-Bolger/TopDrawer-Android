package day.cloudy.apps.assistant;

import android.annotation.SuppressLint;
import android.app.WallpaperManager;
import android.app.assist.AssistContent;
import android.app.assist.AssistStructure;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.LauncherApps;
import android.content.pm.LauncherApps.ShortcutQuery;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.os.UserHandle;
import android.service.voice.VoiceInteractionSession;
import android.support.annotation.RequiresApi;
import android.support.design.widget.AppBarLayout;
import android.support.v4.content.LocalBroadcastManager;
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
import day.cloudy.apps.assistant.activity.DialogActivity;
import day.cloudy.apps.assistant.activity.SettingsActivity;
import day.cloudy.apps.assistant.model.ApplicationItem;
import day.cloudy.apps.assistant.model.RecentApplication;
import day.cloudy.apps.assistant.recycler.FirstRowDividerRecyclerView;
import day.cloudy.apps.assistant.recycler.OnItemClickListener;
import day.cloudy.apps.assistant.recycler.OnItemLongClickListener;
import day.cloudy.apps.assistant.recycler.adapter.ApplicationItemAdapter;
import day.cloudy.apps.assistant.shortcut.ActionItem;
import day.cloudy.apps.assistant.shortcut.QuickAction;
import day.cloudy.apps.assistant.task.LoadApplicationsTask;
import day.cloudy.apps.assistant.util.ShortcutUtils;

import static butterknife.ButterKnife.bind;
import static butterknife.ButterKnife.findById;

/**
 * Created by Gaelan Bolger on 12/15/2016.
 * Assist service
 */
@SuppressWarnings("WeakerAccess")
public class AssistInteractionSession extends VoiceInteractionSession {

    private static final String TAG = AssistInteractionSession.class.getSimpleName();
    private static final String ACTION_REFRESH_APPS = "day.cloudy.apps.assistant.action.REFRESH_APPS";
    private static final UserHandle USER_HANDLE = Process.myUserHandle();

    private View vContent;

    @BindView(R.id.app_bar_layout)
    AppBarLayout vAppBarLayout;
    @BindView(R.id.toolbar)
    Toolbar vToolbar;
    @BindView(R.id.recycler_view)
    FirstRowDividerRecyclerView vRecyclerView;

    private boolean mQueryOnShow = true;
    private AsyncTask mGetApplicationsTask;
    private LauncherApps mLauncherApps;
    private ShortcutQuery mShortcutQuery;
    private QuickAction mShortcutWindow;
    private ApplicationItemAdapter mAdapter;
    private RecyclerView.AdapterDataObserver mEmptyObserver = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onChanged() {
            vRecyclerView.showFirstRowDivider(mAdapter.containsFrequentItems());
            if (TextUtils.isEmpty(mAdapter.getFilterText())) {
                findById(vContent, R.id.progress_bar)
                        .setVisibility(mAdapter.getItemCount() > 0 ? View.GONE : View.VISIBLE);
                findById(vContent, R.id.text_view_empty).setVisibility(View.GONE);
            } else {
                findById(vContent, R.id.text_view_empty)
                        .setVisibility(mAdapter.getItemCount() > 0 ? View.GONE : View.VISIBLE);
                findById(vContent, R.id.progress_bar).setVisibility(View.GONE);
            }
        }
    };
    private BroadcastReceiver mActionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(DialogActivity.ACTION_CANCELED)) {
                String returnAction = intent.getStringExtra(DialogActivity.EXTRA_RETURN_ACTION);
                onDialogCanceled(returnAction);
                return;
            }
            switch (action) {
                case ACTION_REFRESH_APPS:
                    queryApplicationPackages();
                    show(null, 0);
                    break;
            }
        }

        private void onDialogCanceled(String returnAction) {
            switch (returnAction) {
                case ACTION_REFRESH_APPS:
                    show(null, 0);
                    break;
            }
        }
    };

    public AssistInteractionSession(Context context) {
        super(context);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mAdapter = new ApplicationItemAdapter(getContext());
        mAdapter.registerAdapterDataObserver(mEmptyObserver);
        mAdapter.setOnItemClickListener(new OnItemClickListener<ApplicationItem>() {
            @Override
            public void onItemClick(RecyclerView.ViewHolder holder, ApplicationItem item) {
                holder.itemView.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                startApplication(item);
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            mLauncherApps = ShortcutUtils.getLauncherApps(getContext());
            mAdapter.setOnItemLongClickListener(new OnItemLongClickListener<ApplicationItem>() {
                @Override
                public boolean onItemLongClick(RecyclerView.ViewHolder holder, ApplicationItem item) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1 && mLauncherApps.hasShortcutHostPermission()) {
                        mShortcutQuery = new ShortcutQuery();
                        mShortcutQuery.setPackage(item.applicationInfo.packageName);
                        mShortcutQuery.setQueryFlags(ShortcutQuery.FLAG_MATCH_MANIFEST
                                | ShortcutQuery.FLAG_MATCH_DYNAMIC | ShortcutQuery.FLAG_MATCH_PINNED);
                        List<ShortcutInfo> shortcuts = mLauncherApps.getShortcuts(mShortcutQuery, USER_HANDLE);
                        if (null != shortcuts && shortcuts.size() > 0 && showShortcutsPopupWindow(holder.itemView, shortcuts)) {
                            mAdapter.setHighlightedItem(item);
                            return true;
                        }
                    }
                    return false;
                }
            });
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DialogActivity.ACTION_CANCELED);
        intentFilter.addAction(ACTION_REFRESH_APPS);
        LocalBroadcastManager.getInstance(getContext())
                .registerReceiver(mActionReceiver, intentFilter);
    }

    @SuppressLint("InflateParams")
    @Override
    public View onCreateContentView() {
        WallpaperManager wm = WallpaperManager.getInstance(getContext());
        Drawable wall = wm.getFastDrawable();
        vContent = getLayoutInflater().inflate(R.layout.service_assist_interaction, null, false);
        vContent.setBackground(wall);
        bind(this, vContent);

        vToolbar.inflateMenu(R.menu.service_assist_interaction);
        vToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.item_settings:
                        Intent intent = new Intent(getContext(), SettingsActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        getContext().startActivity(intent);
                        hide();
                        return true;
                    case R.id.item_reset_frequents:
                        Intent reset = new Intent(getContext(), DialogActivity.class);
                        reset.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        reset.setAction(DialogActivity.ACTION_RESET_FREQUENTS);
                        reset.putExtra(DialogActivity.EXTRA_RETURN_ACTION, ACTION_REFRESH_APPS);
                        reset.putExtra(DialogActivity.EXTRA_ICON_RES, R.mipmap.ic_launcher);
                        reset.putExtra(DialogActivity.EXTRA_TITLE, getContext().getString(R.string.app_name));
                        reset.putExtra(DialogActivity.EXTRA_MESSAGE, "Reset all most frequently used application data?");
                        getContext().startActivity(reset);
                        hide();
                        return true;
                }
                return false;
            }
        });

        vRecyclerView.setHasFixedSize(true);
        vRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 6));
        vRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            final int elevation = (int) (Resources.getSystem().getDisplayMetrics().density * 8);
            final float threshold = 60f;
            int ydy = 0;

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                ydy += dy;
                float percent = Math.min(threshold, ydy) / threshold;
                vAppBarLayout.setElevation(elevation * percent);
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING)
                    dismissShortcutsPopupWindow();
            }
        });
        vRecyclerView.setAdapter(mAdapter);
        return vContent;
    }

    @Override
    public void onShow(Bundle args, int showFlags) {
        super.onShow(args, showFlags);
        Log.d(TAG, "onShow: ");
        if (mQueryOnShow)
            queryApplicationPackages();
        mQueryOnShow = true;
    }

    @Override
    public void onHide() {
        dismissShortcutsPopupWindow();
        super.onHide();
        Log.d(TAG, "onHide: ");
        mQueryOnShow = false;
        queryApplicationPackages();
    }

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(getContext())
                .unregisterReceiver(mActionReceiver);
        mAdapter.unregisterAdapterDataObserver(mEmptyObserver);
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
        mGetApplicationsTask = new LoadApplicationsTask(getContext(),
                new LoadApplicationsTask.OnCompleteListener() {
                    @Override
                    public void onComplete(List<ApplicationItem> applicationItems) {
                        vRecyclerView.smoothScrollToPosition(0);
                        mAdapter.setApplications(applicationItems);
                    }
                }).setFrequentItemLimit(6).execute(getPackageManager());
    }

    private void cancelTasks() {
        if (null != mGetApplicationsTask) {
            mGetApplicationsTask.cancel(true);
            mGetApplicationsTask = null;
        }
    }

    private void startApplication(ApplicationItem userApplication) {
        String packageName = userApplication.applicationInfo.packageName;
        Intent intent = getPackageManager().getLaunchIntentForPackage(packageName);
        if (null != intent) {
            String activityName = intent.getComponent().getClassName();
            updateLaunchRecord(packageName, activityName);
            try {
                getContext().startActivity(intent);
                hide();
            } catch (ActivityNotFoundException e) {
                Log.e(TAG, String.format("onItemClick: Activity not found for package, %s", userApplication.label));
                Toast.makeText(getContext(), "Application not found", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        Log.w(TAG, String.format("onItemClick: Launch intent not found for package, %s", userApplication.label));
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
    private boolean showShortcutsPopupWindow(View anchor, final List<ShortcutInfo> shortcuts) {
        dismissShortcutsPopupWindow();
        mShortcutWindow = new QuickAction(getContext());
        for (int i = 0; i < shortcuts.size(); i++) {
            ShortcutInfo shortcut = shortcuts.get(i);
            ActionItem actionItem = new ActionItem(i, shortcut.getShortLabel().toString(),
                    ShortcutUtils.getShortcutIcon(getContext(), shortcut, DisplayMetrics.DENSITY_DEFAULT));
            mShortcutWindow.addActionItem(actionItem);
        }
        mShortcutWindow.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {
            @Override
            public void onItemClick(QuickAction source, int pos, int actionId) {
                dismissShortcutsPopupWindow();
                if (ShortcutUtils.startShortcut(getContext(), shortcuts.get(pos)))
                    hide();
                else
                    Toast.makeText(getContext(), "Error launching shortcut", Toast.LENGTH_SHORT).show();
            }
        });
        mShortcutWindow.setOnDismissListener(new QuickAction.OnDismissListener() {
            @Override
            public void onDismiss() {
                mAdapter.setHighlightedItem(null);
            }
        });
        mShortcutWindow.setAnimStyle(QuickAction.ANIM_GROW_FROM_LEFT);
        mShortcutWindow.setFocusable(true);
        mShortcutWindow.show(anchor);
        return true;
    }

    private void dismissShortcutsPopupWindow() {
        if (null != mShortcutWindow) {
            mShortcutWindow.dismiss();
            mShortcutWindow = null;
        }
    }

    private PackageManager getPackageManager() {
        return getContext().getPackageManager();
    }
}
