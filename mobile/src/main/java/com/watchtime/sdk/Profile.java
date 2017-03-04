package com.watchtime.sdk;

import android.util.Log;

import com.watchtime.base.ApiEndPoints;
import com.watchtime.base.backend.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by João Paulo on 04/03/2017.
 */

public class Profile {
    private String name;
    private int timeWatched;
    private String cover;
    private String email;
    private int id;


    public Profile(JSONObject json) throws JSONException {
        Log.d("WTimeSDK", "response: " + json.toString());
        json = json.getJSONObject("data");
        String name = json.getString("name");
        int id = json.getInt("id");
        String email = json.getString("email");
        String cover = json.optString("cover", "");
        int timeWatched = json.getInt("time_watched");

        if (cover.trim().isEmpty() || cover.trim().equals("null"))
            cover = null;

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

    public JSONObject toJSONObject() {
        JSONObject object = new JSONObject();
        try {
            object.put("id", id);
            object.put("email", email);
            object.put("name", name);
            object.put("cover", cover);
            object.put("time_watched", timeWatched);
            return object;
        } catch (JSONException e) {
            return null;
        }
    }

    public static Profile getCurrentProfile() {
        return WatchTimeProfileManager.getInstance().getCurrentProfile();
    }

    public static void fetchProfileForCurrentAccessToken() {
        AccessTokenWT accessToken = AccessTokenWT.getCurrentAccessToken();
        if (accessToken == null) {
            Profile.setCurrentProfile(null);
            return;
        }

        Request request = new Request.Builder()
                .addHeader("Authorization", "Bearer " + accessToken.getAccessToken())
                .url(ApiEndPoints.PROFILE_ME)
                .get()
                .build();

        Call profileFetch = new OkHttpClient().newCall(request);
        profileFetch.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i("WTimeSDK", "Profile fetch failed " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.i("WTimeSDK", "Unsuccessful response");
                    return;
                }

                try {
                    Profile profile = new Profile(new JSONObject(response.body().string()));
                    Profile.setCurrentProfile(profile);
                    Log.i("WTimeSDK", "Profile Set!");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void setCurrentProfile(Profile profile) {
        WatchTimeProfileManager.getInstance().setCurrentProfile(profile);
    }
}
