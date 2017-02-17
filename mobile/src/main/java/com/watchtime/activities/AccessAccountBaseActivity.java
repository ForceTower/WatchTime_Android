package com.watchtime.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.transition.TransitionInflater;

import com.facebook.CallbackManager;

import com.watchtime.R;
import com.watchtime.activities.base.WatchTimeBaseActivity;
import com.watchtime.base.utils.VersionUtils;
import com.watchtime.fragments.account.AccessAccountFragment;

public class AccessAccountBaseActivity extends WatchTimeBaseActivity {
    CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_access_account_base);
        callbackManager = CallbackManager.Factory.create();

        if (VersionUtils.isLollipop()) {
            getWindow().setExitTransition(TransitionInflater.from(this).inflateTransition(android.R.transition.fade));
            getWindow().setEnterTransition(TransitionInflater.from(this).inflateTransition(android.R.transition.fade));
        }

        if (savedInstanceState == null) {
            AccessAccountFragment accessAccountFragment = new AccessAccountFragment();
            FragmentManager fragmentManager = getSupportFragmentManager();

            fragmentManager.beginTransaction()
                    .replace(R.id.container, accessAccountFragment, AccessAccountFragment.TAG)
                    .commit();
        }
    }
}
