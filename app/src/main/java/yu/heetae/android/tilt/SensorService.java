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
    private static final String TIMER_FINISHED = "yu.heetae.android.tilt.TIMER_FINISHED";
    private static final String FIFTEEN = "yu.heetae.android.tilt.FIFTEEN";
    private static final String THIRTY = "yu.heetae.android.tilt.THIRTY";
    private static final String ONEHOUR = "yu.heetae.android.tilt.ONEHOUR";
    private static final String CANCEL_TIMER = "yu.heetae.android.tilt.CANCEL_TIMER";

    private static final String NOTIFICATION_CANCEL = "yu.heetae.android.tilt.NOTIFICATION_CANCEL";

    public static final String PREF_HEADSUP_NOTIFICATION = "headsup_notification";
    public static final String PREF_VIBRATE = "notification_vibrate";
    public static final String PREF_PORTRAIT_ORIENTATION = "portrait_orientation";
    public static final String PREF_LANDSCAPE_ORIENTATION = "landscape_orientation";


    public static boolean isServiceRunning;

    private Sensor mSensor;
    private SensorManager mSensorManager;
    private float[] mAccelValues;
    private float[] mMagnet;
    private float[] mGravity;
    private float[] mOrientation;
    private float[] mRotationMatrix;
    private SensorEventListener mSensorEventListener;
    private boolean noSensor = false;

    private static Object paused = new Object();
    private static Object running = new Object();
    private static Object timed = new Object();
    public static Object sensorState = running;

    private boolean isScreenOn = true;

    private static final int NOTIFICATION_ID = 7483;

    private NotificationManager nm;

    public AlarmManager alarmManager;
    public Intent i = new Intent(TIMER_FINISHED);
    PendingIntent pauseInterval;

    boolean enablePortrait = true;
    boolean enableLandscape = true;

    public static boolean isHeadsUpEnabled;
    public static boolean isVibrateEnabled;
    public static boolean isPortraitEnabled;
    public static boolean isLandscapeEnabled;

    private int orientation;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.i(TAG, "onCreate Called");

        alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);

        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);

        if(mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
        else if(mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null) {
            mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        }
        else {
            noSensor = true;
        }

        mAccelValues = new float[3];
        mMagnet = new float[3];
        mGravity = new float[3];
        mOrientation = new float[3];
        mRotationMatrix = new float[9];

        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        IntentFilter filter = new IntentFilter();
        filter.addAction(NOTIFICATION_CANCEL);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(TIMER_FINISHED);
        filter.addAction(FIFTEEN);
        filter.addAction(THIRTY);
        filter.addAction(ONEHOUR);
        filter.addAction(CANCEL_TIMER);

        registerReceiver(screenReceiver, filter);

        Notification notif = TiltNotification.createNotification(this);
        startForeground(NOTIFICATION_ID, notif);

        setSensorListeners(mSensorManager);

        isHeadsUpEnabled = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PREF_HEADSUP_NOTIFICATION, true);
        isVibrateEnabled = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PREF_VIBRATE, true);
        isPortraitEnabled = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PREF_PORTRAIT_ORIENTATION, true);
        isLandscapeEnabled = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PREF_LANDSCAPE_ORIENTATION, true);

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

    public void setSensorListeners(SensorManager manager) {
        manager.registerListener(this, manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                1000000);
        manager.registerListener(this, manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                1000000);
        manager.registerListener(this, manager.getDefaultSensor(Sensor.TYPE_GRAVITY), SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void unregisterSensor() {
        mSensorManager.unregisterListener(this);
    }

    private void checkTilt() {

        int preferredAngle = PreferenceManager.getDefaultSharedPreferences(this).getInt(TiltActivity.PREF_TILT_ANGLE, -1);
        boolean isPortraitEnabled = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(TiltActivity.PREF_PORTRAIT_ORIENTATION, true);
        boolean isLandscapeEnabled = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(TiltActivity.PREF_LANDSCAPE_ORIENTATION, true);

        if(preferredAngle == -1) Log.i(TAG, "PREFERENCE MANAGER NOT WORKING!!!!!!!!");
        //else Log.i(TAG, "PREFERRED ANGLE = " + preferredAngle);
        SensorManager.getRotationMatrix(mRotationMatrix, null, mAccelValues, mMagnet);
        SensorManager.getOrientation(mRotationMatrix, mOrientation);

        if(mGravity[2] < 0)                                     {
            if (mOrientation[1] > 0) {
                mOrientation[1] = (float) (Math.PI - mOrientation[1]);
            } else {
                mOrientation[1] = (float) (-Math.PI - mOrientation[1]);
            }

        }

        double tiltAngle = Math.toDegrees(mOrientation[1]);
        double tiltAngle2 = Math.toDegrees(mOrientation[2]);


        Log.i(TAG, Double.toString(tiltAngle2));


        if(orientation == Surface.ROTATION_0) {
            if (tiltAngle == 0) {
                if (1 / tiltAngle > 0 && tiltAngle > -preferredAngle && tiltAngle < 30) {
                    Log.i(TAG, "TILT ALERT");
                    tiltAlert();
                    unregisterSensor();
                }
            } else {
                if (tiltAngle > -preferredAngle && tiltAngle < 30) {
                    Log.i(TAG, "TILT ALERTT");
                    tiltAlert();
                    unregisterSensor();
                }
            }
        }

        //if(tiltAngle == 0 && !(1/tiltAngle > 0)) Log.i(TAG, "DISREGARD NEGATIVE ZERO");
    }

    public void tiltAlert() {

        //boolean isHeadsUpOn = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PREF_HEADSUP_NOTIFICATION, true);
        //boolean isVibrateOn = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PREF_VIBRATE, true);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher_notification)
                .setContentTitle("Alert Head Tilt")
                .setContentText("Your phone is titled to far");

        if(!isHeadsUpEnabled) mBuilder.setPriority(Notification.PRIORITY_DEFAULT);
        else mBuilder.setPriority(Notification.PRIORITY_HIGH);
        if(!isVibrateEnabled) mBuilder.setVibrate(new long[0]);
        else mBuilder.setVibrate(new long[] {500, 500});


        Notification alertNotification = mBuilder.build();


        Intent notificationCancel = new Intent(NOTIFICATION_CANCEL);
        alertNotification.deleteIntent = PendingIntent.getBroadcast(this, 0, notificationCancel, 0);
        nm.notify(25, alertNotification);
       // Log.i(TAG, "Head is Tilted");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        switch (((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay().getRotation()) {
            case Surface.ROTATION_0:
                //Log.i(TAG, "NORMAL PORTRAIT");
                orientation = Surface.ROTATION_0;
                if(sensorState == running && isPortraitEnabled) {
                    setSensorListeners(mSensorManager);
                }
                if(!isPortraitEnabled) {
                    unregisterSensor();
                    TiltNotification.update(this, "Pause", -2);
                }
                break;
            case Surface.ROTATION_90:
                //Log.i(TAG, "NORMAL LANDSCAPE");
                orientation = Surface.ROTATION_90;
                if(sensorState == running && isLandscapeEnabled) {
                    setSensorListeners(mSensorManager);
                }
                if(!isLandscapeEnabled) {
                    unregisterSensor();
                    TiltNotification.update(this, "Pause", -2);
                }
                break;
            case Surface.ROTATION_270:
                //Log.i(TAG, "REVERSE LANDSCAPE");
                orientation = Surface.ROTATION_270;
                if(sensorState == running && isLandscapeEnabled) {
                    setSensorListeners(mSensorManager);
                }
                if(!isLandscapeEnabled) {
                    unregisterSensor();
                    TiltNotification.update(this, "Pause", -2);
                }
                break;
        }

    }


    private BroadcastReceiver screenReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Broadcast Intent Received: " + intent.getAction());
            String action = intent.getAction();
            String pauseOrPlay = "pause";
            switch(action) {
                case(NOTIFICATION_CANCEL):
                    if(sensorState == running) {
                        setSensorListeners(mSensorManager);
                        Log.i(TAG, "Notifcation Cancelled, Runing Sensors Now");
                    }
                case(Intent.ACTION_SCREEN_ON):
                    if(sensorState == running) {
                        setSensorListeners(mSensorManager);
                    }
                    isScreenOn = true;
                    Log.i(TAG, "SERVICE Started; SCREEN IS ON");
                    break;
                case(Intent.ACTION_SCREEN_OFF):
                    if(sensorState == running) {
                        unregisterSensor();
                    }
                    isScreenOn = false;
                    Log.i(TAG, "SERVICE PAUSED; SCREEN IS OFF");
                    break;
                case(TIMER_FINISHED):
                    sensorState = running;
                    if(isScreenOn) {
                        setSensorListeners(mSensorManager);
                    }
                    TiltNotification.update(context, "Play", 15);
                    Log.i(TAG, "Pause is finished");
                    break;
                case(FIFTEEN):
                    unregisterSensor();
                    TiltNotification.update(context, "Pause", 30);
                    alarmManager.cancel(pauseInterval);
                    alarmManager.set(AlarmManager.RTC, System.currentTimeMillis()+5000, pauseInterval);
                    sensorState = timed;
                    Log.i(TAG, "5 second Pause");
                    break;
                case(THIRTY):
                    unregisterSensor();
                    TiltNotification.update(context, "Pause", 60);
                    alarmManager.cancel(pauseInterval);
                    alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + 10000, pauseInterval);
                    sensorState = timed;
                    Log.i(TAG, "10 second Pause");
                    break;
                case(ONEHOUR):
                    unregisterSensor();
                    TiltNotification.update(context, "Pause", 0);
                    alarmManager.cancel(pauseInterval);
                    alarmManager.set(AlarmManager.RTC, System.currentTimeMillis()+15000, pauseInterval);
                    sensorState = timed;
                    Log.i(TAG, "15 second Pause");
                    break;
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
