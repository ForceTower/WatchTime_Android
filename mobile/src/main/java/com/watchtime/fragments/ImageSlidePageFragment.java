package com.watchtime.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.watchtime.R;
import com.watchtime.base.utils.AnimUtils;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by João Paulo on 03/03/2017.
 */

public class ImageSlidePageFragment extends Fragment {
    private String url;
    ImageView logo;
    ImageView image;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_image_slider_page, container, false);
        image = (ImageView) view.findViewById(R.id.bg_image);
        logo = (ImageView) view.findViewById(R.id.logo);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        url = getArguments().getString("URL");
        Picasso.with(getContext()).load(url).into(image, new Callback() {
            @Override
            public void onSuccess() {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        AnimUtils.fadeIn(image);
                        logo.setVisibility(View.GONE);
                    }
                });
            }

            @Override
            public void onError() {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Snackbar.make(getView(), "Failed to load Image", Snackbar.LENGTH_LONG).show();
                    }
                });
            }
        });
    }
}
