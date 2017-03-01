package com.watchtime.fragments;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.watchtime.R;
import com.watchtime.adapters.CastAdapter;
import com.watchtime.adapters.GenreAdapter;
import com.watchtime.base.providers.media.models.Movie;
import com.watchtime.base.providers.media.models.Person;
import com.watchtime.base.utils.AnimUtils;
import com.watchtime.base.utils.PixelUtils;
import com.watchtime.base.utils.VersionUtils;
import com.watchtime.fragments.base.DetailMediaBaseFragment;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

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
    @Bind(R.id.director_name)
    TextView directorName;
    @Bind(R.id.director_image)
    CircleImageView directorImage;
    @Bind(R.id.cast_layout)
    LinearLayout castLayout;
    @Bind(R.id.all_genres_layout)
    LinearLayout allGenresLayout;
    @Bind(R.id.cast_recycler_view)
    RecyclerView castRecyclerView;
    @Bind(R.id.all_genre_recycler_view)
    RecyclerView allGenresRecyclerView;

    @Bind(R.id.extras)
    LinearLayout extras;

    LinearLayoutManager layoutManager;
    LinearLayoutManager genresLayoutManager;

    CastAdapter castAdapter;
    //AllGenresAdapter genreAdapter;

    private boolean hasCast = true;
    private boolean hasOtherGenres = true;

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
            setupFloatActionButtons();
            setupTitleToolbarTitle();
            setupRatingBar();
            setupYearDurationGenres();
            setupSynopsis();
            setupDirectorInfo();
            setupCastInfo();
            setupAllGenresInfo();
        }

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        continueCastInfoSetup();
        continueAllGenresInfoSetup();
    }

    public void setupTitleToolbarTitle() {
        title.setText(movie.title);
    }

    public void setupRatingBar() {
        if (!movie.rating.equals("-1")) {
            Double rating_val = Double.parseDouble(movie.rating);
            rating.setProgress(rating_val.intValue());
            rating.setContentDescription("Rating: " + rating_val.intValue() + " out of 10");
            rating.setVisibility(View.VISIBLE);
            extras.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    Log.d("MovieDetails", "click rating!");
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Snackbar.make(v, "Rate this Movie", Snackbar.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        } else {
            rating.setVisibility(View.INVISIBLE);
        }
    }

    public void setupYearDurationGenres() {
        String metaDataStr = movie.year;

        if (!movie.runtime.isEmpty()) {
            int runtime = 0;
            if (movie.runtime != null) {
                runtime = Integer.parseInt(movie.runtime);
            }
            int hours = runtime/60;
            int minutes = runtime%60;

            metaDataStr = metaDataStr + " - " + hours + "h " + minutes + "min";
        }

        if (movie.genre != null && !movie.genre.isEmpty()) {
            metaDataStr = metaDataStr + " - " + movie.genre;
        }

        yearTimeGenre.setText(metaDataStr);
    }

    public void setupSynopsis() {
        if (!movie.synopsis.isEmpty()) {
            plotShort.setText(movie.synopsis);
            plotShort.setVisibility(View.VISIBLE);
        }
    }

    public void setupDirectorInfo() {
        if (!movie.director.isEmpty()) {
            directorName.setText(movie.director);
            directorName.setVisibility(View.VISIBLE);
        } else directorName.setVisibility(View.GONE);

        if (movie.directorImage != null && !movie.directorImage.isEmpty()) {
            Picasso.with(getContext()).load(movie.directorImage).into(directorImage, new Callback() {
                @Override
                public void onSuccess() {
                    AnimUtils.fadeIn(directorImage);
                    //logo.setVisibility(View.GONE);
                }

                @Override
                public void onError() {

                }
            });
        }
    }

    public void setupCastInfo() {
        if (movie.actors == null || movie.actors.isEmpty()) {
            castLayout.setVisibility(View.GONE);
            hasCast = false;
            return;
        }

        castLayout.setVisibility(View.VISIBLE);

        layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        castRecyclerView.setLayoutManager(layoutManager);
    }

    public void setupAllGenresInfo() {
        if (movie.genres == null || movie.genres.isEmpty()) {
            allGenresLayout.setVisibility(View.GONE);
            hasOtherGenres = false;
            return;
        }

        allGenresLayout.setVisibility(View.VISIBLE);
        genresLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        allGenresRecyclerView.setLayoutManager(genresLayoutManager);
    }

    public void continueCastInfoSetup() {
        if (!hasCast) return;

        castRecyclerView.setHasFixedSize(true);

        castAdapter = new CastAdapter(getActivity(), movie.actors);
        castAdapter.setOnPersonClickListener(personClickListener);
        castRecyclerView.setAdapter(castAdapter);
    }

    public void continueAllGenresInfoSetup() {
        if (!hasOtherGenres) return;

        allGenresRecyclerView.setHasFixedSize(true);
        //genresAdapter = new AllGenresAdapter(getActivity(), movie.genres);
        //allGenresRecyclerView.setAdapter(genresAdapter);
    }

    public void setupFloatActionButtons() {
        boolean showNames = true;

        int normal = movie.color;
        int pressed = PixelUtils.colorLighter(movie.color);
        int ripple = PixelUtils.colorDarker(movie.color);

        actionsBtn.setClosedOnTouchOutside(true);

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

        if (showNames) {
            addToList.setLabelText(getString(R.string.add_to_watchlist));
            markWatched.setLabelText(getString(R.string.mark_as_watched));
            recommend.setLabelText(getString(R.string.recommend_title));
        }

        setupFloatButtonsListeners();
    }

    public void setupFloatButtonsListeners() {
        addToList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Snackbar.make(v, getString(R.string.add_to_watchlist), Snackbar.LENGTH_SHORT).show();
                    }
                });
            }
        });

        markWatched.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Snackbar.make(v, getString(R.string.mark_as_watched), Snackbar.LENGTH_SHORT).show();
                    }
                });
            }
        });

        recommend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Snackbar.make(v, getString(R.string.recommend_title), Snackbar.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private CastAdapter.OnPersonClickListener personClickListener = new CastAdapter.OnPersonClickListener() {
        @Override
        public void onPersonClick(final View v, final Person person, int position) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Snackbar.make(v, "Clicked person: " + person.name, Snackbar.LENGTH_SHORT).show();
                }
            });
        }

        public void onShowMoreClick(final View v) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Snackbar.make(v, "Show full cast clicked", Snackbar.LENGTH_SHORT).show();
                }
            });
        }
    };
}
