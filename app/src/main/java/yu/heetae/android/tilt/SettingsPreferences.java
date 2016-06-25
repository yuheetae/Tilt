package yu.heetae.android.tilt;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by yu on 5/31/16.
 */
public class SettingsPreferences {
    //Shared Preference Variable Keys for Settings
    public static final String PREF_TILT_ANGLE = "tilt_angle";
    public static final String PREF_HEADSUP_NOTIFICATION = "headsup_notification";
    public static final String PREF_VIBRATE = "notification_vibrate";
    public static final String PREF_PORTRAIT_ORIENTATION = "portrait_orientation";
    public static final String PREF_LANDSCAPE_ORIENTATION = "landscape_orientation";

    //Set initial settings values for first startup
    public static void setInitialSettings(Context context) {
        if(getTiltAngle(context) == -1) {
            PreferenceManager.getDefaultSharedPreferences(context)
                    .edit()
                    .putInt(PREF_TILT_ANGLE, 0)
                    .putBoolean(PREF_HEADSUP_NOTIFICATION, true)
                    .putBoolean(PREF_VIBRATE, true)
                    .putBoolean(PREF_PORTRAIT_ORIENTATION, false)   //app is on for portrait
                    //.putBoolean(PREF_LANDSCAPE_ORIENTATION, true)
                    .apply();
        }
    }

    //Set preference for tilt angle seekbar
    public static void setTiltAngle(Context context, int angle) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putInt(PREF_TILT_ANGLE, angle)
                .apply();
    }

    //Get preference for tilt angle seekbar
    public static int getTiltAngle(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(PREF_TILT_ANGLE, -1);
    }

    //Set preference for Headups Notification switch
    public static void setHeadsupSwitch(Context context, boolean isEnabled) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(PREF_HEADSUP_NOTIFICATION, isEnabled)
                .apply();
    }

    //Get preference for Headups Notification switch
    public static boolean getHeadsupSwitch(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(PREF_HEADSUP_NOTIFICATION, true);
    }

    //Set preference for Vibrate switch
    public static void setVibrateSwitch(Context context, boolean isEnabled) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(PREF_VIBRATE, isEnabled)
                .apply();
    }

    //Get preference for Vibrate switch
    public static boolean getVibrateSwitch(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(PREF_VIBRATE, true);
    }

    //Set preference for Portrait Orientation switch
    public static void setPortraitSwitch(Context context, boolean isEnabled) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(PREF_PORTRAIT_ORIENTATION, isEnabled)
                .apply();
    }

    //Get preference for Portrait Orientation switch
    public static boolean getPortraitSwitch(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(PREF_PORTRAIT_ORIENTATION, false);
    }


}
