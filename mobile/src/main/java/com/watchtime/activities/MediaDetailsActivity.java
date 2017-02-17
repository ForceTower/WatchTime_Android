package com.watchtime.activities;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.transition.TransitionInflater;
import android.util.Log;
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
import com.watchtime.base.utils.AnimUtils;
import com.watchtime.base.utils.PixelUtils;
import com.watchtime.base.utils.VersionUtils;
import com.watchtime.utils.ActionBarBackground;
import com.watchtime.widget.ObservableParallaxScrollView;

import butterknife.Bind;

public class MediaDetailsActivity extends WatchTimeBaseActivity {
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

    public static ImageView prevCover;

    public static void startActivity(Context context, final Media detail, Bundle options, ImageView view) {
        Intent intent = new Intent(context, MediaDetailsActivity.class);

        if (options == null) {
            options = new Bundle();
            Log.d("DetailsActivity", "Bundle was null");
        }
        //options.putParcelable("Media", detail);
        media = detail;
        prevCover = view;
        context.startActivity(intent, options);
    }

    public static void startActivity(Context context, final Media media) {
        startActivity(context, media, null, null);
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

        if (prevCover != null) {
            logo.setImageDrawable(prevCover.getDrawable());
        }

        getSupportActionBar().setTitle(media.title);
        //scrollView.setListener(onScrollListener);

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

        //TODO: Change content fragment
        if (VersionUtils.isLollipop()) {
            backgroundImage.setTransitionName(getString(R.string.cover_image_transition));
            logo.setTransitionName(getString(R.string.cover_image_transition));
        }

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
                    }
                });
            }

            @Override
            public void onError() {
            }
        });
    }
}
