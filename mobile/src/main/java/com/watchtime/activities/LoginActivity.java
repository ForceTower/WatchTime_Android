package com.watchtime.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginBehavior;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.watchtime.R;
import com.watchtime.activities.base.WatchTimeBaseAuthenticatorActivity;
import com.watchtime.base.utils.PrefUtils;

import java.util.Arrays;

import butterknife.Bind;

public class LoginActivity extends WatchTimeBaseAuthenticatorActivity {
    @Bind(R.id.background_image)
    ImageView background;
    @Bind(R.id.center_text_0)
    TextView centerText;
    @Bind(R.id.btn_google_login)
    Button googleLogin;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_login);
        setupViews();
    }

    private void setupViews() {
        //setupFacebookButton();
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
                return;
            }

            showProgressDialog(R.string.validating_credentials);
            createServerLoginRequest(acct.getId(), acct.getIdToken(), 1);
        } else {
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

}
