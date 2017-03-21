package com.watchtime.base.interfaces;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by João Paulo on 04/03/2017.
 */

public class OnDataChangeHandler {
    public static final int ALL = 0;
    public static final int LOGIN = 1;
    public static final int LOGOUT = 2;
    public static final int USER_LIST_UPDATE = 3;
    private HashMap<String, MicroListener> listeners = new HashMap<>();

    public interface OnDataChangeListener {
        void onDataChange();
    }

    public void registerListener(String key, OnDataChangeListener listener) {
       registerListener(key, listener, new int[]{ALL});
    }

    public void registerListener(String key, OnDataChangeListener listener, int[] options) {
        Log.i("DataChangeListener", "Attempt to add: " + key);
        //if (!listeners.containsKey(key)) {
            listeners.put(key, new MicroListener(listener, options));
            Log.i("DataChangeListener", key + " added");
        //} else {
            //Log.i("DataChangeListener", key + " not added");
        //}
    }

    public void unregisterListener(String key) {
        MicroListener l = listeners.remove(key);
        Log.i("DataChangeListener", "Unregister " + key + ". Result: " + Boolean.toString(l != null));
    }

    public void igniteListeners(int option) {
        for (MicroListener ml : listeners.values()) {
            if (ml.list.contains(option))
                ml.listener.onDataChange();
        }
    }

    private class MicroListener{
        ArrayList<Integer> list;
        OnDataChangeListener listener;

        MicroListener(OnDataChangeListener listener, int[] array) {
            list = new ArrayList<>();
            this.listener = listener;

            if (array != null) {
                for (int n : array)
                    list.add(n);
            }
        }
    }

}
