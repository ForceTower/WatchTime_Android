package com.watchtime.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;
import com.watchtime.R;
import com.watchtime.base.providers.media.models.Media;
import com.watchtime.base.utils.AnimUtils;
import com.watchtime.base.utils.LocaleUtils;
import com.watchtime.base.utils.PixelUtils;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by João Paulo on 16/02/2017.
 */

public class MediaGridAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public interface OnItemClickListener {
        void onItemClick(View v, Media item, int position);
    }
    private int itemWidth, itemHeight, margin, columns;
    private ArrayList<OverviewItem> items = new ArrayList<>();
    private MediaGridAdapter.OnItemClickListener itemClickListener;
    private final int NORMAL = 0, LOADING = 1;

    public MediaGridAdapter(Context context, ArrayList<Media> items, int columns) {
        this.columns = columns;

        int screenWidth = PixelUtils.getScreenWidth(context);
        itemWidth = screenWidth/columns;
        itemHeight = (int)((double)itemWidth/0.7);

        margin = PixelUtils.getPixelsFromDp(context, 2);
        setItems(items);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (viewType == LOADING) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.media_griditem_loading, parent, false);
            return new MediaGridAdapter.LoadingHolder(view);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.media_griditem, parent, false);
            return new MediaGridAdapter.ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int double_margin = margin * 2;
        int top_margin = (position < columns) ? margin * 2 : margin;

        GridLayoutManager.LayoutParams layoutParams = (GridLayoutManager.LayoutParams) holder.itemView.getLayoutParams();
        layoutParams.height = itemHeight;
        layoutParams.width = itemWidth;
        int mod = LocaleUtils.currentLocaleIsRTL() ? 1 : 0;

        if (position % columns == mod) {
            layoutParams.setMargins(double_margin, top_margin, margin, margin);
        } else if (position % columns == columns - 1) {
            layoutParams.setMargins(margin, top_margin, double_margin, margin);
        } else {
            layoutParams.setMargins(margin, top_margin, margin, margin);
        }
        holder.itemView.setLayoutParams(layoutParams);

        if (getItemViewType(position) == NORMAL) {
            final ViewHolder videoViewHolder = (ViewHolder) holder;
            final OverviewItem overviewItem = getItem(position);
            Media item = overviewItem.media;


            videoViewHolder.title.setText(item.title);
            videoViewHolder.year.setText(item.year);

            videoViewHolder.coverImage.setVisibility(View.GONE);
            videoViewHolder.title.setVisibility(View.GONE);
            videoViewHolder.year.setVisibility(View.GONE);

            if (item.image != null && !item.image.equals("")) {
                Picasso.with(videoViewHolder.coverImage.getContext()).load(item.image)
                        .resize(itemWidth, itemHeight)
                        .transform(DrawGradient.INSTANCE)
                        .into(videoViewHolder.coverImage, new Callback() {
                            @Override
                            public void onSuccess() {
                                overviewItem.isImageError = false;
                                AnimUtils.fadeIn(videoViewHolder.coverImage);
                                AnimUtils.fadeIn(videoViewHolder.title);
                                AnimUtils.fadeIn(videoViewHolder.year);
                            }

                            @Override
                            public void onError() {
                                overviewItem.isImageError = true;
                                AnimUtils.fadeIn(videoViewHolder.title);
                                AnimUtils.fadeIn(videoViewHolder.year);
                            }
                        });
            }
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (getItem(position).isLoadingItem)
            return LOADING;
        return NORMAL;
    }

    public OverviewItem getItem(int position) {
        if (position < 0 || items.size() <= position) return null;
        return items.get(position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    public void removeLoading() {
        if (getItemCount() <= 0) return;
        OverviewItem item = items.get(getItemCount() - 1);
        if (item.isLoadingItem) {
            items.remove(getItemCount() - 1);
            notifyDataSetChanged();
        }
    }

    public void addLoading() {
        OverviewItem item = null;
        if (getItemCount() != 0) {
            item = items.get(getItemCount() - 1);
        }

        if (getItemCount() == 0 || (item != null && !item.isLoadingItem)) {
            items.add(new OverviewItem(true));
            notifyDataSetChanged();
        }
    }

    public boolean isLoading() {
        return getItemCount() > 0 && getItemViewType(getItemCount() - 1) == LOADING;
    }

    public void setItems(ArrayList<Media> items) {
        this.items.clear();

        if(items != null) {
            for (Media item : items) {
                this.items.add(new OverviewItem(item));
            }
        }

        notifyDataSetChanged();
    }

    public void clearItems() {
        items.clear();
        notifyDataSetChanged();
    }

    private class LoadingHolder extends RecyclerView.ViewHolder {
        View itemView;

        LoadingHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            itemView.setMinimumHeight(itemHeight);
        }

    }

    private class OverviewItem {
        Media media;
        boolean isLoadingItem = false;
        boolean isImageError = true;

        OverviewItem(Media media) {
            this.media = media;
        }

        OverviewItem(boolean loading) {
            this.isLoadingItem = loading;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        View itemView;
        @Bind(R.id.focus_overlay)
        View focusOverlay;
        @Bind(R.id.cover_image)
        ImageView coverImage;
        @Bind(R.id.title)
        TextView title;
        @Bind(R.id.year)
        TextView year;

        private View.OnFocusChangeListener onFocusChangeListener = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                focusOverlay.setVisibility(hasFocus ? View.VISIBLE : View.INVISIBLE);
            }
        };

        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            this.itemView = itemView;

            itemView.setOnClickListener(this);
            coverImage.setMinimumHeight(itemHeight);
            itemView.setOnFocusChangeListener(onFocusChangeListener);
        }

        public ImageView getCoverImage() {
            return coverImage;
        }

        @Override
        public void onClick(View v) {
            if (itemClickListener != null) {
                int position = getLayoutPosition();
                Media item = getItem(position).media;
                itemClickListener.onItemClick(v, item, position);
            }
        }
    }

    private static class DrawGradient implements Transformation {
        static Transformation INSTANCE = new DrawGradient();

        @Override
        public Bitmap transform(Bitmap src) {
            int w = src.getWidth();
            int h = src.getHeight();
            Bitmap overlay = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(overlay);

            canvas.drawBitmap(src, 0, 0, null);
            src.recycle();

            Paint paint = new Paint();
            float gradientHeight = h / 2f;
            LinearGradient shader = new LinearGradient(0, h - gradientHeight, 0, h, 0xFFFFFFFF, 0x00FFFFFF, Shader.TileMode.CLAMP);
            paint.setShader(shader);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
            canvas.drawRect(0, h - gradientHeight, w, h, paint);
            return overlay;
        }

        @Override
        public String key() {
            return "gradient()";
        }
    }
}
