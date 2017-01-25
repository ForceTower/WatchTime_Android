package com.watchtime.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.watchtime.R;
import com.watchtime.adapters.GenreAdapter;
import com.watchtime.adapters.decorators.DividerItemDecoration;
import com.watchtime.base.providers.media.MediaProvider;
import com.watchtime.base.providers.media.models.Genre;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by João Paulo on 24/01/2017.
 * Genre Layout
 */

public class MediaGenreSelectionFragment extends Fragment {
    public interface Listener {
        void onGenreSelected(String genre);
    }

    public static final String EXTRA_PROVIDER = "extra_provider";

    private Context context;
    private RecyclerView.LayoutManager layoutManager;
    private GenreAdapter genreAdapter;
    private MediaProvider provider;
    private Listener listener;
    private int selectedPosition = 0;

    @Bind(R.id.progressOverlay)
    LinearLayout progressOverlay;
    @Bind(R.id.recyclerView)
    RecyclerView recyclerView;
    @Bind(R.id.emptyView)
    TextView emptyView;
    @Bind(R.id.progress_textview)
    TextView progressTextView;

    public static MediaGenreSelectionFragment newInstance(MediaProvider provider, Listener listener) {
        MediaGenreSelectionFragment fragment = new MediaGenreSelectionFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(EXTRA_PROVIDER, provider);
        fragment.setArguments(bundle);
        fragment.setListener(listener);
        return fragment;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceBundle) {
        super.onCreate(savedInstanceBundle);
        provider = getArguments().getParcelable(EXTRA_PROVIDER);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceBundle) {
        context = getActivity();

        View view = inflater.inflate(R.layout.fragment_media, container, false);
        ButterKnife.bind(this, view);

        layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        List<Genre> genreList = provider.getGenres();

        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL_LIST, R.drawable.list_divider_nospacing));

        genreAdapter = new GenreAdapter(context, genreList, selectedPosition);
        genreAdapter.setOnItemSelectionListener(itemSelectionListener);
        recyclerView.setAdapter(genreAdapter);
    }

    private GenreAdapter.OnItemSelectionListener itemSelectionListener = new GenreAdapter.OnItemSelectionListener() {
        @Override
        public void onItemSelect(View v, Genre item, int position) {
            selectedPosition = position;
            if (listener != null)
                listener.onGenreSelected(item.getKey());
        }
    };
}
