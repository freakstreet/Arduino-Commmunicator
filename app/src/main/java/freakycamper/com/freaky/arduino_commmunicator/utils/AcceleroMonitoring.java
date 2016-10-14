package freakycamper.com.freaky.arduino_commmunicator.utils;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Created by lsa on 14/10/16.
 */

public class AcceleroMonitoring implements SensorEventListener {

    public static final int SHAKE_THRESHOLD = 10;

    float last_x, last_y, last_z;
    long lastUpdate;

    public interface OnShakeDetected {
        public void WakeDisplay();
    }

    private SensorManager senSensorManager;
    private Sensor senAccelerometer;

    private OnShakeDetected shakeListener;

    public AcceleroMonitoring(Context ctx)
    {
        senSensorManager = (SensorManager) ctx.getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, senAccelerometer , SensorManager.SENSOR_DELAY_NORMAL);

    }

    public void setShakeListener(OnShakeDetected listener)
    {
        shakeListener = listener;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];

            long curTime = System.currentTimeMillis();

            if ((curTime - lastUpdate) > 100) {
                long diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;

                float speed = Math.abs(x + y + z - last_x - last_y - last_z)/ diffTime * 10000;

                if (speed > SHAKE_THRESHOLD) {
                    shakeListener.WakeDisplay();
                }

                last_x = x;
                last_y = y;
                last_z = z;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
