package com.watchtime.sdk.validators;

/**
 * Created by João Paulo on 04/03/2017.
 */

public class Validate {
    public static void notNull(Object arg, String name) {
        if (arg == null) {
            throw new NullPointerException("Argument '" + name + "' cannot be null");
        }
    }
}
