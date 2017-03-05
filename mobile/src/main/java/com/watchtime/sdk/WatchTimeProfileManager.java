package com.watchtime.sdk;

import android.util.Log;

import com.watchtime.sdk.validators.Validate;

public class WatchTimeProfileManager {
    private static WatchTimeProfileManager instance;
    private ProfileWTCache profileCache;
    private Profile currentProfile;

    public WatchTimeProfileManager(ProfileWTCache profileCache) {
        Validate.notNull(profileCache, "profileCache");
        this.profileCache = profileCache;
    }

    public static WatchTimeProfileManager getInstance() {
        if (instance == null) {
            instance = new WatchTimeProfileManager(new ProfileWTCache());
        }

        return instance;
    }

    boolean loadCurrentProfile() {
        Profile profile = profileCache.load();
        Log.d("WTimeSDK", "Profile loaded from cache: " + profile);
        if (profile != null) {
            setCurrentProfile(profile, false);
            return true;
        }

        return false;
    }

    public void setCurrentProfile(Profile currentProfile) {
        setCurrentProfile(currentProfile, true);
    }

    Profile getCurrentProfile() {
        return currentProfile;
    }

    private void setCurrentProfile(Profile currentProfile, boolean writeToCache) {
        this.currentProfile = currentProfile;

        if (writeToCache) {
            if (currentProfile != null) {
                profileCache.save(currentProfile);
            } else {
                profileCache.clear();
            }
        }
    }
}
