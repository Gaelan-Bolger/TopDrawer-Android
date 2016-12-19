package day.cloudy.apps.assistant.model;

import com.orm.SugarRecord;

/**
 * Created by Gaelan Bolger on 12/19/2016.
 */
public class UserApplication extends SugarRecord {

    private String packageName;
    private String activityName;
    private String label;
    private String icon;

    public UserApplication() {
    }

    public UserApplication(String packageName, String activityName, String label, String icon) {
        this.packageName = packageName;
        this.activityName = activityName;
        this.label = label;
        this.icon = icon;
    }
}
