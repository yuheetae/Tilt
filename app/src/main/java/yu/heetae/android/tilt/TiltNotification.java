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
    private static int resumeDrawable = R.drawable.ic_play_arrow_black_36dp;
    private static int pauseDrawable = R.drawable.ic_pause_black_36dp;
    private static int settingsDrawable = R.drawable.ic_settings_black_36dp;
    private static int fifteenDrawable = R.drawable.ic_fifteenmin;
    private static int thirtyDrawable = R.drawable.ic_thirtymin;
    private static int oneHourDrawable = R.drawable.ic_onehour_white;
    private static int timerOffDrawable = R.drawable.ic_timer_off_black_36dp;

    //Create default notification
    public static Notification createNotification(Context appContext) {

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            resumeDrawable = R.drawable.ic_play_arrow_white_36dp;
            pauseDrawable = R.drawable.ic_pause_white_36dp;
            settingsDrawable = R.drawable.ic_settings_black_36dp;
            fifteenDrawable = R.drawable.ic_fifteenmin_white;
            thirtyDrawable = R.drawable.ic_thirtymin_white;
            oneHourDrawable = R.drawable.ic_onehour_white;
            timerOffDrawable = R.drawable.ic_timer_off_white_36dp;
        }

        initializeActions(appContext);

        //Initialize NotificationManager
        sManager = (NotificationManager) appContext.getSystemService(Context.NOTIFICATION_SERVICE);

        //Build Default Ongoing Notification
        sNotification = new NotificationCompat.Builder(appContext)
                .setSmallIcon(R.drawable.ic_launcher_notification)
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
    public static void update(Context appContext, String option, int interval) {

        //sManager = (NotificationManager) appContext.getSystemService(Context.NOTIFICATION_SERVICE);
        String TIMER_INTERVAL;

        NotificationCompat.Action timerAction = setTimerAction(appContext, interval);
        NotificationCompat.Action playPauseAction;


        //notification ContextText String
        String contextText = "Expand for More Options";

        //if option = pauseAction, user pressed pauseAction button so change to resumeAction button
        if(option == "Pause") {
            playPauseAction = resumeAction;

            switch(interval) {
                case 30:
                    contextText = "Paused for 15 minutes, press resume to resume";
                    break;
                case 60:
                    contextText = "Paused for 30 minutes, press resume to resume";
                    break;
                case 0:
                    contextText = "Paused for 1 hour, press resume to resume";
                    break;
                default:
                    contextText = "Paused, press resume to resume";
            }
        }
        //else user pressed resumeAction button so change to pauseAction button
        else {
            playPauseAction = pauseAction;
            contextText = "Running, press pause to pause";
        }

        Notification notification = new NotificationCompat.Builder(appContext)
                .setSmallIcon(R.drawable.ic_launcher_notification)
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
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            pauseAction = createAction("Pause", context, SensorService.class, pauseDrawable,
                    context.getString(R.string.pause_action_label), ActionType.SERVICE);

            resumeAction = createAction("Play", context, SensorService.class, resumeDrawable,
                    context.getString(R.string.resume_action_label), ActionType.SERVICE);

            settingsAction = createAction(null, context, TiltActivity.class, settingsDrawable,
                    context.getString(R.string.settings_action_label), ActionType.ACTIVITY);

            fifteenMinAction = createAction(FIFTEEN, context, null, fifteenDrawable,
                    context.getString(R.string.snooze1_action_label), ActionType.BROADCAST);

            thirtyMinAction = createAction(THIRTY, context, null, thirtyDrawable,
                    context.getString(R.string.snooze2_action_label), ActionType.BROADCAST);

            oneHourAction = createAction(ONEHOUR, context, null, oneHourDrawable,
                    context.getString(R.string.snooze3_action_label), ActionType.BROADCAST);

            cancelTimerAction = createAction(CANCEL_TIMER, context, null, timerOffDrawable,
                    context.getString(R.string.snooze4_action_label), ActionType.BROADCAST);

            disabledTimer = createAction(null, null, null, fifteenDrawable,
                    context.getString(R.string.snooze0_action_label), ActionType.DEFAULT);
        } else {
            pauseAction = createAction("Pause", context, SensorService.class, pauseDrawable,
                    null, ActionType.SERVICE);

            resumeAction = createAction("Play", context, SensorService.class, resumeDrawable,
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

    public enum ActionType {
        ACTIVITY,
        SERVICE,
        BROADCAST,
        DEFAULT
    }



    private static NotificationCompat.Action setTimerAction(Context context, int interval) {

        switch(interval) {
            case 30:
                return thirtyMinAction;
            case 60:
                return oneHourAction;
            case 0:
                return cancelTimerAction;
            case -1:
                return disabledTimer;
            default:
                return fifteenMinAction;
        }

    }
}
