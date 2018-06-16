package ru.vitaliybelyaev.sreader;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;


    static {
        System.loadLibrary("native-lib");
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        ArrayList<Sensor> sensors = new ArrayList<>();

        sensors.add(sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
        sensors.add(sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE));
        for (Sensor sensor : sensors) {

            Log.i("SENSORS", "sensor: " + sensor);
        }

        TextView tv = (TextView) findViewById(R.id.sample_text);
    }


    @Override
    public void onSensorChanged(SensorEvent event) {

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public native String stringFromJNI();
}
