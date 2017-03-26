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

    public void registerListener(String key, OnDataChangeListener listener, int[] options) {
        listeners.put(key, new MicroListener(listener, options, key));
    }

    public void unregisterListener(String key) {
        MicroListener l = listeners.remove(key);
    }

    public void igniteListeners(String caller, int option) {
        for (MicroListener ml : listeners.values()) {
            if (ml.list.contains(option))
                ml.listener.onDataChange();
        }
    }

    private class MicroListener{
        ArrayList<Integer> list;
        OnDataChangeListener listener;
        String key;

        MicroListener(OnDataChangeListener listener, int[] array, String key) {
            list = new ArrayList<>();
            this.listener = listener;
            this.key = key;

            if (array != null) {
                for (int n : array)
                    list.add(n);
            }
        }
    }

}
