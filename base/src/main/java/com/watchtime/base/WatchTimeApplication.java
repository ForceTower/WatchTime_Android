package com.watchtime.base;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.support.multidex.MultiDex;

import com.sjl.foreground.Foreground;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;
import com.watchtime.base.content.preferences.Prefs;
import com.watchtime.base.content.preferences.StorageUtils;
import com.watchtime.base.utils.LocaleUtils;
import com.watchtime.base.utils.PrefUtils;

import java.io.File;

/**
 * Created by João Paulo on 23/01/2017.
 */

public class WatchTimeApplication extends Application {
    private static String systemLanguage;
    private static OkHttpClient httpClient;
    private static Application app; //Find a better option

    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(context);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        systemLanguage = LocaleUtils.getCurrentAsString();

        LeakCanary.install(this);
        Foreground.init(this);

        Picasso.Builder builder = new Picasso.Builder(getAppContext());
        OkHttpDownloader downloader = new OkHttpDownloader(getHttpClient());
        builder.downloader(downloader);
        Picasso.setSingletonInstance(builder.build());
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        systemLanguage = LocaleUtils.getCurrentAsString();
    }

    public static OkHttpClient getHttpClient() {
        if (httpClient == null) {
            int cacheSize = 10 * 1024 * 1024;
            File cacheDir = new File(PrefUtils.get(WatchTimeApplication.getAppContext(), Prefs.STORAGE_LOCATION, StorageUtils.getIdealCacheDirectory(WatchTimeApplication.getAppContext()).toString()));
            cacheDir.mkdirs();

            httpClient = new OkHttpClient();

            com.squareup.okhttp.Cache cache = null;
            try {
                cache = new com.squareup.okhttp.Cache(cacheDir, cacheSize);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            httpClient.setCache(cache);
        }

        return httpClient;
    }

    public static String getSystemLanguage() {
        return systemLanguage;
    }

    public static Context getAppContext() {
        return app;
    }
}
