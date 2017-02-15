package com.watchtime.fragments.account;

import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.watchtime.R;

import butterknife.Bind;

public class AccessAccountFragment extends Fragment {
    public static final String TAG = "AcessAccountFragment";
    private TextView loginText;
    private TextView registerText;
    private ImageView imageLogo;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceBundle) {
        View view = inflater.inflate(R.layout.activity_access_account_fragment, container, false);

        imageLogo = (ImageView) view.findViewById(R.id.app_logo_minor);
        imageLogo.setImageResource(R.mipmap.app_logo);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            imageLogo.setTransitionName(getString(R.string.image_logo_transition_login));
        }

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loginText = (TextView)getView().findViewById(R.id.login_text);

        loginText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginFragmentStart(view);
            }
        });

        registerText = (TextView)getView().findViewById(R.id.register_text);
        registerText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signUpFragmentStart(view);
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            loginText.setTransitionName(getString(R.string.fragment_login_transition));
            registerText.setTransitionName(getString(R.string.fragment_register_transition));
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

    }
}
