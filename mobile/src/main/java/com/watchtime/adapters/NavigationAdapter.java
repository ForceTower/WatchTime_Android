package com.watchtime.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import com.watchtime.R;
import com.watchtime.base.WatchTimeApplication;
import com.watchtime.fragments.drawer.NavDrawerItem;


import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by João Paulo on 24/01/2017.
 */

public class NavigationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    public interface Callback {
        int getSelectedPosition();
    }

    public interface OnItemClickListener {
        void onItemClick(View v, ItemRowHolder vh, NavDrawerItem item, int position);
    }

    //RecyclerView Implementation
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == HEADER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.nav_drawer_header, parent, false);
            return new HeaderHolder(view);
        }

        else if (viewType == ITEM) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.nav_drawer_list_item, parent, false);
            return new ItemRowHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int type = getItemViewType(position);
        if (type == HEADER)
            onBindHeaderViewHolder((HeaderHolder)holder, position);
        else if (type == ITEM)
            onBindItemViewHolder((ItemRowHolder)holder, position);
    }

    private void onBindHeaderViewHolder(HeaderHolder holder, int position) {
        //TODO: When user is connected, change the to a user color
        holder.getBackgroundImageView().setBackgroundResource(R.color.primary_dark);

        //TODO: When user is connected, change to the user image
        holder.getProfileImageView().setVisibility(View.VISIBLE);
        holder.getProfileImageView().setImageResource(R.mipmap.ic_creator_picture_delete);

        holder.getTitleTextView().setVisibility(View.VISIBLE);
        holder.getTitleTextView().setText("ForceTower");
        holder.getTitleTextView().setTextColor(normalColor);

        holder.getSubtitleTextView().setVisibility(View.VISIBLE);
        holder.getSubtitleTextView().setText("455382 Hours Watched");
        holder.getSubtitleTextView().setTextColor(normalColor);
    }

    private void onBindItemViewHolder(ItemRowHolder holder, int position) {
        NavDrawerItem item = getItem(position);
        item.setRowHolder(holder);

        if (item.isSwitch()) {
            if (item.showProgress()) {
                holder.getSwitch().setVisibility(View.INVISIBLE);
                holder.getProgressBar().setVisibility(View.VISIBLE);
            } else {
                holder.getSwitch().setVisibility(View.VISIBLE);
                holder.getProgressBar().setVisibility(View.INVISIBLE);
            }
            holder.getSwitch().setChecked(item.getSwitchValue());
        } else {
            holder.getSwitch().setVisibility(View.INVISIBLE);
            holder.getProgressBar().setVisibility(View.INVISIBLE);
        }

        holder.getTitle().setText(item.getTitle());

        boolean selected = callback.getSelectedPosition() == getCorrectPosition(position);
        holder.getTitle().setTextColor(selected ? checkedColor : normalColor);
        holder.itemView.setBackgroundResource(selected ? checkedBackgroundRes : normalBackgroundRes);

        if (item.getIcon() > 0) {
            holder.getIcon().setImageResource(item.getIcon());
            holder.getIcon().setColorFilter(selected ? checkedColor : normalColor, PorterDuff.Mode.SRC_IN);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    //class implementation
    private OnItemClickListener itemClickListener; //Listener of clicks
    private List<NavDrawerItem> items; //items to be displayed
    private Callback callback; //Activity to callback
    private final int HEADER = 0, ITEM = 1; //Type of item
    private final int normalColor, checkedColor, checkedBackgroundRes, normalBackgroundRes;

    public NavigationAdapter(@NonNull Context context, @NonNull Callback callback, List<NavDrawerItem> items) {
        this.items = items;
        this.callback = callback;
        this.normalColor = ContextCompat.getColor(context, R.color.nav_drawer_deselected);
        this.checkedColor = ContextCompat.getColor(context, R.color.primary);
        this.checkedBackgroundRes = R.color.nav_drawer_selected_bg;
        this.normalBackgroundRes = R.drawable.selectable_nav_background;
    }

    public void setItems(List<NavDrawerItem> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    public int getCorrectPosition(int position) {
        return position - 1;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        itemClickListener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        if (getItem(position).isHeader()) {
            return HEADER;
        }
        return ITEM;
    }

    public NavDrawerItem getItem(int position) {
        if (position < 0 || position >= items.size())
            return null;
        return items.get(position);
    }

    /**
     * This is an item that is present int the drawer
     */
    public class ItemRowHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @Bind(android.R.id.icon)
        ImageView icon;
        @Bind(android.R.id.text1)
        TextView title;
        @Bind(android.R.id.checkbox)
        Switch checkbox;
        @Bind(android.R.id.progress)
        ProgressBar progressBar;

        public ItemRowHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            //When clicked, sends the message to the Adapter
            if (itemClickListener != null) {
                int position = getAdapterPosition();
                NavDrawerItem item = getItem(position);
                itemClickListener.onItemClick(view, this, item, position);
            }
        }

        public Switch getSwitch() {
            return checkbox;
        }

        public ProgressBar getProgressBar() {
            return progressBar;
        }

        public ImageView getIcon() {
            return icon;
        }

        public TextView getTitle() {
            return title;
        }
    }

    public static class HeaderHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.bg_imageview)
        ImageView backgroundImageView;
        @Bind(R.id.profile_imageview)
        CircleImageView profileImageView;
        @Bind(R.id.title_textview)
        TextView titleTextView;
        @Bind(R.id.subtitle_textview)
        TextView subtitleTextView;

        public HeaderHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        public CircleImageView getProfileImageView() {
            return profileImageView;
        }

        public ImageView getBackgroundImageView() {
            return backgroundImageView;
        }

        public TextView getSubtitleTextView() {
            return subtitleTextView;
        }

        public TextView getTitleTextView() {
            return titleTextView;
        }
    }
}
