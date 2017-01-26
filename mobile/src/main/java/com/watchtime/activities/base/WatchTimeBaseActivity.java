package com.watchtime.activities.base;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.facebook.FacebookSdk;
import com.watchtime.base.WatchTimeApplication;
import com.watchtime.base.content.preferences.Prefs;
import com.watchtime.base.utils.LocaleUtils;
import com.watchtime.base.utils.PrefUtils;

import butterknife.ButterKnife;

/**
 * Created by João Paulo on 23/01/2017.
 */

public abstract class WatchTimeBaseActivity extends AppCompatActivity {
    protected Handler mHandler;

    /**
     * On Creation sets the language and sets a content view
     * @param savedInstanceState Android Bundle
     * @param layoutId R.layout.id to be displayed
     */
    protected void onCreate(Bundle savedInstanceState, int layoutId) {
        //Location Related
        String language = PrefUtils.get(this, Prefs.LOCALE, WatchTimeApplication.getSystemLanguage());
        LocaleUtils.setCurrent(this, LocaleUtils.toLocale(language));

        FacebookSdk.sdkInitialize(getApplicationContext());

        //View Creation
        super.onCreate(savedInstanceState);
        setContentView(layoutId);

        //Binds the ButterKnife here so we can use @Bind on whomever extends this abstract class
        ButterKnife.bind(this);
        mHandler = new Handler(getMainLooper());
    }

    /**
     * Set's the content view. It changes the current screen layout
     * @param layoutResID R.layout.id to display
     */
    @Override
    public void setContentView(int layoutResID) {
        String language = PrefUtils.get(this, Prefs.LOCALE, WatchTimeApplication.getSystemLanguage());
        LocaleUtils.setCurrent(this, LocaleUtils.toLocale(language));
        super.setContentView(layoutResID);
    }

    /**
     * Returns the Application assigned to the app;
     * @return the instance of the application
     */
    protected WatchTimeApplication getApp() {
        return (WatchTimeApplication) getApplication();
    }

    /**
     * On Application resume, check and change the language
     */
    @Override
    protected void onResume() {
        String language = PrefUtils.get(this, Prefs.LOCALE, WatchTimeApplication.getSystemLanguage());
        LocaleUtils.setCurrent(this, LocaleUtils.toLocale(language));
        super.onResume();
    }

    /**
     * Creates a default menu option (android stuff right here)
     * @param menu Menu to be created
     * @return true
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return true;
    }

    /**
     * If the user selects anything from the menu this method is called
     * @param item item selected
     * @return true if selected anything
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onHomePressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * If the user presses back and the Activity Stack is not correctly set, this will create a new Stack of activities.
     * This way, the user will navigate through the app smoothly
     */
    protected void onHomePressed() {
        Intent upIntent = NavUtils.getParentActivityIntent(this);
        if (upIntent != null && NavUtils.shouldUpRecreateTask(this, upIntent)) {
            // This activity is NOT part of this app's task, so create a new task
            // when navigating up, with a synthesized back stack.
            TaskStackBuilder.create(this)
                    // Add all of this activity's parents to the back stack
                    .addNextIntentWithParentStack(upIntent)
                    // Navigate up to the closest parent
                    .startActivities();
        } else {
            finish();
        }
    }
}
