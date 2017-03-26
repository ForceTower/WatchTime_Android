package com.watchtime.activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.facebook.CallbackManager;
import com.facebook.login.LoginManager;
import com.google.firebase.iid.FirebaseInstanceId;
import com.watchtime.R;
import com.watchtime.activities.base.WatchTimeBaseAuthenticatorActivity;
import com.watchtime.base.Constants;
import com.watchtime.base.WatchTimeApplication;
import com.watchtime.base.backend.User;
import com.watchtime.base.interfaces.OnDataChangeHandler;
import com.watchtime.sdk.AccessTokenWT;
import com.watchtime.sdk.LoginManagerWT;
import com.watchtime.sdk.WatchTimeBaseMethods;

public class LoginActivity extends WatchTimeBaseAuthenticatorActivity {
    //Facebook CallbackManager for facebook login
    private CallbackManager facebookCallback;
    //Account Manager for storing accounts
    private AccountManager accountManager;
    //User referenced in WatchTimeApplication
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_login);
        facebookCallback = CallbackManager.Factory.create();

        setupAccountManagerCode();
    }

    private void setupAccountManagerCode() {
        accountManager = AccountManager.get(this);
        user = ((WatchTimeApplication)getApplication()).getUser();

        if (accountManager.getAccountsByType(Constants.ACCOUNT_TYPE).length == 0) {
            LoginManagerWT.getInstance().logout();
            LoginManager.getInstance().logOut();
        }

        String accountType = getIntent().getStringExtra(Constants.ARG_ACCOUNT_TYPE);
        String accountName = getIntent().getStringExtra(Constants.ARG_ACCOUNT_NAME);
        String authTokenType = getIntent().getStringExtra(Constants.ARG_AUTH_TYPE);

        user.setAccountType(accountType);
        user.setAccountName(accountName);
        user.setAuthTokenType(authTokenType);
    }

    public void createLoginToken(final String email, final AccessTokenWT token) {
        new AsyncTask<Void, Void, Intent>() {
            @Override protected Intent doInBackground(Void... params) {
                Intent intent = new Intent();
                intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, email);
                intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, user.getAccountType());
                intent.putExtra(AccountManager.KEY_AUTHTOKEN, token.getAccessToken());
                return intent;
            }

            @Override protected void onPostExecute(Intent intent) {
                finish(intent, token.getRefreshToken());
            }
        }.execute();
    }

    public void finish(Intent intent, String refresh_token) {
        String accountName = intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
        String accountType = intent.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE);
        String token       = intent.getStringExtra(AccountManager.KEY_AUTHTOKEN);

        Account account = new Account(accountName, accountType);
        accountManager.addAccountExplicitly(account, null, null);
        accountManager.setAuthToken(account, user.getAuthTokenType(), token);
        accountManager.setUserData(account, "refresh_token", refresh_token);

        LoginManagerWT.getInstance().onLogin();
        ((WatchTimeApplication)getApplication()).getDataChangeHandler().igniteListeners(OnDataChangeHandler.LOGIN);
        WatchTimeBaseMethods.getInstance().setFirebaseToken(FirebaseInstanceId.getInstance().getToken());

        setAccountAuthenticatorResult(intent.getExtras());
        finish();
    }
}
