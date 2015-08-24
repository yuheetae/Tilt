package yu.heetae.android.tilt;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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


        /*
        Intent pauseIntent = new Intent(this, SensorService.class);
        pauseIntent.setAction("Pause");
        PendingIntent pause = PendingIntent.getService(this, 0, pauseIntent, 0);

        Intent playIntent = new Intent(this, SensorService.class);
        pauseIntent.setAction("Play");
        PendingIntent play = PendingIntent.getService(this, 0, playIntent, 0);


        Intent settingsIntent = new Intent(SETTINGS);
        PendingIntent settings = PendingIntent.getBroadcast(this, 0, settingsIntent, 0);
        Intent powerIntent = new Intent(POWER);
        powerIntent.putExtra(KEY_ID, NOTIFICATION_ID);
        PendingIntent power = PendingIntent.getBroadcast(this, 0, powerIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher_notification)
                .setContentTitle("Tilt")
                .setContentText("Expand for options")
                .setPriority(android.support.v4.app.NotificationCompat.PRIORITY_MIN)
                        //.setOngoing(true)
                .addAction(R.drawable.ic_pause_black_36dp, null, pause)
                .addAction(R.drawable.ic_power_settings_new_black_24dp, null, power)
                .addAction(R.drawable.ic_settings_black_24dp, null, settings)
                .setColor(Color.parseColor("#303030"));


        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.notify(NOTIFICATION_ID, builder.build());
        */

        //setContentView(R.layout.activity_tilt);

        /*
        mAccelValues = new float[3];
        mMagnet = new float[3];
        mGravity = new float[3];
        mOrientation = new float[3];
        mRotationMatrix = new float[9];

        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);

        mSensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                switch(event.sensor.getType()) {
                    case Sensor.TYPE_ACCELEROMETER:
                        System.arraycopy(event.values, 0, mAccelValues, 0, 3);
                        break;
                    case Sensor.TYPE_MAGNETIC_FIELD:
                        System.arraycopy(event.values, 0, mMagnet, 0, 3);
                        break;
                    case Sensor.TYPE_GRAVITY:
                        System.arraycopy(event.values, 0, mGravity, 0, 3);
                        break;
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

        setSensorListeners(mSensorManager, mSensorEventListener);

        b = (Button)findViewById(R.id.sensor_button);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, Float.toString(mGravity[2]));
                SensorManager.getRotationMatrix(mRotationMatrix, null, mAccelValues, mMagnet);
                SensorManager.getOrientation(mRotationMatrix, mOrientation);

                if(mGravity[2] < 0)                                     {
                    if (mOrientation[1] > 0) {
                        mOrientation[1] = (float) (Math.PI - mOrientation[1]);
                    }
                    else {
                        mOrientation[1] = (float) (-Math.PI - mOrientation[1]);
                    }
                }


                double one = Math.toDegrees(mOrientation[0]);
                double two = Math.toDegrees(mOrientation[1]);
                double three = Math.toDegrees(mOrientation[2]);



                String results = "\nOrientation[0]:  " + mOrientation[0] +
                        "\nOrientation[1]:  " + two +
                        "\nOrientation[2]:  " + mOrientation[2];
                Log.i(TAG, results);

                tiltExceeded(one, two, three);
            }
        });
        */



    /*
    public void tiltExceeded(double tilt1, double tilt2, double tilt3) {
        int orient = getWindowManager().getDefaultDisplay().getRotation();
        switch(orient) {
            case Surface.ROTATION_0:
                //Toast.makeText(this, "Normal Portrait", Toast.LENGTH_SHORT).show();
                if(tilt2 >-40 && tilt2 < 0) {
                    //Log.i(TAG, "VIBRATE");
                    //Log.i(TAG, "Orientation[0]: " + tilt1 + "\nOrientation[1]: " + tilt2 +"\nOrientation[2]: " + tilt3);
                    Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    v.vibrate(300);
                }
                break;
            case Surface.ROTATION_90:
                //Toast.makeText(this, "Normal Landscape", Toast.LENGTH_SHORT).show();
                break;
            case Surface.ROTATION_180:
                //Toast.makeText(this, "Reverse Portrait", Toast.LENGTH_SHORT).show();
                break;
            case Surface.ROTATION_270:
                //Toast.makeText(this, "Reverse Landscape", Toast.LENGTH_SHORT).show();
                break;

        }
    }

    public void setSensorListeners(SensorManager manager, SensorEventListener eventListener) {
        manager.registerListener(eventListener, manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                1000000);
        manager.registerListener(eventListener, manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                1000000);
        manager.registerListener(eventListener, manager.getDefaultSensor(Sensor.TYPE_GRAVITY), SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(mSensorEventListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setSensorListeners(mSensorManager, mSensorEventListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSensorManager.unregisterListener(mSensorEventListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    */


}
