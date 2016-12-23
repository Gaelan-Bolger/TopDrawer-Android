package day.cloudy.apps.assistant.assist;

import android.annotation.SuppressLint;
import android.app.WallpaperManager;
import android.app.assist.AssistContent;
import android.app.assist.AssistStructure;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.LauncherApps;
import android.content.pm.LauncherApps.ShortcutQuery;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.os.UserHandle;
import android.provider.Settings;
import android.service.voice.VoiceInteractionSession;
import android.support.annotation.RequiresApi;
import android.support.design.widget.AppBarLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.common.collect.Lists;
import com.orm.SugarRecord;
import com.orm.query.Condition;
import com.orm.query.Select;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnTextChanged;
import day.cloudy.apps.assistant.R;
import day.cloudy.apps.assistant.activity.ProxyActivity;
import day.cloudy.apps.assistant.activity.SettingsActivity;
import day.cloudy.apps.assistant.model.ApplicationItem;
import day.cloudy.apps.assistant.model.RecentApplication;
import day.cloudy.apps.assistant.recycler.FirstRowDividerRecyclerView;
import day.cloudy.apps.assistant.recycler.LockableGridLayoutManager;
import day.cloudy.apps.assistant.recycler.OnItemClickListener;
import day.cloudy.apps.assistant.recycler.OnItemLongClickListener;
import day.cloudy.apps.assistant.recycler.adapter.ApplicationItemAdapter;
import day.cloudy.apps.assistant.settings.AppPrefs;
import day.cloudy.apps.assistant.shortcut.ActionItem;
import day.cloudy.apps.assistant.shortcut.QuickAction;
import day.cloudy.apps.assistant.task.LoadApplicationsTask;
import day.cloudy.apps.assistant.util.DispUtils;
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
    private static final UserHandle USER_HANDLE = Process.myUserHandle();

    public static final String ACTION_REFRESH_APPS = "day.cloudy.apps.assistant.action.REFRESH_APPS";
    public static final String ACTION_SHOW = "day.cloudy.apps.assistant.action.SHOW";
    public static final String EXTRA_REFRESH_APPS = "refresh_apps";

    private View vContent;

    @BindView(R.id.app_bar_layout)
    AppBarLayout vAppBarLayout;
    @BindView(R.id.toolbar)
    Toolbar vToolbar;
    @BindView(R.id.edit_text_filter)
    EditText vFilter;
    @BindView(R.id.recycler_view)
    FirstRowDividerRecyclerView vRecycler;

    private AppPrefs mPrefs = AppPrefs.getInstance();
    private boolean mQueryOnShow = true;
    private AsyncTask mGetApplicationsTask;
    private LauncherApps mLauncherApps;
    private QuickAction mShortcutsWindow;
    private BroadcastReceiver mActionReceiver;
    private LockableGridLayoutManager mLayoutManager;
    private ApplicationItemAdapter mAdapter;
    private RecyclerView.AdapterDataObserver mEmptyObserver = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onChanged() {
            vRecycler.showFirstRowDivider(mAdapter.containsFrequentItems());
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
                startApplication(item.applicationInfo.packageName);
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            mLauncherApps = ShortcutUtils.getLauncherApps(getContext());
            mAdapter.setOnItemLongClickListener(new OnItemLongClickListener<ApplicationItem>() {
                @Override
                public boolean onItemLongClick(RecyclerView.ViewHolder holder, ApplicationItem item) {
                    showShortcutsWindow(holder.itemView, item);
                    return true;
                }
            });
        }
        IntentFilter actionFilter = new IntentFilter();
        actionFilter.addAction(ACTION_REFRESH_APPS);
        actionFilter.addAction(ACTION_SHOW);
        LocalBroadcastManager.getInstance(getContext())
                .registerReceiver(mActionReceiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        switch (intent.getAction()) {
                            case ACTION_REFRESH_APPS:
                                queryApplicationPackages();
                                break;
                            case ACTION_SHOW:
                                if (intent.getBooleanExtra(EXTRA_REFRESH_APPS, false))
                                    queryApplicationPackages();
                                show(null, 0);
                                break;
                        }
                    }
                }, actionFilter);
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
                    case R.id.item_clear_text:
                        vFilter.setText("");
                        return true;
                    case R.id.item_settings:
                        Intent intent = new Intent(getContext(), SettingsActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        getContext().startActivity(intent);
                        hide();
                        return true;
                    case R.id.item_show_frequents:
                        boolean checked = !item.isChecked();
                        item.setChecked(checked);
                        mPrefs.setBoolean(getContext(), AppPrefs.KEY_SHOW_FREQUENTS, checked);
                        queryApplicationPackages();
                        return true;
                    case R.id.item_reset_frequents:
                        Intent reset = new Intent(getContext(), ProxyActivity.class);
                        reset.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        reset.setAction(ProxyActivity.ACTION_CONFIRMATION_DIALOG);
                        reset.putExtra(ProxyActivity.EXTRA_DIALOG_TITLE, getContext().getString(R.string.app_name));
                        reset.putExtra(ProxyActivity.EXTRA_DIALOG_MESSAGE, "Reset all most frequently used application data?");
                        BroadcastReceiver resultReceiver = new BroadcastReceiver() {
                            @Override
                            public void onReceive(Context context, Intent intent) {
                                LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(this);
                                if (intent.getBooleanExtra(ProxyActivity.EXTRA_RESULT, false)) {
                                    SugarRecord.deleteAll(RecentApplication.class);
                                    mAdapter.setApplications(null);
                                    queryApplicationPackages();
                                    alert("Frequent apps reset");
                                }
                                show(null, 0);
                            }
                        };
                        IntentFilter resultFilter = new IntentFilter(ProxyActivity.ACTION_DELIVER_RESULT);
                        LocalBroadcastManager.getInstance(getContext()).registerReceiver(resultReceiver, resultFilter);
                        getContext().startActivity(reset);
                        hide();
                        return true;
                }
                return false;
            }
        });

        mLayoutManager = new LockableGridLayoutManager(getContext(), 6);
        mLayoutManager.setCanScrollVertically(true);
        vRecycler.setHasFixedSize(true);
        vRecycler.setLayoutManager(mLayoutManager);
        vRecycler.addOnScrollListener(new DrawerScrollListener(vRecycler, vAppBarLayout));
        vRecycler.setAdapter(mAdapter);
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
        dismissShortcutsWindow();
        super.onHide();
        Log.d(TAG, "onHide: ");
        vFilter.setText("");
        vRecycler.smoothScrollToPosition(0);
        mQueryOnShow = false;
        queryApplicationPackages();
    }

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mActionReceiver);
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
        vToolbar.getMenu().findItem(R.id.item_clear_text).setVisible(!TextUtils.isEmpty(text));
        vToolbar.getMenu().setGroupVisible(R.id.group_overflow, TextUtils.isEmpty(text));
    }

    private void queryApplicationPackages() {
        cancelTasks();
        boolean showFrequents = mPrefs.getBoolean(getContext(), AppPrefs.KEY_SHOW_FREQUENTS, true);
        int frequentLimit = showFrequents ? mLayoutManager.getSpanCount() : 0;
        mGetApplicationsTask = new LoadApplicationsTask(getContext(),
                new LoadApplicationsTask.OnCompleteListener() {
                    @Override
                    public void onComplete(List<ApplicationItem> applicationItems) {
                        mAdapter.setApplications(applicationItems);
                    }
                }).setFrequentItemLimit(frequentLimit).execute(getPackageManager());
    }

    private void cancelTasks() {
        if (null != mGetApplicationsTask) {
            mGetApplicationsTask.cancel(true);
            mGetApplicationsTask = null;
        }
    }

    private void startApplication(String packageName) {
        Intent intent = getPackageManager().getLaunchIntentForPackage(packageName);
        if (null != intent) {
            String activityName = intent.getComponent().getClassName();
            updateLaunchRecord(packageName, activityName);
            try {
                getContext().startActivity(intent);
                hide();
            } catch (ActivityNotFoundException e) {
                Log.e(TAG, String.format("onItemClick: Activity not found for package, %s", packageName));
                alert("Application not found");
            }
        } else {
            Log.w(TAG, String.format("onItemClick: Launch intent not found for package, %s", packageName));
            alert("Application not found");
        }
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

    private void showShortcutsWindow(View anchor, ApplicationItem applicationItem) {
        dismissShortcutsWindow();
        mLayoutManager.setCanScrollVertically(false);
        mAdapter.setHighlightedItem(applicationItem);

        mShortcutsWindow = new QuickAction(getContext());
        mShortcutsWindow.setAnimStyle(QuickAction.ANIM_GROW_FROM_LEFT);
        mShortcutsWindow.setFocusable(true);

        ApplicationInfo applicationInfo = applicationItem.applicationInfo;
        String packageName = applicationInfo.packageName;
        List<ShortcutItem> items = getAppShortcutItems(packageName, isUserApp(applicationInfo));
        for (ShortcutItem shortcutItem : items) {
            mShortcutsWindow.addActionItem(shortcutItem);
        }

        mShortcutsWindow.setOnDismissListener(new QuickAction.OnDismissListener() {
            @Override
            public void onDismiss() {
                mLayoutManager.setCanScrollVertically(true);
                mAdapter.setHighlightedItem(null);
            }
        });

        mShortcutsWindow.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {
            @Override
            public void onItemClick(QuickAction source, int pos, int actionId) {
                ShortcutItem shortcutItem = (ShortcutItem) source.getActionItem(pos);
                if (shortcutItem instanceof AppInfoItem) {
                    String packageName = ((AppInfoItem) shortcutItem).packageName;
                    onShowApplicationDetails(packageName);
                } else if (shortcutItem instanceof UninstallItem) {
                    String packageName = ((UninstallItem) shortcutItem).packageName;
                    onUninstallApplication(packageName);
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                    ShortcutInfo shortcutInfo = shortcutItem.shortcutInfo;
                    if (null != shortcutInfo && ShortcutUtils.startShortcut(getContext(), shortcutInfo)) {
                        hide();
                    } else {
                        alert("Error launching shortcut");
                    }
                } else {
                    alert("Something's wrong here");
                }
            }

            private void onShowApplicationDetails(String packageName) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.fromParts("package", packageName, null));
                try {
                    getContext().startActivity(intent);
                    hide();
                } catch (ActivityNotFoundException e) {
                    Log.e(TAG, "onItemClick: Error starting details activity for package, " + e.getMessage());
                    alert("Error launching app info");
                }
            }

            private void onUninstallApplication(String packageName) {
                Intent intent = new Intent(getContext(), ProxyActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.setAction(ProxyActivity.ACTION_UNINSTALL_PACKAGE);
                intent.putExtra(ProxyActivity.EXTRA_PACKAGE_NAME, packageName);
                getContext().startActivity(intent);
                hide();
            }
        });
        mShortcutsWindow.show(anchor);
    }

    private boolean isUserApp(ApplicationInfo applicationInfo) {
        int mask = ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP;
        return (applicationInfo.flags & mask) == 0;
    }

    private List<ShortcutItem> getAppShortcutItems(String packageName, boolean userApp) {
        List<ShortcutItem> items = Lists.newArrayList();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1 && mLauncherApps.hasShortcutHostPermission()) {
            List<ShortcutInfo> shortcuts = getAppShortcuts(packageName);
            for (int i = 0; i < shortcuts.size(); i++) {
                ShortcutInfo shortcut = shortcuts.get(i);
                String label = shortcut.getShortLabel().toString();
                Drawable icon = ShortcutUtils.getShortcutIcon(getContext(), shortcut, DisplayMetrics.DENSITY_DEFAULT);
                items.add(new ShortcutItem(i, label, icon, shortcut));
            }
        }
        Resources res = getResources();
        items.add(new AppInfoItem(items.size(), res.getString(R.string.app_info),
                ResourcesCompat.getDrawable(res, R.drawable.ic_action_info, null), packageName));
        if (userApp) {
            items.add(new UninstallItem(items.size(), res.getString(R.string.uninstall),
                    ResourcesCompat.getDrawable(res, R.drawable.ic_action_info, null), packageName));
        }
        return items;
    }

    @RequiresApi(api = Build.VERSION_CODES.N_MR1)
    private ArrayList<ShortcutInfo> getAppShortcuts(String packageName) {
        ShortcutQuery query = new ShortcutQuery();
        query.setPackage(packageName);
        query.setQueryFlags(ShortcutQuery.FLAG_MATCH_MANIFEST | ShortcutQuery.FLAG_MATCH_DYNAMIC | ShortcutQuery.FLAG_MATCH_PINNED);
        return Lists.newArrayList(mLauncherApps.getShortcuts(query, USER_HANDLE));
    }

    private void dismissShortcutsWindow() {
        if (null != mShortcutsWindow) {
            mShortcutsWindow.dismiss();
            mShortcutsWindow = null;
        }
    }

    private Resources getResources() {
        return getContext().getResources();
    }

    private PackageManager getPackageManager() {
        return getContext().getPackageManager();
    }

    private void alert(String text) {
        Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
    }

    private class DrawerScrollListener extends RecyclerView.OnScrollListener {

        private final int sElevation = DispUtils.dp(8);
        private final float sThreshold = 60f;
        private final RecyclerView sRecyclerView;
        private final AppBarLayout sAppBarLayout;

        public DrawerScrollListener(RecyclerView recyclerView, AppBarLayout appBarLayout) {
            sRecyclerView = recyclerView;
            sAppBarLayout = appBarLayout;
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            int offset = sRecyclerView.computeVerticalScrollOffset();
            float percent = Math.min(sThreshold, offset) / sThreshold;
            sAppBarLayout.setElevation(sElevation * percent);
        }
    }

    private class ShortcutItem extends ActionItem {

        ShortcutInfo shortcutInfo;

        public ShortcutItem(int id, String label, Drawable icon, ShortcutInfo shortcutInfo) {
            super(id, label, icon);
            this.shortcutInfo = shortcutInfo;
        }
    }

    private class AppInfoItem extends ShortcutItem {

        private final String packageName;

        public AppInfoItem(int id, String label, Drawable icon, String packageName) {
            super(id, label, icon, null);
            this.packageName = packageName;
        }
    }

    private class UninstallItem extends ShortcutItem {

        private final String packageName;

        public UninstallItem(int id, String label, Drawable icon, String packageName) {
            super(id, label, icon, null);
            this.packageName = packageName;
        }
    }
}
