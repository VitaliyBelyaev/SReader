package ru.vitaliybelyaev.sreader;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.HashMap;
import java.util.Map;

public class SeriesRepository {

    private static final SeriesRepository INSTANCE = new SeriesRepository();
    private Map<String, LineGraphSeries<DataPoint>> storage;
    private RepositoryListener listener;
    public static final String ACCELEROMETER = "accelerometer_series";

    private SeriesRepository(){
        this.storage = new HashMap<>();
        storage.put(ACCELEROMETER, new LineGraphSeries<DataPoint>());
    }

    public interface RepositoryListener{
        void onDataPointAdd(DataPoint dataPoint);
    }

    public void registerListener(RepositoryListener listener){
        this.listener = listener;
    }

    public void unregisterListener(){
        this.listener = null;
    }

    public static SeriesRepository getInstance(){
        return INSTANCE;
    }

    public LineGraphSeries<DataPoint> getByName(String name){
        return storage.get(name);
    }

    public void saveDataPoint(String name, DataPoint dataPoint){
        storage.get(name).appendData(dataPoint,true,300);
        if(listener != null){
            listener.onDataPointAdd(dataPoint);
        }
    }
}
