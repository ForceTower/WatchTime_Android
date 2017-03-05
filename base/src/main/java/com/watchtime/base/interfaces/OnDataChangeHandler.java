package com.watchtime.base.interfaces;

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
    private HashMap<String, MicroListener> listeners = new HashMap<>();

    public interface OnDataChangeListener {
        void onDataChange();
    }

    public void registerListener(String key, OnDataChangeListener listener) {
       registerListener(key, listener, new int[]{ALL});
    }

    public void registerListener(String key, OnDataChangeListener listener, int[] options) {
        if (!listeners.containsKey(key)) {
            listeners.put(key, new MicroListener(listener, options));
        }
    }

    public void unregisterListener(String key) {
        listeners.remove(key);
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
