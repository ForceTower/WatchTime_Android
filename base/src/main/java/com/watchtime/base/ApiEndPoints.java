package com.watchtime.base;

/**
 * Created by João Paulo on 25/01/2017.
 */

public class ApiEndPoints {
    public static final String CLIENT_ID = "1watch_time1";
    public static final String CLIENT_SECRET = "8suw9ff9122s919slc013";


    public static final String BASE_URL = "https://oqv.000webhostapp.com";
    public static final String OAUTH2_BASE = BASE_URL + "/oauth/access_token";

    public static final String BASE_MOVIES_POPULAR = BASE_URL + "/movies/popular/";
    public static final String BASE_MOVIES_RELEASE = BASE_URL + "/movies/release?page=";
    public static final String BASE_MOVIES_RATING = BASE_URL + "/movies/rating/";
    public static final String BASE_MOVIES_NOW_PLAYING = BASE_URL + "/movies/on_theaters/";
    public static final String BASE_MOVIES_UPCOMING = BASE_URL + "/movies/upcoming/";

    public static final String BASE_MOVIES_DETAILS = BASE_URL + "/movie/";

    public static final String FACEBOOK_LOGIN_REGISTER = BASE_URL + "/api/facebook/login";

    public static final String BASE_CONNECTED = BASE_URL + "/api/watch_time";

    public static final String PROFILE = BASE_CONNECTED + "/user/";
    public static final String PROFILE_ME = BASE_CONNECTED + "/user/me";
    public static final String PROFILE_BASIC = BASE_URL + "/user/";

    public static final String UPDATE_COVER_PICTURE = PROFILE_ME + "/cover";
    public static final String MARK_MOVIE_WATCHED = PROFILE_ME + "/movie_watched";
}
