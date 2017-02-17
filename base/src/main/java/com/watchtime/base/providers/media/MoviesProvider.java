package com.watchtime.base.providers.media;

import android.util.Log;

import com.google.gson.internal.LinkedTreeMap;
import com.watchtime.base.ApiEndPoints;
import com.watchtime.base.R;
import com.watchtime.base.WatchTimeApplication;
import com.watchtime.base.providers.media.models.Genre;
import com.watchtime.base.providers.media.models.Media;
import com.watchtime.base.providers.media.models.Movie;

import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by João Paulo on 16/02/2017.
 */

public class MoviesProvider extends MediaProvider{
    private static final MoviesProvider mediaProvider = new MoviesProvider();
    private static Integer CURRENT_API = 0;
    private static final String[] API_URLS = ApiEndPoints.BASE_MOVIES_URLS;
    private static Filters filters = new Filters();

    @Override
    protected OkHttpClient getClient() {
        return super.getClient(); //TODO: Set up new client here
    }

    @Override
    protected Call enqueue(Request request, okhttp3.Callback requestCallback) {
        return super.enqueue(request, requestCallback);
    }

    @Override
    public Call getList(ArrayList<Media> currentList, Filters filter, Callback callback) {
        filters = filter;

        final ArrayList<Media> list;
        if (currentList == null) {
            list = new ArrayList<>();
        } else {
            //noinspection unchecked
            list = (ArrayList<Media>) currentList.clone();
        }

        ArrayList<NameValuePair> params = new ArrayList<>();
        params.add(new NameValuePair("lang", filter.langCode));
        params.add(new NameValuePair("sort_by", "rating"));
        //params.add(new NameValuePair("page", Integer.toString(filter.page)));

        Request.Builder requestBuilder = new Request.Builder();
        String query = buildQuery(params);
        requestBuilder.url(API_URLS[CURRENT_API] + query);
        requestBuilder.tag(MEDIA_CALL);


        list.add(new Movie("1", "The Only One Movie Possible", "https://image.tmdb.org/t/p/w640/hLudzvGfpi6JlwUnsNhXwKKg4j.jpg", "https://image.tmdb.org/t/p/w640/hLudzvGfpi6JlwUnsNhXwKKg4j.jpg", this, "125", "2017", "10.0", "76", "https://image.tmdb.org/t/p/w533_and_h300_bestv2/x3akjRHhZnIZx0EiQ3eOg66qoS9.jpg"));
        list.add(new Movie("1", "The Only One Movie Possible", "https://image.tmdb.org/t/p/w640/hLudzvGfpi6JlwUnsNhXwKKg4j.jpg", "https://image.tmdb.org/t/p/w640/hLudzvGfpi6JlwUnsNhXwKKg4j.jpg", this, "125", "2017", "10.0", "76", "https://image.tmdb.org/t/p/w533_and_h300_bestv2/x3akjRHhZnIZx0EiQ3eOg66qoS9.jpg"));
        list.add(new Movie("1", "The Only One Movie Possible", "https://image.tmdb.org/t/p/w640/hLudzvGfpi6JlwUnsNhXwKKg4j.jpg", "https://image.tmdb.org/t/p/w640/hLudzvGfpi6JlwUnsNhXwKKg4j.jpg", this, "125", "2017", "10.0", "76", "https://image.tmdb.org/t/p/w533_and_h300_bestv2/x3akjRHhZnIZx0EiQ3eOg66qoS9.jpg"));
        list.add(new Movie("1", "The Only One Movie Possible", "https://image.tmdb.org/t/p/w640/hLudzvGfpi6JlwUnsNhXwKKg4j.jpg", "https://image.tmdb.org/t/p/w640/hLudzvGfpi6JlwUnsNhXwKKg4j.jpg", this, "125", "2017", "10.0", "76", "https://image.tmdb.org/t/p/w533_and_h300_bestv2/x3akjRHhZnIZx0EiQ3eOg66qoS9.jpg"));
        list.add(new Movie("1", "The Only One Movie Possible", "https://image.tmdb.org/t/p/w640/hLudzvGfpi6JlwUnsNhXwKKg4j.jpg", "https://image.tmdb.org/t/p/w640/hLudzvGfpi6JlwUnsNhXwKKg4j.jpg", this, "125", "2017", "10.0", "76", "https://image.tmdb.org/t/p/w533_and_h300_bestv2/x3akjRHhZnIZx0EiQ3eOg66qoS9.jpg"));
        list.add(new Movie("1", "The Only One Movie Possible", "https://image.tmdb.org/t/p/w640/hLudzvGfpi6JlwUnsNhXwKKg4j.jpg", "https://image.tmdb.org/t/p/w640/hLudzvGfpi6JlwUnsNhXwKKg4j.jpg", this, "125", "2017", "10.0", "76", "https://image.tmdb.org/t/p/w533_and_h300_bestv2/x3akjRHhZnIZx0EiQ3eOg66qoS9.jpg"));
        list.add(new Movie("1", "The Only One Movie Possible", "https://image.tmdb.org/t/p/w640/hLudzvGfpi6JlwUnsNhXwKKg4j.jpg", "https://image.tmdb.org/t/p/w640/hLudzvGfpi6JlwUnsNhXwKKg4j.jpg", this, "125", "2017", "10.0", "76", "https://image.tmdb.org/t/p/w533_and_h300_bestv2/x3akjRHhZnIZx0EiQ3eOg66qoS9.jpg"));
        list.add(new Movie("1", "The Only One Movie Possible", "https://image.tmdb.org/t/p/w640/hLudzvGfpi6JlwUnsNhXwKKg4j.jpg", "https://image.tmdb.org/t/p/w640/hLudzvGfpi6JlwUnsNhXwKKg4j.jpg", this, "125", "2017", "10.0", "76", "https://image.tmdb.org/t/p/w533_and_h300_bestv2/x3akjRHhZnIZx0EiQ3eOg66qoS9.jpg"));
        list.add(new Movie("1", "The Only One Movie Possible", "https://image.tmdb.org/t/p/w640/hLudzvGfpi6JlwUnsNhXwKKg4j.jpg", "https://image.tmdb.org/t/p/w640/hLudzvGfpi6JlwUnsNhXwKKg4j.jpg", this, "125", "2017", "10.0", "76", "https://image.tmdb.org/t/p/w533_and_h300_bestv2/x3akjRHhZnIZx0EiQ3eOg66qoS9.jpg"));
        list.add(new Movie("1", "The Only One Movie Possible", "https://image.tmdb.org/t/p/w640/hLudzvGfpi6JlwUnsNhXwKKg4j.jpg", "https://image.tmdb.org/t/p/w640/hLudzvGfpi6JlwUnsNhXwKKg4j.jpg", this, "125", "2017", "10.0", "76", "https://image.tmdb.org/t/p/w533_and_h300_bestv2/x3akjRHhZnIZx0EiQ3eOg66qoS9.jpg"));
        list.add(new Movie("1", "The Only One Movie Possible", "https://image.tmdb.org/t/p/w640/hLudzvGfpi6JlwUnsNhXwKKg4j.jpg", "https://image.tmdb.org/t/p/w640/hLudzvGfpi6JlwUnsNhXwKKg4j.jpg", this, "125", "2017", "10.0", "76", "https://image.tmdb.org/t/p/w533_and_h300_bestv2/x3akjRHhZnIZx0EiQ3eOg66qoS9.jpg"));
        list.add(new Movie("1", "The Only One Movie Possible", "https://image.tmdb.org/t/p/w640/hLudzvGfpi6JlwUnsNhXwKKg4j.jpg", "https://image.tmdb.org/t/p/w640/hLudzvGfpi6JlwUnsNhXwKKg4j.jpg", this, "125", "2017", "10.0", "76", "https://image.tmdb.org/t/p/w533_and_h300_bestv2/x3akjRHhZnIZx0EiQ3eOg66qoS9.jpg"));


        return fetchList(list, requestBuilder, filter, callback);
    }

    private Call fetchList(final ArrayList<Media> currentList, final Request.Builder requestBuilder, final Filters filters, final Callback callback) {
        return enqueue(requestBuilder.build(), new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //callback.onFailure(e);
                callback.onSuccess(filters, currentList, true);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseStr;

                    try {
                        responseStr = response.body().string();
                    } catch (SocketException e) {
                        onFailure(new IOException("Socket Exception"));
                        return;
                    }

                    MovieResponse movies;

                    try {
                        movies = gson.fromJson(responseStr, MovieResponse.class);
                    } catch (Exception e) {
                        onFailure(e);
                    }

                    callback.onSuccess(filters, currentList, true);
                }
            }

            void onFailure(Exception ex) {
                Log.d("MoviesProviderError", "Error: " + ex.toString());
                //callback.onFailure(ex);
                callback.onSuccess(filters, currentList, true);
            }
        });
    }

    @Override
    public Call getDetail(ArrayList<Media> currentList, Integer index, Callback callback) {
        ArrayList<Media> returnList = new ArrayList<>();
        returnList.add(currentList.get(index));
        callback.onSuccess(null, returnList, true);
        return null;
    }

    @Override
    public int getLoadingMessage() {
        return R.string.loading_movies_title;
    }

    @Override
    public List<NavInfo> getNavigation() {
        List<NavInfo> tabs = new ArrayList<>();
        tabs.add(new NavInfo(R.id.movies_trending, Filters.Sort.TRENDING, Filters.Order.DESC, "ALL", R.drawable.filter_trending));
        tabs.add(new NavInfo(R.id.movies_trending, Filters.Sort.TRENDING, Filters.Order.DESC, WatchTimeApplication.getAppContext().getString(R.string.trending), R.drawable.filter_trending));
        return tabs;
    }

    @Override
    public List<Genre> getGenres() {
        ArrayList<Genre> returnList = new ArrayList<>();
        returnList.add(new Genre(null, R.string.all_genres));
        returnList.add(new Genre("action", R.string.genre_action));
        returnList.add(new Genre("adventure", R.string.genre_adventure));
        returnList.add(new Genre("animation", R.string.genre_animation));
        returnList.add(new Genre("comedy", R.string.genre_comedy));
        returnList.add(new Genre("family", R.string.genre_family));
        returnList.add(new Genre("fantasy", R.string.genre_fantasy));
        returnList.add(new Genre("mystery", R.string.genre_mystery));
        returnList.add(new Genre("romance", R.string.genre_romance));
        returnList.add(new Genre("scifi", R.string.genre_sci_fi));
        return returnList;
    }

    private class MovieResponse {
        public String id;
        public String page;
        public LinkedTreeMap<String, Object> results;
        public String total_pages;
        public String total_results;
    }
}
