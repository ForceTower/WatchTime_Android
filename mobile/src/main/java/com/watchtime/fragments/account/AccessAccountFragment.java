package com.watchtime.fragments.account;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.watchtime.R;
import com.watchtime.fragments.account.LoginFragment;

import butterknife.Bind;

public class AccessAccountFragment extends Fragment {
    @Bind(R.id.login_text)
    TextView loginText;
    @Bind(R.id.register_text)
    TextView registerText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceBundle) {
        return inflater.inflate(R.layout.activity_access_account_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loginText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginFragmentStart(view);
            }
        });

        registerText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signUpFragmentStart(view);
            }
        });
    }

    public void loginFragmentStart(View v) {
        LoginFragment loginFragment = new LoginFragment();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            loginText.setTransitionName(getString(R.string.fragment_login_transition));
            registerText.setTransitionName(getString(R.string.fragment_register_transition));

            setExitTransition(TransitionInflater.from(getActivity()).inflateTransition(android.R.transition.fade));
        }
    }

    public void signUpFragmentStart(View v) {

    }
}
