package yu.heetae.android.tilt;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by yu on 7/8/15.
 */
public class TiltReceiver extends BroadcastReceiver {
    private static final String TAG = "tilt.android.heetae.yu";
    private static final String SETTINGS = "yu.heetae.android.tilt.SETTINGS";
    private static final String POWER = "yu.heetae.android.tilt.POWER";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.i(TAG, "RECEIVED IN BROADCAST INTENT");
        switch(action) {
            case(SETTINGS):
                break;
            case(POWER):
                Intent i = new Intent(context, SensorService.class);
                context.stopService(i);
                int notificationId = intent.getIntExtra(TiltActivity.KEY_ID, 0);
                TiltNotification.sManager.cancel(notificationId);
        }
    }
}
