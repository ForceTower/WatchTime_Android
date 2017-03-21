package com.watchtime.base.utils;

import android.os.Handler;
import android.os.Looper;

/**
 * Created by João Paulo on 16/02/2017.
 */

public class ThreadUtils {
    public static void runOnUiThread(Runnable runnable) {
        Thread uiThread = Looper.getMainLooper().getThread();
        if (Thread.currentThread() != uiThread) new Handler(Looper.getMainLooper()).post(runnable);
        else runnable.run();
    }
}
