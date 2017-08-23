package com.lattechiffon.hanium;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class FallingCheckService extends Service implements SensorEventListener {
    public FallingCheckService() {
    }

    private SensorManager sensorManager;
    private Sensor mSensor, lSensor;
    private float[] mGravity;
    private float mAccel;
    private float mAccelCurrent;
    private float mAccelLast;

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Log.d("onSensorChanged", "Last Accel: " + mAccelLast);

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            mGravity = event.values.clone();

            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            float x1 = event.values[0];
            float y1 = event.values[1];
            float z1 = event.values[2];

            mAccelLast = mAccelCurrent;
            mAccelCurrent = (float) Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));

            float delta = mAccelCurrent - mAccelLast;

            mAccel = mAccel * 0.9f + delta;

            if (mAccel >= 3) {
                Log.d("Movement Detected", "mAccel data is over than 3.");
            }
        } else {
            Log.d("End of sensor", "New Accel: " + mAccelCurrent);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // If a Context object is needed, call getApplicationContext() here.
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);       // get an instance of the SensorManager class, lets us access sensors.
        mSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);    // get Accelerometer sensor from the list of sensors.
        lSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);            // get light sensor from the list of sensors.
        sensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, lSensor, SensorManager.SENSOR_DELAY_NORMAL);

        mAccel = 0.00f;
        mAccelCurrent = SensorManager.GRAVITY_EARTH;
        mAccelLast = SensorManager.GRAVITY_EARTH;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);       // get an instance of the SensorManager class, lets us access sensors.
        mSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);    // get Accelerometer sensor from the list of sensors.
        lSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);            // get light sensor from the list of sensors.
        sensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, lSensor, SensorManager.SENSOR_DELAY_NORMAL);

        mAccel = 0.00f;
        mAccelCurrent = SensorManager.GRAVITY_EARTH;
        mAccelLast = SensorManager.GRAVITY_EARTH;

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
