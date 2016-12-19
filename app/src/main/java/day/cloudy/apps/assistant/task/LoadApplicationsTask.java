package day.cloudy.apps.assistant.task;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.LauncherApps;
import android.content.pm.LauncherApps.ShortcutQuery;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ShortcutInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.UserHandle;
import android.util.Log;

import com.google.common.collect.Lists;
import com.orm.query.Select;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;

import day.cloudy.apps.assistant.model.ApplicationItem;
import day.cloudy.apps.assistant.model.FrequentItem;
import day.cloudy.apps.assistant.model.RecentApplication;
import day.cloudy.apps.assistant.model.UserApplication;
import day.cloudy.apps.assistant.util.IconUtils;
import day.cloudy.apps.assistant.util.PackageUtils;
import day.cloudy.apps.assistant.util.ShortCutUtils;

/**
 * Created by Gaelan Bolger on 12/16/2016.
 */
public class LoadApplicationsTask extends AsyncTask<PackageManager, Void, List<ApplicationItem>> {

    public interface OnCompleteListener {
        void onComplete(List<ApplicationItem> applicationItems);
    }

    private static final String TAG = LoadApplicationsTask.class.getSimpleName();

    private final Context sContext;
    private final OnCompleteListener sListsner;

    public LoadApplicationsTask(Context context, OnCompleteListener listener) {
        sContext = context;
        sListsner = listener;
    }

    @Override
    protected List<ApplicationItem> doInBackground(PackageManager... params) {
        PackageManager pm = params[0];
        List<ResolveInfo> activities = PackageUtils.getLauncherActivities(pm);
        List<ApplicationItem> applications = Lists.newArrayList();
        for (ResolveInfo activity : activities) {
            try {
                String packageName = activity.activityInfo.packageName;
                ApplicationInfo applicationInfo = pm.getApplicationInfo(packageName, 0);
                ApplicationItem item = new ApplicationItem(applicationInfo,
                        applicationInfo.loadLabel(pm).toString(),
                        applicationInfo.loadIcon(pm));
                applications.add(item);
            } catch (PackageManager.NameNotFoundException ignore) {
            }
        }
        Collections.sort(applications, new ApplicationItem.Comparator(pm));

        int limit = 6;
        List<RecentApplication> records = Select.from(RecentApplication.class)
                .orderBy(RecentApplication.COLUMN_LAST_LAUNCH + " DESC")
                .orderBy(RecentApplication.COLUMN_LAUNCH_COUNT + " DESC")
                .limit(String.valueOf(limit))
                .list();
        if (records.size() >= limit) {
            for (int i = 0; i < records.size(); i++) {
                RecentApplication record = records.get(i);
                String recordPackageName = record.getPackageName();
                for (int j = applications.size() - 1; j >= limit; j--) {
                    ApplicationItem item = applications.get(j);
                    String itemPackageName = item.applicationInfo.packageName;
                    if (itemPackageName.equals(recordPackageName)) {
                        int recordLaunchCount = record.getLaunchCount();
                        long recordLastLaunch = record.getLastLaunch();
                        FrequentItem frequentItem = new FrequentItem(item.applicationInfo, item.label, item.icon, recordLaunchCount, recordLastLaunch);
                        applications.add(i, frequentItem);
                    }
                }
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            LauncherApps launcherApps = ShortCutUtils.getLauncherApps(sContext);
            if (launcherApps.hasShortcutHostPermission()) {
                Log.d(TAG, "doInBackground: Application is current default home, load launcher shortcuts");
                ShortcutQuery query;
                for (ApplicationItem application : applications) {
                    ApplicationInfo applicationInfo = application.applicationInfo;
                    String packageName = applicationInfo.packageName;
                    query = new ShortcutQuery();
                    query.setPackage(packageName);
                    query.setQueryFlags(ShortcutQuery.FLAG_MATCH_MANIFEST
                            | ShortcutQuery.FLAG_MATCH_DYNAMIC
                            | ShortcutQuery.FLAG_MATCH_PINNED);
                    UserHandle userHandle = UserHandle.getUserHandleForUid(applicationInfo.uid);
                    List<ShortcutInfo> shortcuts = launcherApps.getShortcuts(query, userHandle);
                    if (null != shortcuts && shortcuts.size() > 0) {
                        Log.d(TAG, "doInBackground: \tFound " + shortcuts.size() + " for package, " + packageName);
                        application.shortcuts = shortcuts;
                    }
                }
            } else {
                Log.d(TAG, "doInBackground: Not set as default home, launcher shortcuts unavailable");
            }
        }

        for (int i = 0; i < applications.size(); i++) {
            ApplicationItem applicationItem = applications.get(i);
            Drawable icon = applicationItem.icon;
            Bitmap bitmap = IconUtils.getBitmap(icon);
            new UserApplication(applicationItem.applicationInfo.packageName, "", applicationItem.label, IconUtils.encodeToBase64(bitmap)).save();
        }

        return applications;
    }

    @Override
    protected void onPostExecute(List<ApplicationItem> applications) {
        if (!isCancelled() && null != sListsner)
            sListsner.onComplete(applications);
    }
}
