package com.watchtime.base.providers.media.models;

import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.ArrayAdapter;

import com.watchtime.base.providers.media.MediaProvider;

import java.util.ArrayList;

/**
 * Created by João Paulo on 24/01/2017.
 */

public class Media implements Parcelable{
    public String videoId;
    public String imdbId;
    public String title;
    public String year;
    public String genre;
    public String rating;
    public String reviews;
    public boolean isMovie = false;
    public String image;
    public String fullImage;
    public String headerImage;
    public int color;
    protected MediaProvider provider;
    public ArrayList<String> backdrops;

    public Media(String videoId, String title, String image, String fullImage, MediaProvider provider, String year, String rating, String reviews, String headerImage) {
        this.videoId = videoId;
        this.title = title;
        this.image = image;
        this.fullImage = fullImage;
        this.headerImage = headerImage;
        this.provider = provider;
        this.year = year;
        this.rating = rating;
        this.reviews = reviews;
    }

    public Media(Parcel in) {
        videoId = in.readString();
        imdbId = in.readString();
        title = in.readString();
        year = in.readString();
        genre = in.readString();
        rating = in.readString();
        isMovie = in.readInt() == 1;
        image = in.readString();
        fullImage = in.readString();
        headerImage = in.readString();

        String className = in.readString();
        provider = null;
        try {
            Class<?> clazz = Class.forName(className);
            provider = (MediaProvider) clazz.newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(videoId);
        dest.writeString(imdbId);
        dest.writeString(title);
        dest.writeString(year);
        dest.writeString(genre);
        dest.writeString(rating);
        dest.writeInt(isMovie ? 1 : 2);
        dest.writeString(image);
        dest.writeString(fullImage);
        dest.writeString(headerImage);

        dest.writeString(provider != null ? provider.getClass().getCanonicalName() : "");
    }

    public static final Creator<Media> CREATOR = new Creator<Media>() {
        @Override
        public Media createFromParcel(Parcel source) {
            return new Media(source);
        }

        @Override
        public Media[] newArray(int size) {
            return new Media[size];
        }
    };

    public MediaProvider getMediaProvider() {
        return provider;
    }
}
