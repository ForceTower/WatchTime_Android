package com.watchtime.sdk;

/**
 * Created by João Paulo on 04/03/2017.
 */

public class LoginManagerWT {
    private static LoginManagerWT instance;

    public static LoginManagerWT getInstance() {
        if (instance == null) {
            instance = new LoginManagerWT();
        }
        return instance;
    }

    public void onLogin() {
        WatchTimeAccessTokenManager.getInstance().loadCurrentAccessToken();
        Profile.fetchProfileForCurrentAccessToken();
    }

    public void logout() {
        WatchTimeAccessTokenManager.getInstance().setCurrentAccessToken(null);
        WatchTimeProfileManager.getInstance().setCurrentProfile(null);
    }
}
