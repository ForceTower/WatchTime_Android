package com.watchtime.fragments;

import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.watchtime.R;
import com.watchtime.activities.MediaDetailsActivity;
import com.watchtime.adapters.AllGenresAdapter;
import com.watchtime.adapters.CastAdapter;
import com.watchtime.base.ApiEndPoints;
import com.watchtime.base.WatchTimeApplication;
import com.watchtime.base.interfaces.OnDataChangeHandler;
import com.watchtime.base.providers.media.models.Movie;
import com.watchtime.base.providers.media.models.Person;
import com.watchtime.base.utils.AnimUtils;
import com.watchtime.base.utils.PixelUtils;
import com.watchtime.fragments.base.DetailMediaBaseFragment;
import com.watchtime.sdk.AccessTokenWT;
import com.watchtime.sdk.Profile;
import com.watchtime.sdk.WatchTimeBaseMethods;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MovieDetailsFragment extends DetailMediaBaseFragment implements MediaDetailsActivity.ActivityToFragmentEvents {
    private static Movie movie;

    @Bind(R.id.title)
    TextView title;
    @Bind(R.id.year_time)
    TextView yearTime;
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
    @Bind(R.id.genres)
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
    AllGenresAdapter genresAdapter;

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

        /*if (movie.genre != null && !movie.genre.isEmpty()) {
            metaDataStr = metaDataStr + " - " + movie.genre;
        }*/

        yearTime.setText(metaDataStr);
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
        if (movie.allGenres == null || movie.allGenres.isEmpty()) {
            allGenresLayout.setVisibility(View.GONE);
            hasOtherGenres = false;
            return;
        }

        /*String genresText = "";
        boolean first = true;
        for (String string : movie.allGenres.keySet()) {
            if (first) {
                genresText = string;
                first = false;
            } else {
                genresText = genresText + " - " + string;
            }
        }
        genres.setText(genresText);*/
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
        genresAdapter = new AllGenresAdapter(getActivity(), movie.allGenres);
        genresAdapter.setGenreClickListener(genreClickListener);
        allGenresRecyclerView.setAdapter(genresAdapter);
    }

    public void setupFloatActionButtons() {
        boolean showNames = false;

        if (AccessTokenWT.getCurrentAccessToken() == null) {
            actionsBtn.setVisibility(View.GONE);
            return;
        }

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
            addToList.setLabelVisibility(View.VISIBLE);
            markWatched.setLabelText(getString(R.string.mark_as_watched));
            markWatched.setLabelVisibility(View.VISIBLE);
            recommend.setLabelText(getString(R.string.recommend_title));
            recommend.setLabelVisibility(View.VISIBLE);
        } else {
            addToList.setLabelVisibility(View.INVISIBLE);
            markWatched.setLabelVisibility(View.INVISIBLE);
            recommend.setLabelVisibility(View.INVISIBLE);
        }

        setupFloatButtonsListeners();
    }

    public boolean onBackPressed() {
        if (actionsBtn.isOpened()) {
            actionsBtn.close(true);
            return true;
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (actionsBtn.isOpened()) {
                Rect outRect = new Rect();
                actionsBtn.getGlobalVisibleRect(outRect);

                if(!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {
                    actionsBtn.close(true);
                    return true;
                }
            }
        }
        return false;
    }

    public void setupFloatButtonsListeners() {
        addToList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Snackbar.make(v, getString(R.string.not_yet_implemented), Snackbar.LENGTH_SHORT).show();
                    }
                });
                addToWatchList(movie.videoId);
                actionsBtn.close(true);
            }
        });

        markWatched.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                actionsBtn.close(true);
                markMovieWatched(movie.videoId);
            }
        });

        recommend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Snackbar.make(v, getString(R.string.not_yet_implemented), Snackbar.LENGTH_SHORT).show();
                    }
                });
                actionsBtn.close(true);
            }
        });
    }

    private CastAdapter.OnPersonClickListener personClickListener = new CastAdapter.OnPersonClickListener() {
        @Override
        public void onPersonClick(final View v, final Person person, int position) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Snackbar.make(v, person.name + " as " + person.role, Snackbar.LENGTH_SHORT).show();
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

    private AllGenresAdapter.OnGenreClickListener genreClickListener = new AllGenresAdapter.OnGenreClickListener() {
        @Override
        public void onGenreClicked(final View v, final String genre, int position) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Snackbar.make(v, "Clicked Genre: " + genre, Snackbar.LENGTH_SHORT).show();
                }
            });
        }
    };

    private void markMovieWatched(String id) {
        WatchTimeBaseMethods.getInstance().markMovieAsWatched(id);
    }

    private void addToWatchList(String id) {
        WatchTimeBaseMethods.getInstance().addMovieToWatchList(id);
    }
}
