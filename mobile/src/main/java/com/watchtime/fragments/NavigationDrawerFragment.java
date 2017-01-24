package com.watchtime.fragments;

import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.watchtime.adapters.NavigationAdapter;

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
}
