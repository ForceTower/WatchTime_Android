package com.watchtime.account_manager;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.watchtime.activities.AccessAccountBaseActivity;
import com.watchtime.base.Constants;
import com.watchtime.R;

public class AccountAuthenticator extends AbstractAccountAuthenticator{
    private Context mContext;
    private int ERROR_CODE_ONE_ACCOUNT_ALLOWED = 0;

    public AccountAuthenticator(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
        return null;
    }

    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures, Bundle options) throws NetworkErrorException {
        Log.i("AccMgr - AccountAuthor", "addAccount");

        boolean accountExists = AccountManager.get(mContext).getAccountsByType(accountType).length != 0;
        if (accountExists) {
            final Bundle result = new Bundle();
            result.putInt(AccountManager.KEY_ERROR_CODE, ERROR_CODE_ONE_ACCOUNT_ALLOWED);
            result.putString(AccountManager.KEY_ERROR_MESSAGE, mContext.getString(R.string.one_account_allowed));

            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mContext, R.string.one_account_allowed, Toast.LENGTH_SHORT).show();
                }
            });
            return result;
        }

        Intent intent = new Intent(mContext, AccessAccountBaseActivity.class);
        intent.putExtra(Constants.ARG_ACCOUNT_TYPE, accountType);
        intent.putExtra(Constants.ARG_AUTH_TYPE, authTokenType != null ? authTokenType : Constants.ACCOUNT_TOKEN_TYPE);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);

        Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return bundle;
    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account, Bundle options) throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
        Log.i("AccMgr - AccountAuthor", "getAuthToken");
        AccountManager accountManager = AccountManager.get(mContext);
        String token = accountManager.peekAuthToken(account, authTokenType);

        if (!TextUtils.isEmpty(token)) {
            Bundle bundle = new Bundle();
            bundle.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
            bundle.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
            bundle.putString(AccountManager.KEY_AUTHTOKEN, token);

            return bundle;
        }

        Intent intent = new Intent(mContext, AccessAccountBaseActivity.class);
        intent.putExtra(Constants.ARG_ACCOUNT_NAME, account.name);
        intent.putExtra(Constants.ARG_ACCOUNT_TYPE, account.type);
        intent.putExtra(Constants.ARG_AUTH_TYPE, authTokenType);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);

        Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return bundle;
    }

    @Override
    public String getAuthTokenLabel(String authTokenType) {
        if (authTokenType.equals(Constants.ACCOUNT_TOKEN_TYPE)) {
            return mContext.getString(R.string.default_token_type);
        }
        return null;
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
        Intent intent = new Intent(mContext, AccessAccountBaseActivity.class);//TODO create change password activity
        intent.putExtra(Constants.ARG_ACCOUNT_NAME, account.name);
        intent.putExtra(Constants.ARG_ACCOUNT_TYPE, account.type);
        intent.putExtra(Constants.ARG_AUTH_TYPE, authTokenType);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);

        Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return bundle;
    }

    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account, String[] features) throws NetworkErrorException {
        return null;
    }
}
