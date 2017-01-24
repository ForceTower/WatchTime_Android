package com.watchtime.fragments;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.watchtime.R;
import com.watchtime.adapters.NavigationAdapter;
import com.watchtime.adapters.decorators.OneShotDividerDecorator;
import com.watchtime.base.content.preferences.Prefs;
import com.watchtime.base.utils.PrefUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by João Paulo on 23/01/2017.
 */

public class NavigationDrawerFragment extends Fragment implements NavigationAdapter.Callback{
    public interface Callbacks {
        /**
         * Called when an item in the navigation drawer is selected.
         */
        void onNavigationDrawerItemSelected(NavDrawerItem item, String s);
    }

    //Remembers the position of the selected item
    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";

    //Views Variables
    RecyclerView mRecyclerView;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private ViewGroup mNavigationDrawerContainer;

    //Variables
    private int mCurrentSelectedPosition = 0;
    private boolean mFromSavedInstanceState;
    private boolean mUserLearnedDrawer;
    private NavigationAdapter mAdapter;

    //The current callback instance (Activity)
    private Callbacks mCallbacks;

    //Callback implementation
    @Override
    public int getSelectedPosition() {
        return mCurrentSelectedPosition;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (Callbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Checks if the user knows how to use the drawer
        mUserLearnedDrawer = PrefUtils.get(getActivity(), Prefs.DRAWER_LEARNED, false);

        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION); //Received Navigation position on creation Yeee booy
            mFromSavedInstanceState = true;
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Indicate that this fragment would like to influence the set of actions in the action bar.
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //Creates the view in a container
        mRecyclerView = (RecyclerView) inflater.inflate(R.layout.fragment_navigation_drawer, container, false);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        return mRecyclerView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Creates de Navigation Adapter, It has a reference to the activity, the Drawer, and the items that should be displayed on the drawer
        mAdapter = new NavigationAdapter(getActivity(), this, initItems());

        //Sets a click listener to the Navigation Adapter.
        mAdapter.setOnItemClickListener(itemClickListener);

        //Sets the Decorator on position
        mRecyclerView.addItemDecoration(new OneShotDividerDecorator(getActivity(), 3));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.requestFocus();
    }

    public List<NavDrawerItem> initItems() {
        List<NavDrawerItem> navItems = new ArrayList<>();

        navItems.add(new NavDrawerItem(true)); //Header
        navItems.add(new NavDrawerItem(getString(R.string.title_movies), R.drawable.ic_nav_movies, new MoviesProvider()));
        navItems.add(new NavDrawerItem(getString(R.string.title_shows), R.drawable.ic_nav_tv, new ShowsProvider()));
        navItems.add(new NavDrawerItem(getString(R.string.title_my_watch_list), R.drawable.ic_nav_vpn, new MyListsProvider()));
        navItems.add(new NavDrawerItem(getString(R.string.title_friends_activities), R.drawable.ic_nav_tv, new FriendsWatchsProvider()));

        navItems.add(new NavDrawerItem(getString(R.string.preferences), R.drawable.ic_nav_settings, settingsClickListener));

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
        // Forward the new configuration the drawer toggle component.
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

        //The Action Toolbar Toggle ties everything together
        //This will create the proper interactions between Drawer and the Action Bar
        mDrawerToggle = new ActionBarDrawerToggle(getActivity(), mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {
            public  void onDrawerOpened(View drawerView) {
                //Opens the Drawer
                super.onDrawerOpened(drawerView);
                if (!isAdded()) return; //If the drawer is not added in the activity return

                //If the user opened the drawer by himself, it means that he learned how to open it.
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

        //If the user doesn't know about the drawer, open it to introduce how to open.
        if (!mUserLearnedDrawer && !mFromSavedInstanceState) {
            mDrawerLayout.openDrawer(mNavigationDrawerContainer);
        }

        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });

        //Set is deprecated
        mDrawerLayout.addDrawerListener(mDrawerToggle);
    }

    private ActionBar getActionBar() {
        return ((AppCompatActivity) getActivity()).getSupportActionBar();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //consume the home button press
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Method called when the user selects something from the list on the drawer
     * @param position
     */
    public void selectItem(int position) {
        mCurrentSelectedPosition = position;

        //When the user selects something in the drawer, close the drawer.
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mNavigationDrawerContainer);
        }

        if (mCallbacks != null) {
            NavDrawerItem navItem = mAdapter.getItem(position + 1); //gets the item selected
            //Calls the superior Activity to handle this change (MainActivity in this case)
            mCallbacks.onNavigationDrawerItemSelected(navItem, navItem != null ? navItem.getTitle() : null);
        }

        //Notify the change;
        mAdapter.notifyDataSetChanged();
    }
}
