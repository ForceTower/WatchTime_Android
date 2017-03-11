package com.watchtime.base.providers.media;

import android.util.Log;

import com.watchtime.base.ApiEndPoints;
import com.watchtime.base.R;
import com.watchtime.base.WatchTimeApplication;
import com.watchtime.base.providers.media.models.Genre;
import com.watchtime.base.providers.media.models.Media;
import com.watchtime.base.providers.media.models.WatchlistItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

public class UserListsProvider extends MediaProvider {
    private static UserListsProvider mediaProvider = new UserListsProvider();

    public Call getList(ArrayList<Media> currentList, Filters filters, Callback callback, String token) {
        final ArrayList<Media> list;
        if (currentList == null) {
            list = new ArrayList<>();
        } else {
            //noinspection unchecked
            list = (ArrayList<Media>) currentList.clone();
        }

        String requestWebsite = "";
        if (filters.category == Filters.Category.WATCHLIST)
            requestWebsite = ApiEndPoints.MY_WATCHLIST;

        Request.Builder requestBuilder = new Request.Builder();
        requestBuilder.url(requestWebsite);
        requestBuilder.addHeader("Authorization", "Bearer " + token);
        Log.i("UserListProv", "Using token: " + token);
        requestBuilder.tag(MEDIA_CALL);

        return fetchList(list, requestBuilder, filters, callback);
    }

    private Call fetchList(final ArrayList<Media> list, Request.Builder requestBuilder, final Filters filters, final Callback callback) {
        return enqueue(requestBuilder.build(), new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                callback.onSuccess(filters, list, true);

                if (!response.isSuccessful()) {
                    String strResp = response.body().string();

                    try {
                        JSONObject json = new JSONObject(strResp);
                        if (json.has("error")) {
                            if (json.optString("error", "empty").equals("access_denied")) {
                                Log.i("UserListsProv", "Token is invalid");
                                callback.onFailure(new Exception("InvalidToken"));
                                return;
                            }
                        }
                        Log.i("UserListsProv", strResp);
                        callback.onFailure(new Exception("Unknown"));
                    } catch(JSONException e) {
                        Log.i("UserListsProv", "JSONEx: " + e.getMessage());
                        callback.onFailure(e);
                    }
                } else {
                    String string = response.body().string();

                    try {
                        JSONObject values = new JSONObject(string);
                        list.addAll(process(values));
                    } catch (JSONException e) {
                        Log.i("UserListsProv", "JSONEx: " + e.getMessage());
                        callback.onFailure(e);
                    }

                    callback.onSuccess(filters, list, true);
                }
            }
        });
    }

    @Override
    public Call getDetail(ArrayList<Media> currentList, Integer index, Callback callback, String token) {
        return null;
    }

    @Override
    public List<Genre> getGenres() {
        return new ArrayList<>();
    }

    @Override
    public int getLoadingMessage() {
        return R.string.loading_lists_message;
    }

    @Override
    public List<NavInfo> getNavigation() {
        List<NavInfo> tabs = new ArrayList<>();
        tabs.add(new NavInfo(R.id.movies_watch_list, null, Filters.Order.DESC, WatchTimeApplication.getAppContext().getString(R.string.watchlist), R.drawable.filter_movie_watchlist, Filters.Category.WATCHLIST));
        return tabs;
    }

    private static List<Media> process(JSONObject json) throws JSONException {
        List<Media> media = new ArrayList<>();

        JSONArray data = json.getJSONArray("data");
        for (int i = 0; i < data.length(); i++) {
            JSONObject entry = data.getJSONObject(i);

            String tmdb = entry.getString("tmdb");
            String title = entry.getString("name");
            String image = entry.getString("image");
            int runtime = entry.getInt("runtime");
            String added = entry.getString("date_added");
            String genresStr = "";

            JSONObject genresObj = entry.getJSONObject("genres");
            JSONArray genres = genresObj.getJSONArray("data");
            for (int j = 0; j < genres.length(); j++) {
                JSONObject genre = genres.getJSONObject(j);

                if (j == 0) {
                    genresStr += genre.getString("genre");
                } else {
                    genresStr += " - " + genre.getString("genre");
                }
            }

            if (image.trim().equals("") || image.equals("null")) {
                image = "";
            } else {
                image = "https://image.tmdb.org/t/p/w780/" + image;
            }

            Log.i("UserListsProv", "Item fetch: " + title);
            WatchlistItem mediaWatchlist = new WatchlistItem(tmdb, title, image, mediaProvider, added, runtime, genresStr);
            media.add(mediaWatchlist);
        }

        return media;
    }
}
