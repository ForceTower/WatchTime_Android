package com.watchtime.base.providers.media.models;

import com.watchtime.base.providers.media.MediaProvider;


public class WatchlistItem extends Media {
    public String runtime;
    public String genres;
    public String tagline;

    public WatchlistItem(String videoId, String title, String image, MediaProvider provider, String date, int runtime, String rating, String genresStr, String tagline) {
        super(videoId, title, image, null, provider, date, rating, null, null);

        this.tagline = tagline;

        this.runtime = "";
        int hours = runtime/60;
        int minutes = runtime%60;

        this.runtime = hours + "h " + minutes + "min";
        this.genres = genresStr;
    }
}
