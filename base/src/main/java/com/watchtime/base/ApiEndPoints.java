package com.watchtime.base;

/**
 * Created by João Paulo on 25/01/2017.
 */

public class ApiEndPoints {
    public static String BASE_URL = "http://192.168.15.7";
    public static String OAUTH2_BASE = BASE_URL + "/oauth/access_token";

    public static String BASE_MOVIES_POPULAR = BASE_URL + "/movies/popular/";
    public static String BASE_MOVIES_RELEASE = BASE_URL + "/movies/release?page=";
    public static String BASE_MOVIES_RATING = BASE_URL + "/movies/rating/";
    public static String BASE_MOVIES_NOW_PLAYING = BASE_URL + "/movies/on_theaters/";
    public static String BASE_MOVIES_UPCOMING = BASE_URL + "/movies/upcoming/";

    public static String BASE_MOVIES_DETAILS = BASE_URL + "/movie/";
}
