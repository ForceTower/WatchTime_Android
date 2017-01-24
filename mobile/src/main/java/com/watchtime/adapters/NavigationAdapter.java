package com.watchtime.adapters;

import android.support.v7.widget.RecyclerView;

/**
 * Created by João Paulo on 24/01/2017.
 */

public class NavigationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    public interface Callback {
        int getSelectedPosition();
    }

    //RecyclerView Implementation
}
