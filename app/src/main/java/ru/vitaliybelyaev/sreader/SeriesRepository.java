package ru.vitaliybelyaev.sreader;

import com.jjoe64.graphview.series.DataPoint;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class SeriesRepository {

    private static final SeriesRepository INSTANCE = new SeriesRepository();
    private Map<String, ArrayList<DataPoint>> storage;
    private RepositoryListener listener;

    public static final String ACCELEROMETER = "accelerometer_series";
    public static final String GYROSCOPE = "gyroscope_series";

    private SeriesRepository(){
        this.storage = new HashMap<>();
        storage.put(ACCELEROMETER, new ArrayList<DataPoint>());
        storage.put(GYROSCOPE, new ArrayList<DataPoint>());
    }

    public interface RepositoryListener{
        void onDataPointAdd(String sensorKey, DataPoint dataPoint);
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

    public ArrayList<DataPoint> getByName(String name){
        return storage.get(name);
    }

    public void saveDataPoint(String name, DataPoint dataPoint){
        storage.get(name).add(dataPoint);
        if(listener != null){
            listener.onDataPointAdd(name, dataPoint);
        }
    }

    public void clear(){
        Collection<ArrayList<DataPoint>> collection = storage.values();
        for(ArrayList<DataPoint> series:collection){
            series.clear();
        }
    }
}
