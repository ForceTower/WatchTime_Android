package com.watchtime.activities.base;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.iid.FirebaseInstanceId;
import com.watchtime.R;
import com.watchtime.account_manager.MyAccountAuthenticatorActivity;
import com.watchtime.base.ApiEndPoints;
import com.watchtime.base.Constants;
import com.watchtime.base.WatchTimeApplication;
import com.watchtime.base.backend.User;
import com.watchtime.base.content.preferences.Prefs;
import com.watchtime.base.interfaces.OnDataChangeHandler;
import com.watchtime.base.utils.LocaleUtils;
import com.watchtime.base.utils.PrefUtils;
import com.watchtime.sdk.AccessTokenWT;
import com.watchtime.sdk.LoginManagerWT;
import com.watchtime.sdk.WatchTimeBaseMethods;
import com.watchtime.sdk.WatchTimeSdk;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public abstract class WatchTimeBaseAuthenticatorActivity extends MyAccountAuthenticatorActivity {
    //Constants used to create request to the server
    public static final int FACEBOOK_LOGIN = 0;
    public static final int GOOGLE_LOGIN = 1;

    //Activity Request Codes
    protected static final int GOOGLE_SIGN_IN = 400;

    //TAG for logs
    private static final String TAG = "AccountAuthActivity";
    //Handler for Messages
    protected Handler mHandler;
    //Facebook CallbackManager for facebook login
    protected CallbackManager facebookCallback;
    //Google API Client for google login
    protected GoogleApiClient googleApiClient;
    //Account Manager for storing accounts
    protected AccountManager accountManager;
    //User referenced in WatchTimeApplication
    protected User user;

    //Connection failed Listener for GoogleAPI
    protected GoogleApiClient.OnConnectionFailedListener connectionFailedListener = new GoogleApiClient.OnConnectionFailedListener() {
        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            Log.i(TAG, "Connection failed");
        }
    };

    //Abstract Methods
    protected abstract void showProgressDialog(@StringRes int idMessage);
    protected abstract void dismissProgressDialog();

    //Activity's Life Cycle
    protected void onCreate(Bundle savedInstanceState, int layoutId) {
        String language = PrefUtils.get(this, Prefs.LOCALE, WatchTimeApplication.getSystemLanguage());
        LocaleUtils.setCurrent(this, LocaleUtils.toLocale(language));

        FacebookSdk.sdkInitialize(getApplicationContext());
        WatchTimeSdk.initializeSdk(getApplicationContext(), null);

        super.onCreate(savedInstanceState);

        facebookCallback = CallbackManager.Factory.create();
        setupAccountManagerCode();
        setupGoogleAPIClient();

        setContentView(layoutId);

        ButterKnife.bind(this);
        mHandler = new Handler(getMainLooper()) {
            @Override
            public void handleMessage(Message message) {
                if (message.what != 2) dismissProgressDialog();
                Toast.makeText(getApplication(), message.obj.toString(), Toast.LENGTH_SHORT).show();
            }
        };
    }

    @Override
    public void setContentView(int layoutResID) {
        String language = PrefUtils.get(this, Prefs.LOCALE, WatchTimeApplication.getSystemLanguage());
        LocaleUtils.setCurrent(this, LocaleUtils.toLocale(language));
        super.setContentView(layoutResID);
    }

    @Override
    protected void onResume() {
        String language = PrefUtils.get(this, Prefs.LOCALE, WatchTimeApplication.getSystemLanguage());
        LocaleUtils.setCurrent(this, LocaleUtils.toLocale(language));
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onHomePressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void onHomePressed() {
        Intent upIntent = NavUtils.getParentActivityIntent(this);
        if (upIntent != null && NavUtils.shouldUpRecreateTask(this, upIntent)) {
            TaskStackBuilder.create(this)
                    .addNextIntentWithParentStack(upIntent)
                    .startActivities();
        } else {
            finish();
        }
    }

    //Login Success/Fail Methods
    protected void onLoginSuccess(String email, AccessTokenWT token) {
        Message completeMessage = mHandler.obtainMessage(0, getString(R.string.logged_in));
        completeMessage.sendToTarget();

        getApp().getDataChangeHandler().igniteListeners(OnDataChangeHandler.LOGIN);
        createLoginToken(email, token);
    }

    protected void onLoginFailed(@StringRes int resId) {
        onLoginFailed(getString(resId));
    }

    protected void onLoginFailed(String reason) {
        Message completeMessage = mHandler.obtainMessage(1, reason);
        completeMessage.sendToTarget();
        getApp().setUser(null);

        LoginManager.getInstance().logOut();
        LoginManagerWT.getInstance().logout();
        //googleSignOut();
    }

    //Google Methods
    protected void setupGoogleAPIClient() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken("119095877396-gdedk829shkbgpd7aoq36g4bs1fk50t7.apps.googleusercontent.com")
                .build();

        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, connectionFailedListener)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    //General Login Methods
    protected void createLoginToken(final String email, final AccessTokenWT token) {
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

    public void createServerLoginRequest(final String id, final String token, final int type) {
        FormBody.Builder formBody = new FormBody.Builder();
        if (type == FACEBOOK_LOGIN) {
            formBody.add("facebook_id", id);
            formBody.add("facebook_token", token);
        } else if (type == GOOGLE_LOGIN) {
            formBody.add("google_id", id);
            formBody.add("google_token", token);
        }

        RequestBody requestBody = formBody.build();

        Request.Builder requestBuilder = new Request.Builder()
                .post(requestBody);

        if (type == 0)
            requestBuilder.url(ApiEndPoints.FACEBOOK_LOGIN_REGISTER);
        else if(type == 1)
            requestBuilder.url(ApiEndPoints.GOOGLE_LOGIN_REGISTER);


        Request request = requestBuilder.build();

        Call loginSocialCall = new OkHttpClient().newCall(request);
        loginSocialCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("AccessAccount", "Failed: " + e.getMessage());
                LoginManager.getInstance().logOut();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    onLoginFailed(R.string.unsuccessful_response);
                    LoginManager.getInstance().logOut();
                    return;
                }

                try {
                    JSONObject loginObject = new JSONObject(response.body().string());
                    Log.d(TAG, loginObject.toString());
                    if (loginObject.has("error")) {
                        onLoginFailed(R.string.unknown_error);
                        Log.i(TAG, loginObject.getString("error_description"));
                    } else if (loginObject.has("not_registered")){
                        Message completeMessage = mHandler.obtainMessage(0, getString(R.string.account_created));
                        completeMessage.sendToTarget();

                        loginObject = loginObject.getJSONObject("data");
                        getApp().userFromJSON(loginObject);
                        obtainToken(loginObject.optString("email", ""), id, token, type);
                    } else {
                        loginObject = loginObject.getJSONObject("data");
                        getApp().userFromJSON(loginObject);
                        obtainToken(loginObject.optString("email", ""), id, token, type);
                    }
                } catch (JSONException e) {
                    onLoginFailed(R.string.unknown_error);
                    Log.i(TAG, e.getMessage());
                }
            }
        });
    }

    protected void obtainToken(final String email, String id, String token, int type) {
        Log.i(TAG, "obtainToken(" + email + ", " + id + ", " + token + ", " + type + ")");
        FormBody.Builder formBody = new FormBody.Builder()
                .add("email", email)
                .add("client_id", ApiEndPoints.CLIENT_ID)
                .add("client_secret", ApiEndPoints.CLIENT_SECRET);

        if (type == FACEBOOK_LOGIN) {
            formBody.add("facebook_id", id);
            formBody.add("facebook_token", token);
            formBody.add("grant_type", "no_password");
        } else if (type == GOOGLE_LOGIN){
            formBody.add("google_id", id);
            formBody.add("google_token", token);
            formBody.add("grant_type", "no_password_google");
        }

        RequestBody requestBody = formBody.build();

        Request request = new Request.Builder()
                .url(ApiEndPoints.OAUTH2_BASE)
                .post(requestBody)
                .build();

        Call tokenCall = new OkHttpClient().newCall(request);
        tokenCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                LoginManager.getInstance().logOut();
                onLoginFailed(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.i(TAG, response.body().string());
                    onLoginFailed("Unsuccessful response");
                    return;
                }
                JSONObject token;
                try {
                    token = new JSONObject(response.body().string());
                    AccessTokenWT accessToken = AccessTokenWT.createFromJSON(token);
                    onLoginSuccess(email, accessToken);
                } catch (JSONException e) {
                    LoginManager.getInstance().logOut();
                    onLoginFailed(e.getMessage());
                }

            }
        });
    }

    //Account Manager Methods
    protected void setupAccountManagerCode() {
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

    public void finish(Intent intent, String refresh_token) {
        String accountName = intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
        String accountType = intent.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE);
        String token       = intent.getStringExtra(AccountManager.KEY_AUTHTOKEN);

        Account account = new Account(accountName, accountType);
        accountManager.addAccountExplicitly(account, null, null);
        accountManager.setAuthToken(account, user.getAuthTokenType(), token);
        accountManager.setUserData(account, "refresh_token", refresh_token);

        LoginManagerWT.getInstance().onLogin();
        getApp().getDataChangeHandler().igniteListeners(OnDataChangeHandler.LOGIN);
        WatchTimeBaseMethods.getInstance().setFirebaseToken(FirebaseInstanceId.getInstance().getToken());

        setAccountAuthenticatorResult(intent.getExtras());
        finish();
    }

    //Utility Methods
    protected WatchTimeApplication getApp() {
        return (WatchTimeApplication) getApplication();
    }
}
