package ru.vitaliybelyaev.sreader;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.Iterator;

import static ru.vitaliybelyaev.sreader.SeriesRepository.ACCELEROMETER;

public class MainActivity extends AppCompatActivity
        implements SeriesRepository.RepositoryListener {

    private GraphView graphView;
    private LineGraphSeries<DataPoint> aSeries;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        graphView = findViewById(R.id.graph);
        aSeries = SeriesRepository.getInstance().getByName(ACCELEROMETER);
        graphView.addSeries(aSeries);

        graphView.getViewport().setMaxXAxisSize(300);
        graphView.getViewport().setXAxisBoundsManual(true);
        graphView.getViewport().setYAxisBoundsManual(true);
        graphView.getViewport().setMaxX(300);
        graphView.getViewport().setMaxY(80);

        Intent intent = new Intent(this,SensorService.class);
        startService(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SeriesRepository.getInstance().registerListener(this);

        LineGraphSeries<DataPoint> series = SeriesRepository.getInstance().getByName(ACCELEROMETER);
        Iterator<DataPoint> dataPointIterator = series.getValues(0, 999999999);
        ArrayList<DataPoint> dpList = new ArrayList<>();
        while (dataPointIterator.hasNext()) {
            dpList.add(dataPointIterator.next());
        }

        DataPoint[] dpArray =  new DataPoint[dpList.size()];
        for(int i=0;i<dpList.size();i++){
            dpArray[i] = dpList.get(i);
            Log.i("DATAPOINT","dp: "+dpArray[i]);
        }
        aSeries.resetData(dpArray);
    }

    @Override
    protected void onPause() {
        super.onPause();
        SeriesRepository.getInstance().unregisterListener();
    }


    @Override
    public void onDataPointAdd(DataPoint dataPoint) {
        aSeries.appendData(dataPoint, true, 300);
    }

}
