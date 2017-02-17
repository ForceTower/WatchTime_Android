package com.watchtime.fragments;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.graphics.ColorUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.watchtime.R;
import com.watchtime.base.providers.media.models.Movie;
import com.watchtime.base.utils.PixelUtils;
import com.watchtime.base.utils.VersionUtils;
import com.watchtime.fragments.base.DetailMediaBaseFragment;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by João Paulo on 17/02/2017.
 */

public class MovieDetailsFragment extends DetailMediaBaseFragment {
    private static Movie movie;

    @Bind(R.id.title)
    TextView title;
    @Bind(R.id.year_time_genre)
    TextView yearTimeGenre;
    @Bind(R.id.plot_short)
    TextView plotShort;
    @Bind(R.id.rating)
    RatingBar rating;
    @Bind(R.id.actions_button)
    FloatingActionMenu actionsBtn;
    @Bind(R.id.add_to_list)
    FloatingActionButton addToList;
    @Bind(R.id.mark_watched)
    FloatingActionButton markWatched;
    @Bind(R.id.recommend)
    FloatingActionButton recommend;

    public static MovieDetailsFragment newInstance(Movie m) {
        movie = m;
        return new MovieDetailsFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_movie_detail, container, false);

        if (container != null)
            rootView.setMinimumHeight(container.getMinimumHeight());

        ButterKnife.bind(this, rootView);

        if (movie != null) {
            setupFloatActionButtonsColors();

            title.setText(movie.title);

            if (!movie.rating.equals("-1")) {
                Double rating_val = Double.parseDouble(movie.rating);
                rating.setProgress(rating_val.intValue());
                rating.setContentDescription("Rating: " + rating_val.intValue() + " out of 10");
                rating.setVisibility(View.VISIBLE);
            } else {
                rating.setVisibility(View.INVISIBLE);
            }

            String metaDataStr = movie.year;

            if (!movie.runtime.isEmpty()) {
                int runtime = Integer.parseInt(movie.runtime);
                int hours = runtime/60;
                int minutes = runtime%60;

                metaDataStr = metaDataStr + " - " + hours + "h " + minutes + "min";
            }

            if (movie.genre != null && !movie.genre.isEmpty()) {
                metaDataStr = metaDataStr + " - " + movie.genre;
            }

            yearTimeGenre.setText(metaDataStr);

            if (!movie.synopsis.isEmpty()) {
                plotShort.setText(movie.synopsis);
                plotShort.setVisibility(View.VISIBLE);
            }

        }

        return rootView;
    }

    public void setupFloatActionButtonsColors() {
        int normal = movie.color;
        int pressed = PixelUtils.colorLighter(movie.color);
        int ripple = PixelUtils.colorDarker(movie.color);

        actionsBtn.setMenuButtonColorNormal(normal);
        actionsBtn.setMenuButtonColorPressed(pressed);
        actionsBtn.setMenuButtonColorRipple(ripple);

        addToList.setColorNormal(normal);
        addToList.setColorPressed(pressed);
        addToList.setColorRipple(ripple);

        markWatched.setColorNormal(normal);
        markWatched.setColorPressed(pressed);
        markWatched.setColorRipple(ripple);

        recommend.setColorNormal(normal);
        recommend.setColorPressed(pressed);
        recommend.setColorRipple(ripple);
    }
}
