package com.watchtime.base.providers.media.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.watchtime.base.providers.media.MediaProvider;

/**
 * Created by João Paulo on 16/02/2017.
 */

public class Movie extends Media implements Parcelable{
    public String type = "movie";
    public String runtime = "";

    protected Movie(Parcel in) {
        super(in);
        runtime = in.readString();
        isMovie = true;
    }

    public Movie(String videoId, String title, String image, String fullImage, MediaProvider provider, String runtime, String year, String rating, String reviews, String headerImage) {
        super(videoId, title, image, fullImage, provider, year, rating, reviews, headerImage);
        isMovie = true;
        this.runtime = runtime;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(runtime);
    }

    public static final Creator<Movie> CREATOR = new Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel source) {
            return new Movie(source);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };
}
