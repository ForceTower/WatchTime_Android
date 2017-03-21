package com.watchtime.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import com.watchtime.R;
import com.watchtime.activities.base.WatchTimeBaseActivity;
import com.watchtime.base.utils.PrefUtils;
import com.watchtime.utils.ToolbarUtils;

import butterknife.Bind;

public class TermsActivity extends WatchTimeBaseActivity {
    public static String TERMS_ACCEPTED = "terms_accepted";

    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.accept_terms)
    Button acceptTerms;
    @Bind(R.id.reject_terms)
    Button rejectTerms;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_terms);
        setSupportActionBar(toolbar);

        acceptTerms.setBackgroundResource(R.color.primary_green);
        rejectTerms.setBackgroundResource(R.color.red);
        ToolbarUtils.updateToolbarHeight(this, toolbar);
    }

    public void acceptClick(View v) {
        PrefUtils.save(this, TERMS_ACCEPTED, true);
        Intent overviewIntent = new Intent(this, MainActivity.class);
        startActivity(overviewIntent);
        finish();
    }

    public void leaveClick(View v) {
        finish();
    }

}
