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

    public static final String PACKAGE_NAME = "day.cloudy.apps.assistant";

    public static List<ResolveInfo> getLauncherActivities(PackageManager packageManager) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        return packageManager.queryIntentActivities(intent, 0);
    }

    public static Intent getHomeIntent() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        return intent;
    }

    public static Intent getHomeIntent(String packageName, String activityName) {
        Intent intent = getHomeIntent();
        intent.setComponent(new ComponentName(packageName, activityName));
        return intent;
    }

    public static List<ResolveInfo> getHomePackages(PackageManager packageManager) {
        Intent intent = getHomeIntent();
        List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
        for (int i = activities.size() - 1; i >= 0; i--) {
            if (activities.get(i).activityInfo.packageName.equals(PACKAGE_NAME))
                activities.remove(i);
        }
        return activities;
    }

    public static String getDefaultHomePackageName(Context context) {
        ResolveInfo homePackage = getDefaultHomePackage(context.getPackageManager());
        return null != homePackage ? homePackage.activityInfo.packageName : null;
    }

    public static ResolveInfo getDefaultHomePackage(PackageManager packageManager) {
        return packageManager.resolveActivity(getHomeIntent(), PackageManager.MATCH_DEFAULT_ONLY);
    }

    public static boolean isDefaultHomePackage(Context context) {
        return isDefaultHomePackage(context.getPackageManager());
    }

    public static boolean isDefaultHomePackage(PackageManager packageManager) {
        ResolveInfo resolveInfo = getDefaultHomePackage(packageManager);
        return null != resolveInfo && resolveInfo.activityInfo.packageName.equals(PACKAGE_NAME);
    }

    public static void resetDefaultHomePackage(Context context) {
        PackageManager packageManager = context.getPackageManager();
        ComponentName componentName = new ComponentName(context, LaunchActivity.class);
        packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);

        packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT, PackageManager.DONT_KILL_APP);
    }
}
