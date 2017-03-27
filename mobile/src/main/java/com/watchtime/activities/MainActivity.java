package com.watchtime.activities;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.watchtime.R;
import com.watchtime.activities.base.WatchTimeBaseActivity;
import com.watchtime.base.Constants;
import com.watchtime.base.WatchTimeApplication;
import com.watchtime.base.backend.User;
import com.watchtime.base.content.preferences.Prefs;
import com.watchtime.base.interfaces.OnDataChangeHandler;
import com.watchtime.base.utils.PrefUtils;
import com.watchtime.base.utils.VersionUtils;
import com.watchtime.fragments.MediaContainerFragment;
import com.watchtime.fragments.NavigationDrawerFragment;
import com.watchtime.fragments.UserListsFragment;
import com.watchtime.fragments.drawer.NavDrawerItem;
import com.watchtime.sdk.LoginManagerWT;
import com.watchtime.utils.ToolbarUtils;
import com.watchtime.widget.ScrimInsetsFrameLayout;

import java.io.IOException;

import butterknife.Bind;

/**
 * Main Application Activity, all starts from here.
 * The fact that this activity extends the base activity, we should keep track of the 2 files.
 * This Activity that houses the navigation drawer, and controls navigation between fragments
 */
public class MainActivity extends WatchTimeBaseActivity implements NavigationDrawerFragment.Callbacks{
    private static final int PERMISSIONS_REQUEST = 123;

    private Fragment mCurrentFragment;

    @Bind(R.id.toolbar)
    Toolbar mToolbar;

    @Bind(R.id.navigation_drawer_container)
    ScrimInsetsFrameLayout mNavigationDrawerContainer;

    @Bind(R.id.tabs)
    TabLayout mTabs;

    NavigationDrawerFragment mNavigationDrawerFragment;

    private AccountManager accountManager;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_main);

        setupAccountManagerCode();
        //Request Permission to Write And Read From External Memory
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE }, PERMISSIONS_REQUEST);
        }

        if (VersionUtils.isLollipop()) {
            getWindow().setExitTransition(TransitionInflater.from(this).inflateTransition(android.R.transition.fade));
            getWindow().setEnterTransition(TransitionInflater.from(this).inflateTransition(android.R.transition.fade));
        }

        setSupportActionBar(mToolbar);
        ToolbarUtils.updateToolbarHeight(this, mToolbar);

        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerLayout.setStatusBarBackgroundColor(ContextCompat.getColor(this, R.color.primary_dark));

        mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer_fragment);
        mNavigationDrawerFragment.initialise(mNavigationDrawerContainer, drawerLayout);

        if (null != savedInstanceState) return;

        int providerId = PrefUtils.get(this, Prefs.DEFAULT_VIEW, 1);
        mNavigationDrawerFragment.selectItem(providerId);
    }

    /**
     * On Resume App, checks for the title, and rebuild the Items on NavigationDrawerFragments
     */
    @Override
    protected void onResume() {
        super.onResume();

        getApp().getDataChangeHandler().registerListener("main", dataChangedListener, new int[]{OnDataChangeHandler.LOGOUT});

        String title = mNavigationDrawerFragment.getCurrentItem().getTitle();

        setTitle(null != title ? title : getString(R.string.app_name));
        supportInvalidateOptionsMenu();

        if (mNavigationDrawerFragment.getCurrentItem() != null && mNavigationDrawerFragment.getCurrentItem().getTitle() != null) {
            setTitle(mNavigationDrawerFragment.getCurrentItem().getTitle());
        }

        mNavigationDrawerFragment.initItems();
    }

    @Override
    protected void onPause() {
        super.onPause();
        getApp().getDataChangeHandler().unregisterListener("main");
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
            if (item.hasProvider() && item.getTag() == 0) {
                mCurrentFragment = MediaContainerFragment.newInstance(item.getMediaProvider(), item);
                mTabs.setVisibility(View.VISIBLE);
            } else {
                if (item.getTag() == NavDrawerItem.ItemTags.USER_LIST) {
                    mTabs.setVisibility(View.GONE);
                    mCurrentFragment = UserListsFragment.newInstance(item.getMediaProvider(), item.getMediaProvider().getNavigation().get(item.getMediaProvider().getDefaultNavigationIndex()-1).getCategory());
                }
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

        if(mTabs.getTabCount() > 0)
            mTabs.getTabAt(0).select();

        if (mCurrentFragment == null)
            return;

        fragmentManager.beginTransaction().replace(R.id.container, mCurrentFragment, tag).commit();

        if(mCurrentFragment instanceof MediaContainerFragment) {
            updateTabs((MediaContainerFragment) mCurrentFragment, ((MediaContainerFragment) mCurrentFragment).getCurrentSelection());
        }
    }

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
                    finish();
                }
            }
        }
    }

    private void setupAccountManagerCode() {
        accountManager = AccountManager.get(this);
        user = ((WatchTimeApplication)getApplication()).getUser();

        getAccounts();

        if (accountManager.getAccountsByType(Constants.ACCOUNT_TYPE).length == 0) {
            LoginManager.getInstance().logOut();
            finish();
        }

    }

    public void onLoginClicked() {
        addAccount();
    }

    public void addAccount() {
        Log.i("MainAct", "-> addAccount()");
        AccountManagerCallback<Bundle> callback = new AccountManagerCallback<Bundle>() {
            @Override
            public void run(AccountManagerFuture<Bundle> future) {
                try {
                    Bundle bundle = future.getResult();
                    String accountName = bundle.getString(AccountManager.KEY_ACCOUNT_NAME);
                    String accountType = bundle.getString(AccountManager.KEY_ACCOUNT_TYPE);
                    String token       = bundle.getString(AccountManager.KEY_AUTHTOKEN);
                    Log.i("MainAct", "->addAccount()->callback()::Name: " + accountName+ "\nType: " + accountType + "\nToken: " + token);
                    getAccount(accountName, accountType);
                } catch (OperationCanceledException | IOException | AuthenticatorException e) {
                    Log.i("MainAct", "->addAccount()->callback()->catch(Exception " + e.getMessage() + ")");
                }
            }
        };

        accountManager.addAccount(Constants.ACCOUNT_TYPE, Constants.ACCOUNT_TOKEN_TYPE, null, null, this, callback, null);
    }

    public void getAccount(String accountName, String accountType) {
        Log.i("MainAct", "-> getAccount()");
        AccountManagerCallback<Bundle> callback = new AccountManagerCallback<Bundle>() {
            @Override
            public void run(AccountManagerFuture<Bundle> future) {
                try {
                    Bundle bundle = future.getResult();
                    String accountName = bundle.getString(AccountManager.KEY_ACCOUNT_NAME);
                    String accountType = bundle.getString(AccountManager.KEY_ACCOUNT_TYPE);
                    String token       = bundle.getString(AccountManager.KEY_AUTHTOKEN);
                    Log.i("MainAct", "->getAccount()->callback()::Name: " + accountName+ "\nType: " + accountType + "\nToken: " + token);

                    user.setAccountName(accountName);
                    user.setAccountType(accountType);
                    user.setToken(token);
                } catch (OperationCanceledException | IOException | AuthenticatorException e) {
                    Log.i("MainAct", "->getAccount()->callback()->catch(Exception " + e.getMessage() + ")");
                }
            }
        };

        accountManager.getAuthToken(new Account(accountName, accountType), Constants.ACCOUNT_TOKEN_TYPE, null, null, callback, null);
    }

    public void getAccounts() {
        Log.i("MainAct", "-> getAccounts()");
        AccountManagerCallback<Bundle> callback = new AccountManagerCallback<Bundle>() {
            @Override
            public void run(AccountManagerFuture<Bundle> future) {
                try {
                    Bundle bundle = future.getResult();
                    String accountName = bundle.getString(AccountManager.KEY_ACCOUNT_NAME);
                    String accountType = bundle.getString(AccountManager.KEY_ACCOUNT_TYPE);
                    String token       = bundle.getString(AccountManager.KEY_AUTHTOKEN);
                    Log.i("MainAct", "->getAccounts()->callback()::Name: " + accountName+ "\nType: " + accountType + "\nToken: " + token);

                    user.setAccountName(accountName);
                    user.setAccountType(accountType);
                    user.setToken(token);
                } catch (OperationCanceledException | IOException | AuthenticatorException e) {
                    Log.i("MainAct", "->getAccounts()->callback()->catch(Exception " + e.getMessage() + ")");
                }
            }
        };

        accountManager.getAuthTokenByFeatures(Constants.ACCOUNT_TYPE, Constants.ACCOUNT_TOKEN_TYPE, null, this, null, null, callback, null);
    }

    public void onLogoutClicked() {
        if (logout()) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    LoginManager.getInstance().logOut();
                    LoginManagerWT.getInstance().logout();
                }
            }, 250);

        }
    }

    public boolean logout() {
        Account[] accounts = accountManager.getAccountsByType(Constants.ACCOUNT_TYPE);

        for (Account account : accounts) {
            if (account.name.equals(user.getAccountName())) {
                accountManager.removeAccount(account, null, null);
                return true;
            }
        }

        Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show();
        return false;
    }

    private OnDataChangeHandler.OnDataChangeListener dataChangedListener = new OnDataChangeHandler.OnDataChangeListener() {
        @Override
        public void onDataChange() {
            finish();
            addAccount();
        }
    };
}
