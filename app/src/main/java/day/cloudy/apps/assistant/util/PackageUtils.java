package day.cloudy.apps.assistant.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.util.List;

import day.cloudy.apps.assistant.activity.LaunchActivity;

/**
 * Created by Gaelan Bolger on 12/15/2016.
 */
@SuppressWarnings("WeakerAccess")
public class PackageUtils {

    public static boolean isDefaultHomePackage(Context context) {
        return isDefaultHomePackage(context.getPackageManager());
    }

    public static boolean isDefaultHomePackage(PackageManager packageManager) {
        ResolveInfo resolveInfo = getDefaultLauncher(packageManager);
        return null != resolveInfo && resolveInfo.activityInfo.packageName.equals("day.cloudy.apps.assistant");
    }

    public static String getDefaultLauncher(Context context) {
        return getDefaultLauncher(context.getPackageManager()).activityInfo.packageName;
    }

    public static ResolveInfo getDefaultLauncher(PackageManager packageManager) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        return packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
    }

    public static void resetDefaultLauncher(Context context) {
        PackageManager packageManager = context.getPackageManager();
        ComponentName componentName = new ComponentName(context, LaunchActivity.class);
        packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);

        packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT, PackageManager.DONT_KILL_APP);
    }

    public static List<ResolveInfo> getLauncherActivities(PackageManager packageManager) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        return packageManager.queryIntentActivities(intent, 0);
    }

    public static List<ResolveInfo> getHomeActivities(PackageManager packageManager) {
        Intent intent = getHomeIntent();
        List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
        for (int i = activities.size() - 1; i >= 0; i--) {
            if (activities.get(i).activityInfo.packageName.equals("day.cloudy.apps.assistant"))
                activities.remove(i);
        }
        return activities;
    }

    public static Intent getHomeIntent(String packageName, String activityName) {
        Intent intent = getHomeIntent();
        intent.setComponent(new ComponentName(packageName, activityName));
        return intent;
    }

    public static Intent getHomeIntent() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        return intent;
    }
}
