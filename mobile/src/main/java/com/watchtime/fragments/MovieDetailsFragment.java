package com.watchtime.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.watchtime.R;
import com.watchtime.base.providers.media.models.Movie;
import com.watchtime.fragments.base.DetailMediaBaseFragment;

import butterknife.ButterKnife;

/**
 * Created by João Paulo on 17/02/2017.
 */

public class MovieDetailsFragment extends DetailMediaBaseFragment {
    private static Movie movie;

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

        return rootView;
    }
}
