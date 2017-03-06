package com.watchtime.sdk;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import com.watchtime.R;
import com.watchtime.base.ApiEndPoints;
import com.watchtime.base.WatchTimeApplication;
import com.watchtime.base.interfaces.OnDataChangeHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by João Paulo on 06/03/2017.
 */

public final class WatchTimeBaseMethods {
    private static WatchTimeBaseMethods instance;
    private static Context applicationContext;

    private WatchTimeBaseMethods() {
        applicationContext = WatchTimeSdk.getApplicationContext();
    }

    public static void instantiate() {
        if (instance == null) {
            instance = new WatchTimeBaseMethods();
        }
    }

    public static WatchTimeBaseMethods getInstance() {
        instantiate();
        return instance;
    }

    private Context getContext() {
        return applicationContext;
    }

    private RequestBody requestBodyBuilder(List<Pair<String, String>> values) {
        FormBody.Builder requestBody = new FormBody.Builder();

        for (Pair<String, String> value : values) {
            requestBody.add(value.first, value.second);
        }

        return requestBody.build();
    }

    private Request.Builder requestBuilder(String url, boolean addAuthHeader) {
        Request.Builder builder = new Request.Builder();
        builder.url(url);

        if (addAuthHeader)
            builder.addHeader("Authorization", "Bearer " + AccessTokenWT.getCurrentAccessToken().getAccessToken());
        return builder;
    }

    public void markMovieAsWatched(String id) {
        List<Pair<String, String>> values = new ArrayList<>();
        Pair<String, String> value = new Pair<>("tmdb", id);
        values.add(value);

        RequestBody requestBody = requestBodyBuilder(values);

        Request.Builder requestBuilder = requestBuilder(ApiEndPoints.MARK_MOVIE_WATCHED, true);
        requestBuilder.post(requestBody);

        Request request = requestBuilder.build();

        Call call = new OkHttpClient().newCall(request);
        call.enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i("MovieDetailsFrag", "Failed to mark watched: " + e.getMessage());
                Toast.makeText(getContext(), getContext().getString(R.string.mark_failed), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    final String strResp = response.body().string();
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.i("MovieDetailsFrag", "Unsuccessful response: " + strResp);
                            Toast.makeText(getContext(), getContext().getString(R.string.mark_failed), Toast.LENGTH_SHORT).show();
                        }
                    });
                    return;
                }

                try {
                    JSONObject obj = new JSONObject(response.body().string());
                    if (obj.has("error")) {
                        final int code = obj.getInt("error_code");
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (code == -1)
                                    Toast.makeText(getContext(), getContext().getString(R.string.no_permission), Toast.LENGTH_SHORT).show();
                                else if (code == 0)
                                    Toast.makeText(getContext(), getContext().getString(R.string.mark_failed), Toast.LENGTH_SHORT).show();
                                else if (code == 1)
                                    Toast.makeText(getContext(), getContext().getString(R.string.already_marked), Toast.LENGTH_SHORT).show();
                            }
                        });
                        return;
                    }
                } catch (JSONException e) {
                    return;
                }

                Log.i("MovieDetailsFrag", "Success marking");
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(), getContext().getString(R.string.marked_as_watched), Toast.LENGTH_SHORT).show();
                    }
                });

                Profile.fetchProfileForCurrentAccessToken();
                ((WatchTimeApplication)getContext()).getDataChangeHandler().igniteListeners(OnDataChangeHandler.ALL);
            }
        });
    }
}
