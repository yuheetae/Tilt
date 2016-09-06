package yu.heetae.android.tilt;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import static yu.heetae.android.tilt.NotificationStatus.Status;
import static yu.heetae.android.tilt.NotificationStatus.SensorState;


/**
 * Created by yu on 9/2/16.
 */
public class ManualFragment extends Fragment implements SensorEventListener{
    private static final String TAG = "tilt.android.heetae.yu";

    private TextView mDegreeNumber;
    private Button mConfirmationButton;
    private Button mCancelButton;

    //Variables to hold Sensor Data
    private float[] mAccelValues;
    private float[] mMagnet;
    private float[] mGravity;
    private float[] mOrientation;
    private float[] mRotationMatrix;

    private SensorManager mSensorManager;

    //Holds previous state of SensorService sensors
    private SensorState mPreviousState;

    private Intent mPauseIntent;
    private Intent mResumeIntent;

    private int mDegreeAngle;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ((AppCompatActivity)getActivity()).getSupportActionBar().hide();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSensorManager = (SensorManager)getActivity().getSystemService(Context.SENSOR_SERVICE);

        //Initialize arrays to contain sensor data
        mAccelValues = new float[3];
        mMagnet = new float[3];
        mGravity = new float[3];
        mOrientation = new float[3];
        mRotationMatrix = new float[9];

        mPauseIntent = new Intent(getActivity(), SensorService.class);
        mPauseIntent.setAction(Status.PAUSE.name());

        mResumeIntent = new Intent(getActivity(), SensorService.class);
        mResumeIntent.setAction(Status.RESUME.name());

        mPreviousState = SensorService.sensorState;

        if(mPreviousState == SensorState.RUNNING) {
            getActivity().startService(mPauseIntent);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.manual_fragment, container, false);

        int preferenceAngle = SettingsPreferences.getTiltAngle(getActivity());

        mDegreeNumber = (TextView)v.findViewById(R.id.text_degree_number);
        mDegreeNumber.setText(Integer.toString(preferenceAngle));

        mConfirmationButton = (Button)v.findViewById(R.id.button_confirmation);
        mConfirmationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String buttonText = mConfirmationButton.getText().toString();

                if(buttonText.equals(getString(R.string.start_button))) {
                    registerListeners();
                    mConfirmationButton.setText(R.string.set_button);
                    mCancelButton.setEnabled(true);
                } else if(buttonText.equals(getString(R.string.set_button))) {
                    unregisterSensor();
                    mConfirmationButton.setText(R.string.start_button);
                    mCancelButton.setEnabled(false);
                    SettingsPreferences.setTiltAngle(getActivity(), mDegreeAngle);
                }
            }
        });

        mCancelButton = (Button)v.findViewById(R.id.button_cancel);
        mCancelButton.setEnabled(false);
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                unregisterSensor();
                mConfirmationButton.setText(R.string.start_button);
                mCancelButton.setEnabled(false);
            }
        });

        return v;
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterSensor();

        if(mPreviousState == SensorState.RUNNING) {
            getActivity().startService(mResumeIntent);
        }

        ((AppCompatActivity)getActivity()).getSupportActionBar().show();
    }

    @Override
    //Put sensor data into arrays
    public void onSensorChanged(SensorEvent event) {
        switch(event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                System.arraycopy(event.values, 0, mAccelValues, 0, 3);
                calculateAngle();
                mDegreeNumber.setText(Integer.toString(mDegreeAngle));
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                System.arraycopy(event.values, 0, mMagnet, 0, 3);
                break;
            case Sensor.TYPE_GRAVITY:
                System.arraycopy(event.values, 0, mGravity, 0, 3);
                break;
            default:
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    //Register multiple listeners to a SensorManager
    public void registerListeners() {
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                1000000, 1000000);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                1000000, 1000000);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY), SensorManager.SENSOR_DELAY_NORMAL);
    }

    //Unregister SensorManager
    public void unregisterSensor() {
        mSensorManager.unregisterListener(this);
    }

    private void calculateAngle() {
        //Retrieve Rotation Matrix and Orientation
        SensorManager.getRotationMatrix(mRotationMatrix, null, mAccelValues, mMagnet);
        SensorManager.getOrientation(mRotationMatrix, mOrientation);

        //Adjust Pitch(rotation about x-axis) based on force of gravity on y-axis
        if (mGravity[2] < 0) {
            if (mOrientation[1] > 0) {
                mOrientation[1] = (float) (Math.PI - mOrientation[1]);
            } else {
                mOrientation[1] = (float) (-Math.PI - mOrientation[1]);
            }
        }

        Double angle = Math.toDegrees(mOrientation[1]);

        if(angle > 0) {
            mDegreeAngle = 0;
        } else if(angle < -90) {
            mDegreeAngle = 90;
        } else {
            angle = -angle;
            mDegreeAngle = angle.intValue();
        }

    }
}
