package com.watchtime.adapters;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.watchtime.R;
import com.watchtime.base.providers.media.models.Media;
import com.watchtime.base.providers.media.models.Movie;
import com.watchtime.base.providers.media.models.WatchlistItem;
import com.watchtime.base.utils.AnimUtils;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by João Paulo on 10/03/2017.
 */

public class MediaListRowsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private ArrayList<ListItem> items = new ArrayList<>();
    private final int NORMAL = 0, LOADING = 1;

    public MediaListRowsAdapter(Context context, ArrayList<Media> items) {
        setItems(items);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (viewType == LOADING) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.loading_media_itemrow, parent, false);
            return new MediaListRowsAdapter.LoadingHolder(view);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.media_itemrow, parent, false);
            return new MediaListRowsAdapter.ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(getItemViewType(position) == NORMAL) {
            final ViewHolder viewHolder = (ViewHolder)holder;
            final ListItem listItem = getItem(position);
            WatchlistItem media = (WatchlistItem)listItem.media;

            viewHolder.title.setText(media.title);
            viewHolder.genres.setText(viewHolder.itemView.getContext().getString(R.string.score) + " " + media.rating);

            if (media.tagline.equals("")) {
                viewHolder.tagline.setVisibility(View.GONE);
            } else {
                viewHolder.tagline.setVisibility(View.VISIBLE);
                viewHolder.tagline.setText(media.tagline);
            }

            if (media.image != null && !media.image.equals("")) {
                Picasso.with(viewHolder.cover.getContext()).load(media.image).into(viewHolder.cover, new Callback() {
                    @Override
                    public void onSuccess() {
                        listItem.isImageError = false;
                        AnimUtils.fadeIn(viewHolder.cover);
                        AnimUtils.fadeOut(viewHolder.placeholder);
                    }

                    @Override
                    public void onError() {
                        listItem.isImageError = true;
                    }
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public boolean isLoading() {
        return getItemCount() > 0 && getItemViewType(getItemCount() - 1) == LOADING;
    }

    public void removeLoading() {
        if (getItemCount() <= 0) return;
        ListItem item = items.get(getItemCount() - 1);
        if (item.isLoadingItem) {
            items.remove(getItemCount() - 1);
            notifyDataSetChanged();
        }
    }

    public void addLoading() {
        ListItem item = null;
        if (getItemCount() != 0) {
            item = items.get(getItemCount() - 1);
        }

        if (getItemCount() == 0 || (item != null && !item.isLoadingItem)) {
            items.add(new ListItem(true));
            final Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    notifyDataSetChanged();
                }
            };
            new Handler(Looper.getMainLooper()).post(runnable);
        }
    }

    private ListItem getItem(int position) {
        if (position < 0 || items.size() <= position) return null;
        return items.get(position);
    }

    public void setItems(ArrayList<Media> items) {
        this.items.clear();

        if(items != null) {
            for (Media item : items) {
                this.items.add(new ListItem(item));
            }
        }

        notifyDataSetChanged();
    }

    public int getItemViewType(int position) {
        if (getItem(position).isLoadingItem)
            return LOADING;
        return NORMAL;
    }

    private class ListItem {
        public Media media;
        boolean isLoadingItem = false;
        boolean isImageError = true;

        ListItem(Media media) {
            this.media = media;
        }

        ListItem(boolean loading) {
            this.isLoadingItem = loading;
        }
    }

    public class LoadingHolder extends RecyclerView.ViewHolder {
        View itemView;

        public LoadingHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        View itemView;
        @Bind(R.id.cover_image)
        ImageView cover;
        @Bind(R.id.title)
        TextView title;
        @Bind(R.id.genres)
        TextView genres;
        @Bind(R.id.tag_line)
        TextView tagline;
        @Bind(R.id.placeholder_image)
        ImageView placeholder;

        public ViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
            this.itemView = itemView;

        }
    }
}
