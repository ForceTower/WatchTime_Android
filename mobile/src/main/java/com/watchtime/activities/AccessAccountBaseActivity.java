package com.watchtime.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;

import com.watchtime.R;
import com.watchtime.activities.base.WatchTimeBaseActivity;
import com.watchtime.fragments.account.AccessAccountFragment;

public class AccessAccountBaseActivity extends WatchTimeBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_access_account_base);

        AccessAccountFragment accessAccountFragment = new AccessAccountFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();

        fragmentManager.beginTransaction()
                .replace(R.id.container, accessAccountFragment)
                .commit();
    }
}
