package com.watchtime.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.transition.TransitionInflater;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.watchtime.R;
import com.watchtime.activities.base.WatchTimeBaseActivity;
import com.watchtime.base.content.preferences.Prefs;
import com.watchtime.base.utils.PrefUtils;
import com.watchtime.base.utils.VersionUtils;
import com.watchtime.fragments.MediaContainerFragment;
import com.watchtime.fragments.NavigationDrawerFragment;
import com.watchtime.fragments.drawer.NavDrawerItem;
import com.watchtime.utils.ToolbarUtils;
import com.watchtime.widget.ScrimInsetsFrameLayout;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import butterknife.Bind;

import static android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS;
import static android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION;

/**
 * Main Application Activity, all starts from here.
 * The fact that this activity extends the base activity, we should keep track of the 2 files.
 * This Activity that houses the navigation drawer, and controls navigation between fragments
 */
public class MainActivity extends WatchTimeBaseActivity implements NavigationDrawerFragment.Callbacks{
    //Variable to keep track of permissions
    private static final int PERMISSIONS_REQUEST = 123;

    //Current fragment being displayed
    private Fragment mCurrentFragment;

    //Toolbar in the view
    @Bind(R.id.toolbar)
    Toolbar mToolbar;

    //Drawer
    @Bind(R.id.navigation_drawer_container)
    ScrimInsetsFrameLayout mNavigationDrawerContainer;

    //Tab layout for a view
    @Nullable
    @Bind(R.id.tabs)
    TabLayout mTabs;

    //Navigation Drawer used when user slides to the right
    NavigationDrawerFragment mNavigationDrawerFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_main);

        //Request Permission to Write And Read From External Memory
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE }, PERMISSIONS_REQUEST);
        }

        if (VersionUtils.isLollipop()) {
            getWindow().setExitTransition(TransitionInflater.from(this).inflateTransition(android.R.transition.fade));
            getWindow().setEnterTransition(TransitionInflater.from(this).inflateTransition(android.R.transition.fade));
        }

        //Supports Action Bar
        setSupportActionBar(mToolbar);
        //Updates the Toolbar Height
        ToolbarUtils.updateToolbarHeight(this, mToolbar);

        //Gets the drawer reference (This is the top most in view group, everything is inside it)
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        //Changes the color of the drawer
        drawerLayout.setStatusBarBackgroundColor(ContextCompat.getColor(this, R.color.primary_dark));

        //Reference to the NavigationDrawerFragment
        mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer_fragment);

        //Initialise the NavigationDrawer, creates content to insert and stuff
        mNavigationDrawerFragment.initialise(mNavigationDrawerContainer, drawerLayout);

        if (null != savedInstanceState) return;

        //Gets the user preference for the start view and shows it
        int providerId = PrefUtils.get(this, Prefs.DEFAULT_VIEW, 1);
        mNavigationDrawerFragment.selectItem(providerId);
    }

    /**
     * On Resume App, checks for the title, and rebuild the Items on NavigationDrawerFragments
     */
    @Override
    protected void onResume() {
        super.onResume();
        //Checks the title
        String title = mNavigationDrawerFragment.getCurrentItem().getTitle();
        //If title is null, updates the title with AppName
        setTitle(null != title ? title : getString(R.string.app_name));
        supportInvalidateOptionsMenu();

        //If an Activity is started, updates the title to Activity's name
        if (mNavigationDrawerFragment.getCurrentItem() != null && mNavigationDrawerFragment.getCurrentItem().getTitle() != null) {
            setTitle(mNavigationDrawerFragment.getCurrentItem().getTitle());
        }

        //Init the items on Drawer
        mNavigationDrawerFragment.initItems();
    }

    /**
     * Creates Menu Options
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.activity_overview, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                return false;
            case R.id.action_search:
                //start the search activity
                //SearchActivity.startActivity(this, mNavigationDrawerFragment.getCurrentItem().getMediaProvider());
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onNavigationDrawerItemSelected(NavDrawerItem item, String title) {
        setTitle(title != null ? title : getString(R.string.app_name));

        FragmentManager fragmentManager = getSupportFragmentManager();

        String tag = title + "_tag";
        mCurrentFragment = fragmentManager.findFragmentByTag(tag);

        if (mCurrentFragment == null) {
            if (item.hasProvider())
                mCurrentFragment = MediaContainerFragment.newInstance(item.getMediaProvider());
            else {
                /*
                if (item.getTag() == NavDrawerItem.ItemTags.PROFILE)
                    mCurrentFragment = new ProfileFragment();
                else if (item.getTag() == NavDrawerItem.ItemTags.USER_LIST)
                    mCurrentFragment = new UserListFragment();
                else if (item.getTag() == NavDrawerItem.ItemTags.USER_ACTIVITY)
                    mCurrentFragment = new UserActivityFragment();
                else
                    mCurrentFragment = new UnknownSelectionFragment();
                    */
            }
        }

        //Selects the first tab on the fragment
        if(mTabs.getTabCount() > 0)
            mTabs.getTabAt(0).select();

        //Exchange Fragments
        Log.d("FragmentChange", "mCurrentFragment " + mCurrentFragment);
        if (mCurrentFragment == null)
            return;

        fragmentManager.beginTransaction().replace(R.id.container, mCurrentFragment, tag).commit();

        //If this new Fragment is a media container, update the tabs
        if(mCurrentFragment instanceof MediaContainerFragment) {
            updateTabs((MediaContainerFragment) mCurrentFragment, ((MediaContainerFragment) mCurrentFragment).getCurrentSelection());
        }
    }

    /**
     * Method called every time a tab is changed
     * @param containerFragment the media container
     * @param position the new position
     */
    public void updateTabs(MediaContainerFragment containerFragment, final int position) {
        if(mTabs == null)
            return;

        if(containerFragment != null) {
            ViewPager viewPager = containerFragment.getViewPager();
            if(viewPager == null)
                return;

            mTabs.setupWithViewPager(viewPager);
            mTabs.setTabGravity(TabLayout.GRAVITY_CENTER);
            mTabs.setTabMode(TabLayout.MODE_SCROLLABLE);
            mTabs.setVisibility(View.VISIBLE);

            viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabs));

            //check if wont be added multiple times
            //mTabs.clearOnTabSelectedListeners();
            mTabs.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager));

            if(mTabs.getTabCount() > 0) {
                mTabs.getTabAt(0).select();
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(mTabs.getTabCount() > position)
                            mTabs.getTabAt(position).select();
                    }
                }, 10);
            }

        } else {
            mTabs.setVisibility(View.GONE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST: {
                if (grantResults.length < 1 || grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    System.out.println("No permission!");
                    finish(); //End app it user doesn't give the permissions
                }
            }
        }
    }
}
