package com.watchtime.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.devspark.robototextview.widget.RobotoTextView;
import com.facebook.AccessToken;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginBehavior;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.watchtime.R;
import com.watchtime.activities.base.WatchTimeBaseAuthenticatorActivity;
import com.watchtime.base.utils.AnimUtils;
import com.watchtime.base.utils.PrefUtils;
import com.watchtime.sdk.WatchTimeBaseMethods;

import java.util.Arrays;

import butterknife.Bind;

public class LoginActivity extends WatchTimeBaseAuthenticatorActivity {
    @Bind(R.id.background_image)
    ImageView background;
    @Bind(R.id.center_text_0)
    RobotoTextView centerText0;
    @Bind(R.id.center_text_1)
    RobotoTextView centerText1;
    @Bind(R.id.center_text_2)
    RobotoTextView centerText2;

    private ProgressDialog progressDialog;
    private int currentIndex = 1;
    private int backgroundIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_login);

        setupViews();
    }

    private void setupViews() {
        mHandler.postDelayed(updater, 5000);
    }

    public void facebookLogin(View view) {
        LoginManager.getInstance().setLoginBehavior(LoginBehavior.NATIVE_WITH_FALLBACK);
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email", "user_friends", "user_actions.video"));
        LoginManager.getInstance().registerCallback(facebookCallback, callbackLoginResult);
    }

    public void googleLogin(View view) {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(signInIntent, GOOGLE_SIGN_IN);
    }

    public void handleGoogleSignIn(GoogleSignInResult result) {
        if (result.isSuccess()) {
            GoogleSignInAccount acct = result.getSignInAccount();
            if (acct == null) {
                onLoginFailed(R.string.login_failed);
                Log.i("LoginAct", "acc is null");
                return;
            }

            showProgressDialog(R.string.validating_credentials);
            createServerLoginRequest(acct.getId(), acct.getIdToken(), 1);
        } else {

            Log.i("LoginAct", "Unsuccessful. Status: " + result.getStatus().getStatusCode() + ". " + result.toString());
            onLoginFailed(R.string.login_failed);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GOOGLE_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleGoogleSignIn(result);
        }

        else facebookCallback.onActivityResult(requestCode, resultCode, data);
    }

    private void nextElement() {
        if (isDestroyed())
            return;

        mHandler.postDelayed(updater, 5000);
        switch (currentIndex) {
            case 0:
                AnimUtils.fadeIn(centerText0);
                AnimUtils.fadeOut(centerText1);
                AnimUtils.fadeOut(centerText2);
                break;
            case 1:
                AnimUtils.fadeOut(centerText0);
                AnimUtils.fadeIn(centerText1);
                AnimUtils.fadeOut(centerText2);
                break;
            case 2:
                AnimUtils.fadeOut(centerText0);
                AnimUtils.fadeOut(centerText1);
                AnimUtils.fadeIn(centerText2);
                break;
        }

        changeBackground();

        currentIndex++;
        if (currentIndex > 2)
            currentIndex = 0;
    }

    private void changeBackground() {
        if (backgroundIndex >= WatchTimeBaseMethods.getInstance().backgroundsLimit())
            backgroundIndex = 0;

        String url = WatchTimeBaseMethods.getInstance().getBackground(backgroundIndex);
        if (url == null) return;

        Picasso.with(this).load(url).into(background, new Callback() {
            @Override
            public void onSuccess() {
                background.setVisibility(View.INVISIBLE);
                AnimUtils.fadeIn(background);
            }

            @Override
            public void onError() {}
        });

        backgroundIndex++;
    }

    @Override protected void showProgressDialog(@StringRes int idMessage) {
        progressDialog = new ProgressDialog(this, R.style.Theme_WatchTime_Dark_ProgressBar);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(getString(idMessage));
        progressDialog.show();
    }

    @Override protected void dismissProgressDialog() {
        if (progressDialog != null) progressDialog.dismiss();
    }

    private FacebookCallback<LoginResult> callbackLoginResult = new FacebookCallback<LoginResult>() {
        @Override
        public void onSuccess(LoginResult loginResult) {
            AccessToken facebookToken = loginResult.getAccessToken();
            PrefUtils.save(getApplicationContext(), "fb_session", true);
            PrefUtils.save(getApplicationContext(), "fb_tk", facebookToken.getToken());

            String id = facebookToken.getUserId();
            String token = facebookToken.getToken();

            showProgressDialog(R.string.validating_credentials);
            createServerLoginRequest(id, token, 0);
        }

        @Override
        public void onCancel() {
            onLoginFailed(R.string.com_facebook_login_canceled);
        }

        @Override
        public void onError(FacebookException error) {
            onLoginFailed(R.string.error_getting_response);
        }
    };

    private Runnable updater = new Runnable() {
        @Override
        public void run() {
            nextElement();
        }
    };
}
