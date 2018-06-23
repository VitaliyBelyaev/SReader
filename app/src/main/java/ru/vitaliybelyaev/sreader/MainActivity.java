package ru.vitaliybelyaev.sreader;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;

import static ru.vitaliybelyaev.sreader.SensorService.STOP_FOREGROUND;
import static ru.vitaliybelyaev.sreader.SeriesRepository.ACCELEROMETER;
import static ru.vitaliybelyaev.sreader.SeriesRepository.GYROSCOPE;

public class MainActivity extends AppCompatActivity
        implements SeriesRepository.RepositoryListener {

    private GraphView aGraphView;
    private LineGraphSeries<DataPoint> aSeries;

    private GraphView gGraphView;
    private LineGraphSeries<DataPoint> gSeries;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        aGraphView = findViewById(R.id.accelerometer_graph);
        setupGraphView(aGraphView);
        aSeries = initSeries(ACCELEROMETER);
        aGraphView.addSeries(aSeries);


        gGraphView = findViewById(R.id.gyroscope_graph);
        setupGraphView(gGraphView);
        gSeries = initSeries(GYROSCOPE);
        gGraphView.addSeries(gSeries);


        Intent intent = new Intent(this, SensorService.class);
        startService(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SeriesRepository.getInstance().registerListener(this);

        updateGraph(ACCELEROMETER, aSeries);
        updateGraph(GYROSCOPE, gSeries);
    }

    @Override
    protected void onPause() {
        super.onPause();
        SeriesRepository.getInstance().unregisterListener();
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
    public void onDataPointAdd(String sensorKey, DataPoint dataPoint) {
        if (sensorKey.equals(ACCELEROMETER)) {
            aSeries.appendData(dataPoint, true, 300);
        } else if (sensorKey.equals(GYROSCOPE)) {
            gSeries.appendData(dataPoint, true, 300);
        }

    }

    private void updateGraph(String sensorKey, LineGraphSeries<DataPoint> sensorSeries) {
        ArrayList<DataPoint> dpList = SeriesRepository.getInstance().getByName(sensorKey);

        DataPoint[] dpArray = new DataPoint[dpList.size()];
        for (int i = 0; i < dpList.size(); i++) {
            dpArray[i] = dpList.get(i);
        }
        sensorSeries.resetData(dpArray);
    }

    private LineGraphSeries<DataPoint> initSeries(String sensorKey) {
        ArrayList<DataPoint> dpList = SeriesRepository.getInstance().getByName(sensorKey);
        DataPoint[] dpArray = new DataPoint[dpList.size()];
        for (int i = 0; i < dpList.size(); i++) {
            dpArray[i] = dpList.get(i);
        }
        return new LineGraphSeries<>(dpArray);
    }

    private void setupGraphView(GraphView graphView) {
        graphView.getViewport().setMaxXAxisSize(300);
        graphView.getViewport().setXAxisBoundsManual(true);
        graphView.getViewport().setYAxisBoundsManual(true);
        graphView.getViewport().setMaxX(300);
        graphView.getViewport().setMaxY(80);
    }


}
