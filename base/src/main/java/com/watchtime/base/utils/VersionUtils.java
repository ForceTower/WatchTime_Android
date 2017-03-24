package com.watchtime.base.utils;

import android.os.Build;

/**
 * Created by João Paulo on 17/02/2017.
 */

public class VersionUtils {

    public static boolean isLollipop() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    public static boolean isNougat() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N;
    }
}
