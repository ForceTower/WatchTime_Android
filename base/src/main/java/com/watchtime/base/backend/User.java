package com.watchtime.base.backend;

import com.watchtime.base.utils.PrefUtils;

public class User {
    private String name;
    private int timeWatched;
    private String cover;
    private String email;
    private int id;
    private String accountType;
    private String accountName;
    private String authTokenType;
    private String token;

    public User() {
    }

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

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public void setAuthTokenType(String authTokenType) {
        this.authTokenType = authTokenType;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getAccountName() {
        return accountName;
    }

    public String getAccountType() {
        return accountType;
    }

    public String getAuthTokenType() {
        return authTokenType;
    }

    public String getToken() {
        return token;
    }
}
