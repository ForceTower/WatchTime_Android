package com.watchtime.base.backend.token;

import android.util.Log;

public class TokenAPI {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private int expiration;

    public TokenAPI(String access_token, String refresh_token, String token_type, int expiration) {
        this.accessToken = access_token;
        this.refreshToken = refresh_token;
        this.tokenType = token_type;
        this.expiration = expiration;
        Log.d("TokenAPI", "WatchTimeAccessToken: " + access_token);
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
}
