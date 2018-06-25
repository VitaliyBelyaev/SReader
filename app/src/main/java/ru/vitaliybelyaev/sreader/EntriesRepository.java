package ru.vitaliybelyaev.sreader;

import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class EntriesRepository {

    private static final EntriesRepository INSTANCE = new EntriesRepository();
    private Map<String, ArrayList<Entry>> storage;
    private RepositoryListener listener;

    public static final String ACCELEROMETER = "accelerometer_series";
    public static final String GYROSCOPE = "gyroscope_series";

    private EntriesRepository(){
        this.storage = new HashMap<>();
        storage.put(ACCELEROMETER, new ArrayList<Entry>());
        storage.put(GYROSCOPE, new ArrayList<Entry>());
        storage.get(ACCELEROMETER).add(new Entry(0,0));
        storage.get(GYROSCOPE).add(new Entry(0,0));
    }

    public interface RepositoryListener{
        void onDataPointAdd(String sensorKey, Entry entry);
    }

    public void registerListener(RepositoryListener listener){
        this.listener = listener;
    }

    public void unregisterListener(){
        this.listener = null;
    }

    public static EntriesRepository getInstance(){
        return INSTANCE;
    }

    public ArrayList<Entry> getByName(String name){
        return storage.get(name);
    }

    public void saveEntry(String name, Entry entry){
        storage.get(name).add(entry);
        if(listener != null){
            listener.onDataPointAdd(name, entry);
        }
    }

    public void clear(){
        Collection<ArrayList<Entry>> collection = storage.values();
        for(ArrayList<Entry> entries:collection){
            entries.clear();
            entries.add(new Entry(0,0));
        }
    }
}
