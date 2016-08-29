package yu.heetae.android.tilt;

/**
 * Created by yu on 8/29/16.
 */
public class NotificationStatus {

    public enum Timer {
        FIFTEEN,
        THIRTY,
        SIXTY,
        CANCELLED,   //0
        DISABLED     //default
    }

    public enum Status {
        RESUME,
        PAUSE
    }

    public enum SensorState {
        RUNNING,
        PAUSED,
        TIMED
    }

    public enum ActionType {
        ACTIVITY,
        SERVICE,
        BROADCAST,
        DEFAULT
    }
}
