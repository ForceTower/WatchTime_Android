package com.watchtime.adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.watchtime.R;
import com.watchtime.base.providers.media.models.Genre;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class GenreAdapter extends RecyclerView.Adapter{
    public interface OnItemSelectionListener {
        void onItemSelect(View v, Genre item, int position);
    }

    private Context context;
    private View selectedItem;
    private int selectedPosition = 0;
    private List<Genre> data;
    private OnItemSelectionListener itemSelectionListener;

    private int selectedColor, normalColor;

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @Bind(android.R.id.text1)
        TextView text1;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (itemSelectionListener != null) {
                //TODO Make sure this is right
                selectedPosition = getAdapterPosition();
                if (selectedItem != null) {
                    selectedItem.setBackgroundColor(normalColor);
                    selectedItem = itemView;
                    selectedItem.setBackgroundColor(selectedColor);
                }

                itemSelectionListener.onItemSelect(view, getItem(selectedPosition), selectedPosition);
            }
        }
    }

    public GenreAdapter(Context context, List<Genre> data, int selectedPosition) {
        this.context = context;
        this.data = data;
        this.selectedPosition = selectedPosition;

        selectedColor = ContextCompat.getColor(context, R.color.selectable_focused);
        normalColor = ContextCompat.getColor(context, R.color.transparent);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_singleline_item, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ViewHolder viewHolder = (ViewHolder) holder;

        if (selectedPosition == position && selectedItem == null)
            selectedItem = viewHolder.itemView;

        viewHolder.itemView.setBackgroundColor(selectedPosition == position ? selectedColor : normalColor);
        viewHolder.text1.setText(getItem(position).getLabelId());
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public Genre getItem(int position) {
        return data.get(position);
    }

    public void setOnItemSelectionListener(OnItemSelectionListener listener) {
        itemSelectionListener = listener;
    }

}
