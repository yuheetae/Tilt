package yu.heetae.android.tilt;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;


public class TiltActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener{
    private static final String TAG = "tilt.android.heetae.yu";

    //Shared Preference Variable Keys for Settings
    public static final String PREF_TILT_ANGLE = "tilt_angle";
    public static final String PREF_HEADSUP_NOTIFICATION = "headsup_notification";
    public static final String PREF_VIBRATE = "notification_vibrate";
    public static final String PREF_PORTRAIT_ORIENTATION = "portrait_orientation";
    public static final String PREF_LANDSCAPE_ORIENTATION = "landscape_orientation";

    public static final String KEY_ID = "yu.heetae.android.tilt.NOTIFICATION_ID_KEY";

    private SeekBar mSeekBar;
    private TextView angle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_tilt);

        //Initialize tilt angle seekbar
        mSeekBar = (SeekBar)findViewById(R.id.seekbar);
        mSeekBar.setMax(45);
        mSeekBar.setOnSeekBarChangeListener(this);

        //Initialize textview for tilt angle
        angle = (TextView)findViewById(R.id.tilt_angle);

        //Initiate Switch Buttons
        Switch headsup = (Switch) findViewById(R.id.headsup_switch);
        Switch vibrate = (Switch) findViewById(R.id.vibrate_switch);
        Switch portrait = (Switch) findViewById(R.id.portrait_orientation_switch);
        //Switch landscape = (Switch) findViewById(R.id.landscape_orientation_switch);

        //Get SharedPreference Variables
        int preferredAngle = PreferenceManager.getDefaultSharedPreferences(this).getInt(TiltActivity.PREF_TILT_ANGLE, -1);
        boolean isHeadsUpOn = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PREF_HEADSUP_NOTIFICATION, true);
        boolean isVibrateOn = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PREF_VIBRATE, true);
        boolean isPortraitEnabled = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PREF_PORTRAIT_ORIENTATION, false);
        boolean isLandscapeEnabled = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PREF_LANDSCAPE_ORIENTATION, false);

        //First time setting up preference variables
        if(preferredAngle == -1) {
            PreferenceManager.getDefaultSharedPreferences(this)
                    .edit()
                    .putInt(PREF_TILT_ANGLE, 0)
                    .putBoolean(PREF_HEADSUP_NOTIFICATION, true)
                    .putBoolean(PREF_VIBRATE, true)
                    .putBoolean(PREF_PORTRAIT_ORIENTATION, false)   //app is on for portrait
                    //.putBoolean(PREF_LANDSCAPE_ORIENTATION, true)
                    .commit();
        }
        //Setup seekbar and switches based on preferences
        else {
            angle.setText("Tilt Angle " + preferredAngle + "\u00B0");
            mSeekBar.setProgress(preferredAngle);
            headsup.setChecked(isHeadsUpOn);
            vibrate.setChecked(isVibrateOn);
            portrait.setChecked(isPortraitEnabled);
            //landscape.setChecked(isLandscapeEnabled);
        }

        //Headsup Switch Listener
        headsup.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //If user wants headsup notification set preference variable true
                if (isChecked) {
                    PreferenceManager.getDefaultSharedPreferences(TiltActivity.this)
                            .edit()
                            .putBoolean(PREF_HEADSUP_NOTIFICATION, true)
                            .commit();
                }
                //If user does not want headsup notification set preference variable false
                else {
                    PreferenceManager.getDefaultSharedPreferences(TiltActivity.this)
                            .edit()
                            .putBoolean(PREF_HEADSUP_NOTIFICATION, false)
                            .commit();
                }
            }
        });

        //Vibrate Switch Listener
        vibrate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //If user wants vibration with notification set preference variable true
                if(isChecked) {
                    PreferenceManager.getDefaultSharedPreferences(TiltActivity.this)
                            .edit()
                            .putBoolean(PREF_VIBRATE, true)
                            .commit();
                }
                //If user does not want vibration with notification set preference variable false
                else {
                    PreferenceManager.getDefaultSharedPreferences(TiltActivity.this)
                            .edit()
                            .putBoolean(PREF_VIBRATE, false)
                            .commit();
                }
            }
        });

        //Portrait Switch Listener
        portrait.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //If user wants the app enabled when phone is in portrait mode
                if (isChecked) {
                    PreferenceManager.getDefaultSharedPreferences(TiltActivity.this)
                            .edit()
                            .putBoolean(PREF_PORTRAIT_ORIENTATION, true)
                            .commit();
                }
                //If user does not want the app enabled when phone is in portrait mode
                else {
                    PreferenceManager.getDefaultSharedPreferences(TiltActivity.this)
                            .edit()
                            .putBoolean(PREF_PORTRAIT_ORIENTATION, false)
                            .commit();
                }
            }
        });

        //Landscape Switch Listener
        /*
        landscape.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //If user wants the app enabled when phone is in landscape mode
                if (isChecked) {
                    PreferenceManager.getDefaultSharedPreferences(TiltActivity.this)
                            .edit()
                            .putBoolean(PREF_PORTRAIT_ORIENTATION, true)
                            .commit();
                }
                //If user does not want the app enabled when phone is in landscape mode
                else {
                    PreferenceManager.getDefaultSharedPreferences(TiltActivity.this)
                            .edit()
                            .putBoolean(PREF_PORTRAIT_ORIENTATION, false)
                            .commit();
                }
            }
        });
        */

        //Start SensorService to detect phone tilt
        if(!SensorService.isServiceRunning) {
            Intent i = new Intent(getApplicationContext(), SensorService.class);
            startService(i);
            Log.i(TAG, "Service Started From TiltActivity");
        }

        //SharedPreferenceChange Listener
        SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                Log.i(TAG, key + "Preference Changed");
                switch(key) {
                    //If tilt angle is changed
                    case(PREF_TILT_ANGLE):
                        SensorService.preferredTiltAngle = PreferenceManager.getDefaultSharedPreferences(TiltActivity.this).getInt(PREF_TILT_ANGLE, 0);
                        int orientation = getWindowManager().getDefaultDisplay().getRotation();
                        if(orientation == Surface.ROTATION_0 || orientation == Surface.ROTATION_90) {
                            SensorService.preferredTiltAngle = -SensorService.preferredTiltAngle;
                        }
                        break;
                    case(PREF_HEADSUP_NOTIFICATION):
                        SensorService.isHeadsUpEnabled = PreferenceManager.getDefaultSharedPreferences(TiltActivity.this).getBoolean(PREF_HEADSUP_NOTIFICATION, true);
                        break;
                    case(PREF_VIBRATE):
                        SensorService.isVibrateEnabled = PreferenceManager.getDefaultSharedPreferences(TiltActivity.this).getBoolean(PREF_VIBRATE, true);
                        break;
                    case(PREF_PORTRAIT_ORIENTATION):
                        SensorService.isPortraitEnabled = PreferenceManager.getDefaultSharedPreferences(TiltActivity.this).getBoolean(PREF_PORTRAIT_ORIENTATION, true);
                        break;
                    case(PREF_LANDSCAPE_ORIENTATION):
                        SensorService.isLandscapeEnabled = PreferenceManager.getDefaultSharedPreferences(TiltActivity.this).getBoolean(PREF_LANDSCAPE_ORIENTATION, true);
                        break;
                }
            }
        };

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener);
    }

    //Change text displaying tilt angle when seekbar progress is changed
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        angle.setText("Tilt Angle " + progress + "\u00B0");
    }

    //When user releases seekbar set preference variable
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        PreferenceManager.getDefaultSharedPreferences(this)
                .edit()
                .putInt(PREF_TILT_ANGLE, mSeekBar.getProgress())
                .commit();
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

}
