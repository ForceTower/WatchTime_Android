package com.watchtime.base.utils;

import android.content.Context;

import com.watchtime.base.Constants;
import com.watchtime.base.WatchTimeApplication;
import com.watchtime.base.backend.token.TokenAPI;
import com.watchtime.base.content.ObscuredSharedPreferences;

/**
 * Created by João Paulo on 23/01/2017.
 */

public class PrefUtils {
    public static void save(Context context, String key, boolean value) {
        getPrefs(context).edit().putBoolean(key, value).apply();
    }

    public static void save(Context context, String key, String value) {
        getPrefs(context).edit().putString(key, value).apply();
    }

    public static void save(Context context, String key, int value) {
        getPrefs(context).edit().putInt(key, value).apply();
    }

    public static String get(Context context, String key, String defaultValue) {
        return getPrefs(context).getString(key, defaultValue);
    }
    public static Boolean get(Context context, String key, boolean defaultValue) {
        return getPrefs(context).getBoolean(key, defaultValue);
    }
    public static int get(Context context, String key, int defaultValue) {
        return getPrefs(context).getInt(key, defaultValue);
    }

    public static ObscuredSharedPreferences getPrefs(Context context) {
        return new ObscuredSharedPreferences(context, context.getSharedPreferences(Constants.PREFS_FILE, Context.MODE_PRIVATE));
    }

    public static Boolean contains(Context context, String key) {
        return getPrefs(context).contains(key);
    }
}
