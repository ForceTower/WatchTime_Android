package com.watchtime.adapters;

import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.watchtime.R;
import com.watchtime.base.ApiEndPoints;
import com.watchtime.base.WatchTimeApplication;
import com.watchtime.base.interfaces.OnDataChangeHandler;
import com.watchtime.base.utils.AnimUtils;
import com.watchtime.fragments.drawer.NavDrawerItem;
import com.watchtime.sdk.AccessTokenWT;
import com.watchtime.sdk.Profile;


import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

import static com.facebook.FacebookSdk.getApplicationContext;

public class NavigationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements OnDataChangeHandler.OnDataChangeListener {
    public interface Callback {
        int getSelectedPosition();
    }

    public void onDataChange() {
        Log.d("Drawer", "OnDataChanged");
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();

                //Updates again in 2 secs to make sure everything is all right (Bad Programming manner)
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        notifyDataSetChanged();
                    }
                }, 2000);
            }
        }, 500);
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
        holder.getBackgroundImageView().setBackgroundResource(R.color.primary_dark);
        holder.getProfileImageView().setVisibility(View.VISIBLE);

        holder.getTitleTextView().setVisibility(View.VISIBLE);
        holder.getTitleTextView().setTextColor(normalColor);

        Profile.fetchProfileForCurrentAccessToken();
        Profile user = Profile.getCurrentProfile();

        if (user != null) {
            final int id = user.getId();
            holder.getSubtitleTextView().setVisibility(View.VISIBLE);

            int minutes = user.getTimeWatched()%60;
            int hours = user.getTimeWatched()/60;

            int days = hours/24;
            hours = hours%24;

            int months = days/30;
            days = days%24;

            String str = WatchTimeApplication.getAppContext().getString(R.string.nothing_watched);

            if (months > 0)
                str = months + " months, " + days + " days, " + hours + " hours and " + minutes + " minutes watched";
            else if (days > 0)
                str = days + " days, " + hours + " hours and " + minutes + " minutes watched";
            else if (hours > 0)
                str = hours + " hours and " + minutes + " minutes watched";
            else if (minutes > 0)
                str = minutes + " minutes watched";

            holder.getSubtitleTextView().setText(str);
            holder.getTitleTextView().setText(user.getName());

            final CircleImageView profileImage = holder.getProfileImageView();
            final ImageView coverImage = holder.getBackgroundImageView();

            coverImage.setVisibility(View.VISIBLE);

            if (user.getCover() == null)
                coverImage.setImageResource(R.drawable.background_test_image3);
            else {
                Picasso.with(getApplicationContext()).load("https://image.tmdb.org/t/p/w780" + user.getCover()).into(coverImage, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {
                        Handler mHandler = new Handler(Looper.getMainLooper());
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                AnimUtils.fadeIn(coverImage);
                            }
                        });
                    }
                    @Override
                    public void onError() {}
                });
            }

            Picasso.with(getApplicationContext()).load(ApiEndPoints.PROFILE_IMAGE + id + ".png").into(profileImage, new com.squareup.picasso.Callback() {
                @Override
                public void onSuccess() {
                    Handler mHandler = new Handler(Looper.getMainLooper());
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            AnimUtils.fadeIn(profileImage);
                        }
                    });
                }
                @Override
                public void onError() {
                    Log.i("NavAdapter", "Error profile: " + ApiEndPoints.PROFILE_IMAGE + id + ".png");
                    profileImage.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_no_profile_image));
                }
            });
        } else {
            holder.getProfileImageView().setImageResource(R.mipmap.app_logo);
            holder.getBackgroundImageView().setImageResource(R.drawable.background_test_image3);
            holder.getSubtitleTextView().setVisibility(View.INVISIBLE);
            holder.getTitleTextView().setText(R.string.guest_name);
        }

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
        this.checkedColor = ContextCompat.getColor(context, R.color.primary_green);
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

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        Log.i("NavDrawer", "Attached To RecyclerView");
        ((WatchTimeApplication)getApplicationContext()).getDataChangeHandler().registerListener("NavAdapter", this, new int[] {OnDataChangeHandler.LOGIN, OnDataChangeHandler.LOGOUT, OnDataChangeHandler.ALL});
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        Log.i("NavDrawer", "Detached To RecyclerView");
        ((WatchTimeApplication)getApplicationContext()).getDataChangeHandler().unregisterListener("NavAdapter");
    }
}
