package ru.vitaliybelyaev.sreader;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.jjoe64.graphview.series.DataPoint;

import static java.lang.Math.abs;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import static ru.vitaliybelyaev.sreader.SeriesRepository.ACCELEROMETER;

public class SensorService extends Service implements SensorEventListener {

    private Handler workerHandler;
    private SensorManager sensorManager;
    private Sensor lAccelerometer;
    private float[] values = new float[100];
    private int counter = 0;
    private int secondsCounter = 1;
    private static final int SENSOR_NOTIFICATION_ID = 6234;
    private static final String CHANNEL_ID = "sensor_channel";

    static {
        System.loadLibrary("native-lib");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        createNotificationChannel();

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, 0);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Getting data from sensor")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent);

        Notification notification = mBuilder.build();

        startForeground(SENSOR_NOTIFICATION_ID, notification);

        return START_STICKY;
    }


    @Override
    public void onCreate() {
        super.onCreate();


        initWorkerThread();
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        lAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        sensorManager.registerListener(this, lAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        float resultLength = findResultLength(event);
        Log.i("SERVICE","resultLength: "+resultLength);
        if (counter < 100) {
            values[counter] = resultLength;
            counter++;
        } else {
            float average = findAverage(values);
            DataPoint dataPoint = new DataPoint(secondsCounter, average);
            SeriesRepository.getInstance().saveDataPoint(ACCELEROMETER, dataPoint);
            secondsCounter++;

            counter = 0;
            values[counter] = resultLength;
            counter++;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    private void initWorkerThread() {
        workerHandler = Workers.getWorkerThread().newWorkerHandler();
    }

    private float findAverage(float[] values) {
        float min = values[0];
        float max = values[0];
        float sum = 0;
        int m = 0;

        for (int i = 1; i < values.length; i++) {
            if (values[i] < min) min = values[i];
            else if (values[i] > max) max = values[i];
        }

        for (float v : values) {
            if (v != min && v != max) sum = sum + v;
            else m = m + 1;
        }

        //number of element in array without min and max
        int k = values.length - m;
        return sum / k;
    }

    private float findResultLength(SensorEvent event) {
        float xAbs = abs(event.values[0]);
        float yAbs = abs(event.values[1]);
        float zAbs = abs(event.values[2]);
        return (float) sqrt(pow(xAbs, 2) + pow(yAbs, 2) + pow(zAbs, 2));
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "SensorReader";
            String description = "Channel for foreground notification";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


    public native String stringFromJNI();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
