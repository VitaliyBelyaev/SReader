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

import com.github.mikephil.charting.data.Entry;

import static java.lang.Math.abs;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import static ru.vitaliybelyaev.sreader.EntriesRepository.ACCELEROMETER;
import static ru.vitaliybelyaev.sreader.EntriesRepository.GYROSCOPE;

public class SensorService extends Service implements SensorEventListener {

    private final float PERIOD_IN_SECONDS= 1;

    //we calculate N using desirable period in seconds and sensor fastest delay value
    private int N = (int) (PERIOD_IN_SECONDS / 0.01);

    private Sensor lAccelerometer;
    private float[] aValues = new float[N];
    private int aCounter = 0;
    private float aSecondsCounter = PERIOD_IN_SECONDS;

    private Sensor gyroscope;
    private float[] gValues = new float[N];
    private int gCounter = 0;
    private float gSecondsCounter = PERIOD_IN_SECONDS;

    private SensorManager sensorManager;
    private Handler workerHandler;
    private Handler uiHandler;
    private static final int SENSOR_NOTIFICATION_ID = 6234;
    private static final String CHANNEL_ID = "sensor_channel";
    public static final String STOP_FOREGROUND = "stop_service";

    static {
        System.loadLibrary("native-lib");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent.hasExtra(Intent.EXTRA_TEXT)) {
            workerHandler.removeCallbacksAndMessages(null);
            Workers.clear();
            EntriesRepository.getInstance().clear();
            stopSelf();
        } else {
            //this is need for API level 26 and higher
            createNotificationChannel();

            Intent notificationIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent =
                    PendingIntent.getActivity(this, 0, notificationIntent, 0);

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_chart_notifcation_icon)
                    .setContentTitle(getString(R.string.notificationTitle))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent);

            Notification notification = mBuilder.build();

            startForeground(SENSOR_NOTIFICATION_ID, notification);
        }
        return START_NOT_STICKY;
    }


    @Override
    public void onCreate() {
        super.onCreate();

        initWorkerThread();
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        lAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        sensorManager.registerListener(this, lAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);

        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        float resultMagnitude = findResultMagnitude(event);

        if (event.sensor == lAccelerometer) {
            if (aCounter < N) {
                aValues[aCounter] = resultMagnitude;
                aCounter++;
            } else {
                workerHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        float average = findAverageInC(aValues);
                        final Entry entry = new Entry(aSecondsCounter, average);

                        uiHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                EntriesRepository.getInstance().saveEntry(ACCELEROMETER, entry);
                            }
                        });
                        aSecondsCounter = aSecondsCounter + PERIOD_IN_SECONDS;
                    }
                });


                aCounter = 0;
                aValues[aCounter] = resultMagnitude;
                aCounter++;
            }
        } else if (event.sensor == gyroscope) {

            if (gCounter < N) {
                gValues[gCounter] = resultMagnitude;
                gCounter++;
            } else {
                workerHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        float average = findAverageInC(gValues);
                        final Entry entry = new Entry(gSecondsCounter, average);

                        uiHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                EntriesRepository.getInstance().saveEntry(GYROSCOPE, entry);
                            }
                        });
                        gSecondsCounter = gSecondsCounter + PERIOD_IN_SECONDS;
                    }
                });

                gCounter = 0;
                gValues[gCounter] = resultMagnitude;
                gCounter++;
            }
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    private void initWorkerThread() {
        workerHandler = Workers.getWorkerThread().newWorkerHandler();
        uiHandler = new Handler(getMainLooper());
    }


    private float findResultMagnitude(SensorEvent event) {
        float xAbs = abs(event.values[0]);
        float yAbs = abs(event.values[1]);
        float zAbs = abs(event.values[2]);
        return (float) sqrt(pow(xAbs, 2) + pow(yAbs, 2) + pow(zAbs, 2));
    }

    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "SensorReader";
            String description = "Channel for foreground notification";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public native float findAverageInC(float[] values);

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
