package com.watchtime.sdk;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.watchtime.base.Constants;
import com.watchtime.base.WatchTimeApplication;
import com.watchtime.base.interfaces.OnDataChangeHandler;
import com.watchtime.services.firebase.WTFirebaseTokenRefresh;

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

    public boolean isConnected(Context context) {
        if (context == null)
            context = WatchTimeApplication.getAppContext();

        AccountManager manager = AccountManager.get(context);
        Account[] accounts = manager.getAccountsByType(Constants.ACCOUNT_TYPE);
        if (accounts.length != 0) {
            Account account = accounts[0];
            String token = manager.peekAuthToken(account, Constants.ACCOUNT_TOKEN_TYPE);
            if (!TextUtils.isEmpty(token)) {
                Log.i("WTimeSDK", "Logged as: " + account.name + ". Token: " + token);
                return true;
            }
        }

        Log.i("WTimeSDK", "Not connected");
        return false;
    }

    public void onLogin() {
        WatchTimeAccessTokenManager.getInstance().loadCurrentAccessToken();
        Profile.fetchProfileForCurrentAccessToken();
    }

    public void logout() {
        WatchTimeAccessTokenManager.getInstance().setCurrentAccessToken(null);
        WatchTimeProfileManager.getInstance().setCurrentProfile(null);

        WTFirebaseTokenRefresh.startActionRefreshFirebase(WatchTimeApplication.getAppContext());

        ((WatchTimeApplication)WatchTimeApplication.getAppContext()).getDataChangeHandler().igniteListeners("LoginManagerWT", OnDataChangeHandler.LOGOUT);
    }
}
