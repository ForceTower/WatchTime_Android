package com.watchtime.fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.watchtime.R;
import com.watchtime.activities.MediaDetailsActivity;
import com.watchtime.adapters.MediaGridAdapter;
import com.watchtime.base.WatchTimeApplication;
import com.watchtime.base.content.preferences.Prefs;
import com.watchtime.base.providers.media.MediaProvider;
import com.watchtime.base.providers.media.models.Media;
import com.watchtime.base.utils.LocaleUtils;
import com.watchtime.base.utils.PrefUtils;
import com.watchtime.base.utils.ThreadUtils;
import com.watchtime.base.utils.VersionUtils;
import com.watchtime.fragments.dialog.LoadingDetailDialogFragment;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import okhttp3.Call;

/**
 * Created by João Paulo on 24/01/2017.
 *
 * This is the main screen for all the media the user is going to see in the app.
 */

public class MediaListFragment extends Fragment implements LoadingDetailDialogFragment.Callback{
    public static final String EXTRA_PROVIDER = "extra_provider";
    public static final String EXTRA_SORT = "extra_sort";
    public static final String EXTRA_ORDER = "extra_order";
    public static final String EXTRA_GENRE = "extra_genre";
    public static final String EXTRA_MODE = "extra_mode";
    public static final String DIALOG_LOADING_DETAIL = "DIALOG_LOADING_DETAIL";

    public static final int LOADING_DIALOG_FRAGMENT = 1;

    private Context context;
    private MediaGridAdapter gridAdapter;
    private GridLayoutManager layoutManager;

    //Number of columns and retries to callback;
    private Integer columns = 2, retries = 0;
    private int mLoadingMessage = R.string.loading_data;

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
        gridAdapter.clearItems();
        this.genre = filters.genre = genre;
        filters.page = 1;
        currentCall = provider.getList(new MediaProvider.Filters(filters), callback);
        setState(State.LOADING);
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
        recyclerView.setLayoutManager(layoutManager);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView.setHasFixedSize(true);
        recyclerView.addOnScrollListener(onScrollListener);

        gridAdapter = new MediaGridAdapter(context, items, columns);
        gridAdapter.setOnItemClickListener(onItemClickListener);
        recyclerView.setAdapter(gridAdapter);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        gridAdapter.setOnItemClickListener(onItemClickListener);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        provider = getArguments().getParcelable(EXTRA_PROVIDER);

        String language = PrefUtils.get(getActivity(), Prefs.LOCALE, WatchTimeApplication.getSystemLanguage());
        filters.langCode = LocaleUtils.toLocale(language).getLanguage();

        mode = (Mode) getArguments().getSerializable(EXTRA_MODE);
        if (mode == Mode.SEARCH) emptyView.setText(getString(R.string.no_search_results));

        if (mode != Mode.SEARCH && gridAdapter.getItemCount() == 0) {
            currentCall = provider.getList(new MediaProvider.Filters(filters), callback);
            setState(State.LOADING);
        } else {
            updateUI();
        }
    }

    private void updateUI() {
        if (!isAdded()) return;

        ThreadUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (currentState) {
                    case LOADING_DETAIL:
                        mLoadingMessage = R.string.loading_details;
                        break;
                    case SEARCHING:
                        mLoadingMessage = R.string.searching;
                        break;
                    default:
                        int providerMessage = provider.getLoadingMessage();
                        mLoadingMessage = providerMessage > 0 ? providerMessage : R.string.loading_data;
                        break;
                }

                switch (currentState) {
                    case LOADING_DETAIL:
                    case SEARCHING:
                    case LOADING:
                        if (gridAdapter.isLoading())
                            gridAdapter.removeLoading();

                        recyclerView.setVisibility(View.VISIBLE);

                        emptyView.setVisibility(View.GONE);
                        progressOverlay.setVisibility(View.VISIBLE);
                        break;
                    case LOADED:
                        if (gridAdapter.isLoading())
                            gridAdapter.removeLoading();

                        progressOverlay.setVisibility(View.GONE);
                        boolean hasItems = items.size() > 0;

                        recyclerView.setVisibility(hasItems ? View.VISIBLE : View.INVISIBLE);
                        emptyView.setVisibility(hasItems ? View.GONE : View.VISIBLE);
                        break;
                    case LOADING_PAGE:
                        if (!gridAdapter.isLoading())
                            gridAdapter.addLoading();

                        emptyView.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        break;
                }
                updateLoadingMessage();
            }
        });
    }

    private void updateLoadingMessage() {
        progressTextView.setText(mLoadingMessage);
    }

    private void setState(State state) {
        if (currentState == state) return;//do nothing
        currentState = state;
        updateUI();
    }

    private MediaProvider.Callback callback = new MediaProvider.Callback() {
        @Override
        public void onSuccess(MediaProvider.Filters filters, final ArrayList<Media> list, boolean changed) {
            //if (!(genre == null ? "" : genre).equals(filters.genre == null ? "" : filters.genre)) return; // nothing changed according to the provider, so don't do anything
            if(!changed) {
                setState(State.LOADED);
                return;
            }

            items.clear();
            if (list != null) {
                items.addAll(list);
            }
            ThreadUtils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setState(State.LOADED);
                }
            });

            //fragment may be detached, so we dont want to update the UI
            if (!isAdded())
                return;

            endOfList = false;

            page = page + 1;
            ThreadUtils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    gridAdapter.setItems(list);
                    previousTotal = totalItemCount = gridAdapter.getItemCount();
                }
            });
        }

        @Override
        public void onFailure(Exception e) {
            if (isDetached() || e.getMessage().equals("Canceled")) {
                ThreadUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (gridAdapter == null) {
                            return;
                        }

                        gridAdapter.removeLoading();
                        setState(State.LOADED);
                    }
                });
            } else if (e.getMessage() != null && e.getMessage().equals(WatchTimeApplication.getAppContext().getString(R.string.movies_error))) {
                endOfList = true;
                ThreadUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (gridAdapter == null) {
                            return;
                        }

                        gridAdapter.removeLoading();
                        setState(State.LOADED);
                    }
                });
            } else {
                e.printStackTrace();
                if (retries > 1) {
                    ThreadUtils.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Snackbar.make(rootView, R.string.unknown_error, Snackbar.LENGTH_SHORT).show();
                            setState(State.LOADED);
                        }
                    });
                } else {
                    currentCall = provider.getList(items, new MediaProvider.Filters(filters), this);
                }
                retries++;
            }
        }
    };

    private RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            visibleItemCount = layoutManager.getChildCount();
            totalItemCount = layoutManager.getItemCount() - (gridAdapter.isLoading() ? 1 : 0);
            firstVisibleItem = layoutManager.findFirstVisibleItemPosition();

            if (currentState == State.LOADING_PAGE) {
                if (totalItemCount > previousTotal) {
                    previousTotal = totalItemCount;
                    previousTotal = totalItemCount = layoutManager.getItemCount();
                    setState(State.LOADED);
                }
            }

            if (!endOfList && !(currentState == State.SEARCHING) && !(currentState == State.LOADING_PAGE) && !(currentState == State.LOADING) && (totalItemCount - visibleItemCount) <= (firstVisibleItem +
                    loadingTreshold)) {

                filters.page = page;
                currentCall = provider.getList(items, new MediaProvider.Filters(filters), callback);

                previousTotal = totalItemCount = layoutManager.getItemCount();
                setState(State.LOADING_PAGE);
            }
        }
    };

    private MediaGridAdapter.OnItemClickListener onItemClickListener = new MediaGridAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(final View v, final Media item, final int position) {
            RecyclerView.ViewHolder holder = recyclerView.getChildViewHolder(v);

            if (holder instanceof  MediaGridAdapter.ViewHolder) {
                final ImageView cover = ((MediaGridAdapter.ViewHolder) holder).getCoverImage();

                if (cover.getDrawable() == null) {
                    showLoadingDialog(null, position);
                    return;
                }

                Bitmap coverBitmap = ((BitmapDrawable)cover.getDrawable()).getBitmap();
                Palette.generateAsync(coverBitmap, 5, new Palette.PaletteAsyncListener() {
                    @Override
                    public void onGenerated(Palette palette) {
                        int vibrantColor = palette.getVibrantColor(-1);
                        int paletteColor;
                        if (vibrantColor == -1) {
                            paletteColor = palette.getMutedColor(ContextCompat.getColor(getContext(), R.color.primary));
                        } else {
                            paletteColor = vibrantColor;
                        }
                        item.color = paletteColor;
                        showLoadingDialog(cover, position);
                    }
                });
            } else {
                showLoadingDialog(null, position);
            }
        }
    };

    private void showLoadingDialog(ImageView transitionImage, int position) {
        LoadingDetailDialogFragment loadingFragment = LoadingDetailDialogFragment.newInstance(position, transitionImage);
        loadingFragment.setTargetFragment(MediaListFragment.this, LOADING_DIALOG_FRAGMENT);
        loadingFragment.show(getFragmentManager(), DIALOG_LOADING_DETAIL);
    }

    @Override
    public void onDetailLoadFailure() {
        Snackbar.make(rootView, R.string.unknown_error, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onDetailLoadSuccess(final Media item, ImageView cover) {
        if (VersionUtils.isLollipop()) {
            @SuppressWarnings("unchecked")
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(), cover, cover.getTransitionName());
            MediaDetailsActivity.startActivity(context, item, options.toBundle(), cover);
        }
        else {
            MediaDetailsActivity.startActivity(context, item);
        }
    }

    @Override
    public ArrayList<Media> getCurrentList() {
        return items;
    }
}
