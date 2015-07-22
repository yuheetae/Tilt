package yu.heetae.android.tilt;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.Vibrator;
import android.service.notification.NotificationListenerService;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Surface;
import android.widget.Button;
import android.widget.RemoteViews;

import static android.view.Surface.*;

/**
 * Created by yu on 6/23/15.
 */
public class SensorService extends Service implements SensorEventListener{
    private static final String TAG = "tilt.android.heetae.yu";
    private static final String PAUSE = "yu.heetae.android.tilt.PLAY/PAUSE";
    private static final String SETTINGS = "yu.heetae.android.tilt.SETTINGS";
    private static final String POWER = "yu.heetae.android.tilt.POWER";
    private static final int NOTIFICATION_ID = 7483;

    private Sensor mSensor;
    private SensorManager mSensorManager;
    private float[] mAccelValues;
    private float[] mMagnet;
    private float[] mGravity;
    private float[] mOrientation;
    private float[] mRotationMatrix;
    private SensorEventListener mSensorEventListener;
    private boolean noSensor = false;

    private boolean isReceiverRegistered;

    private NotificationManager nm;
    private Notification ongoingNotification;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(intent != null && intent.getAction() != null) {

            if (intent.getAction() == "Pause") {
                Log.i(TAG, "SERVICE IS OFF");
                TiltNotification.update(this, intent.getAction());
                unregisterSensor();
                //stopSelf();
            } else if (intent.getAction() == "Play") {
                Log.i(TAG, "SERVICE IS ON");
                TiltNotification.update(this, intent.getAction());
            }
        }

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

        setSensorListeners(mSensorManager, this);

        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        IntentFilter filter = new IntentFilter();
        filter.addAction(PAUSE);
        filter.addAction(SETTINGS);
        filter.addAction(POWER);

        /*if(intent != null && intent.getAction() == "Play") {
            registerReceiver(notificationReceiver, filter);
        }
*/

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
        super.onDestroy();
    }

    public void setSensorListeners(SensorManager manager, SensorEventListener eventListener) {
        manager.registerListener(eventListener, manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                1000000);
        manager.registerListener(eventListener, manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                1000000);
        manager.registerListener(eventListener, manager.getDefaultSensor(Sensor.TYPE_GRAVITY), SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void unregisterSensor() {
        mSensorManager.unregisterListener(this);
    }

    private void checkTilt() {
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

        if (tiltAngle > -15 && tiltAngle < 0) {
            tiltAlert();
        }

    }

    public void tiltAlert() {
        Notification alertNotification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher_notification)
                .setContentTitle("Alert Head Tilt")
                .setContentText("Your phone is titled to far")
                .build();

        nm.notify(25, alertNotification);
    }


    /*private BroadcastReceiver notificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch(action) {
                case(SETTINGS):
                    Log.i(TAG, "SETTINGS BUTTON");
                    //tiltAlert();
                    break;
                case(POWER):
                    Log.i(TAG, "POWER BUTTON");
                    int notificationId = intent.getIntExtra(TiltActivity.KEY_ID, 0);
                    TiltNotification.close(NOTIFICATION_ID);
                    if(mSensorManager != null) Log.i(TAG, "SENSOR IS NOT NULL");
                    unregisterSensor();
                    if(mSensorManager == null) Log.i(TAG, "SENSOR IS NULL");
                    //unregisterReceiver(notificationReceiver);
                    stopSelf();
                    break;
            }
        }
    };  */
}
