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


        mSeekBar = (SeekBar)findViewById(R.id.seekbar);
        mSeekBar.setMax(45);
        mSeekBar.setOnSeekBarChangeListener(this);

        angle = (TextView)findViewById(R.id.tilt_angle);
        Switch headsup = (Switch) findViewById(R.id.headsup_switch);
        Switch vibrate = (Switch) findViewById(R.id.vibrate_switch);
        Switch portrait = (Switch) findViewById(R.id.portrait_orientation_switch);
        Switch landscape = (Switch) findViewById(R.id.landscape_orientation_switch);

        int preferredAngle = PreferenceManager.getDefaultSharedPreferences(this).getInt(TiltActivity.PREF_TILT_ANGLE, -1);
        boolean isHeadsUpOn = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PREF_HEADSUP_NOTIFICATION, true);
        boolean isVibrateOn = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PREF_VIBRATE, true);
        boolean isPortraitEnabled = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PREF_PORTRAIT_ORIENTATION, true);
        boolean isLandscapeEnabled = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PREF_LANDSCAPE_ORIENTATION, true);

        if(preferredAngle == -1) {
            PreferenceManager.getDefaultSharedPreferences(this)
                    .edit()
                    .putInt(PREF_TILT_ANGLE, 0)
                    .putBoolean(PREF_HEADSUP_NOTIFICATION, true)
                    .putBoolean(PREF_VIBRATE, true)
                    .commit();
        }
        else {
            angle.setText("Tilt Angle " + preferredAngle + "\u00B0");
            mSeekBar.setProgress(preferredAngle);
            headsup.setChecked(isHeadsUpOn);
            vibrate.setChecked(isVibrateOn);
        }



        headsup.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    PreferenceManager.getDefaultSharedPreferences(TiltActivity.this)
                            .edit()
                            .putBoolean(PREF_HEADSUP_NOTIFICATION, true)
                            .commit();
                } else {
                    PreferenceManager.getDefaultSharedPreferences(TiltActivity.this)
                            .edit()
                            .putBoolean(PREF_HEADSUP_NOTIFICATION, false)
                            .commit();
                }
            }
        });


        vibrate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    PreferenceManager.getDefaultSharedPreferences(TiltActivity.this)
                            .edit()
                            .putBoolean(PREF_VIBRATE, true)
                            .commit();
                }
                else {
                    PreferenceManager.getDefaultSharedPreferences(TiltActivity.this)
                            .edit()
                            .putBoolean(PREF_VIBRATE, false)
                            .commit();
                }
            }
        });

        portrait.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    PreferenceManager.getDefaultSharedPreferences(TiltActivity.this)
                            .edit()
                            .putBoolean(PREF_PORTRAIT_ORIENTATION, true)
                            .commit();
                } else {
                    PreferenceManager.getDefaultSharedPreferences(TiltActivity.this)
                            .edit()
                            .putBoolean(PREF_PORTRAIT_ORIENTATION, false)
                            .commit();
                }
            }
        });

        landscape.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    PreferenceManager.getDefaultSharedPreferences(TiltActivity.this)
                            .edit()
                            .putBoolean(PREF_PORTRAIT_ORIENTATION, true)
                            .commit();
                } else {
                    PreferenceManager.getDefaultSharedPreferences(TiltActivity.this)
                            .edit()
                            .putBoolean(PREF_PORTRAIT_ORIENTATION, false)
                            .commit();
                }
            }
        });

            Intent i = new Intent(getApplicationContext(), SensorService.class);
            startService(i);
            Log.i(TAG, "Service Started From TiltActivity");


        SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                Log.i(TAG, key + "Preference Changed");
                switch(key) {
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


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        angle.setText("Tilt Angle " + progress + "\u00B0");
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        PreferenceManager.getDefaultSharedPreferences(this)
                .edit()
                .putInt(PREF_TILT_ANGLE, mSeekBar.getProgress())
                .commit();
    }


}
