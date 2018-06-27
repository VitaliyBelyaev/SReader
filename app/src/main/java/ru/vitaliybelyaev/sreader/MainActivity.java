package ru.vitaliybelyaev.sreader;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;

import static ru.vitaliybelyaev.sreader.EntriesRepository.ACCELEROMETER;
import static ru.vitaliybelyaev.sreader.EntriesRepository.GYROSCOPE;
import static ru.vitaliybelyaev.sreader.SensorService.STOP_FOREGROUND;

public class MainActivity extends AppCompatActivity
        implements EntriesRepository.RepositoryListener {

    private static final float PERIOD_IN_MINUTES = 5;

    private LineChart aChart;
    private LineData aLineData;

    private LineChart gChart;
    private LineData gLineData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        aChart = findViewById(R.id.accelerometer_chart);
        aLineData = new LineData();
        aChart.setData(aLineData);
        styleChart(aChart, getString(R.string.aDescription));

        gChart = findViewById(R.id.gyroscope_chart);
        gLineData = new LineData();
        gChart.setData(gLineData);
        styleChart(gChart, getString(R.string.gDescription));

        Intent intent = new Intent(this, SensorService.class);
        startService(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        EntriesRepository.getInstance().registerListener(this);

        refreshChart(ACCELEROMETER, aLineData, aChart);
        refreshChart(GYROSCOPE, gLineData, gChart);
    }

    @Override
    protected void onPause() {
        super.onPause();
        EntriesRepository.getInstance().unregisterListener();
    }

    @Override
    protected void onStop() {
        super.onStop();
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
            finishAndRemoveTask();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDataPointAdd(String sensorKey, Entry entry) {
        if (sensorKey.equals(ACCELEROMETER)) {
            updateChart(aChart, aLineData, entry);
        } else if (sensorKey.equals(GYROSCOPE)) {
            updateChart(gChart, gLineData, entry);
        }
    }

    private void updateChart(LineChart sensorChart, LineData sensorLineData, Entry entry) {
        float periodInSeconds = PERIOD_IN_MINUTES * 60;
        sensorLineData.addEntry(entry, 0);
        sensorChart.setVisibleXRangeMaximum(periodInSeconds);
        if (entry.getX() > periodInSeconds) {
            sensorChart.moveViewToX(entry.getX() - periodInSeconds);
        }
        sensorChart.notifyDataSetChanged();
        sensorChart.invalidate();
    }

    private void refreshChart(String sensorKey,
                              LineData sensorLineData,
                              LineChart sensorChart) {

        sensorLineData.clearValues();
        sensorChart.invalidate();
        ArrayList<Entry> entryList = EntriesRepository.getInstance().getByName(sensorKey);

        String label;
        int color;
        if (sensorKey.equals(ACCELEROMETER)) {
            label = getString(R.string.aChartLabel);
            color = Color.BLUE;
        } else {
            label = getString(R.string.gChartLabel);
            color = Color.RED;
        }

        LineDataSet dataSet = new LineDataSet(entryList, label);

        dataSet.setCircleRadius(1);
        dataSet.setCircleColor(color);
        dataSet.setDrawValues(false);
        dataSet.setColor(color);
        sensorLineData.addDataSet(dataSet);
        sensorChart.notifyDataSetChanged();
        sensorChart.invalidate();
    }

    private void styleChart(LineChart sensorChart, String description) {
        sensorChart.getDescription().setText(description);
        Legend l = sensorChart.getLegend();
        l.setFormSize(10f);
        l.setForm(Legend.LegendForm.CIRCLE);
    }

}
