package com.watchtime.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.watchtime.R;
import com.watchtime.activities.MainActivity;
import com.watchtime.adapters.MediaPagerAdapter;
import com.watchtime.base.providers.media.MediaProvider;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by João Paulo on 24/01/2017.
 */

public class MediaContainerFragment extends Fragment{
    public static final String EXTRA_PROVIDER = "provider";

    private MediaPagerAdapter pagerAdapter;
    private MediaProvider provider;
    private Integer selection = 0;

    @Bind(R.id.pager)
    ViewPager viewPager;

    public static MediaContainerFragment newInstance(MediaProvider provider) {
        MediaContainerFragment fragment = new MediaContainerFragment();
        Bundle args = new Bundle();
        args.putParcelable(EXTRA_PROVIDER, provider);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_media_container, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ButterKnife.bind(this, view);

        provider = getArguments().getParcelable(EXTRA_PROVIDER);
        pagerAdapter = new MediaPageAdapter(provider, getChildFragmentManager(), provider.getNavigation());
        viewPager.setAdapter(pagerAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                selection = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        selection = provider.getDefaultNavigationIndex();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ((MainActivity)getActivity()).updateTabs(this, selection);
    }

    public ViewPager getViewPager() {
        return viewPager;
    }

    public Integer getCurrentSelection() {
        return selection;
    }
}
