/**
 * This class provides functionality related to the accelerometer sensor.
 * It allows registering and unregistering a listener for translation events,
 * and it notifies the listener when a translation event occurs.
 */

package com.example.gleechat.utilities;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class Accelerometer {

    public interface Listener{
        void onTranslation(float tx, float ty, float tz);
    }

    private Listener listener;

    // Sets the listener for translation events
    public void setListener(Listener l){
        listener = l;
    }
    private SensorManager sensorManager;
    private Sensor sensor;
    private SensorEventListener sensorEventListener;

    public Accelerometer(Context context){
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        sensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                if (listener != null){
                    listener.onTranslation(sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2]);
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };
    }

    // Registers the sensor listener
    public void register(){
        sensorManager.registerListener(sensorEventListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    // Unregisters the sensor listener
    public void unregister(){
        sensorManager.unregisterListener(sensorEventListener);
    }
}
