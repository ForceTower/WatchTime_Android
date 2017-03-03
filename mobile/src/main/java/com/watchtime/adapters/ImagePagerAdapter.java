package com.watchtime.adapters;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.watchtime.fragments.ImageSlidePageFragment;

import java.util.ArrayList;
import java.util.List;

public class ImagePagerAdapter extends FragmentStatePagerAdapter {
    private List<String> urls;

    public ImagePagerAdapter(FragmentManager fm, List<String> urls) {
        super(fm);
        if (urls == null) urls = new ArrayList<>();
        this.urls = urls;
    }

    @Override
    public int getCount() {
        return urls.size();
    }

    @Override
    public Fragment getItem(int position) {
        ImageSlidePageFragment imageFragment = new ImageSlidePageFragment();

        Bundle bundle = new Bundle();
        bundle.putString("URL", urls.get(position));
        imageFragment.setArguments(bundle);

        return imageFragment;
    }
}
