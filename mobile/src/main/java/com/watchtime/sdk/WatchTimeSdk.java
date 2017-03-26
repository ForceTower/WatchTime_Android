package com.watchtime.sdk;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.watchtime.sdk.callbacks.InitializeCallback;
import com.watchtime.sdk.validators.Validate;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;

public final class WatchTimeSdk {
    private static final Object LOCK = new Object();
    private static Executor executor;
    private static boolean sdkInitialized = false;
    private static Context applicationContext = null;

    public static void initializeSdk(final Context context, final InitializeCallback callback) {
        if (sdkInitialized) {
            if (callback != null) callback.onInitialize();
            return;
        }

        Validate.notNull(context, "Application Context");

        WatchTimeSdk.applicationContext = context.getApplicationContext();
        sdkInitialized = true;

        FutureTask<Void> futureTask = new FutureTask<Void>(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                WatchTimeAccessTokenManager.getInstance().loadCurrentAccessToken();
                WatchTimeProfileManager.getInstance().loadCurrentProfile();

                if (AccessTokenWT.getCurrentAccessToken() != null && Profile.getCurrentProfile() == null) {
                    Profile.fetchProfileForCurrentAccessToken();
                }

                WatchTimeBaseMethods.instantiate();
                return null;
            }
        });

        getExecutor().execute(futureTask);
    }

    public static Executor getExecutor() {
        synchronized (LOCK) {
            if (WatchTimeSdk.executor == null) {
                WatchTimeSdk.executor = AsyncTask.THREAD_POOL_EXECUTOR;
            }
        }
        return WatchTimeSdk.executor;
    }

    public static Context getApplicationContext() {
        if (isSdkInitialized())
            Log.i("WTSDK", "Not initialized");
        return applicationContext;
    }

    public static boolean isSdkInitialized() {
        return sdkInitialized;
    }
}
