package day.cloudy.apps.assistant.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import day.cloudy.apps.assistant.assist.AssistInteractionSession;

/**
 * Created by Gaelan Bolger on 12/22/2016.
 */
public class PackageChangeReceiver extends BroadcastReceiver {

    private static final String TAG = PackageChangeReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e(TAG, "onReceive: ");
        String action = intent.getAction();
        if (action.equals(Intent.ACTION_PACKAGE_CHANGED)
                || action.equals(Intent.ACTION_PACKAGE_ADDED)
                || action.equals(Intent.ACTION_PACKAGE_REMOVED)) {
            LocalBroadcastManager.getInstance(context)
                    .sendBroadcast(new Intent(AssistInteractionSession.ACTION_REFRESH_APPS));

        }
    }
}
