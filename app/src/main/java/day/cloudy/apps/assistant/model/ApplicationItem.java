package day.cloudy.apps.assistant.model;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

/**
 * Created by Gaelan Bolger on 12/15/2016.
 */
public class ApplicationItem {

    public ApplicationInfo applicationInfo;
    public String label;
    public Drawable icon;

    public ApplicationItem(ApplicationInfo applicationInfo, String label, Drawable icon) {
        this.applicationInfo = applicationInfo;
        this.label = label;
        this.icon = icon;
    }

    public static class Comparator implements java.util.Comparator<ApplicationItem> {

        private ApplicationInfo.DisplayNameComparator sComparator;

        public Comparator(PackageManager packageManager) {
            sComparator = new ApplicationInfo.DisplayNameComparator(packageManager);
        }

        @Override
        public int compare(ApplicationItem lhs, ApplicationItem rhs) {
            return sComparator.compare(lhs.applicationInfo, rhs.applicationInfo);
        }
    }
}
