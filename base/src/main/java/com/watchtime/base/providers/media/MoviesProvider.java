package com.watchtime.base.providers.media;

import android.util.Log;

import com.watchtime.base.ApiEndPoints;
import com.watchtime.base.R;
import com.watchtime.base.WatchTimeApplication;
import com.watchtime.base.providers.media.models.Genre;
import com.watchtime.base.providers.media.models.Media;
import com.watchtime.base.providers.media.response.MovieResponse;

import org.json.JSONArray;
import org.json.JSONObject;

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
    public Call getList(ArrayList<Media> currentList, Filters filter, Callback callback, String token) {
        filters = filter;

        final ArrayList<Media> list;
        if (currentList == null) {
            list = new ArrayList<>();
        } else {
            //noinspection unchecked
            list = (ArrayList<Media>) currentList.clone();
        }


        String requestWebsite = ApiEndPoints.BASE_MOVIES_POPULAR;
        if (filter.sort == Filters.Sort.RELEASE) {
            requestWebsite = ApiEndPoints.BASE_MOVIES_RELEASE;
        } else if (filter.sort == Filters.Sort.RATING) {
            requestWebsite = ApiEndPoints.BASE_MOVIES_RATING;
        } else if (filter.sort == Filters.Sort.NOW_PLAYING) {
            requestWebsite = ApiEndPoints.BASE_MOVIES_NOW_PLAYING;
        } else if (filter.sort == Filters.Sort.UPCOMING) {
            requestWebsite = ApiEndPoints.BASE_MOVIES_UPCOMING;
        }

        Request.Builder requestBuilder = new Request.Builder();
        requestBuilder.url(requestWebsite + filter.page);
        requestBuilder.tag(MEDIA_CALL);

        return fetchList(list, requestBuilder, filter, callback);
    }

    private Call fetchList(final ArrayList<Media> currentList, final Request.Builder requestBuilder, final Filters filters, final Callback callback) {
        return enqueue(requestBuilder.build(), new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (e.getMessage() == null) {
                    e.printStackTrace();
                    e = new IOException("Failed Fetching for a random reason");
                }
                callback.onFailure(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseStr;

                    try {
                        responseStr = response.body().string();
                    } catch (SocketException e) {
                        onFailure(e);
                        return;
                    }

                    boolean object = false;
                    try {
                        new JSONArray(responseStr);
                    } catch(Exception e) {
                        object = true;
                    }

                    MovieResponse movies;
                    try {
                        if (object) {
                            JSONObject jsonObject = new JSONObject(responseStr);
                            JSONArray array = jsonObject.getJSONArray("data");
                            responseStr = array.toString();
                        }
                        movies = new MovieResponse(responseStr, mediaProvider);
                        currentList.addAll(movies.asList());
                    } catch (Exception e) {
                        onFailure(e);
                    }
                    callback.onSuccess(filters, currentList, true);
                } else {
                    onFailure(new Exception("Response is Unsuccessful"));
                }
            }

            void onFailure(Exception ex) {
                Log.d("MoviesProviderError", "Error: " + ex.getMessage());
                callback.onFailure(ex);
            }
        });
    }

    @Override
    public Call getDetail(ArrayList<Media> currentList, Integer index, Callback callback, String token) {
        Media media = currentList.get(index);
        Request.Builder requestBuilder = new Request.Builder();
        requestBuilder.url(ApiEndPoints.BASE_MOVIES_DETAILS + media.videoId);
        requestBuilder.tag(MEDIA_CALL);
        return fetchDetails(requestBuilder, callback);
    }

    private Call fetchDetails(final Request.Builder requestBuilder, final Callback callback) {
        return enqueue(requestBuilder.build(), new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("MoviesProviderError", "Failed Fetching Details");
                callback.onFailure(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if (response.isSuccessful()) {
                    String responseStr;
                    try {
                        responseStr = response.body().string();
                    } catch (SocketException e) {
                        onFailure(e);
                        return;
                    }

                    MovieResponse movieResponse;
                    try {
                        movieResponse = new MovieResponse(mediaProvider);
                        JSONObject json = new JSONObject(responseStr);
                        callback.onSuccess(null, movieResponse.singleMovie(json), true);
                    } catch (Exception e) {
                        onFailure(e);
                    }

                }
            }

            void onFailure(Exception ex) {
                Log.d("MoviesProviderError", "Error: " + ex.getMessage());
                callback.onFailure(ex);
            }
        });
    }

    @Override
    public int getLoadingMessage() {
        return R.string.loading_movies_title;
    }

    @Override
    public List<NavInfo> getNavigation() {
        List<NavInfo> tabs = new ArrayList<>();
        tabs.add(new NavInfo(R.id.movies_release, Filters.Sort.RELEASE, Filters.Order.DESC, WatchTimeApplication.getAppContext().getString(R.string.release), R.drawable.filter_release));
        tabs.add(new NavInfo(R.id.movies_popular, Filters.Sort.POPULARITY, Filters.Order.DESC, WatchTimeApplication.getAppContext().getString(R.string.popularity), R.drawable.filter_popularity));
        tabs.add(new NavInfo(R.id.movies_rating, Filters.Sort.RATING, Filters.Order.DESC, WatchTimeApplication.getAppContext().getString(R.string.rating), R.drawable.filter_rating));
        tabs.add(new NavInfo(R.id.movies_now_playing, Filters.Sort.NOW_PLAYING, Filters.Order.DESC, WatchTimeApplication.getAppContext().getString(R.string.now_playing), R.drawable.filter_now_playing));
        tabs.add(new NavInfo(R.id.movies_upcoming, Filters.Sort.UPCOMING, Filters.Order.DESC, WatchTimeApplication.getAppContext().getString(R.string.upcoming), R.drawable.filter_upcoming));
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
}
