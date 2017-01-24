package com.watchtime.fragments.drawer;

import android.view.View;

import com.watchtime.adapters.NavigationAdapter;

/**
 * Created by João Paulo on 24/01/2017.
 * Item to be displayed in the Navigation List
 */
public class NavDrawerItem {
    //Interface to handle click on the items
    public interface OnClickListener{
        void onClick(View v, NavigationAdapter.ItemRowHolder rowHolder, int position);
    }

    //Click Listener to this item
    private NavDrawerItem.OnClickListener onClickListener;

    private boolean isHeader = false;
    private boolean isSwitch = false;
    private boolean switchValue = false;
    private boolean showProgress = false;

    private String title; //title of the item
    private int icon; //icon of the item

    private MediaProvider mediaProvider; //media provider used to fetch items when this item is selected
    private NavigationAdapter.ItemRowHolder rowHolder; //Row in the drawer

    public NavDrawerItem(String title, int icon) {
        this.title = title;
        this.icon = icon;
    }

    public NavDrawerItem(String title, int icon, MediaProvider provider) {
        this(title, icon);
        mediaProvider = provider;
    }

    public NavDrawerItem(String title, int icon, OnClickListener listener, boolean isSwitch) {
        this(title, icon);
        onClickListener = listener;
        this.isSwitch = true;
        switchValue = isSwitch;
    }

    public NavDrawerItem(boolean isHeader) {
        this.isHeader = isHeader;
    }

    public void setRowHolder(NavigationAdapter.ItemRowHolder rowHolder) {
        this.rowHolder = rowHolder;
    }

    public String getTitle() {
        return title;
    }

    public int getIcon() {
        return icon;
    }

    public MediaProvider getMediaProvider() {
        return mediaProvider;
    }

    public boolean isHeader() {
        return isHeader;
    }

    public boolean isSwitch() {
        return isSwitch;
    }

    public boolean getSwitchValue() {
        return switchValue;
    }

    public boolean hasProvider() {
        return mediaProvider != null;
    }

    public OnClickListener getOnClickListener() {
        return onClickListener;
    }

    public void onClick(View v, NavigationAdapter.ItemRowHolder itemRowHolder, int position) {
        onClickListener.onClick(v, itemRowHolder, position);
    }

    public boolean showProgress() {
        return showProgress;
    }

    public void setSwitchValue(boolean b) {
        if(rowHolder != null)
            rowHolder.getSwitch().setChecked(b);
    }

    public void setShowProgress(boolean progress) {
        showProgress = progress;

        if (rowHolder != null) {
            rowHolder.getProgressBar().setVisibility(progress ? View.VISIBLE : View.INVISIBLE);

            if (isSwitch)
                rowHolder.getSwitch().setVisibility(progress ? View.INVISIBLE : View.VISIBLE);
        }
    }
}
