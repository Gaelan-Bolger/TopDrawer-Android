package day.cloudy.apps.assistant.task;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;

import com.google.common.collect.Lists;
import com.orm.query.Select;

import java.util.Collections;
import java.util.List;

import day.cloudy.apps.assistant.model.ApplicationItem;
import day.cloudy.apps.assistant.model.FrequentItem;
import day.cloudy.apps.assistant.model.RecentApplication;
import day.cloudy.apps.assistant.util.PackageUtils;

/**
 * Created by Gaelan Bolger on 12/16/2016.
 */
public class LoadApplicationsTask extends AsyncTask<PackageManager, Void, List<ApplicationItem>> {

    public interface OnCompleteListener {
        void onComplete(List<ApplicationItem> applicationItems);
    }

    private static final String TAG = LoadApplicationsTask.class.getSimpleName();

    private final Context mContext;
    private final OnCompleteListener mListener;
    private int mFrequentLimit = 0;

    public LoadApplicationsTask(Context context, OnCompleteListener listener) {
        mContext = context;
        mListener = listener;
    }

    @Override
    protected List<ApplicationItem> doInBackground(PackageManager... params) {
        PackageManager pm = params[0];
        List<ResolveInfo> activities = PackageUtils.getLauncherActivities(pm);
        List<ApplicationItem> applications = Lists.newArrayList();
        for (ResolveInfo activity : activities) {
            try {
                String packageName = activity.activityInfo.packageName;
                ApplicationInfo info = pm.getApplicationInfo(packageName, 0);
                ApplicationItem item = new ApplicationItem(info,
                        String.valueOf(info.loadLabel(pm)), info.loadIcon(pm));
                applications.add(item);
            } catch (PackageManager.NameNotFoundException ignore) {
            }
        }
        Collections.sort(applications, new ApplicationItem.Comparator(pm));

        if (mFrequentLimit > 0) {
            List<RecentApplication> records = Select.from(RecentApplication.class)
                    .orderBy(RecentApplication.COLUMN_LAST_LAUNCH + " DESC")
                    .orderBy(RecentApplication.COLUMN_LAUNCH_COUNT + " DESC")
                    .limit(String.valueOf(mFrequentLimit))
                    .list();
            if (records.size() >= mFrequentLimit) {
                for (int i = 0; i < records.size(); i++) {
                    RecentApplication record = records.get(i);
                    String recordPackageName = record.getPackageName();
                    for (int j = applications.size() - 1; j >= mFrequentLimit; j--) {
                        ApplicationItem item = applications.get(j);
                        ApplicationInfo info = item.applicationInfo;
                        String itemPackageName = info.packageName;
                        if (itemPackageName.equals(recordPackageName)) {
                            int recordLaunchCount = record.getLaunchCount();
                            long recordLastLaunch = record.getLastLaunch();
                            FrequentItem frequentItem = new FrequentItem(info,
                                    item.label, item.icon,
                                    recordLaunchCount, recordLastLaunch);
                            applications.add(i, frequentItem);
                        }
                    }
                }
            }
        }
        return applications;
    }

    @Override
    protected void onPostExecute(List<ApplicationItem> applicationItems) {
        if (!isCancelled() && null != mListener)
            mListener.onComplete(applicationItems);
    }

    public LoadApplicationsTask setFrequentItemLimit(int frequentLimit) {
        mFrequentLimit = frequentLimit;
        return this;
    }
}
