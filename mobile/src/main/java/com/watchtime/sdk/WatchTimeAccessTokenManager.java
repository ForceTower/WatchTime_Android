package com.watchtime.sdk;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;

import com.watchtime.base.Constants;

public class WatchTimeAccessTokenManager {
    private static volatile WatchTimeAccessTokenManager instance;
    private AccessTokenWT currentAccessToken;
    private AccountManager accountManager;

    private WatchTimeAccessTokenManager(AccountManager accountManager) {
        this.accountManager = accountManager;
    }

    public static WatchTimeAccessTokenManager getInstance() {
        if (instance == null) {
            synchronized (WatchTimeAccessTokenManager.class) {
                if (instance == null) {
                    Context applicationContext = WatchTimeSdk.getApplicationContext();
                    AccountManager accountManager = AccountManager.get(applicationContext);

                    instance = new WatchTimeAccessTokenManager(accountManager);
                }
            }
        }

        return instance;
    }

    public boolean loadCurrentAccessToken() {
        AccessTokenWT accessToken = null;

        Account[] account = accountManager.getAccountsByType(Constants.ACCOUNT_TYPE);
        if (account.length != 0) {
            String token = accountManager.peekAuthToken(account[0], Constants.ACCOUNT_TOKEN_TYPE);
            accessToken = new AccessTokenWT(token, null, "Bearer", 7200);
        }

        currentAccessToken = accessToken;
        return false;
    }

    public void setCurrentAccessToken(AccessTokenWT currentAccessToken) {
        this.currentAccessToken = currentAccessToken;
    }

    public AccessTokenWT getCurrentAccessToken() {
        return currentAccessToken;
    }
}
