package com.lattechiffon.hanium;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;

public class FallingCheckService extends Service implements SensorEventListener {
    public FallingCheckService() {
    }

    private SensorManager sensorManager;
    private Sensor accelSensor;
    private float[] mGravity;
    private float accelPivot;
    private float accelCurrentNormal;
    private float accelCurrent;
    private float accelLast;

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            mGravity = event.values.clone();

            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            accelLast = 0.5f * accelLast + 0.5f * accelCurrent;
            accelCurrentNormal = (float) Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));
            accelCurrent = (float) Math.sqrt(Math.pow(x, 2) + 1.75 * Math.pow(y, 2) + 0.25 * Math.pow(z, 2));

            float delta = accelCurrent - accelLast;
            accelPivot = accelPivot * 0.9f + delta;

            if (delta >= 10 && accelCurrent > accelCurrentNormal) {
                Log.d("낙상 인식", "직전 가속도: " + accelLast + " / 측정 가속도 (실제): " + accelCurrent + " (" + accelCurrentNormal + ") / 가속도 피벗: " + accelPivot);
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        sensorManager.registerListener(this, accelSensor, SensorManager.SENSOR_DELAY_NORMAL);

        accelPivot = 0.00f;
        accelCurrentNormal = SensorManager.GRAVITY_EARTH;
        accelCurrent = SensorManager.GRAVITY_EARTH;
        accelLast = SensorManager.GRAVITY_EARTH;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        sensorManager.registerListener(this, accelSensor, SensorManager.SENSOR_DELAY_NORMAL);

        accelPivot = 0.00f;
        accelCurrentNormal = SensorManager.GRAVITY_EARTH;
        accelCurrent = SensorManager.GRAVITY_EARTH;
        accelLast = SensorManager.GRAVITY_EARTH;

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
