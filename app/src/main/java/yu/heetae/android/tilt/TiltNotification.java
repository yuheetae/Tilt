package yu.heetae.android.tilt;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;

/**
 * Created by yu on 7/11/15.
 */
public class TiltNotification {
    private static final String TAG = "tilt.android.heetae.yu";
    private static final String PAUSE = "yu.heetae.android.tilt.PLAY/PAUSE";
    private static final String SETTINGS = "yu.heetae.android.tilt.SETTINGS";
    private static final String POWER = "yu.heetae.android.tilt.POWER";

    public static final String KEY_ID = "yu.heetae.android.tilt.NOTIFICATION_ID_KEY";
    private static final int NOTIFICATION_ID = 7483;

    private static TiltNotification sTiltNotification;

    public static NotificationManager sManager;
    public static Notification sNotification;

    private static PendingIntent pause;
    private static PendingIntent play;
    private static PendingIntent power;
    private static PendingIntent settings;

    public static void createNotification(Context appContext) {
        Intent pauseIntent = new Intent(appContext, SensorService.class);
        pauseIntent.setAction("Pause");
        pause = PendingIntent.getService(appContext, 0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent playIntent = new Intent(appContext, SensorService.class);
        playIntent.setAction("Play");
        play = PendingIntent.getService(appContext, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        Intent settingsIntent = new Intent(SETTINGS);
        settings = PendingIntent.getBroadcast(appContext, 0, settingsIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Intent powerIntent = new Intent(POWER);
        powerIntent.putExtra(KEY_ID, NOTIFICATION_ID);
        power = PendingIntent.getBroadcast(appContext, 0, powerIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        sManager = (NotificationManager) appContext.getSystemService(Context.NOTIFICATION_SERVICE);

        sNotification = new NotificationCompat.Builder(appContext)
                .setSmallIcon(R.drawable.ic_launcher_notification)
                .setContentTitle("Tilt")
                .setContentText("Expand for options")
                .setPriority(android.support.v4.app.NotificationCompat.PRIORITY_MIN)
                //.setOngoing(true)
                .addAction(R.drawable.ic_pause_black_36dp, null, pause)
                .addAction(R.drawable.ic_power_settings_new_black_24dp, null, power)
                .addAction(R.drawable.ic_settings_black_24dp, null, settings)
                .setColor(Color.parseColor("#303030"))
                .build();


        sManager.notify(NOTIFICATION_ID, sNotification);
    }

    public static void close(int id) {
        sManager.cancel(id);
    }

    public static void update(Context appContext, String option) {

        sManager = (NotificationManager) appContext.getSystemService(Context.NOTIFICATION_SERVICE);


        Intent settingsIntent = new Intent(SETTINGS);
        settings = PendingIntent.getBroadcast(appContext, 0, settingsIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Intent powerIntent = new Intent(POWER);
        powerIntent.putExtra(KEY_ID, NOTIFICATION_ID);
        power = PendingIntent.getBroadcast(appContext, 0, powerIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        int playPauseId;
        PendingIntent playPauseIntent;
        if(option == "Pause") {
            playPauseId = R.drawable.ic_play_arrow_black_36dp;
            Intent playIntent = new Intent(appContext, SensorService.class);
            playIntent.setAction("Play");
            play = PendingIntent.getService(appContext, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            playPauseIntent = play;
        }
        else {
            playPauseId = R.drawable.ic_pause_black_36dp;
            Intent pauseIntent = new Intent(appContext, SensorService.class);
            pauseIntent.setAction("Pause");
            pause = PendingIntent.getService(appContext, 0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            playPauseIntent = pause;
        }

        Notification notification = new NotificationCompat.Builder(appContext)
                .setSmallIcon(R.drawable.ic_launcher_notification)
                .setContentTitle("Tilt")
                .setContentText("Expand for options")
                .setPriority(android.support.v4.app.NotificationCompat.PRIORITY_MIN)
                //.setOngoing(true)
                .addAction(playPauseId, null, playPauseIntent)
                .addAction(R.drawable.ic_power_settings_new_black_24dp, null, power)
                .addAction(R.drawable.ic_settings_black_24dp, null, settings)
                .setColor(Color.parseColor("#303030"))
                .build();

        sManager.notify(NOTIFICATION_ID, notification);
    }



}
