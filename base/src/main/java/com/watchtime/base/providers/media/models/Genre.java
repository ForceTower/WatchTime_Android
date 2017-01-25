package com.watchtime.base.providers.media.models;

/**
 * Created by João Paulo on 24/01/2017.
 */

public class Genre {
    private String mKey;
    private int mLabel;

    public Genre(String key, int label) {
        mKey = key;
        mLabel = label;
    }

    public String getKey() {
        return mKey;
    }

    public int getLabelId() {
        return mLabel;
    }
}
