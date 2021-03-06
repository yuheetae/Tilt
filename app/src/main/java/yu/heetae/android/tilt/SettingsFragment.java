package yu.heetae.android.tilt;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import static yu.heetae.android.tilt.NotificationStatus.SensorState;

/**
 * Created by yu on 8/31/16.
 */
public class SettingsFragment extends Fragment implements SeekBar.OnSeekBarChangeListener {
    private static final String TAG = "tilt.android.heetae.yu";

    //Shared Preference Variable Keys for Settings
    public static final String PREF_TILT_ANGLE = "tilt_angle";
    public static final String PREF_HEADSUP_NOTIFICATION = "headsup_notification";
    public static final String PREF_VIBRATE = "notification_vibrate";
    public static final String PREF_PORTRAIT_ORIENTATION = "portrait_orientation";
    public static final String PREF_LANDSCAPE_ORIENTATION = "landscape_orientation";

    public static final String KEY_ID = "yu.heetae.android.tilt.NOTIFICATION_ID_KEY";
    public static final String MENU_ITEM_BUTTON = "menu_item_resume_pause_button";

    private TextView mAngleTextView;
    private LinearLayout mLinearLayout;
    private SeekBar mSeekBar;
    private Switch mHeadsupSwitch;
    private Switch mVibrateSwitch;
    private Switch mPortraitSwitch;
    private MenuItem mStateChange;

    private Intent mPauseIntent;
    private Intent mResumeIntent;

    private static boolean sIsVisible = false;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        Log.i(TAG, "onCreate()\tSettingsFragment");

        mPauseIntent = new Intent(getActivity(), SensorService.class);
        mPauseIntent.setAction(NotificationStatus.Status.PAUSE.name());

        mResumeIntent = new Intent(getActivity(), SensorService.class);
        mResumeIntent.setAction(NotificationStatus.Status.RESUME.name());

        //Start SensorService to detect phone tilt
        if(SensorService.sensorState != SensorState.RUNNING) {
            Intent i = new Intent(getActivity().getApplicationContext(), SensorService.class);
            getActivity().startService(i);
            //Log.i(TAG, "Service Started From TiltActivity");
        }

        //SharedPreferenceChange Listener
        SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                //Log.i(TAG, key + "Preference Changed");
                switch(key) {
                    //If tilt mAngleTextView is changed
                    case(PREF_TILT_ANGLE):
                        SensorService.preferredTiltAngle = SettingsPreferences.getTiltAngle(getActivity());
                        int orientation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
                        if(orientation == Surface.ROTATION_0 || orientation == Surface.ROTATION_90) {
                            SensorService.preferredTiltAngle = -SensorService.preferredTiltAngle;
                        }
                        break;
                    case(PREF_HEADSUP_NOTIFICATION):
                        SensorService.isHeadsUpEnabled = SettingsPreferences.getHeadsupSwitch(getActivity());
                        break;
                    case(PREF_VIBRATE):
                        SensorService.isVibrateEnabled = SettingsPreferences.getVibrateSwitch(getActivity());
                        break;
                    case(PREF_PORTRAIT_ORIENTATION):
                        SensorService.isPortraitEnabled = SettingsPreferences.getPortraitSwitch(getActivity());
                        break;
                    /*
                    case(PREF_LANDSCAPE_ORIENTATION):
                        SensorService.isLandscapeEnabled = PreferenceManager.getDefaultSharedPreferences(TiltActivity.this).getBoolean(PREF_LANDSCAPE_ORIENTATION, true);
                        break;
                        */
                }
            }
        };

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        preferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saveInstanceState) {
        View v = inflater.inflate(R.layout.settings_fragment, container, false);

        Log.i(TAG, "onCreateView()\tSettingsFragment");

        //Initialize tilt mAngleTextView seekbar
        mSeekBar = (SeekBar)v.findViewById(R.id.seekbar);
        mSeekBar.setMax(90);
        mSeekBar.setProgress(SettingsPreferences.getTiltAngle(getActivity()));
        mSeekBar.setOnSeekBarChangeListener(this);

        mLinearLayout = (LinearLayout)v.findViewById(R.id.manual);
        mLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment manualFragment = new ManualFragment();
                FragmentManager fm = getFragmentManager();
                fm.beginTransaction()
                        .replace(R.id.fragment_container, manualFragment)
                        .addToBackStack(null)
                        .commit();



            }
        });

        //Initialize textview for tilt mAngleTextView
        mAngleTextView = (TextView)v.findViewById(R.id.tilt_angle);
        mAngleTextView.setText(SettingsPreferences.getTiltAngle(getActivity()) + "\u00B0");

        //Initiate Switch Buttons
        mHeadsupSwitch = (Switch)v.findViewById(R.id.switch_headsup);
        mVibrateSwitch = (Switch)v.findViewById(R.id.switch_vibrate);
        mPortraitSwitch = (Switch)v.findViewById(R.id.switch_portrait_orientation);
        //Switch landscape = (Switch) findViewById(R.id.landscape_orientation_switch);

        //Headsup Switch Listener
        mHeadsupSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //If user wants mHeadsupSwitch notification set preference variable true
                if (isChecked) {
                    SettingsPreferences.setHeadsupSwitch(getActivity(), true);
                }
                //If user does not want mHeadsupSwitch notification set preference variable false
                else {
                    SettingsPreferences.setHeadsupSwitch(getActivity(), false);
                }
            }
        });

        //Vibrate Switch Listener
        mVibrateSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //If user wants vibration with notification set preference variable true
                if(isChecked) {
                    SettingsPreferences.setVibrateSwitch(getActivity(), true);
                }
                //If user does not want vibration with notification set preference variable false
                else {
                    SettingsPreferences.setVibrateSwitch(getActivity(), false);
                }
            }
        });

        //Portrait Switch Listener
        mPortraitSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //If user wants the app enabled when phone is in mPortraitSwitch mode
                if (isChecked) {
                    SettingsPreferences.setPortraitSwitch(getActivity(), true);
                }
                //If user does not want the app enabled when phone is in mPortraitSwitch mode
                else {
                    SettingsPreferences.setPortraitSwitch(getActivity(), false);
                }
            }
        });

        //Landscape Switch Listener
        /*
        landscape.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //If user wants the app enabled when phone is in landscape mode
                if (isChecked) {
                    PreferenceManager.getDefaultSharedPreferences(TiltActivity.this)
                            .edit()
                            .putBoolean(PREF_PORTRAIT_ORIENTATION, true)
                            .commit();
                }
                //If user does not want the app enabled when phone is in landscape mode
                else {
                    PreferenceManager.getDefaultSharedPreferences(TiltActivity.this)
                            .edit()
                            .putBoolean(PREF_PORTRAIT_ORIENTATION, false)
                            .commit();
                }
            }
        });
        */

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.i(TAG, "onStart()\tSettingsFragment");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume()\tSettingsFragment");
        mSeekBar.setProgress(SettingsPreferences.getTiltAngle(getActivity()));
        mAngleTextView.setText(SettingsPreferences.getTiltAngle(getActivity()) + "\u00B0");
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(messageReceiver, new IntentFilter(MENU_ITEM_BUTTON));
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(messageReceiver);
        Log.i(TAG, "onPause()\tSettingsFragment");
    }
    @Override
    public void onStop() {
        super.onStop();
        Log.i(TAG, "onStop()\tSettingsFragment");
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_main, menu);
        Log.i(TAG, "onCreateOptionsMenu()\tSettingsFragment");

    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        mStateChange = menu.findItem(R.id.menu_item_change_state);
        Log.i(TAG, "onPrepareOptionsMenu()\tSettingsFragment");

        updateMenuItem();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_item_change_state:
                if(SensorService.sensorState == SensorState.RUNNING) {
                    getActivity().startService(mPauseIntent);
                    mStateChange.setIcon(R.drawable.ic_play_arrow_white_24dp);
                } else {
                    getActivity().startService(mResumeIntent);
                    mStateChange.setIcon(R.drawable.ic_pause_white_24dp);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //Change text displaying tilt mAngleTextView when seekbar progress is changed
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        mAngleTextView.setText(progress + "\u00B0");
    }

    //When user releases seekbar set preference variable
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        SettingsPreferences.setTiltAngle(getActivity(), mSeekBar.getProgress());
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    public void updateMenuItem() {
        if(SensorService.sensorState == SensorState.RUNNING) {
            mStateChange.setIcon(R.drawable.ic_pause_white_24dp);
        } else {
            mStateChange.setIcon(R.drawable.ic_play_arrow_white_24dp);
        }
    }

    private BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case MENU_ITEM_BUTTON:
                    updateMenuItem();
                    break;
                default:
                    break;
            }
        }
    };
}
