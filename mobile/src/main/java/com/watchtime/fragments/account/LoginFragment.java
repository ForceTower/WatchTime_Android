package com.watchtime.fragments.account;

import android.app.ProgressDialog;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.watchtime.R;
import com.watchtime.activities.AccessAccountBaseActivity;
import com.watchtime.base.ApiEndPoints;
import com.watchtime.base.WatchTimeApplication;
import com.watchtime.base.interfaces.OnDataChangeHandler;
import com.watchtime.sdk.AccessTokenWT;
import com.watchtime.sdk.LoginManagerWT;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class LoginFragment extends Fragment {
    public static final String TAG = "LoginFragment";

    private EditText emailText;
    private EditText passwordText;
    private Button loginBtn;
    private ImageView loginImage;

    ProgressDialog progressDialog;
    Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message message) {
            Toast.makeText(getActivity(), message.obj.toString(), Toast.LENGTH_LONG).show();
            if (progressDialog != null) progressDialog.dismiss();
        }
    };

    public LoginFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        String transitionName = "";

        if (bundle != null) {
            transitionName = bundle.getString("TRANS_NAME");
        }

        View view = inflater.inflate(R.layout.fragment_login, container, false);

        loginImage = (ImageView) view.findViewById(R.id.login_image_view);
        emailText = (EditText) view.findViewById(R.id.input_email);
        passwordText = (EditText) view.findViewById(R.id.input_password);
        loginBtn = (Button) view.findViewById(R.id.btn_login);
        loginImage = (ImageView) view.findViewById(R.id.login_image_view);


        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            loginImage.setTransitionName(transitionName);
        }

        loginImage.setImageResource(R.drawable.app_logo_writen);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loginBtn.setBackgroundResource(R.color.primary_dark);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });
    }

    public void login() {
        if (!validate())
            return;

        final String email = emailText.getText().toString();
        final String password = passwordText.getText().toString();

        progressDialog = new ProgressDialog(getActivity(), R.style.Theme_WatchTime_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(getString(R.string.connecting));
        progressDialog.show();

        performLoginCall(email, password);
    }

    public void performLoginCall(final String email, final String password) {
        RequestBody requestBody = new FormBody.Builder()
                .add("grant_type", "password")
                .add("username", email)
                .add("password", password)
                .add("client_id", ApiEndPoints.CLIENT_ID)
                .add("client_secret", ApiEndPoints.CLIENT_SECRET)
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
                if (!response.isSuccessful()) {
                    onLoginFailed(getString(R.string.failed_to_login));
                } else {
                    JSONObject token;
                    try {
                        token = new JSONObject(response.body().string());
                        AccessTokenWT accessToken = AccessTokenWT.createFromJSON(token);
                        onLoginSuccess(email, accessToken);
                    } catch (JSONException e) {
                        onLoginFailed(getString(R.string.error_on_response));
                        Log.e("LoginActivity:Response", e.getMessage());
                    }
                }
            }
        });
    }

    public boolean validate() {
        boolean valid = true;

        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailText.setError(getString(R.string.invalid_email));
            valid = false;
        } else {
            emailText.setError(null);
        }

        if (password.length() < 4 || password.length() > 16) {
            passwordText.setError(getString(R.string.invalid_password));
            valid = false;
        } else {
            passwordText.setError(null);
        }

        return valid;
    }

    public void onLoginSuccess(String email, AccessTokenWT token) {
        Message completeMessage = mHandler.obtainMessage(0, getString(R.string.logged_in));
        completeMessage.sendToTarget();

        ((WatchTimeApplication)getActivity().getApplication()).getDataChangeHandler().igniteListeners(OnDataChangeHandler.LOGIN);
        ((AccessAccountBaseActivity)getActivity()).createLoginToken(email, token);
    }

    public void onLoginFailed(String reason) {
        Message completeMessage = mHandler.obtainMessage(0, reason);
        completeMessage.sendToTarget();

        ((WatchTimeApplication)getActivity().getApplication()).setUser(null);

        LoginManager.getInstance().logOut();
        LoginManagerWT.getInstance().logout();
    }
}
