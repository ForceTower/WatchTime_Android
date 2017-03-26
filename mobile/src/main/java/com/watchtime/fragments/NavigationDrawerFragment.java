package com.watchtime.fragments;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.watchtime.R;
import com.watchtime.adapters.NavigationAdapter;
import com.watchtime.adapters.decorators.OneShotDividerDecorator;
import com.watchtime.base.WatchTimeApplication;
import com.watchtime.base.content.preferences.Prefs;
import com.watchtime.base.interfaces.OnDataChangeHandler;
import com.watchtime.base.providers.media.MoviesProvider;
import com.watchtime.base.providers.media.UserListsProvider;
import com.watchtime.base.utils.PrefUtils;
import com.watchtime.fragments.drawer.NavDrawerItem;
import com.watchtime.sdk.LoginManagerWT;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by João Paulo on 23/01/2017.
 * Drawer Fragment... Its the Drawer on the side
 */

@SuppressWarnings("unchecked")
public class NavigationDrawerFragment extends Fragment implements NavigationAdapter.Callback, OnDataChangeHandler.OnDataChangeListener{

    public interface Callbacks {
        void onNavigationDrawerItemSelected(NavDrawerItem item, String s);
        void onLoginClicked();
        void onLogoutClicked();
    }

    @Override
    public void onDataChange() {
        Log.i("NavDrawerFrag", "Data Changed Ignited. Added? " + isAdded() + ". Visible? " + isVisible() + ". Detached? " + isDetached());
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(isDetached() || !isVisible())
                    return;
                Log.i("NavDrawerFrag", "Exec: " + (isDetached() || !isVisible()));
                mAdapter.setItems(initItems());

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (isDetached() || !isVisible())
                            return;
                        Log.i("NavDrawerFrag", "Exec2: " + isDetached());
                        mAdapter.setItems(initItems());
                    }
                }, 1500);
            }
        }, 500);

    }

    private NavDrawerItem.OnClickListener settingsClickListener = new NavDrawerItem.OnClickListener() {
        @Override
        public void onClick(View v, NavigationAdapter.ItemRowHolder rowHolder, int position) {
            //SettingsActivity.startActivity(getActivity());
            mDrawerLayout.closeDrawer(mNavigationDrawerContainer);
        }
    };

    private NavDrawerItem.OnClickListener loginClickListener = new NavDrawerItem.OnClickListener() {
        @Override
        public void onClick(View v, NavigationAdapter.ItemRowHolder rowHolder, int position) {
            mDrawerLayout.closeDrawer(mNavigationDrawerContainer);
            mCallbacks.onLoginClicked();
        }
    };

    private NavDrawerItem.OnClickListener logoutClickListener = new NavDrawerItem.OnClickListener() {
        @Override
        public void onClick(View v, NavigationAdapter.ItemRowHolder rowHolder, int position) {
            mCallbacks.onLogoutClicked();
            mDrawerLayout.closeDrawer(mNavigationDrawerContainer);
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    mAdapter.setItems(initItems());
                }
            });
        }
    };

    private NavigationAdapter.OnItemClickListener itemClickListener = new NavigationAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(View v, NavigationAdapter.ItemRowHolder vh, NavDrawerItem item, int position) {
            if (item.getOnClickListener() != null) {
                item.onClick(v, vh, position);
                return;
            }
            selectItem(mAdapter.getCorrectPosition(position));
        }
    };

    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";

    RecyclerView mRecyclerView;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private ViewGroup mNavigationDrawerContainer;

    private int mCurrentSelectedPosition = 0;
    private boolean mFromSavedInstanceState;
    private boolean mUserLearnedDrawer;
    private NavigationAdapter mAdapter;

    private Callbacks mCallbacks;

    @Override
    public int getSelectedPosition() {
        return mCurrentSelectedPosition;
    }

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (Callbacks) activity;
            Log.i("NavDrawerFrag", "Attach");
            ((WatchTimeApplication)WatchTimeApplication.getAppContext()).getDataChangeHandler().registerListener("NavDrawerFragment", this, new int[] {OnDataChangeHandler.LOGIN, OnDataChangeHandler.LOGOUT});
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.i("NavDrawerFrag", "Detach");
        ((WatchTimeApplication)WatchTimeApplication.getAppContext()).getDataChangeHandler().unregisterListener("NavDrawerFragment");
        mCallbacks = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mUserLearnedDrawer = PrefUtils.get(getActivity(), Prefs.DRAWER_LEARNED, false);

        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            mFromSavedInstanceState = true;
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRecyclerView = (RecyclerView) inflater.inflate(R.layout.fragment_navigation_drawer, container, false);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        return mRecyclerView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAdapter = new NavigationAdapter(getActivity(), this, initItems());

        mAdapter.setOnItemClickListener(itemClickListener);


        mRecyclerView.addItemDecoration(new OneShotDividerDecorator(getActivity(), 1));
        mRecyclerView.addItemDecoration(new OneShotDividerDecorator(getActivity(), 3));
        mRecyclerView.addItemDecoration(new OneShotDividerDecorator(getActivity(), 7));

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.requestFocus();
    }

    public List<NavDrawerItem> initItems() {
        List<NavDrawerItem> navItems = new ArrayList<>();

        navItems.add(new NavDrawerItem(true));

        boolean connected = LoginManagerWT.getInstance().isConnected(getContext());
        if (connected) {
            navItems.add(new NavDrawerItem(getString(R.string.your_profile), R.drawable.my_profile_icons, NavDrawerItem.ItemTags.PROFILE));
        } else {
            navItems.add(new NavDrawerItem(getString(R.string.title_login), R.drawable.ic_login_image, loginClickListener));
        }

        navItems.add(new NavDrawerItem(getString(R.string.title_movies), R.drawable.ic_nav_movies, new MoviesProvider()));
        navItems.add(new NavDrawerItem(getString(R.string.title_shows), R.drawable.ic_nav_tv/*, new ShowsProvider()*/));

        if (connected) {
            navItems.add(new NavDrawerItem(getString(R.string.title_my_watch_list), R.drawable.ic_your_list, NavDrawerItem.ItemTags.USER_LIST, new UserListsProvider()));
            navItems.add(new NavDrawerItem(getString(R.string.title_friends_activities), R.drawable.ic_friends_watch, NavDrawerItem.ItemTags.FRIENDS));
            navItems.add(new NavDrawerItem(getString(R.string.title_discover_movies), R.drawable.ic_discover/*, new FriendsWatchsProvider()*/));
            navItems.add(new NavDrawerItem(getString(R.string.your_activity), R.drawable.ic_your_activity, NavDrawerItem.ItemTags.USER_ACTIVITY));
        }

        navItems.add(new NavDrawerItem(getString(R.string.preferences), R.drawable.ic_nav_settings, settingsClickListener));
        if (connected) {
            navItems.add(new NavDrawerItem(getString(R.string.logout), R.drawable.ic_login_image, logoutClickListener));
        }

        return navItems;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    public NavDrawerItem getCurrentItem() {
        return mAdapter.getItem(getSelectedPosition() + 1);
    }

    public void initialise(ViewGroup navigationContainer, DrawerLayout drawerLayout) {
        mNavigationDrawerContainer = navigationContainer;
        mDrawerLayout = drawerLayout;

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        mDrawerToggle = new ActionBarDrawerToggle(getActivity(), mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {
            public  void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (!isAdded()) return;

                if (!mUserLearnedDrawer) {
                    mUserLearnedDrawer = true;
                    PrefUtils.save(getActivity(), Prefs.DRAWER_LEARNED, true);
                }
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, 0);
            }
        };

        if (!mUserLearnedDrawer && !mFromSavedInstanceState) {
            mDrawerLayout.openDrawer(mNavigationDrawerContainer);
        }

        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });

        mDrawerLayout.addDrawerListener(mDrawerToggle);
    }

    private ActionBar getActionBar() {
        return ((AppCompatActivity) getActivity()).getSupportActionBar();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void selectItem(int position) {
        mCurrentSelectedPosition = position;

        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mNavigationDrawerContainer);
        }

        if (mCallbacks != null) {
            NavDrawerItem navItem = mAdapter.getItem(position + 1);
            mCallbacks.onNavigationDrawerItemSelected(navItem, navItem != null ? navItem.getTitle() : null);
        }

        mAdapter.notifyDataSetChanged();
    }
}
