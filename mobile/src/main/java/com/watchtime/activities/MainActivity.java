package com.watchtime.activities;

import android.os.Bundle;

import com.watchtime.R;
import com.watchtime.activities.base.WatchTimeBaseActivity;

public class MainActivity extends WatchTimeBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
