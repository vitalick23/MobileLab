package vitaly.balance.framework;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;


// работа с сенсором
public class AccelerometerHandler implements SensorEventListener {
    public static final int SENSOR_RESOLUTION_POWER = 100;

    private SensorManager sensorManager;
    private Sensor sensor;

    private float[] vector;

    public float[] getVector() {
        return vector;
    }

    public AccelerometerHandler(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);//ускорение, включая гравитацию
        if (sensor != null) {

            vector = new float[]{0, 0};

        } else {
            vector = new float[]{-1, -1};
        }

    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        vector[0] = ((float) Math.round(event.values[0] * SENSOR_RESOLUTION_POWER)) / SENSOR_RESOLUTION_POWER;
        vector[1] = ((float) Math.round(event.values[1] * SENSOR_RESOLUTION_POWER)) / SENSOR_RESOLUTION_POWER;

        vector[0] = (Math.abs(vector[0]) <= 0.5) ? 0 : vector[0];
        vector[1] = (Math.abs(vector[1]) <= 0.5) ? 0 : vector[1];

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void onPause() {
        sensorManager.unregisterListener(this);

    }

    public void onResume() {
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);

    }
}
