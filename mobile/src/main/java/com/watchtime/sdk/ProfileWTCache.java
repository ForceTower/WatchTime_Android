package com.watchtime.sdk;

import android.util.Log;

import com.watchtime.base.content.ObscuredSharedPreferences;
import com.watchtime.base.utils.PrefUtils;
import com.watchtime.sdk.validators.Validate;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by João Paulo on 04/03/2017.
 */

public class ProfileWTCache {
    private static final String CACHED_PROFILE_KEY = "com.watchtime.WatchTimeProfileManager.CachedProfile";
    private ObscuredSharedPreferences sharedPreferences;

    public ProfileWTCache() {
        sharedPreferences = PrefUtils.getPrefs(WatchTimeSdk.getApplicationContext());
    }

    Profile load() {
        String jsonString = sharedPreferences.getString(CACHED_PROFILE_KEY, null);
        if (jsonString != null) {
            try {
                JSONObject jsonObject = new JSONObject(jsonString);
                return new Profile(jsonObject);
            } catch (JSONException e) {
                Log.d("WTimeSDK", "JSONError: " + e.getMessage());
                return null;
            }
        }
        return null;
    }

    void save(Profile profile) {
        Validate.notNull(profile, "profile");
        JSONObject jsonObject = profile.toJSONObject();
        if (jsonObject != null) {
            sharedPreferences
                    .edit()
                    .putString(CACHED_PROFILE_KEY, jsonObject.toString())
                    .apply();
        }
    }

    void clear() {
        sharedPreferences
                .edit()
                .remove(CACHED_PROFILE_KEY)
                .apply();

    }
}
