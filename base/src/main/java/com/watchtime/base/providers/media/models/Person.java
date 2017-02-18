package com.watchtime.base.providers.media.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by João Paulo on 17/02/2017.
 */

public class Person implements Parcelable {
    public String name = "";
    public String profileImage = "";
    public String role = "";
    public String id = "";

    public Person(String id, String name, String profileImage, String role) {
        this.id = id;
        this.name = name;
        this.profileImage = profileImage;
        this.role = role;
    }

    public Person(Parcel in) {
        name = in.readString();
        profileImage = in.readString();
        id = in.readString();
        role = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(profileImage);
        dest.writeString(id);
        dest.writeString(role);
    }

    public static final Creator<Person> CREATOR = new Creator<Person>() {
        @Override
        public Person createFromParcel(Parcel source) {
            return new Person(source);
        }

        @Override
        public Person[] newArray(int size) {
            return new Person[size];
        }
    };
}
