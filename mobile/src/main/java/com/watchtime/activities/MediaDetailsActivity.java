package com.watchtime.activities;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.watchtime.R;
import com.watchtime.activities.base.WatchTimeBaseActivity;
import com.watchtime.base.providers.media.models.Media;
import com.watchtime.base.providers.media.models.Movie;
import com.watchtime.base.utils.AnimUtils;
import com.watchtime.base.utils.PixelUtils;
import com.watchtime.base.utils.VersionUtils;
import com.watchtime.fragments.MovieDetailsFragment;
import com.watchtime.utils.ActionBarBackground;
import com.watchtime.widget.ObservableParallaxScrollView;

import butterknife.Bind;

public class MediaDetailsActivity extends WatchTimeBaseActivity {
    public interface ActivityToFragmentEvents {
        boolean onBackPressed();
        boolean onTouchEvent(MotionEvent event);
    }

    private static Media media;
    private int headerHeight = 0, toolbarHeight = 0, topHeight;
    private boolean transparentBar = true, isTablet = false;

    @Bind(R.id.toolbar)
    Toolbar toolbar;
    TextView toolbarTitle;

    @Bind(R.id.scrollview)
    ObservableParallaxScrollView scrollView;
    @Nullable
    @Bind(R.id.parallax)
    RelativeLayout parallaxLayout;

    @Nullable
    @Bind(R.id.parallax_color)
    View parallaxColor;

    @Bind(R.id.content)
    FrameLayout content;
    @Bind(R.id.logo)
    ImageView logo;
    @Bind(R.id.bg_image)
    ImageView backgroundImage;

    private Fragment fragment = null;

    public static void startActivity(Context context, final Media detail) {
        Intent intent = new Intent(context, MediaDetailsActivity.class);
        media = detail;
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        super.onCreate(savedInstanceState, R.layout.activity_media_details);

        setSupportActionBar(toolbar);

        if(getSupportActionBar() != null) {
            getSupportActionBar().setTitle("   ");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        ActionBarBackground.fadeOut(this);

        if (toolbar.getChildAt(0) instanceof TextView)
            toolbarTitle = (TextView) toolbar.getChildAt(0);
        else
            toolbarTitle = (TextView) toolbar.getChildAt(1);

        toolbarTitle.setVisibility(View.INVISIBLE);

        isTablet = parallaxLayout == null;


        if (media == null) {
            finish();
            return;
        }

        getSupportActionBar().setTitle(media.title);
        scrollView.setListener(onScrollListener);

        scrollView.setOverScrollEnabled(false);

        if (!isTablet) {
            int parallaxHeight = parallaxLayout.getLayoutParams().height = PixelUtils.getScreenHeight(this);
            topHeight = (parallaxHeight / 3) * 2;
            ((LinearLayout.LayoutParams) content.getLayoutParams()).topMargin = -(parallaxHeight / 3);
            content.setMinimumHeight(topHeight / 3);

            parallaxColor.setBackgroundColor(media.color);
            parallaxColor.getBackground().setAlpha(0);
            toolbar.setBackgroundColor(media.color);
            toolbar.getBackground().setAlpha(0);
        } else {
            topHeight = (PixelUtils.getScreenHeight(this) / 2);
            ((LinearLayout.LayoutParams) content.getLayoutParams()).topMargin = topHeight;
            content.setMinimumHeight(topHeight);
        }

        fragment = null;

        if (media.isMovie) {
            fragment = MovieDetailsFragment.newInstance((Movie)media);
        }

        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.content, fragment, media.title + "_tag").commit();
        }

        if (VersionUtils.isLollipop()) {
            backgroundImage.setTransitionName(getString(R.string.cover_image_transition));
            logo.setTransitionName(getString(R.string.cover_image_transition));

            getWindow().setExitTransition(TransitionInflater.from(this).inflateTransition(android.R.transition.fade));
            getWindow().setEnterTransition(TransitionInflater.from(this).inflateTransition(android.R.transition.fade));
            getWindow().setNavigationBarColor(media.color);
        }

        backgroundImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Snackbar.make(v, "Show More Pictures Activity will be created soon", Snackbar.LENGTH_SHORT).show();
                if (media.backdrops != null && !media.backdrops.isEmpty()) {
                    if (VersionUtils.isLollipop())
                        getWindow().setExitTransition(TransitionInflater.from(MediaDetailsActivity.this).inflateTransition(android.R.transition.explode));

                    MediaImagesActivity.startActivity(MediaDetailsActivity.this, media);
                }
                else {
                    Snackbar.make(v, getString(R.string.no_images_to_show), Snackbar.LENGTH_SHORT).show();
                }
            }
        });

        String imageUrl = media.image;
        if (isTablet || !PixelUtils.screenIsPortrait(this)) {
            imageUrl = media.headerImage;
        }

        Picasso.with(this).load(imageUrl).into(backgroundImage, new Callback() {
            @Override
            public void onSuccess() {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        AnimUtils.fadeIn(backgroundImage);
                        logo.setVisibility(View.GONE);
                        Log.d("Media details", "Success");
                    }
                });
            }

            @Override
            public void onError() {
                Log.d("Media details", "Error!!!!!");
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (fragment != null && fragment instanceof ActivityToFragmentEvents)
            ((ActivityToFragmentEvents) fragment).onTouchEvent(event);

        return super.dispatchTouchEvent(event);
    }

    @Override
    public void onBackPressed() {
        if (fragment != null && fragment instanceof ActivityToFragmentEvents) {
            if (((ActivityToFragmentEvents) fragment).onBackPressed())
                return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        supportInvalidateOptionsMenu();
    }

    private ObservableParallaxScrollView.Listener onScrollListener = new ObservableParallaxScrollView.Listener() {
        @Override
        public void onScroll(int scrollY, ObservableParallaxScrollView.Direction direction) {
            if (toolbarHeight == 0) {
                toolbarHeight = toolbar.getHeight();
                headerHeight = topHeight - toolbarHeight;
            }

            if (!isTablet) {
                if (scrollY > 0) {
                    if (scrollY < headerHeight) {
                        float diff = (float) scrollY / (float) headerHeight;
                        int alpha = (int) Math.ceil(255 * diff);
                        parallaxColor.getBackground().setAlpha(alpha);
                        toolbar.getBackground().setAlpha(0);
                        AnimUtils.fadeOut(toolbarTitle);
                    } else {
                        toolbar.getBackground().setAlpha(255);
                        parallaxColor.getBackground().setAlpha(255);
                        AnimUtils.fadeIn(toolbarTitle);
                    }
                }
            } else {
                if (topHeight - scrollY < 0) {
                    if (transparentBar) {
                        transparentBar = false;
                        ActionBarBackground.changeColor(MediaDetailsActivity.this, media.color, true);
                    }
                } else {
                    if (!transparentBar) {
                        transparentBar = true;
                        ActionBarBackground.fadeOut(MediaDetailsActivity.this);
                    }
                }
            }
        }
    };
}
