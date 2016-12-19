package day.cloudy.apps.assistant.util;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ScaleDrawable;

/**
 * Created by Gaelan Bolger on 12/15/2016.
 */
public class IconUtil {

    private static int mAppIconSize = -1;

    public static Drawable normalize(Drawable icon) {
        if (mAppIconSize == -1) {
            mAppIconSize = Resources.getSystem().getDimensionPixelSize(android.R.dimen.app_icon_size);
        }
        icon = new ScaleDrawable(icon, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight()).getDrawable();
        icon.setBounds(0, 0, mAppIconSize, mAppIconSize);
        return icon;
    }
}
