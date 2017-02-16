package com.watchtime.fragments.account;

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginBehavior;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.watchtime.R;
import com.watchtime.activities.SignUpActivity;
import com.watchtime.base.utils.PrefUtils;

import butterknife.Bind;

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

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message message) {
            Toast.makeText(getContext(), message.obj.toString(), Toast.LENGTH_LONG).show();
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
        fragmentManager.beginTransaction()
                .replace(R.id.container, loginFragment, LoginFragment.TAG)
                .addSharedElement(imageLogo, imageTransitionName)
                .addToBackStack(TAG)
                .commit();
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
        fragmentManager.beginTransaction()
                .replace(R.id.container, signUp, SignUpFragment.TAG)
                .addSharedElement(imageLogo, imageTransitionName)
                .addToBackStack(TAG)
                .commit();
    }

    public void onLoginSuccess() {
        Message completeMessage = mHandler.obtainMessage(0, getString(R.string.logged_in));
        completeMessage.sendToTarget();
        if (loginListener != null) {
           loginListener.onLogin();
        }

        getActivity().finish();
    }

    public void onLoginFailed(String reason) {
        Message completeMessage = mHandler.obtainMessage(0, reason);
        completeMessage.sendToTarget();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
