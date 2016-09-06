package yu.heetae.android.tilt;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.app.NotificationCompat;
import java.util.HashMap;
import static yu.heetae.android.tilt.NotificationStatus.*;

/**
 * Created by yu on 7/11/15.
 */
public class TiltNotification {

    //PendingIntent Keys
    private static final String FIFTEEN = "yu.heetae.android.tilt.FIFTEEN";
    private static final String THIRTY = "yu.heetae.android.tilt.THIRTY";
    private static final String ONEHOUR = "yu.heetae.android.tilt.ONEHOUR";
    private static final String CANCEL_TIMER = "yu.heetae.android.tilt.CANCEL_TIMER";


    private static final int NOTIFICATION_ID = 7483;

    public static NotificationManager sManager;
    public static Notification sNotification;

    private static NotificationCompat.Action pauseAction;
    private static NotificationCompat.Action resumeAction;
    private static NotificationCompat.Action cancelTimerAction;
    private static NotificationCompat.Action disabledTimer;
    private static NotificationCompat.Action fifteenMinAction;
    private static NotificationCompat.Action thirtyMinAction;
    private static NotificationCompat.Action oneHourAction;
    private static NotificationCompat.Action settingsAction;

    private static HashMap<String, NotificationCompat.Action> hm = new HashMap<>();

    //R.drawable ids for notification actions
    private static int resumeDrawable = R.drawable.ic_play_arrow_black_24dp;
    private static int pauseDrawable = R.drawable.ic_pause_black_24dp;
    private static int settingsDrawable = R.drawable.ic_settings_black_24dp;
    private static int fifteenDrawable = R.drawable.ic_fifteenmin_black;
    private static int thirtyDrawable = R.drawable.ic_thirtymin_black;
    private static int oneHourDrawable = R.drawable.ic_onehr_black;
    private static int timerOffDrawable = R.drawable.ic_timer_off_black_36dp;

    //Create default notification
    public static Notification createNotification(Context appContext) {

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            resumeDrawable = R.drawable.ic_play_arrow_white_24dp;
            pauseDrawable = R.drawable.ic_pause_white_24dp;
            settingsDrawable = R.drawable.ic_settings_white_24dp;
            fifteenDrawable = R.drawable.ic_fifteenmin_white;
            thirtyDrawable = R.drawable.ic_thirtymin_white;
            oneHourDrawable = R.drawable.ic_onehr_white;
            timerOffDrawable = R.drawable.ic_timer_off_white_36dp;
        }

        initializeActions(appContext);

        //Initialize NotificationManager
        sManager = (NotificationManager) appContext.getSystemService(Context.NOTIFICATION_SERVICE);

        //Build Default Ongoing Notification
        sNotification = new NotificationCompat.Builder(appContext)
                .setSmallIcon(R.drawable.ic_play_arrow_black_24dp)
                .setContentTitle("Tilt")
                .setContentText("Expand for options")
                .setPriority(android.support.v4.app.NotificationCompat.PRIORITY_MIN)
                .setDefaults(Notification.FLAG_SHOW_LIGHTS)
                //.setOngoing(true)
                .addAction(pauseAction)
                .addAction(fifteenMinAction)
                //.addAction(R.drawable.ic_settings_black_24dp, null, settingsAction)
                .addAction(settingsAction)
                .setColor(Color.parseColor("#6441A4"))
                .build();

        sNotification.ledARGB = 0xff0000ff;
        //sManager.notify(NOTIFICATION_ID, sNotification);
        return sNotification;
    }

    //Remove a notification
    public static void close(int id) {
        sManager.cancel(id);
    }

    //Update Ongoing Notification
    public static void update(Context appContext, Status status, Timer interval) {

        //sManager = (NotificationManager) appContext.getSystemService(Context.NOTIFICATION_SERVICE);
        String TIMER_INTERVAL;

        NotificationCompat.Action timerAction;
        NotificationCompat.Action playPauseAction;


        //notification ContextText String
        String contextText = "Expand for More Options";

        //if option = pauseAction, user pressed pauseAction button so change to resumeAction button

        if(status == Status.PAUSE) {
            playPauseAction = resumeAction;
            contextText = "Paused, press resume to resume";
        } else {
            playPauseAction = pauseAction;
            contextText = "Running, press pause to pause";
        }

        switch (interval) {
            case THIRTY:
                contextText = "Paused for 15 minutes, press resume to resume";
                timerAction = thirtyMinAction;
                break;
            case SIXTY:
                contextText = "Paused for 30 minutes, press resume to resume";
                timerAction = oneHourAction;
                break;
            case CANCELLED:
                contextText = "Paused for 1 hour, press resume to resume";
                timerAction = cancelTimerAction;
                break;
            case DISABLED:
                timerAction = disabledTimer;
                break;
            default:
                timerAction = fifteenMinAction;
        }


        Notification notification = new NotificationCompat.Builder(appContext)
                .setSmallIcon(R.drawable.ic_play_arrow_black_24dp)
                .setContentTitle("Tilt")
                .setContentText(contextText)
                .setPriority(Notification.PRIORITY_MIN)
                //.setDefaults(Notification.FLAG_SHOW_LIGHTS)
                //.setOngoing(true)
                .addAction(playPauseAction)
                .addAction(timerAction)
                .addAction(settingsAction)
                .setColor(Color.parseColor("#6441A4"))
                .build();


        sManager.notify(NOTIFICATION_ID, notification);
    }


    private static void initializeActions(Context context) {

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            pauseAction = createAction(Status.PAUSE.name(), context, SensorService.class, pauseDrawable,
                    null, ActionType.SERVICE);

            resumeAction = createAction(Status.RESUME.name(), context, SensorService.class, resumeDrawable,
                    null, ActionType.SERVICE);

            settingsAction = createAction(null, context, TiltActivity.class, settingsDrawable,
                    null, ActionType.ACTIVITY);

            fifteenMinAction = createAction(FIFTEEN, context, null, fifteenDrawable,
                    null, ActionType.BROADCAST);

            thirtyMinAction = createAction(THIRTY, context, null, thirtyDrawable,
                    null, ActionType.BROADCAST);

            oneHourAction = createAction(ONEHOUR, context, null, oneHourDrawable,
                    null, ActionType.BROADCAST);

            cancelTimerAction = createAction(CANCEL_TIMER, context, null, timerOffDrawable,
                    null, ActionType.BROADCAST);

            disabledTimer = createAction(null, null, null, fifteenDrawable,
                    null, ActionType.DEFAULT);
        }

        else {
            pauseAction = createAction(Status.PAUSE.name(), context, SensorService.class, 0,
                    context.getString(R.string.pause_action_label), ActionType.SERVICE);

            resumeAction = createAction(Status.RESUME.name(), context, SensorService.class, 0,
                    context.getString(R.string.resume_action_label), ActionType.SERVICE);

            settingsAction = createAction(null, context, TiltActivity.class, 0,
                    context.getString(R.string.settings_action_label), ActionType.ACTIVITY);

            fifteenMinAction = createAction(FIFTEEN, context, null, 0,
                    context.getString(R.string.snooze_fifteen), ActionType.BROADCAST);

            thirtyMinAction = createAction(THIRTY, context, null, 0,
                    context.getString(R.string.snooze_thirty), ActionType.BROADCAST);

            oneHourAction = createAction(ONEHOUR, context, null, 0,
                    context.getString(R.string.snooze_onehour), ActionType.BROADCAST);

            cancelTimerAction = createAction(CANCEL_TIMER, context, null, 0,
                    context.getString(R.string.cancel_snooze), ActionType.BROADCAST);

            disabledTimer = createAction(null, null, null, 0,
                    context.getString(R.string.snooze_fifteen), ActionType.DEFAULT);
        }
    }


    private static NotificationCompat.Action createAction(String action, Context context, Class c, int icon, String title, ActionType type) {
        Intent i;
        PendingIntent pi;

        switch(type) {
            case SERVICE:
                i = new Intent(context, c);
                i.setAction(action);
                pi = PendingIntent.getService(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
                break;
            case ACTIVITY:
                i = new Intent(context, c);
                pi = PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
                break;
            case BROADCAST:
                i = new Intent(action);
                pi = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
                break;
            default:
                pi = null;
        }

        return new NotificationCompat.Action.Builder(icon,
                title,
                pi)
                .build();
    }

}
