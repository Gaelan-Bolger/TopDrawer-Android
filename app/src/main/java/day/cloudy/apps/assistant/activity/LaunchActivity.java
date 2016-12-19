package day.cloudy.apps.assistant.activity;

import android.app.Activity;
import android.app.LauncherActivity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import day.cloudy.apps.assistant.receiver.HomePackageReceiver;
import day.cloudy.apps.assistant.settings.AppPrefs;
import day.cloudy.apps.assistant.util.AssistUtils;
import day.cloudy.apps.assistant.util.PackageUtils;

/**
 * Created by Gaelan Bolger on 12/15/2016.
 * Application entry {@link Activity}
 */
public class LaunchActivity extends Activity {

    private static final String TAG = LauncherActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean defaultAssistPackage = AssistUtils.isDefaultAssistPackage(this);
        boolean defaultHomePackage = PackageUtils.isDefaultHomePackage(this);
        if (defaultAssistPackage && defaultHomePackage) {
            String packageName = AppPrefs.getInstance().getHomePackageName(this);
            String activityName = AppPrefs.getInstance().getHomeActivityName(this);
            if (TextUtils.isEmpty(packageName) || TextUtils.isEmpty(activityName)) {
                showHomePackageChooser();
            } else {
                Intent homeIntent = PackageUtils.getHomeIntent(packageName, activityName);
                homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                startActivity(homeIntent);
                finish();
            }
        } else {
            startActivity(new Intent(this, SettingsActivity.class));
            finish();
        }
    }

    private void showHomePackageChooser() {
        Log.d(TAG, "showHomePackageChooser: ");
        Intent receiver = new Intent(this, HomePackageReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, receiver, PendingIntent.FLAG_UPDATE_CURRENT);
        IntentSender intentSender = pendingIntent.getIntentSender();
        Intent intent = PackageUtils.getHomeIntent();
        Intent chooser = Intent.createChooser(intent, "Select home app", intentSender);
        startActivity(chooser);
        finish();
    }
}
