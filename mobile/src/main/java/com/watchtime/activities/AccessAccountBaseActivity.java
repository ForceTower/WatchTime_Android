package com.watchtime.activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.FragmentManager;
import android.util.Log;

import com.facebook.CallbackManager;

import com.watchtime.R;
import com.watchtime.activities.base.WatchTimeBaseAuthenticatorActivity;
import com.watchtime.base.Constants;
import com.watchtime.base.WatchTimeApplication;
import com.watchtime.base.backend.User;
import com.watchtime.fragments.account.AccessAccountFragment;
import com.watchtime.sdk.AccessTokenWT;
import com.watchtime.sdk.LoginManagerWT;

public class AccessAccountBaseActivity extends WatchTimeBaseAuthenticatorActivity {
    CallbackManager callbackManager;
    private AccountManager accountManager;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_access_account_base);

        Log.i("AccMgr - AccessAccount", "Create Activity");
        callbackManager = CallbackManager.Factory.create();

        setupAccountManagerCode();

        if (savedInstanceState == null) {
            AccessAccountFragment accessAccountFragment = new AccessAccountFragment();
            FragmentManager fragmentManager = getFragmentManager();

            fragmentManager.beginTransaction()
                    .replace(R.id.container, accessAccountFragment, AccessAccountFragment.TAG)
                    .commit();
        }
    }

    private void setupAccountManagerCode() {
        accountManager = AccountManager.get(this);
        user = ((WatchTimeApplication)getApplication()).getUser();

        String accountType = getIntent().getStringExtra(Constants.ARG_ACCOUNT_TYPE);
        String accountName = getIntent().getStringExtra(Constants.ARG_ACCOUNT_NAME);
        String authTokenType = getIntent().getStringExtra(Constants.ARG_AUTH_TYPE);

        user.setAccountType(accountType);
        user.setAccountName(accountName);
        user.setAuthTokenType(authTokenType);
    }

    public void facebookLoginToken(final String email, final AccessTokenWT token) {
        Log.i("AccMgr - AccessAccount", "facebookLoginToken");

        new AsyncTask<Void, Void, Intent>() {
            @Override
            protected Intent doInBackground(Void... params) {
                Intent intent = new Intent();
                intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, email);
                intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, user.getAccountType());
                intent.putExtra(AccountManager.KEY_AUTHTOKEN, token.getAccessToken());

                return intent;
            }

            @Override
            protected void onPostExecute(Intent intent) {
                finish(intent);
            }
        }.execute();
    }

    public void onPasswordChange(final String token) {
        new AsyncTask<Void, Void, Intent>() {
            @Override
            protected Intent doInBackground(Void... params) {
                Intent intent = new Intent();
                intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, user.getAccountName());
                intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, user.getAccountType());
                intent.putExtra(AccountManager.KEY_AUTHTOKEN, token);
                return intent;
            }

            @Override
            protected void onPostExecute(Intent intent) {
                finishPassword(intent);
            }

        }.execute();

    }

    public void finishPassword(Intent intent) {
        Log.i("AccMgr - AccessAccount", "Finishing Password...");
        String accountName = intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
        String accountType = intent.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE);
        String token       = intent.getStringExtra(AccountManager.KEY_AUTHTOKEN);

        Account account = new Account(accountName, accountType);
        accountManager.invalidateAuthToken(accountType, user.getToken());
        accountManager.setAuthToken(account, user.getAuthTokenType(), token);

        setAccountAuthenticatorResult(intent.getExtras());
        finish();
    }

    public void finish(Intent intent) {
        Log.i("AccMgr - AccessAccount", "Finishing...");
        String accountName = intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
        String accountType = intent.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE);
        String token       = intent.getStringExtra(AccountManager.KEY_AUTHTOKEN);

        Account account = new Account(accountName, accountType);
        accountManager.addAccountExplicitly(account, null, null);
        accountManager.setAuthToken(account, user.getAuthTokenType(), token);

        LoginManagerWT.getInstance().onLogin();

        setAccountAuthenticatorResult(intent.getExtras());
        finish();
    }
}
