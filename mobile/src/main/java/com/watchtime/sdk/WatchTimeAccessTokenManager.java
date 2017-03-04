package com.watchtime.sdk;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;

import com.watchtime.base.Constants;
import com.watchtime.sdk.validators.Validate;

/**
 * Created by João Paulo on 04/03/2017.
 */

public class WatchTimeAccessTokenManager {
    static final String SHARED_PREFERENCES_NAME = "com.watchtime.sdk";
    private static volatile WatchTimeAccessTokenManager instance;
    private AccessTokenWT currentAccessToken;
    private AccessTokenWTCache tokenCache;
    private AccountManager accountManager;

    private WatchTimeAccessTokenManager(AccountManager accountManager, AccessTokenWTCache tokenCache) {
        Validate.notNull(accountManager, "Account Manager");
        Validate.notNull(tokenCache, "Token Cache");
        this.accountManager = accountManager;
        this.tokenCache = tokenCache;
    }

    static WatchTimeAccessTokenManager getInstance() {
        if (instance == null) {
            synchronized (WatchTimeAccessTokenManager.class) {
                if (instance == null) {
                    Context applicationContext = WatchTimeSdk.getApplicationContext();
                    AccountManager accountManager = AccountManager.get(applicationContext);
                    AccessTokenWTCache tokenCache = new AccessTokenWTCache();

                    instance = new WatchTimeAccessTokenManager(accountManager, tokenCache);
                }
            }
        }

        return instance;
    }

    boolean loadCurrentAccessToken() {
        AccessTokenWT accessToken = tokenCache.load();
        if (accessToken != null) {
            setCurrentAccessToken(accessToken, false);
            return true;
        }
        return false;
    }

    void setCurrentAccessToken(AccessTokenWT currentAccessToken) {
        setCurrentAccessToken(currentAccessToken, true);
    }

    AccessTokenWT getCurrentAccessToken() {
        return currentAccessToken;
    }

    public void setCurrentAccessToken(AccessTokenWT currentAccessToken, boolean save) {
        this.currentAccessToken = currentAccessToken;

        if (save) {
            if (currentAccessToken != null) {
                tokenCache.save(currentAccessToken);
            } else {
                tokenCache.clear();
            }
        }
    }
}
