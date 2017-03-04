package com.watchtime.base.backend;

public class User {
    private String name;
    private int timeWatched;
    private String cover;
    private String email;
    private int id;

    public User(int id, String name, String email, int timeWatched, String cover) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.timeWatched = timeWatched;
        this.cover = cover;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getCover() {
        return cover;
    }

    public int getTimeWatched() {
        return timeWatched;
    }
}
