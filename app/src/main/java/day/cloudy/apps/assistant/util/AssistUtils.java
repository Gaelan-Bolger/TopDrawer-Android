package day.cloudy.apps.assistant.util;

import android.content.ComponentName;
import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;

/**
 * Created by Gaelan Bolger on 12/16/2016.
 */
public class AssistUtils {

    public static boolean isDefaultAssistPackage(Context context) {
        String assistant = Settings.Secure.getString(context.getContentResolver(), "voice_interaction_service");
        if (!TextUtils.isEmpty(assistant)) {
            ComponentName componentName = ComponentName.unflattenFromString(assistant);
            if (componentName.getPackageName().equals(context.getPackageName()))
                return true;
        }
        return false;
    }
}
