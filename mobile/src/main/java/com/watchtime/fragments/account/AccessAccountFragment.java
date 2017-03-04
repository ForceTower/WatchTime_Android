package com.watchtime.fragments.account;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.app.FragmentManager;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginBehavior;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.watchtime.R;
import com.watchtime.activities.AccessAccountBaseActivity;
import com.watchtime.base.ApiEndPoints;
import com.watchtime.base.WatchTimeApplication;
import com.watchtime.base.backend.token.TokenAPI;
import com.watchtime.base.utils.PrefUtils;
import com.watchtime.base.utils.VersionUtils;

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

public class AccessAccountFragment extends Fragment {
    public interface OnLoginListener {
        void onLogin();
        void onLogout();
    }
    public static OnLoginListener loginListener;

    public static final String TAG = "AccessAccountFragment";
    private ImageView imageLogo;

    private LoginButton facebookLogin;
    private Button loginBtn;
    private Button signUpBtn;
    private CallbackManager callbackManager;
    ProgressDialog progressDialog;

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message message) {
            if (message.what != 2)
                if (progressDialog != null) progressDialog.dismiss();


            if (getActivity() != null)
                Toast.makeText(getActivity(), message.obj.toString(), Toast.LENGTH_SHORT).show();
            else
                Log.d(TAG, "Delayed message: " + message.obj.toString());
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceBundle) {
        View view = inflater.inflate(R.layout.activity_access_account_fragment, container, false);

        imageLogo = (ImageView) view.findViewById(R.id.app_logo_minor);
        facebookLogin = (LoginButton) view.findViewById(R.id.btn_facebook_login);
        loginBtn = (Button) view.findViewById(R.id.btn_login);
        signUpBtn = (Button) view.findViewById(R.id.btn_sign_up);

        setupTransitionNames();
        registerFacebookButton();
        registerClickListeners();

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public void registerFacebookButton() {
        facebookLogin.setLoginBehavior(LoginBehavior.NATIVE_WITH_FALLBACK);
        facebookLogin.setReadPermissions("email");
        facebookLogin.setFragment(this);

        callbackManager = CallbackManager.Factory.create();

        facebookLogin.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                AccessToken facebookToken = loginResult.getAccessToken();
                PrefUtils.save(getActivity().getApplicationContext(), "fb_session", true);
                PrefUtils.save(getActivity().getApplicationContext(), "fb_tk", facebookToken.getToken());

                String id = facebookToken.getUserId();
                String token = facebookToken.getToken();

                progressDialog = new ProgressDialog(getActivity(), R.style.Theme_WatchTime_Dark_ProgressBar);
                progressDialog.setIndeterminate(true);
                progressDialog.setMessage(getString(R.string.validating_credentials));
                progressDialog.show();

                createServerLoginRequest(id, token);
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
    }

    public void createServerLoginRequest(final String id, final String token) {
        RequestBody requestBody = new FormBody.Builder()
                .add("facebook_id", id)
                .add("facebook_token", token)
                .build();

        Request request = new Request.Builder()
                .url(ApiEndPoints.FACEBOOK_LOGIN_REGISTER)
                .post(requestBody)
                .build();

        Call facebookCall = new OkHttpClient().newCall(request);
        facebookCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("AccessAccountFacebook", "Failed: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    onLoginFailed("Unsuccessful response");
                    return;
                }

                try {
                    JSONObject loginObject = new JSONObject(response.body().string());
                    Log.d(TAG, loginObject.toString());
                    if (loginObject.has("error")) {
                        onLoginFailed(loginObject.getString("error_description"));
                    } else if (loginObject.has("not_registered")){
                        Message completeMessage = mHandler.obtainMessage(0, getString(R.string.account_created));
                        completeMessage.sendToTarget();

                        loginObject = loginObject.getJSONObject("data");
                        ((WatchTimeApplication)getActivity().getApplication()).userFromJSON(loginObject);
                        obtainToken(loginObject.optString("email", ""), id, token);
                    } else {
                        loginObject = loginObject.getJSONObject("data");
                        ((WatchTimeApplication)getActivity().getApplication()).userFromJSON(loginObject);
                        obtainToken(loginObject.optString("email", ""), id, token);
                    }
                } catch (JSONException e) {
                    onLoginFailed(e.getMessage());
                }
            }
        });
    }

    public void obtainToken(final String email, String id, String token) {
        RequestBody requestBody = new FormBody.Builder()
                .add("email", email)
                .add("facebook_id", id)
                .add("facebook_token", token)
                .add("grant_type", "no_password")
                .add("client_id", ApiEndPoints.CLIENT_ID)
                .add("client_secret", ApiEndPoints.CLIENT_SECRET)
                .build();

        Request request = new Request.Builder()
                .url(ApiEndPoints.OAUTH2_BASE)
                .post(requestBody)
                .build();

        Call tokenCall = new OkHttpClient().newCall(request);
        tokenCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                onLoginFailed(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    onLoginFailed("Unsuccessful response");
                    return;
                }
                JSONObject token;
                try {
                    token = new JSONObject(response.body().string());
                    TokenAPI api = WatchTimeApplication.tokenFromJSON(token);
                    onLoginSuccess(email, api.getAccessToken());
                } catch (JSONException e) {
                    onLoginFailed(e.getMessage());
                }

            }
        });
    }

    public void registerClickListeners() {
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginFragmentStart(view);
            }
        });
        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signUpFragmentStart(view);
            }
        });
    }

    public void setupTransitionNames() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            imageLogo.setTransitionName(getString(R.string.image_logo_transition_login));
        }
    }

    public void loginFragmentStart(View v) {
        LoginFragment loginFragment = new LoginFragment();

        String imageTransitionName = "";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setSharedElementReturnTransition(TransitionInflater.from(getActivity()).inflateTransition(R.transition.change_image_trans));
            setExitTransition(TransitionInflater.from(getActivity()).inflateTransition(android.R.transition.fade));

            loginFragment.setSharedElementEnterTransition(TransitionInflater.from(getActivity()).inflateTransition(R.transition.change_image_trans));
            loginFragment.setEnterTransition(TransitionInflater.from(getActivity()).inflateTransition(android.R.transition.fade));

            imageTransitionName = imageLogo.getTransitionName();
        }

        Bundle bundle = new Bundle();
        bundle.putString("TRANS_NAME", imageTransitionName);
        loginFragment.setArguments(bundle);

        FragmentManager fragmentManager = getFragmentManager();
        if (VersionUtils.isLollipop())
            fragmentManager.beginTransaction().replace(R.id.container, loginFragment, LoginFragment.TAG).addSharedElement(imageLogo, imageTransitionName).addToBackStack(TAG).commit();
        else
            fragmentManager.beginTransaction().replace(R.id.container, loginFragment, LoginFragment.TAG).addToBackStack(TAG).commit();
    }

    public void signUpFragmentStart(View v) {
        SignUpFragment signUp = new SignUpFragment();
        String imageTransitionName = "";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setSharedElementReturnTransition(TransitionInflater.from(getActivity()).inflateTransition(R.transition.change_image_trans));
            setExitTransition(TransitionInflater.from(getActivity()).inflateTransition(android.R.transition.fade));

            signUp.setSharedElementEnterTransition(TransitionInflater.from(getActivity()).inflateTransition(R.transition.change_image_trans));
            signUp.setEnterTransition(TransitionInflater.from(getActivity()).inflateTransition(android.R.transition.fade));

            imageTransitionName = imageLogo.getTransitionName();
        }

        Bundle bundle = new Bundle();
        bundle.putString("TRANS_NAME", imageTransitionName);
        signUp.setArguments(bundle);

        FragmentManager fragmentManager = getFragmentManager();
        if (VersionUtils.isLollipop())
            fragmentManager.beginTransaction().replace(R.id.container, signUp, SignUpFragment.TAG).addToBackStack(TAG).addSharedElement(imageLogo, imageTransitionName).commit();
        else
            fragmentManager.beginTransaction().replace(R.id.container, signUp, SignUpFragment.TAG).addToBackStack(TAG).commit();
    }

    public void onLoginSuccess(String email, String token) {
        Message completeMessage = mHandler.obtainMessage(0, getString(R.string.logged_in));
        completeMessage.sendToTarget();
        if (loginListener != null) {
           loginListener.onLogin();
        }

        ((AccessAccountBaseActivity)getActivity()).facebookLoginToken(email, token);
    }

    public void onLoginFailed(String reason) {
        Message completeMessage = mHandler.obtainMessage(1, reason);
        completeMessage.sendToTarget();
        ((WatchTimeApplication)getActivity().getApplication()).setUser(null);

        LoginManager.getInstance().logOut();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
