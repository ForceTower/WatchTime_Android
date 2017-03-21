package com.watchtime.adapters;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.watchtime.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class AllGenresAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    public interface OnGenreClickListener {
        void onGenreClicked(View v, String genre, int position);
    }

    private Context context;
    private OnGenreClickListener genreClickListener;
    private List<FullGenre> genres = new ArrayList<>();

    public AllGenresAdapter(Context context, HashMap<String, Integer> allGenres) {
        this.context = context;
        setItems(allGenres);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.all_genres_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final ViewHolder view = (ViewHolder)holder;
        final FullGenre genre = getGenre(position);

        view.getGenreName().setVisibility(View.VISIBLE);
        view.getGenreName().setText(genre.getName());

        view.getGenreFitness().setVisibility(View.VISIBLE);
        String text = "Kappa";
        float number = 5;
        if (number == 5)
            text = "5 - THIS IS AMAZING!";
        else if (number <= 4.5)
            text = "It describes the movie almost perfectly";
        else if (number <= 3.5)
            text = "Kinda fits";
        else if (number <= 2.5)
            text = "I think it fits";
        else if (number <= 1.5)
            text = "Like, 10% of the movie is this genre";
        else if (number <= 0.5)
            text = "Has nothing to do with this movie";
        view.getGenreFitness().setText(text);

        view.getGenreImage().setVisibility(View.VISIBLE);
        view.getGenreImage().setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_smilly));
    }

    @Override
    public int getItemCount() {
        return genres.size();
    }

    public void setItems(HashMap<String, Integer> allGenres) {
        genres.clear();

        for (String key : allGenres.keySet()) {
            genres.add(new FullGenre(allGenres.get(key), key));
        }

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        });
    }

    public void setGenreClickListener(OnGenreClickListener clickListener) {
        genreClickListener = clickListener;
    }

    private FullGenre getGenre(int position) {
        if (position < 0 || position >= genres.size())
            return null;
        return genres.get(position);
    }

    public class FullGenre {
        private int id;
        private String name;

        public FullGenre(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private View itemView;
        @Bind(R.id.genre_name)
        TextView genreName;
        @Bind(R.id.genre_fitness)
        TextView genreFitness;
        @Bind(R.id.genre_image)
        ImageView genreImage;

        public ViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;

            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (genreClickListener != null) {
                int position = getLayoutPosition();
                genreClickListener.onGenreClicked(v, getGenre(position).getName(), position);
            }
        }


        public View getItemView() {
            return itemView;
        }

        public TextView getGenreName() {
            return genreName;
        }

        public TextView getGenreFitness() {
            return genreFitness;
        }

        public ImageView getGenreImage() {
            return genreImage;
        }
    }
}
