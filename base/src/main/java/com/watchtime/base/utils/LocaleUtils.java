package com.watchtime.base.utils;

import android.content.Context;
import android.content.res.Configuration;

import java.util.Locale;

/**
 * Created by João Paulo on 23/01/2017.
 */

public class LocaleUtils {

    public static String getCurrentAsString() {
        return getLanguageCode(getCurrent());
    }

    public static String getLanguageCode(Locale locale) {
        String languageCode = locale.getLanguage();
        if (!locale.getCountry().isEmpty()) {
            languageCode += "-" + locale.getCountry();
        }
        return languageCode;
    }

    public static Locale getCurrent() {
        return Locale.getDefault();
    }

    public static void setCurrent(Context context, Locale locale) {
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);

        //Only on API 25 it's not deprecated, use createConfigurationContext;
        context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
    }

    public static Locale toLocale(String langCode) {
        if (langCode == null) {
            System.out.println("Keepoooooooo");
            langCode = getCurrentAsString();
        }
        String[] language = langCode.split("-");
        if (language.length > 1) {
            return new Locale(language[0], language[1]);
        }
        return new Locale(language[0]);
    }
}

