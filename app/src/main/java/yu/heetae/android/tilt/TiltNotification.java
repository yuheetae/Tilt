package yu.heetae.android.tilt;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

/**
 * Created by yu on 7/11/15.
 */
public class TiltNotification {//

    private static final String SETTINGS = "yu.heetae.android.tilt.SETTINGS";
    private static final String FIFTEEN = "yu.heetae.android.tilt.FIFTEEN";
    private static final String THIRTY = "yu.heetae.android.tilt.THIRTY";
    private static final String ONEHOUR = "yu.heetae.android.tilt.ONEHOUR";
    private static final String CANCEL_TIMER = "yu.heetae.android.tilt.CANCEL_TIMER";


    private static final int NOTIFICATION_ID = 7483;

    public static NotificationManager sManager;
    public static Notification sNotification;

    private static PendingIntent pause;
    private static PendingIntent play;
    private static PendingIntent timeInterval;
    private static PendingIntent settings;


    private static int playButton = R.drawable.ic_play_arrow_black_36dp;
    private static int pauseButton = R.drawable.ic_pause_black_36dp;
    private static int settingsButton = R.drawable.ic_settings_black_36dp;
    private static int fifteenButton = R.drawable.ic_fifteenmin;
    private static int thirtyButton = R.drawable.ic_thirtymin;
    private static int onehourButton = R.drawable.ic_onehour_white;
    private static int timerOffButton = R.drawable.ic_timer_off_black_36dp;



    public static Notification createNotification(Context appContext) {

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            playButton = R.drawable.ic_play_arrow_white_36dp;
            pauseButton = R.drawable.ic_pause_white_36dp;
            settingsButton = R.drawable.ic_settings_black_36dp;
            fifteenButton = R.drawable.ic_fifteenmin_white;
            thirtyButton = R.drawable.ic_thirtymin_white;
            onehourButton = R.drawable.ic_onehour_white;
            timerOffButton = R.drawable.ic_timer_off_white_36dp;
        }

        Intent pauseIntent = new Intent(appContext, SensorService.class);
        pauseIntent.setAction("Pause");
        pause = PendingIntent.getService(appContext, 0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent playIntent = new Intent(appContext, SensorService.class);
        playIntent.setAction("Play");
        play = PendingIntent.getService(appContext, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        Intent settingsIntent = new Intent(SETTINGS);
        settings = PendingIntent.getBroadcast(appContext, 0, settingsIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent timeIntervalIntent = new Intent(FIFTEEN);
        timeInterval = PendingIntent.getBroadcast(appContext, 0, timeIntervalIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        sManager = (NotificationManager) appContext.getSystemService(Context.NOTIFICATION_SERVICE);

        sNotification = new NotificationCompat.Builder(appContext)
                .setSmallIcon(R.drawable.ic_launcher_notification)
                .setContentTitle("Tilt")
                .setContentText("Expand for options")
                .setPriority(android.support.v4.app.NotificationCompat.PRIORITY_MIN)
                .setDefaults(Notification.FLAG_SHOW_LIGHTS)
                //.setOngoing(true)
                .addAction(pauseButton, null, pause)
                .addAction(fifteenButton, null, timeInterval)
                .addAction(R.drawable.ic_settings_black_24dp, null, settings)
                .setColor(Color.parseColor("#303030"))
                .build();

        sNotification.ledARGB = 0xff0000ff;
        //sManager.notify(NOTIFICATION_ID, sNotification);
        return sNotification;
    }

    public static void close(int id) {
        sManager.cancel(id);
    }

    public static void update(Context appContext, String option, int interval) {

        sManager = (NotificationManager) appContext.getSystemService(Context.NOTIFICATION_SERVICE);
        String TIME_INTERVAL = FIFTEEN;
        int intervalDrawable = fifteenButton;

        if(interval == 30) {
            TIME_INTERVAL = THIRTY;
            intervalDrawable = thirtyButton;
        }
        else if(interval == 60) {
            TIME_INTERVAL = ONEHOUR;
            intervalDrawable = onehourButton;
        }
        else if(interval == 0){
            TIME_INTERVAL = CANCEL_TIMER;
            intervalDrawable = timerOffButton;
        }

        Intent timeIntervalIntent = new Intent(TIME_INTERVAL);
        timeInterval = PendingIntent.getBroadcast(appContext, 0, timeIntervalIntent, 0);


        Intent settingsIntent = new Intent(SETTINGS);
        settings = PendingIntent.getBroadcast(appContext, 0, settingsIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        int playPauseId;
        String contextText = "Expand for More Options";

        PendingIntent playPauseIntent;
        if(option == "Pause") {
            playPauseId = playButton;
            Intent playIntent = new Intent(appContext, SensorService.class);
            playIntent.setAction("Play");
            play = PendingIntent.getService(appContext, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            playPauseIntent = play;
            contextText = "Paused, press play to resume";
            if(interval == -1) {
                timeInterval = null;
            }
            else if(interval == -2) {
                timeInterval = null;
                playPauseIntent = null;
                contextText = "Tilt disabled in current orientation";
            }
        }
        else {
            playPauseId = pauseButton;
            Intent pauseIntent = new Intent(appContext, SensorService.class);
            pauseIntent.setAction("Pause");
            pause = PendingIntent.getService(appContext, 0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            playPauseIntent = pause;
            contextText = "Running, press pause to pause";
        }

        Notification notification = new NotificationCompat.Builder(appContext)
                .setSmallIcon(R.drawable.ic_launcher_notification)
                .setContentTitle("Tilt")
                .setContentText(contextText)
                .setPriority(Notification.PRIORITY_MIN)
                //.setDefaults(Notification.FLAG_SHOW_LIGHTS)
                //.setOngoing(true)
                .addAction(playPauseId, null, playPauseIntent)
                .addAction(intervalDrawable, null, timeInterval)
                .addAction(R.drawable.ic_settings_black_24dp, null, settings)
                .setColor(Color.parseColor("#303030"))
                .build();


        sManager.notify(NOTIFICATION_ID, notification);
    }
}
