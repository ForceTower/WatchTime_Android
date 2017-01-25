package com.watchtime.fragments.drawer;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.okhttp.Call;
import com.watchtime.R;
import com.watchtime.base.providers.media.MediaProvider;
import com.watchtime.base.providers.media.models.Media;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by João Paulo on 24/01/2017.
 *
 * This is the main screen for all the media the user is going to see in the app.
 */

public class MediaListFragment extends Fragment {
    public static final String EXTRA_PROVIDER = "extra_provider";
    public static final String EXTRA_SORT = "extra_sort";
    public static final String EXTRA_ORDER = "extra_order";
    public static final String EXTRA_GENRE = "extra_genre";
    public static final String EXTRA_MODE = "extra_mode";
    public static final String DIALOG_LOADING_DETAIL = "DIALOG_LOADING_DETAIL";

    public static final int LOADING_DIALOG_FRAGMENT = 1;

    private Context context;
    //private MediaGridAdapter gridAdapter;
    private GridLayoutManager layoutManager;

    //Number of columns and retries to callback;
    private Integer columns = 2, retries = 0;

    private State currentState = State.UNINITIALISED;
    private Mode mode;
    private MediaProvider.Filters.Sort sortDefinition;
    private MediaProvider.Filters.Order orderDefinition;

    public enum Mode {
        NORMAL, SEARCH
    }
    private enum State {
        UNINITIALISED, LOADING, SEARCHING, LOADING_PAGE, LOADED, LOADING_DETAIL
    }

    private ArrayList<Media> items = new ArrayList<>();
    private boolean endOfList = false;

    private int firstVisibleItem, visibleItemCount, totalItemCount = 0, loadingTreshold = columns * 3, previousTotal = 0;

    private MediaProvider provider;
    private Call currentCall;
    private int page = 1;
    private MediaProvider.Filters filters = new MediaProvider.Filters();
    private String genre;

    View rootView;
    @Bind(R.id.progressOverlay)
    LinearLayout progressOverlay;
    @Bind(R.id.recyclerView)
    RecyclerView recyclerView;
    @Bind(R.id.emptyView)
    TextView emptyView;
    @Bind(R.id.progress_textview)
    TextView progressTextView;

    public static MediaListFragment newInstance(Mode mode, MediaProvider provider, MediaProvider.Filters.Sort filter, MediaProvider.Filters.Order defOrder) {
        return newInstance(mode, provider, filter, defOrder, null);
    }

    public static MediaListFragment newInstance(Mode mode, MediaProvider provider, MediaProvider.Filters.Sort filter, MediaProvider.Filters.Order defOrder, String genre) {
        MediaListFragment frag = new MediaListFragment();
        Bundle args = new Bundle();
        args.putParcelable(EXTRA_PROVIDER, provider);
        args.putSerializable(EXTRA_MODE, mode);
        args.putSerializable(EXTRA_SORT, filter);
        args.putSerializable(EXTRA_ORDER, defOrder);
        args.putString(EXTRA_GENRE, genre);
        frag.setArguments(args);
        return frag;
    }

    public void changeGenre(String genre) {
        //setState(State.LOADING);
        //TODO Incomplete;
        this.genre = filters.genre = genre;
        filters.page = 1;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        context = getActivity();

        rootView = inflater.inflate(R.layout.fragment_media, container, false);
        ButterKnife.bind(this, rootView);

        columns = getResources().getInteger(R.integer.overview_cols);
        loadingTreshold = columns * 3;

        layoutManager = new GridLayoutManager(context, columns);
        recyclerView.setLayoutManager(layoutManager);;

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView.setHasFixedSize(true);
        //recyclerView.addOnScrollListener(scrollListener);

        //gridAdapter = new MediaGridAdapter(context, items, columns);
        //gridAdapter.setOnItemClickListener(onItemClickListener);
        //recyclerView.setAdapter(gridAdapter);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        //gridAdapter.setOnItemClickListener(onItemClickListener);
    }

}
