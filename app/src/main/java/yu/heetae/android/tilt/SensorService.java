package yu.heetae.android.tilt;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;

/**
 * Created by yu on 6/23/15.
 */
public class SensorService extends Service implements SensorEventListener{
    private static final String TAG = "tilt.android.heetae.yu";

    //Intent actions
    private static final String TIMER_FINISHED = "yu.heetae.android.tilt.TIMER_FINISHED";
    private static final String FIFTEEN = "yu.heetae.android.tilt.FIFTEEN";
    private static final String THIRTY = "yu.heetae.android.tilt.THIRTY";
    private static final String ONEHOUR = "yu.heetae.android.tilt.ONEHOUR";
    private static final String CANCEL_TIMER = "yu.heetae.android.tilt.CANCEL_TIMER";
    private static final String NOTIFICATION_CANCEL = "yu.heetae.android.tilt.NOTIFICATION_CANCEL";

    //Shared Preferences Variable Keys
    public static final String PREF_HEADSUP_NOTIFICATION = "headsup_notification";
    public static final String PREF_VIBRATE = "notification_vibrate";
    public static final String PREF_PORTRAIT_ORIENTATION = "portrait_orientation";
    public static final String PREF_LANDSCAPE_ORIENTATION = "landscape_orientation";

    //boolean determining if Service is running
    public static boolean isServiceRunning;

    //Sensor & SensorManger
    private Sensor mSensor;
    private SensorManager mSensorManager;

    //Variables to hold Sensor Data
    private float[] mAccelValues;
    private float[] mMagnet;
    private float[] mGravity;
    private float[] mOrientation;
    private float[] mRotationMatrix;

    //Unused variable at this point
    private boolean noSensor = false;

    //Different States Sensor can be in
    private static Object paused = new Object();
    private static Object running = new Object();
    private static Object timed = new Object();

    //Sensor's current state
    public static Object sensorState = running;

    private boolean isScreenOn = true;

    private static final int NOTIFICATION_ID = 7483;

    private NotificationManager nm;

    public AlarmManager alarmManager;
    public Intent i = new Intent(TIMER_FINISHED);
    PendingIntent pauseInterval;

    //Variables to hold Preference variable values
    public static int preferredTiltAngle;
    public static boolean isHeadsUpEnabled;
    public static boolean isVibrateEnabled;
    public static boolean isPortraitEnabled;
    public static boolean isLandscapeEnabled;

    private int orientation;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.i(TAG, "onCreate Called");

        //Initialize AlarmManager & SensorManager
        alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);

        //If device has an accelerometer use it as main sensor
        if(mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
        //If device does not have an accelerometer use magnetic field as main sensor
        else if(mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null) {
            mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        }
        else {
            noSensor = true;
        }

        //Initialize arrays to contain sensor data
        mAccelValues = new float[3];
        mMagnet = new float[3];
        mGravity = new float[3];
        mOrientation = new float[3];
        mRotationMatrix = new float[9];

        //Initialize Notification Manager
        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        //Initialize IntentFilter and addActions
        IntentFilter filter = new IntentFilter();
        filter.addAction(NOTIFICATION_CANCEL);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(TIMER_FINISHED);
        filter.addAction(FIFTEEN);
        filter.addAction(THIRTY);
        filter.addAction(ONEHOUR);
        filter.addAction(CANCEL_TIMER);

        //Register Broadcast Receiver and filter
        registerReceiver(screenReceiver, filter);


        Notification notif = TiltNotification.createNotification(this);
        startForeground(NOTIFICATION_ID, notif);

        //Set sensorListener
        setSensorListeners(mSensorManager);

        //Initialize Preference Variable values
        isHeadsUpEnabled = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PREF_HEADSUP_NOTIFICATION, true);
        isVibrateEnabled = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PREF_VIBRATE, true);
        isPortraitEnabled = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PREF_PORTRAIT_ORIENTATION, true);
        isLandscapeEnabled = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PREF_LANDSCAPE_ORIENTATION, true);

        //Get orientation of device
        orientation = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay().getRotation();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(!isServiceRunning) isServiceRunning = true;
        Log.i(TAG, "onStartCommand Called");

        pauseInterval = PendingIntent.getBroadcast(this, 0, i, 0);

        if(intent != null && intent.getAction() != null) {

            if (intent.getAction() == "Pause") {
                Log.i(TAG, "SERVICE IS PAUSED");
                alarmManager.cancel(pauseInterval);
                TiltNotification.update(this, intent.getAction(), -1);

                unregisterSensor();
                sensorState = paused;
            } else if (intent.getAction() == "Play") {
                Log.i(TAG, "SERVICE IS Running");
                alarmManager.cancel(pauseInterval);
                sensorState = running;
                setSensorListeners(mSensorManager);
                Log.i(TAG, "Sensor Registered and isSensorOn==True");
                TiltNotification.update(this, intent.getAction(), 15);
            }
        }

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    //Put sensor data into arrays
    public void onSensorChanged(SensorEvent event) {
        switch(event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                System.arraycopy(event.values, 0, mAccelValues, 0, 3);
                checkTilt();
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                System.arraycopy(event.values, 0, mMagnet, 0, 3);
                break;
            case Sensor.TYPE_GRAVITY:
                System.arraycopy(event.values, 0, mGravity, 0, 3);
            default:
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onDestroy() {
        unregisterSensor();
        Log.i(TAG, "Service Destroyed");
        stopForeground(true);
        super.onDestroy();
    }

    //Register multiple listeners to a SensorManager
    public void setSensorListeners(SensorManager manager) {
        manager.registerListener(this, manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                1000000);
        manager.registerListener(this, manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                1000000);
        manager.registerListener(this, manager.getDefaultSensor(Sensor.TYPE_GRAVITY), SensorManager.SENSOR_DELAY_NORMAL);
    }

    //Unregister SensorManager
    public void unregisterSensor() {
        mSensorManager.unregisterListener(this);
    }

    //Check Phone Tilt
    private void checkTilt() {
        //Get user preferred tilt angle threshold
        int preferredAngle = PreferenceManager.getDefaultSharedPreferences(this).getInt(TiltActivity.PREF_TILT_ANGLE, -1);

        //set boolean variables depending on if device is in Portrait or Landscape Orientation
        boolean isPortraitEnabled = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(TiltActivity.PREF_PORTRAIT_ORIENTATION, true);
        boolean isLandscapeEnabled = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(TiltActivity.PREF_LANDSCAPE_ORIENTATION, true);

        //If preferred tilt angle threshold is default value ERROR
        if(preferredAngle == -1) Log.i(TAG, "PREFERENCE MANAGER NOT WORKING!!!!!!!!");

        //Retrieve Rotation Matrix and Orientation
        SensorManager.getRotationMatrix(mRotationMatrix, null, mAccelValues, mMagnet);
        SensorManager.getOrientation(mRotationMatrix, mOrientation);

        //Adjust Pitch(rotation about x-axis) based on force of gravity on y-axis
        if(mGravity[2] < 0) {
            if (mOrientation[1] > 0) {
                mOrientation[1] = (float) (Math.PI - mOrientation[1]);
            } else {
                mOrientation[1] = (float) (-Math.PI - mOrientation[1]);
            }

        }

        //Convert orientation for radians to degrees
        double tiltAnglePortrait = Math.toDegrees(mOrientation[1]);
        double tiltAngleLandscape = Math.toDegrees(mOrientation[2]);

        //if device orientation is in normal portrait
        if(orientation == Surface.ROTATION_0) {
            if (tiltAnglePortrait == 0) {
                if (1 / tiltAnglePortrait > 0 && tiltAnglePortrait > preferredTiltAngle && tiltAnglePortrait < 30) {
                    Log.i(TAG, "TILT ALERT");
                    tiltAlert();            //post alertNotification that phone is tilted too far
                    unregisterSensor();     //turn off sensors until alertNotification is dismissed
                }
            } else {
                if (tiltAnglePortrait > preferredTiltAngle && tiltAnglePortrait < 30) {
                    Log.i(TAG, "TILT ALERTT");
                    tiltAlert();
                    unregisterSensor();
                }
            }
        }

        //if device orientation is in either normal or reverse landscape
        else if(orientation == Surface.ROTATION_90 || orientation == Surface.ROTATION_270) {
            if (tiltAnglePortrait == 0) {
                if (1 / tiltAngleLandscape > 0 && tiltAngleLandscape > preferredTiltAngle && tiltAngleLandscape < 30) {
                    Log.i(TAG, "TILT ALERT");
                    tiltAlert();
                    unregisterSensor();
                }
            } else {
                if (tiltAngleLandscape > preferredTiltAngle && tiltAngleLandscape < 30) {
                    Log.i(TAG, "TILT ALERTT");
                    tiltAlert();
                    unregisterSensor();
                }
            }
        }


    }

    //Alert user with a notification that device is tilted beyond threshold
    public void tiltAlert() {

        //boolean isHeadsUpOn = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PREF_HEADSUP_NOTIFICATION, true);
        //boolean isVibrateOn = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PREF_VIBRATE, true);

        //Create notification
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher_notification)
                .setContentTitle("Alert Head Tilt")
                .setContentText("Your phone is titled to far");

        //Check whether headsup is enabled or not
        if(!isHeadsUpEnabled) mBuilder.setPriority(Notification.PRIORITY_DEFAULT);
        else mBuilder.setPriority(Notification.PRIORITY_HIGH);
        //Check whether notification vibration is enabled or not
        if(!isVibrateEnabled) mBuilder.setVibrate(new long[0]);
        else mBuilder.setVibrate(new long[] {500, 500});

        //Build alert notification
        Notification alertNotification = mBuilder.build();

        //Set intent to be executed when alertNotification is dismissed
        Intent notificationCancel = new Intent(NOTIFICATION_CANCEL);
        alertNotification.deleteIntent = PendingIntent.getBroadcast(this, 0, notificationCancel, 0);

        //Post alertNotification
        nm.notify(25, alertNotification);
    }


    @Override
    //Update variable that holds device's current orientation
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        switch (((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay().getRotation()) {
            case Surface.ROTATION_0:
                Log.i(TAG, "NORMAL PORTRAIT");
                orientation = Surface.ROTATION_0;
                break;
            case Surface.ROTATION_90:
                Log.i(TAG, "NORMAL LANDSCAPE");
                orientation = Surface.ROTATION_90;
                break;
            case Surface.ROTATION_270:
                Log.i(TAG, "REVERSE LANDSCAPE");
                orientation = Surface.ROTATION_270;
                break;
        }

    }

    //SensorService Broadcast Receiver
    private BroadcastReceiver screenReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Broadcast Intent Received: " + intent.getAction());
            String action = intent.getAction();
            String pauseOrPlay = "pause";
            switch(action) {
                //if user removes tilt alertNotification resume sensors
                case(NOTIFICATION_CANCEL):
                    if(sensorState == running) {
                        setSensorListeners(mSensorManager);
                        Log.i(TAG, "Notifcation Cancelled, Runing Sensors Now");
                    }
                //if screen is turned on turn on/off sensor depending on previous state
                case(Intent.ACTION_SCREEN_ON):
                    if(sensorState == running) {
                        setSensorListeners(mSensorManager);
                    }
                    isScreenOn = true;
                    Log.i(TAG, "SERVICE Started; SCREEN IS ON");
                    break;
                //if screen is turned off, turn off sensor and save it's previous state
                case(Intent.ACTION_SCREEN_OFF):
                    if(sensorState == running) {
                        unregisterSensor();
                    }
                    isScreenOn = false;
                    Log.i(TAG, "SERVICE PAUSED; SCREEN IS OFF");
                    break;
                //If timer is finished, turn on sensor if screen is on, change previous sensorstate if screen is off
                case(TIMER_FINISHED):
                    sensorState = running;
                    if(isScreenOn) {
                        setSensorListeners(mSensorManager);
                    }
                    TiltNotification.update(context, "Play", 15);
                    Log.i(TAG, "Pause is finished");
                    break;
                //Pause sensor for 15 minutes
                case(FIFTEEN):
                    unregisterSensor();
                    TiltNotification.update(context, "Pause", 30);
                    alarmManager.cancel(pauseInterval);
                    alarmManager.set(AlarmManager.RTC, System.currentTimeMillis()+5000, pauseInterval);
                    sensorState = timed;
                    Log.i(TAG, "5 second Pause");
                    break;
                //Pause sensor for thirty minutes
                case(THIRTY):
                    unregisterSensor();
                    TiltNotification.update(context, "Pause", 60);
                    alarmManager.cancel(pauseInterval);
                    alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + 10000, pauseInterval);
                    sensorState = timed;
                    Log.i(TAG, "10 second Pause");
                    break;
                //Pause sensor for one hour
                case(ONEHOUR):
                    unregisterSensor();
                    TiltNotification.update(context, "Pause", 0);
                    alarmManager.cancel(pauseInterval);
                    alarmManager.set(AlarmManager.RTC, System.currentTimeMillis()+15000, pauseInterval);
                    sensorState = timed;
                    Log.i(TAG, "15 second Pause");
                    break;
                //Cancel current timer and start sensor
                case(CANCEL_TIMER):
                    alarmManager.cancel(pauseInterval);
                    sensorState = running;
                    if(isScreenOn) {
                        setSensorListeners(mSensorManager);
                    }
                    TiltNotification.update(context, "Play", 15);
                    Log.i(TAG, "TIMER is CANCELLED");
                    break;
            }
        }
    };

}
