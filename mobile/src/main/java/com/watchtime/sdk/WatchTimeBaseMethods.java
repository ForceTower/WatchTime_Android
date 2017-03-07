package com.watchtime.sdk;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import com.watchtime.R;
import com.watchtime.activities.MainActivity;
import com.watchtime.base.ApiEndPoints;
import com.watchtime.base.Constants;
import com.watchtime.base.WatchTimeApplication;
import com.watchtime.base.interfaces.OnDataChangeHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public final class WatchTimeBaseMethods {
    private static final int MARK_MOVIE_WATCHED = 0;

    private static WatchTimeBaseMethods instance = null;
    private static Context applicationContext;
    private AccountManager accountManager;

    private WatchTimeBaseMethods() {
        applicationContext = WatchTimeSdk.getApplicationContext();
        accountManager = AccountManager.get(WatchTimeSdk.getApplicationContext());
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

    public boolean refreshToken() {
        Account[] accounts = accountManager.getAccountsByType(Constants.ACCOUNT_TYPE);

        if (accounts.length != 1)
            return false;

        Account account = accounts[0];

        if (tryRefreshWithFacebook(account))
            return true;

        String refreshToken = accountManager.getUserData(account, "refresh_token");

        if (TextUtils.isEmpty(refreshToken)) {
            Toast.makeText(getContext(), getContext().getString(R.string.login_again), Toast.LENGTH_SHORT).show();
            LoginManager.getInstance().logOut();
            LoginManagerWT.getInstance().logout();
            ((WatchTimeApplication)getContext()).getDataChangeHandler().igniteListeners(OnDataChangeHandler.LOGOUT);
            return false;
        }

        return tryRefreshNormalMode(account, refreshToken);
    }

    private boolean tryRefreshWithFacebook(final Account account) {
        if (AccessToken.getCurrentAccessToken() == null || AccessToken.getCurrentAccessToken().isExpired() || AccessToken.getCurrentAccessToken().getToken() == null)
            return false;

        RequestBody requestBody = new FormBody.Builder()
                .add("email", account.name)
                .add("facebook_id", AccessToken.getCurrentAccessToken().getUserId())
                .add("facebook_token", AccessToken.getCurrentAccessToken().getToken())
                .add("grant_type", "no_password")
                .add("client_id", ApiEndPoints.CLIENT_ID)
                .add("client_secret", ApiEndPoints.CLIENT_SECRET)
                .build();

        Request request = new Request.Builder()
                .url(ApiEndPoints.OAUTH2_BASE)
                .post(requestBody)
                .build();

        Call tokenCall = new OkHttpClient().newCall(request);
        try {
            Response response = tokenCall.execute();
            if (!response.isSuccessful()) {
                Log.i("FacebookTK-RFR", "Refresh Token with facebook failed: " + response.body().string());
                LoginManager.getInstance().logOut();
                return false;
            }
            JSONObject token;
            try {
                token = new JSONObject(response.body().string());
                AccessTokenWT accessToken = AccessTokenWT.createFromJSON(token);
                accountManager.setAuthToken(account, accessToken.getTokenType(), accessToken.getAccessToken());
                accountManager.setUserData(account, "refresh_token", accessToken.getRefreshToken());
                Log.i("FacebookTK-RFR", "Regained Access");
                WatchTimeAccessTokenManager.getInstance().setCurrentAccessToken(accessToken);
                LoginManagerWT.getInstance().onLogin();
                return true;
            } catch (JSONException e) {
                Log.i("FacebookTK-RFR", "Refresh Token with facebook failed, jsonException: " + e.getMessage());
                LoginManager.getInstance().logOut();
            }
        } catch (IOException e) {
            LoginManager.getInstance().logOut();
            Log.i("FacebookTK-RFR", "Refresh Token with facebook failed, IOException: " + e.getMessage());
            return false;
        }

        return false;
    }

    private boolean tryRefreshNormalMode(final Account account, String refreshToken) {
        RequestBody requestBody = new FormBody.Builder()
                .add("refresh_token", refreshToken)
                .add("grant_type", "refresh_token")
                .add("client_id", ApiEndPoints.CLIENT_ID)
                .add("client_secret", ApiEndPoints.CLIENT_SECRET)
                .build();
        Request request = new Request.Builder()
                .url(ApiEndPoints.OAUTH2_BASE)
                .post(requestBody)
                .build();


        Call tokenCall = new OkHttpClient().newCall(request);

        try {
            Response response = tokenCall.execute();
            if (!response.isSuccessful()) {
                Log.i("NormalTK-RFR", "Refresh Token with normal failed: " + response.body().string());
                return false;
            }
            JSONObject token;
            try {
                token = new JSONObject(response.body().string());
                AccessTokenWT accessToken = AccessTokenWT.createFromJSON(token);
                accountManager.setAuthToken(account, accessToken.getTokenType(), accessToken.getAccessToken());
                accountManager.setUserData(account, "refresh_token", accessToken.getRefreshToken());
                Log.i("NormalTK-RFR", "Regained Access");
                WatchTimeAccessTokenManager.getInstance().setCurrentAccessToken(accessToken);
                LoginManagerWT.getInstance().onLogin();
                return true;
            } catch (JSONException e) {
                Log.i("NormalTK-RFR", "Refresh Token with normal failed, jsonException: " + e.getMessage());
                LoginManager.getInstance().logOut();
            }
        } catch (IOException e) {
            Log.i("NormalTK-RFR", "Refresh Token with normal failed, IOException: " + e.getMessage());
        }

        return false;
    }

    private Request.Builder requestBuilder(String url, boolean addAuthHeader) {
        Request.Builder builder = new Request.Builder();
        builder.url(url);

        if (addAuthHeader)
            builder.addHeader("Authorization", "Bearer " + AccessTokenWT.getCurrentAccessToken().getAccessToken());
        return builder;
    }

    public void markMovieAsWatched(final String id) {
        List<Pair<String, String>> values = new ArrayList<>();
        Pair<String, String> value = new Pair<>("tmdb", id);
        values.add(value);

        RequestBody requestBody = requestBodyBuilder(values);

        Request.Builder requestBuilder = requestBuilder(ApiEndPoints.MARK_MOVIE_WATCHED, true);
        requestBuilder.post(requestBody);

        final Request request = requestBuilder.build();

        Callback callback = new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i("MovieDetailsFrag", "Failed to mark watched: " + e.getMessage());
                Toast.makeText(getContext(), getContext().getString(R.string.mark_failed), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    final String strResp = response.body().string();
                    Log.i("MovieDetailsFrag", "Unsuccessful response: " + strResp);

                    try {
                        JSONObject json = new JSONObject(strResp);
                        if (json.has("error")) {
                            if (json.optString("error", "empty").equals("access_denied")) {
                                Log.i("MovieMark", "Token is invalid");

                                if (refreshToken()) {
                                    markMovieAsWatched(id);
                                }

                            }
                        }
                    } catch (JSONException e) {
                        Log.i("MovieMark", "Exception is: " + e.getMessage());
                        return;
                    }

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
        };

        Call call = new OkHttpClient().newCall(request);
        call.enqueue(callback);
    }

    public void updateCoverPicture(final int id) {
        RequestBody requestBody = new FormBody.Builder()
                .add("id", Integer.toString(id))
                .build();

        Request request = new Request.Builder()
                .addHeader("Authorization", "Bearer " + AccessTokenWT.getCurrentAccessToken().getAccessToken())
                .url(ApiEndPoints.UPDATE_COVER_PICTURE)
                .post(requestBody)
                .build();

        Call call = new OkHttpClient().newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i("MediaImageActivity", "Failed to update cover: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    String strResp = response.body().string();
                    Log.i("MediaImageActivity", "Unsuccessful response: " + strResp);
                    try {

                        JSONObject json = new JSONObject(strResp);
                        if (json.has("error")) {
                            if (json.optString("error", "empty").equals("access_denied")) {
                                Log.i("CoverUpdate", "Token is invalid");

                                if (refreshToken()) {
                                    updateCoverPicture(id);
                                }
                            }
                        }
                    } catch (JSONException e) {
                        Log.i("CoverUpdate", "Exception is: " + e.getMessage());
                        return;
                    }
                    return;
                }

                Log.i("MediaImageActivity", "Success Changing cover");
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(), R.string.cover_update_success, Toast.LENGTH_SHORT).show();
                        ((WatchTimeApplication)getContext()).getDataChangeHandler().igniteListeners(OnDataChangeHandler.ALL);
                    }
                });

                Profile.fetchProfileForCurrentAccessToken();
            }
        });
    }

}
