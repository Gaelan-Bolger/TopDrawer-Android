package day.cloudy.apps.assistant.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Gaelan Bolger on 12/15/2016.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class AppPrefs {

    public static final String KEY_HOME_PACKAGE = "home_package_name";
    public static final String KEY_HOME_ACTIVITY = "home_activity_name";
    public static final String KEY_SHOW_FREQUENTS = "show_frequents";
    public static final String KEY_DEBUG_DRAWER = "debug_drawer";
    private static AppPrefs mInstance;
    private SharedPreferences mPreferences;

    private AppPrefs() {
        // no instances
    }

    public static AppPrefs getInstance() {
        if (null == mInstance) {
            mInstance = new AppPrefs();
        }
        return mInstance;
    }

    public void registerListener(Context context, SharedPreferences.OnSharedPreferenceChangeListener listener) {
        getSharedPreferences(context).registerOnSharedPreferenceChangeListener(listener);
    }

    public void unregisterListener(Context context, SharedPreferences.OnSharedPreferenceChangeListener listener) {
        getSharedPreferences(context).unregisterOnSharedPreferenceChangeListener(listener);
    }

    public String getHomePackageName(Context context) {
        return getString(context, KEY_HOME_PACKAGE, "");
    }

    public boolean setHomePackageName(Context context, String packageName) {
        return setString(context, KEY_HOME_PACKAGE, packageName);
    }

    public String getHomeActivityName(Context context) {
        return getString(context, KEY_HOME_ACTIVITY, "");
    }

    public boolean setHomeActivityName(Context context, String activityName) {
        return setString(context, KEY_HOME_ACTIVITY, activityName);
    }

    public String getString(Context context, String key, String defValue) {
        return getSharedPreferences(context).getString(key, defValue);
    }

    public boolean setString(Context context, String key, String value) {
        return getEditor(context).putString(key, value).commit();
    }

    public int getInt(Context context, String key, int defValue) {
        return getSharedPreferences(context).getInt(key, defValue);
    }

    public boolean setInt(Context context, String key, int value) {
        return getEditor(context).putInt(key, value).commit();
    }

    public boolean getBoolean(Context context, String key, boolean defValue) {
        return getSharedPreferences(context).getBoolean(key, defValue);
    }

    public boolean setBoolean(Context context, String key, boolean value) {
        return getEditor(context).putBoolean(key, value).commit();
    }

    private SharedPreferences.Editor getEditor(Context context) {
        return getSharedPreferences(context).edit();
    }

    private SharedPreferences getSharedPreferences(Context context) {
        if (null == mPreferences) {
            mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        }
        return mPreferences;
    }
}
