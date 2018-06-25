package ru.vitaliybelyaev.sreader;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;

import static ru.vitaliybelyaev.sreader.EntriesRepository.ACCELEROMETER;
import static ru.vitaliybelyaev.sreader.EntriesRepository.GYROSCOPE;
import static ru.vitaliybelyaev.sreader.SensorService.STOP_FOREGROUND;

public class MainActivity extends AppCompatActivity
        implements EntriesRepository.RepositoryListener {

    private LineChart aChart;
    private LineData aLineData;

    private LineChart gChart;
    private LineData gLineData;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        aChart= findViewById(R.id.accelerometer_chart);
        LineDataSet aDataSet = new LineDataSet(EntriesRepository
                .getInstance()
                .getByName(ACCELEROMETER), "Accelerometer");
        aLineData = new LineData(aDataSet);
        aChart.setData(aLineData);


        gChart = findViewById(R.id.gyroscope_chart);
        LineDataSet gDataSet = new LineDataSet(EntriesRepository
                .getInstance()
                .getByName(GYROSCOPE), "Gyroscope");
        gLineData = new LineData(gDataSet);
        gChart.setData(gLineData);


        Intent intent = new Intent(this, SensorService.class);
        startService(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        EntriesRepository.getInstance().registerListener(this);

        updateGraph(ACCELEROMETER, aLineData, aChart);
        updateGraph(GYROSCOPE, gLineData, gChart);
    }

    @Override
    protected void onPause() {
        super.onPause();
        EntriesRepository.getInstance().unregisterListener();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.controls, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.stop) {
            Intent intent = new Intent(this, SensorService.class);
            intent.putExtra(Intent.EXTRA_TEXT, STOP_FOREGROUND);
            startService(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDataPointAdd(String sensorKey, Entry entry) {
        if (sensorKey.equals(ACCELEROMETER)) {
            aLineData.addEntry(entry,0);
            aChart.notifyDataSetChanged();
            aChart.invalidate();
        } else if (sensorKey.equals(GYROSCOPE)) {
            gLineData.addEntry(entry,0);
            gChart.notifyDataSetChanged();
            gChart.invalidate();
        }

    }

    private void updateGraph(String sensorKey, LineData sensorLineData, LineChart sensorChart) {
        ArrayList<Entry> entryList = EntriesRepository.getInstance().getByName(sensorKey);

        sensorLineData.removeDataSet(0);
        LineDataSet dataSet = new LineDataSet(entryList,sensorKey);
        sensorLineData.addDataSet(dataSet);
        sensorChart.notifyDataSetChanged();
        sensorChart.invalidate();
    }

}
