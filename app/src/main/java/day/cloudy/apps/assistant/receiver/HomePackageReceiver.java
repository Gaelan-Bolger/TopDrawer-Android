package day.cloudy.apps.assistant.receiver;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import day.cloudy.apps.assistant.activity.LaunchActivity;
import day.cloudy.apps.assistant.settings.AppPrefs;

/**
 * Created by Gaelan Bolger on 12/19/2016.
 */
public class HomePackageReceiver extends BroadcastReceiver {

    private static final String TAG = HomePackageReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        ComponentName component = intent.getParcelableExtra(Intent.EXTRA_CHOSEN_COMPONENT);
        String packageName = component.getPackageName();
        String activityName = component.getClassName();
        AppPrefs.getInstance().setHomePackageName(context, packageName);
        AppPrefs.getInstance().setHomeActivityName(context, activityName);
        context.startActivity(new Intent(context, LaunchActivity.class));
    }
}
