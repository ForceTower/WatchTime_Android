package com.watchtime.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.watchtime.R;
import com.watchtime.activities.base.WatchTimeBaseActivity;
import com.watchtime.base.ApiEndPoints;
import com.watchtime.base.WatchTimeApplication;
import com.watchtime.base.backend.token.TokenAPI;
import com.watchtime.base.utils.PrefUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import butterknife.Bind;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivityNoDesign extends WatchTimeBaseActivity {
    private static final int SIGNUP = 0;

    public interface OnLoginListener {
        void onLogin();
        void onLogout();
    }

    @Bind(R.id.input_email)
    EditText emailText;
    @Bind(R.id.input_password)
    EditText passwordText;
    @Bind(R.id.btn_login)
    Button loginBtn;
    @Bind(R.id.link_signup)
    TextView signUpText;
    @Bind(R.id.login_image_view)
    ImageView loginImage;
    @Bind(R.id.btn_facebook_login)
    LoginButton facebookLogin;

    public static OnLoginListener loginListener;

    CallbackManager callbackManager;
    ProgressDialog progressDialog;
    Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message message) {
            Toast.makeText(getBaseContext(), message.obj.toString(), Toast.LENGTH_LONG).show();
            if (progressDialog != null) progressDialog.dismiss();

            if (message.what == 1) {
                finish();
            }
        }
    };


    public static Intent startActivity(Activity activity) {
        Intent intent = new Intent(activity, LoginActivityNoDesign.class);
        activity.startActivity(intent);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_login_no_design);
        loginImage.setBackgroundResource(R.drawable.app_logo_writen);

        facebookLogin.setReadPermissions("email");
        callbackManager = CallbackManager.Factory.create();

        facebookLogin.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                AccessToken facebookToken = loginResult.getAccessToken();
                Log.d("WatchTime:FB_TK", facebookToken.getToken());
                PrefUtils.save(getApplicationContext(), "fb_session", true);
                onLoginSuccess();
            }

            @Override
            public void onCancel() {
                onLoginFailed(getString(R.string.com_facebook_login_canceled));
            }

            @Override
            public void onError(FacebookException error) {
                onLoginFailed(error.getMessage());
            }
        });

        loginBtn.setBackgroundResource(R.color.primary_dark);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });

        signUpText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signUp();
            }
        });
    }

    public void login() {
        if (!validate())
            return;

        final String email = emailText.getText().toString();
        final String password = passwordText.getText().toString();

        progressDialog = new ProgressDialog(LoginActivityNoDesign.this, R.style.Theme_WatchTime_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(getString(R.string.connecting));
        progressDialog.show();

        performLoginCall(email, password);
    }

    public void performLoginCall(String email, String password) {
        RequestBody requestBody = new FormBody.Builder()
                .add("grant_type", "password")
                .add("username", email)
                .add("password", password)
                .add("client_id", "appid_1")
                .add("client_secret", "secret")
                .build();

        Request request = new Request.Builder()
                .url(ApiEndPoints.OAUTH2_BASE)
                .post(requestBody)
                .build();

        Call loginCall = new OkHttpClient().newCall(request);
        loginCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("Resp: ", "Failed: " + e.toString());
                onLoginFailed(getString(R.string.error_getting_response));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    JSONObject json = new JSONObject(response.body().string());
                    if (json.has("error")) {
                        String error = json.getString("error_description");
                        onLoginFailed(error);
                    } else {
                        String access_token = json.getString("access_token");
                        String refresh_token = json.getString("refresh_token");
                        String token_type = json.getString("token_type");
                        int expiration = json.getInt("expires_in");

                        WatchTimeApplication.token = new TokenAPI(access_token, refresh_token, token_type, expiration);
                        onLoginSuccess();
                    }
                } catch (JSONException e) {
                    onLoginFailed(getString(R.string.error_on_response));
                    Log.e("LoginActivityNoDesign:Response", e.getMessage());
                }
            }
        });
    }

    public void signUp() {
        Intent intent = new Intent(LoginActivityNoDesign.this, SignUpActivity.class);
        startActivityForResult(intent, SIGNUP);
    }

    public boolean validate() {
        boolean valid = true;

        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailText.setError(getString(R.string.invalid_email));
            valid = false;
        } else {
            emailText.setError(null);
        }

        if (password.isEmpty() || (password.length() < 4 && password.length() > 16)) {
            passwordText.setError(getString(R.string.invalid_password));
            valid = false;
        } else {
            passwordText.setError(null);
        }

        return valid;
    }

    public void onLoginSuccess() {
        Message completeMessage = mHandler.obtainMessage(1, getString(R.string.logged_in));
        completeMessage.sendToTarget();

        if (loginListener != null) {
            loginListener.onLogin();
        }
    }

    public void onLoginFailed(String reason) {
        Message completeMessage = mHandler.obtainMessage(0, reason);
        completeMessage.sendToTarget();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SIGNUP) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(getBaseContext(), getString(R.string.account_created), Toast.LENGTH_LONG).show();
                this.finish();
            }
        }
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    public void onPause() {
        exitActivity();
        super.onPause();
    }

    public void onDestroy() {
        if (loginListener != null) {
            if (AccessToken.getCurrentAccessToken() == null)
                loginListener.onLogout();
            else
                loginListener.onLogin();
        }
        super.onDestroy();
    }


    public void exitActivity() {
        if (loginListener != null) {
            if (AccessToken.getCurrentAccessToken() == null)
                loginListener.onLogout();
            else
                loginListener.onLogin();
        }
    }
}
