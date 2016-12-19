package day.cloudy.apps.assistant.model;

import android.os.SystemClock;

import com.orm.SugarRecord;
import com.orm.dsl.Unique;

/**
 * Created by Gaelan Bolger on 12/19/2016.
 */
public class RecentApplication extends SugarRecord {

    public static final String COLUMN_PACKAGE_NAME = "PACKAGE_NAME";
    public static final String COLUMN_ACTIVITY_NAME = "ACTIVITY_NAME";
    public static final String COLUMN_LAUNCH_COUNT = "LAUNCH_COUNT";
    public static final String COLUMN_LAST_LAUNCH = "LAST_LAUNCH";

    @Unique
    private String packageName;
    private String activityName;
    private int launchCount = 0;
    private long lastLaunch;

    public RecentApplication() {
        // required for Sugar ORM
    }

    public RecentApplication(String packageName, String activityName) {
        this.packageName = packageName;
        this.activityName = activityName;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getActivityName() {
        return activityName;
    }

    public int getLaunchCount() {
        return launchCount;
    }

    public long getLastLaunch() {
        return lastLaunch;
    }

    public void incrementLaunchCount() {
        launchCount++;
        lastLaunch = SystemClock.currentThreadTimeMillis();
    }

}
