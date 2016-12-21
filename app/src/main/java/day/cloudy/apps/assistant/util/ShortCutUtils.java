package day.cloudy.apps.assistant.util;

import android.content.Context;
import android.content.pm.LauncherApps;
import android.content.pm.ShortcutInfo;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.RequiresApi;

/**
 * Created by Gaelan Bolger on 12/16/2016.
 */
public class ShortcutUtils {

    @RequiresApi(api = Build.VERSION_CODES.N_MR1)
    public static boolean startShortcut(Context context, ShortcutInfo shortcutInfo) {
        LauncherApps launcherApps = getLauncherApps(context);
        if (launcherApps.hasShortcutHostPermission()) {
            launcherApps.startShortcut(shortcutInfo, null, null);
            return true;
        }
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.N_MR1)
    public static Drawable getShortcutIcon(Context context, ShortcutInfo shortcut, int density) {
        return getLauncherApps(context).getShortcutIconDrawable(shortcut, density);
    }

    public static LauncherApps getLauncherApps(Context context) {
        return (LauncherApps) context.getSystemService(Context.LAUNCHER_APPS_SERVICE);
    }
}
