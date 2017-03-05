package com.watchtime.base.interfaces;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by João Paulo on 04/03/2017.
 */

public class OnDataChangeHandler {
    private HashMap<String, OnDataChangeListener> listeners = new HashMap<>();

    public interface OnDataChangeListener {
        void onDataChange();
    }

    public void registerListener(String key, OnDataChangeListener listener) {
        listeners.put(key, listener);
    }

    public void unregisterListener(String key) {
        listeners.remove(key);
    }

    public void igniteListeners() {
        for (OnDataChangeListener listener : listeners.values())
            listener.onDataChange();
    }

}
