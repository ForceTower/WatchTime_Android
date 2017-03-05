package com.watchtime.sdk;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by João Paulo on 04/03/2017.
 */

public class AccessTokenWT {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private int expiration;

    public AccessTokenWT(String access_token, String refresh_token, String token_type, int expiration) {
        this.accessToken = access_token;
        this.refreshToken = refresh_token;
        this.tokenType = token_type;
        this.expiration = expiration;
    }

    public int getExpiration() {
        return expiration;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public static AccessTokenWT createFromJSON(JSONObject json) throws JSONException {
        String accessToken = json.getString("access_token");
        String refreshToken = json.optString("refresh_token");
        String tokenType = json.getString("token_type");
        int expires = json.getInt("expires_in");

        return new AccessTokenWT(accessToken, refreshToken, tokenType, expires);
    }

    public static AccessTokenWT getCurrentAccessToken() {
        return WatchTimeAccessTokenManager.getInstance().getCurrentAccessToken();
    }
}
