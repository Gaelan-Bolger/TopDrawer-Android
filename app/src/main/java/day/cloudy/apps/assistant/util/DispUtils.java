package day.cloudy.apps.assistant.util;

import android.content.res.Resources;

/**
 * Created by Gaelan Bolger on 12/16/2016.
 */
public class DispUtils {

    public static int dp(int i) {
        return (int) (density() * i);
    }

    public static float density() {
        return Resources.getSystem().getDisplayMetrics().density;
    }
}
