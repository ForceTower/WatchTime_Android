package com.watchtime.fragments.account;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.watchtime.R;
import com.watchtime.activities.AccessAccountBaseActivity;
import com.watchtime.base.ApiEndPoints;
import com.watchtime.base.WatchTimeApplication;
import com.watchtime.base.interfaces.OnDataChangeHandler;
import com.watchtime.base.utils.PrefUtils;
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

import static android.app.Activity.RESULT_OK;

public class AccessAccountFragment extends Fragment implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {
    public static final String TAG = "AccessAccountFragment";
    private static final int REQUEST_RESOLVE_ERROR = 1001;

    private static final int GOOGLE_SIGN_IN = 400;
    private static final String DIALOG_ERROR = "dialog_error";
    private boolean mResolvingError = false;

    private static final String STATE_RESOLVING_ERROR = "resolving_error";

    private GoogleApiClient mGoogleApiClient;

    private ImageView imageLogo;

    private LoginButton facebookLogin;
    private SignInButton googleLogin;

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

        mResolvingError = savedInstanceState != null && savedInstanceState.getBoolean(STATE_RESOLVING_ERROR, false);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceBundle) {
        View view = inflater.inflate(R.layout.activity_access_account_fragment, container, false);

        imageLogo = (ImageView) view.findViewById(R.id.app_logo_minor);
        facebookLogin = (LoginButton) view.findViewById(R.id.btn_facebook_login);
        googleLogin = (SignInButton) view.findViewById(R.id.btn_google_login);

        setupTransitionNames();
        registerFacebookButton();
        setupGoogleSignIn();

        return view;
    }

    private void setupGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken("119095877396-gdedk829shkbgpd7aoq36g4bs1fk50t7.apps.googleusercontent.com")
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        googleLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                googleSignIn();
            }
        });
        googleLogin.setSize(SignInButton.SIZE_WIDE);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_RESOLVING_ERROR, mResolvingError);
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

                createServerLoginRequest(id, token, 0);
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

    public void googleSignIn() {
        progressDialog = new ProgressDialog(getActivity(), R.style.Theme_WatchTime_Dark_ProgressBar);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(getString(R.string.validating_credentials));
        progressDialog.show();

        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, GOOGLE_SIGN_IN);
    }

    public void handleGoogleSignIn(GoogleSignInResult result) {
        if (result.isSuccess()) {
            GoogleSignInAccount acct = result.getSignInAccount();
            if (acct == null) {
                Log.i(TAG, "Null Account");
                return;
            }

            Log.i(TAG, "Google Sign in success");
            Log.i(TAG, "ID: " + acct.getId());
            Log.i(TAG, "Name: " + acct.getDisplayName());
            Log.i(TAG, "Token: " + acct.getIdToken());
            Log.i(TAG, "Email: " + acct.getEmail());

            createServerLoginRequest(acct.getId(), acct.getIdToken(), 1);
        } else {
            Log.i(TAG, "Google sign in failed");
        }
    }

    public void createServerLoginRequest(final String id, final String token, final int type) {
         FormBody.Builder formBody = new FormBody.Builder();
        if (type == 0) {
            formBody.add("facebook_id", id);
            formBody.add("facebook_token", token);
        } else if (type == 1) {
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
                    onLoginFailed("Unsuccessful response");
                    LoginManager.getInstance().logOut();
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
                        obtainToken(loginObject.optString("email", ""), id, token, type);
                    } else {
                        loginObject = loginObject.getJSONObject("data");
                        ((WatchTimeApplication)getActivity().getApplication()).userFromJSON(loginObject);
                        obtainToken(loginObject.optString("email", ""), id, token, type);
                    }
                } catch (JSONException e) {
                    onLoginFailed(e.getMessage());
                }
            }
        });
    }

    public void obtainToken(final String email, String id, String token, int type) {
        Log.i(TAG, "obtainToken(" + email + ", " + id + ", " + token + ", " + type + ")");
        FormBody.Builder formBody = new FormBody.Builder()
                .add("email", email)
                .add("client_id", ApiEndPoints.CLIENT_ID)
                .add("client_secret", ApiEndPoints.CLIENT_SECRET);

        if (type == 0) {
            formBody.add("facebook_id", id);
            formBody.add("facebook_token", token);
            formBody.add("grant_type", "no_password");
        } else if (type == 1){
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

    public void setupTransitionNames() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            imageLogo.setTransitionName(getString(R.string.image_logo_transition_login));
        }
    }

    public void onLoginSuccess(String email, AccessTokenWT token) {
        Message completeMessage = mHandler.obtainMessage(0, getString(R.string.logged_in));
        completeMessage.sendToTarget();

        ((WatchTimeApplication)getActivity().getApplication()).getDataChangeHandler().igniteListeners(OnDataChangeHandler.LOGIN);
        ((AccessAccountBaseActivity)getActivity()).createLoginToken(email, token);
    }

    public void onLoginFailed(String reason) {
        Message completeMessage = mHandler.obtainMessage(1, reason);
        completeMessage.sendToTarget();
        ((WatchTimeApplication)getActivity().getApplication()).setUser(null);

        LoginManager.getInstance().logOut();
        LoginManagerWT.getInstance().logout();
        googleSignOut();
    }

    private void googleSignOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_RESOLVE_ERROR) {
            mResolvingError = false;
            if (resultCode == RESULT_OK) {
                // Make sure the app is not already connected or attempting to connect
                if (!mGoogleApiClient.isConnecting() && !mGoogleApiClient.isConnected()) {
                    mGoogleApiClient.connect();
                }
            }
        } else if (requestCode == GOOGLE_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleGoogleSignIn(result);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        if (mResolvingError) {
            // Already attempting to resolve an error.
            return;
        } else if (result.hasResolution()) {
            try {
                mResolvingError = true;
                result.startResolutionForResult(getActivity(), REQUEST_RESOLVE_ERROR);
            } catch (IntentSender.SendIntentException e) {
                // There was an error with the resolution intent. Try again.
                mGoogleApiClient.connect();
            }
        } else {
            // Show dialog using GoogleApiAvailability.getErrorDialog()
            showErrorDialog(result.getErrorCode());
            mResolvingError = true;
        }
    }

    private void showErrorDialog(int errorCode) {
        // Create a fragment for the error dialog
        ErrorDialogFragment dialogFragment = new ErrorDialogFragment();
        // Pass the error that should be displayed
        Bundle args = new Bundle();
        args.putInt(DIALOG_ERROR, errorCode);
        dialogFragment.setArguments(args);
        dialogFragment.show(getFragmentManager(), "errordialog");
    }

    public void onDialogDismissed() {
        mResolvingError = false;
    }

    public static class ErrorDialogFragment extends DialogFragment {
        public ErrorDialogFragment() { }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Get the error code and retrieve the appropriate dialog
            int errorCode = this.getArguments().getInt(DIALOG_ERROR);
            return GoogleApiAvailability.getInstance().getErrorDialog(
                    this.getActivity(), errorCode, REQUEST_RESOLVE_ERROR);
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            if (getFragmentManager().findFragmentByTag(TAG) != null)
                ((AccessAccountFragment)getFragmentManager().findFragmentByTag(TAG)).onDialogDismissed();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "onConnected()");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "onConnectionSuspended(" + i + ")");
    }
}
