package day.cloudy.apps.assistant.model;

import android.content.pm.ApplicationInfo;
import android.graphics.drawable.Drawable;

/**
 * Created by Gaelan Bolger on 12/19/2016.
 */
public class FrequentItem extends ApplicationItem {

    public int launchCount;
    public long lastLaunch;

    public FrequentItem(ApplicationInfo applicationInfo, String label, Drawable icon, int launchCount, long lastLaunch) {
        super(applicationInfo, label, icon);
        this.launchCount = launchCount;
        this.lastLaunch = lastLaunch;
    }
}
