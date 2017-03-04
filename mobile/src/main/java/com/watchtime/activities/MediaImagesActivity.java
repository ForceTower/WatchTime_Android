package com.watchtime.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.github.clans.fab.FloatingActionButton;
import com.watchtime.R;
import com.watchtime.activities.base.WatchTimeBaseActivity;
import com.watchtime.adapters.ImagePagerAdapter;
import com.watchtime.adapters.transformers.ZoomOutPageTransformer;
import com.watchtime.base.ApiEndPoints;
import com.watchtime.base.providers.media.models.Media;
import com.watchtime.base.utils.PixelUtils;
import com.watchtime.base.utils.VersionUtils;
import com.watchtime.sdk.AccessTokenWT;
import com.watchtime.sdk.Profile;
import com.watchtime.utils.ActionBarBackground;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.view.View.GONE;


public class MediaImagesActivity extends WatchTimeBaseActivity {
    private static Media media;
    @Bind(R.id.pager)
    ViewPager viewPager;
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.image_menu)
    FloatingActionButton floatingBtn;
    TextView toolbarTitle;

    ImagePagerAdapter imagePagerAdapter;

    @SuppressWarnings("unchecked")
    public static void startActivity(Activity context, final Media detail) {
        Intent intent = new Intent(context, MediaImagesActivity.class);
        media = detail;

        if (VersionUtils.isLollipop()) {
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(context);
            context.startActivity(intent, options.toBundle());
        } else {
            context.startActivity(intent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_media_images);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        if(media == null)
            finish();

        setupTransition();
        setupToolbar();
        setupPager();
        setupFloatingButton();
    }

    private void setupToolbar() {
        if (toolbar != null)
            toolbar.setBackgroundColor(media.color);

        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setTitle(media.title + " - Images");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        ActionBarBackground.fadeOut(this);

        if (toolbar.getChildAt(0) instanceof TextView)
            toolbarTitle = (TextView) toolbar.getChildAt(0);
        else
            toolbarTitle = (TextView) toolbar.getChildAt(1);

        toolbarTitle.setVisibility(View.VISIBLE);
    }

    private void setupPager() {
        imagePagerAdapter = new ImagePagerAdapter(getSupportFragmentManager(), media.backdrops);
        viewPager.setPageTransformer(true, new ZoomOutPageTransformer());
        viewPager.setAdapter(imagePagerAdapter);
    }

    private void setupFloatingButton() {
        int normal = media.color;
        int pressed = PixelUtils.colorLighter(media.color);
        int ripple = PixelUtils.colorDarker(media.color);

        floatingBtn.setColorNormal(normal);
        floatingBtn.setColorPressed(pressed);
        floatingBtn.setColorRipple(ripple);

        if (AccessToken.getCurrentAccessToken() != null) {
            floatingBtn.setVisibility(View.VISIBLE);
        } else {
            floatingBtn.setVisibility(GONE);
        }

        floatingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                int position = viewPager.getCurrentItem();
                List<String> urls = new ArrayList<>(media.backdrops.keySet());
                int id = media.backdrops.get(urls.get(position));

                RequestBody requestBody = new FormBody.Builder()
                        .add("id", Integer.toString(id))
                        .build();

                Request request = new Request.Builder()
                        .addHeader("Authorization", "Bearer " + AccessTokenWT.getCurrentAccessToken().getAccessToken())
                        .url(ApiEndPoints.UPDATE_COVER_PICTURE)
                        .post(requestBody)
                        .build();

                Call call = new OkHttpClient().newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.i("MediaImageActivity", "Failed to update cover: " + e.getMessage());
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (!response.isSuccessful()) {
                            Log.i("MediaImageActivity", "Unsuccessful response: " + response.body().string());
                            return;
                        }

                        Log.i("MediaImageActivity", "Success Changing cover");
                        Handler handler = new Handler(getMainLooper());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Snackbar.make(view, getString(R.string.cover_update_success), Snackbar.LENGTH_SHORT).show();
                            }
                        });

                        Profile.fetchProfileForCurrentAccessToken();
                    }
                });
            }
        });
    }

    private void setupTransition() {
        if (VersionUtils.isLollipop()) {
            getWindow().setExitTransition(TransitionInflater.from(this).inflateTransition(android.R.transition.fade));
            getWindow().setEnterTransition(TransitionInflater.from(this).inflateTransition(android.R.transition.explode));
        }
    }
}
