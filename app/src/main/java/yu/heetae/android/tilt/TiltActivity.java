package yu.heetae.android.tilt;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
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

    private TextView mAngleTextView;
    private LinearLayout mLinearLayout;
    private SeekBar mSeekBar;
    private Switch mHeadsupSwitch;
    private Switch mVibrateSwitch;
    private Switch mPortraitSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_tilt);

        //Initialize tilt mAngleTextView seekbar
        mSeekBar = (SeekBar)findViewById(R.id.seekbar);
        mSeekBar.setMax(45);
        mSeekBar.setOnSeekBarChangeListener(this);

        mLinearLayout = (LinearLayout)findViewById(R.id.manual);
        mLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                
            }
        });

        //Initialize textview for tilt mAngleTextView
        mAngleTextView = (TextView)findViewById(R.id.tilt_angle);

        //Initiate Switch Buttons
        mHeadsupSwitch = (Switch) findViewById(R.id.headsup_switch);
        mVibrateSwitch = (Switch) findViewById(R.id.vibrate_switch);
        mPortraitSwitch = (Switch) findViewById(R.id.portrait_orientation_switch);
        //Switch landscape = (Switch) findViewById(R.id.landscape_orientation_switch);

        //Set preference values for first startup
        SettingsPreferences.setInitialSettings(this);

        //Get SharedPreference Variables
        int tiltAngle = SettingsPreferences.getTiltAngle(this);
        boolean isHeadsUpOn = SettingsPreferences.getHeadsupSwitch(this);
        boolean isVibrateOn = SettingsPreferences.getVibrateSwitch(this);
        boolean isPortraitEnabled = SettingsPreferences.getPortraitSwitch(this);
        //boolean isLandscapeEnabled = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PREF_LANDSCAPE_ORIENTATION, false);


        //Setup seekbar and switches based on preferences
        mAngleTextView.setText("Tilt Angle " + tiltAngle + "\u00B0");
        mSeekBar.setProgress(tiltAngle);
        mHeadsupSwitch.setChecked(isHeadsUpOn);
        mVibrateSwitch.setChecked(isVibrateOn);
        mPortraitSwitch.setChecked(isPortraitEnabled);
        //landscape.setChecked(isLandscapeEnabled);


        //Headsup Switch Listener
        mHeadsupSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //If user wants mHeadsupSwitch notification set preference variable true
                if (isChecked) {
                    SettingsPreferences.setHeadsupSwitch(TiltActivity.this, true);
                }
                //If user does not want mHeadsupSwitch notification set preference variable false
                else {
                    SettingsPreferences.setHeadsupSwitch(TiltActivity.this, false);
                }
            }
        });

        //Vibrate Switch Listener
        mVibrateSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //If user wants vibration with notification set preference variable true
                if(isChecked) {
                    SettingsPreferences.setVibrateSwitch(TiltActivity.this, true);
                }
                //If user does not want vibration with notification set preference variable false
                else {
                    SettingsPreferences.setVibrateSwitch(TiltActivity.this, false);
                }
            }
        });

        //Portrait Switch Listener
        mPortraitSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //If user wants the app enabled when phone is in mPortraitSwitch mode
                if (isChecked) {
                    SettingsPreferences.setPortraitSwitch(TiltActivity.this, true);
                }
                //If user does not want the app enabled when phone is in mPortraitSwitch mode
                else {
                    SettingsPreferences.setPortraitSwitch(TiltActivity.this, false);
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
                    //If tilt mAngleTextView is changed
                    case(PREF_TILT_ANGLE):
                        SensorService.preferredTiltAngle = SettingsPreferences.getTiltAngle(TiltActivity.this);
                        int orientation = getWindowManager().getDefaultDisplay().getRotation();
                        if(orientation == Surface.ROTATION_0 || orientation == Surface.ROTATION_90) {
                            SensorService.preferredTiltAngle = -SensorService.preferredTiltAngle;
                        }
                        break;
                    case(PREF_HEADSUP_NOTIFICATION):
                        SensorService.isHeadsUpEnabled = SettingsPreferences.getHeadsupSwitch(TiltActivity.this);
                        break;
                    case(PREF_VIBRATE):
                        SensorService.isVibrateEnabled = SettingsPreferences.getVibrateSwitch(TiltActivity.this);
                        break;
                    case(PREF_PORTRAIT_ORIENTATION):
                        SensorService.isPortraitEnabled = SettingsPreferences.getPortraitSwitch(TiltActivity.this);
                        break;
                    /*
                    case(PREF_LANDSCAPE_ORIENTATION):
                        SensorService.isLandscapeEnabled = PreferenceManager.getDefaultSharedPreferences(TiltActivity.this).getBoolean(PREF_LANDSCAPE_ORIENTATION, true);
                        break;
                        */
                }
            }
        };

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener);
    }

    //Change text displaying tilt mAngleTextView when seekbar progress is changed
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        mAngleTextView.setText("Tilt Angle " + progress + "\u00B0");
    }

    //When user releases seekbar set preference variable
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        SettingsPreferences.setTiltAngle(this, mSeekBar.getProgress());
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

}
