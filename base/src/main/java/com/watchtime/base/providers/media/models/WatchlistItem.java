package com.watchtime.base.providers.media.models;

import com.watchtime.base.providers.media.MediaProvider;


public class WatchlistItem extends Media {
    public String runtime;
    public String genres;

    public WatchlistItem(String videoId, String title, String image, MediaProvider provider, String date, int runtime, String genresStr) {
        super(videoId, title, image, null, provider, date, null, null, null);

        this.runtime = "";
        int hours = runtime/60;
        int minutes = runtime%60;

        this.runtime = hours + "h " + minutes + "min";
        this.genres = genresStr;
    }
}
