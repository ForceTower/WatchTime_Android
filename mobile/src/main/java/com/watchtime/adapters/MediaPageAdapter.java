package com.watchtime.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import com.watchtime.R;
import com.watchtime.base.WatchTimeApplication;
import com.watchtime.base.providers.media.MediaProvider;
import com.watchtime.base.utils.LocaleUtils;
import com.watchtime.fragments.MediaGenreSelectionFragment;
import com.watchtime.fragments.MediaListFragment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by João Paulo on 24/01/2017.
 * For a Selected type of media (eg Movies or Shows), creates a layout with all the media, separated some categories
 */

public class MediaPageAdapter extends FragmentPagerAdapter {
    private FragmentManager fragmentManager;
    private Map<Integer, String> fragmentsTags = new HashMap<>();
    private final List<MediaProvider.NavInfo> tabs;
    private MediaProvider provider;
    private String genre;
    private int hasGenreTab = 0;
    private Fragment genreTabFragment;

    public MediaPageAdapter(MediaProvider provider, FragmentManager manager, List<MediaProvider.NavInfo> tabs) {
        super(manager);
        fragmentManager = manager;
        this.tabs = tabs;
        this.provider = provider;
        hasGenreTab = (provider.getGenres() != null && provider.getGenres().size() > 0) ? 1 : 0;
    }

    @Override
    public int getCount() {
        return tabs.size() + hasGenreTab;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (hasGenreTab > 0 && position == 0)
            return WatchTimeApplication.getAppContext().getString(R.string.genres).toUpperCase(LocaleUtils.getCurrent());

        position -= hasGenreTab;
        return tabs.get(position).getLabel().toUpperCase(LocaleUtils.getCurrent());
    }

    @Override
    public Fragment getItem(int position) {
        if (hasGenreTab > 0 && position == 0) {
            if (genreTabFragment != null)
                return genreTabFragment;
            genreTabFragment = MediaGenreSelectionFragment.newInstance(provider, mediaGenreSelectionFragment);
            return genreTabFragment;
        }

        position -= hasGenreTab;
        return MediaListFragment.newInstance(MediaListFragment.Mode.NORMAL, provider, tabs.get(position).getFilter(), tabs.get(position).getOrder(), genre);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Object obj = super.instantiateItem(container, position);

        if (obj instanceof Fragment) {
            Fragment f = (Fragment)obj;
            String tag = f.getTag();
            fragmentsTags.put(position, tag);
        }

        if (obj instanceof MediaGenreSelectionFragment && genreTabFragment != null)
            return genreTabFragment;

        return obj;
    }

    public MediaListFragment getMediaListFragment(int position) {
        if (fragmentsTags.size() > position) {
            String tag = fragmentsTags.get(position);
            if (tag != null) {
                Fragment frag = fragmentManager.findFragmentByTag(tag);
                if (frag instanceof MediaListFragment)
                    return (MediaListFragment)frag;
            }
        }
        return null;
    }

    private MediaGenreSelectionFragment.Listener mediaGenreSelectionFragment = new MediaGenreSelectionFragment.Listener() {
        @Override
        public void onGenreSelected(String genre) {
            MediaPageAdapter.this.genre = genre;
            provider.cancel();
            for (int i = 0; i < getCount(); i++) {
                MediaListFragment mediaListFragment = getMediaListFragment(i);
                if (mediaListFragment != null)
                    mediaListFragment.changeGenre(genre);
            }
        }
    };
}
