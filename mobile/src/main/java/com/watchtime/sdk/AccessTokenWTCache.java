package com.watchtime.sdk;

import com.watchtime.base.content.ObscuredSharedPreferences;
import com.watchtime.base.utils.PrefUtils;
import com.watchtime.sdk.validators.Validate;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by João Paulo on 04/03/2017.
 */

public class AccessTokenWTCache {
    public static final String CACHED_ACCESS_TOKEN_KEY = "com.watch_time.WatchTimeAccessTokenManager.CachedAccessToken";
    private ObscuredSharedPreferences sharedPreferences;

    public AccessTokenWTCache() {
        this(PrefUtils.getPrefs(WatchTimeSdk.getApplicationContext()));
    }

    public AccessTokenWTCache(ObscuredSharedPreferences prefs) {
        this.sharedPreferences = prefs;
    }

    public AccessTokenWT load() {
        AccessTokenWT accessToken = null;

        if (hasTokenCached()) {
            accessToken = getCachedAccessToken();
        }

        return accessToken;
    }

    private boolean hasTokenCached() {
        return sharedPreferences.contains(CACHED_ACCESS_TOKEN_KEY);
    }

    private AccessTokenWT getCachedAccessToken() {
        String jsonString = sharedPreferences.getString(CACHED_ACCESS_TOKEN_KEY, null);
        if (jsonString != null) {
            try {
                JSONObject jsonObject = new JSONObject(jsonString);
                return AccessTokenWT.createFromJSON(jsonObject);
            } catch (JSONException e) {
                return null;
            }
        }
        return null;
    }

    public void save(AccessTokenWT accessToken) {
        Validate.notNull(accessToken, "accessToken");

        JSONObject jsonObject = null;
        try {
            jsonObject = accessToken.toJSONObject();
            sharedPreferences.edit().putString(CACHED_ACCESS_TOKEN_KEY, jsonObject.toString()).apply();
        } catch (JSONException e) {

        }
    }


    public void clear() {
        sharedPreferences.edit().remove(CACHED_ACCESS_TOKEN_KEY).apply();
    }
}
