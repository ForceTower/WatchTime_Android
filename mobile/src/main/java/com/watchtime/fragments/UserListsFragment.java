package com.watchtime.fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.watchtime.R;
import com.watchtime.adapters.MediaListRowsAdapter;
import com.watchtime.adapters.decorators.DividerItemDecoration;
import com.watchtime.base.WatchTimeApplication;
import com.watchtime.base.content.preferences.Prefs;
import com.watchtime.base.interfaces.OnDataChangeHandler;
import com.watchtime.base.providers.media.MediaProvider;
import com.watchtime.base.providers.media.models.Media;
import com.watchtime.base.utils.LocaleUtils;
import com.watchtime.base.utils.PrefUtils;
import com.watchtime.base.utils.ThreadUtils;
import com.watchtime.sdk.AccessTokenWT;
import com.watchtime.sdk.WatchTimeBaseMethods;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by João Paulo on 10/03/2017.
 */

public class UserListsFragment extends Fragment {
    public static final String EXTRA_PROVIDER = "extra_provider";
    public static final String EXTRA_CATEGORY = "extra_category";

    public static final String DIALOG_LOADING_DETAIL = "DIALOG_LOADING_DETAIL";
    public static final int LOADING_DIALOG_FRAGMENT = 1;

    private Context context;

    private int mLoadingMessage = R.string.loading_data;
    private State currentState = State.UNINITIALISED;

    private enum State {
        UNINITIALISED, LOADING, LOADING_PAGE, LOADED, LOADING_DETAIL
    }

    private int firstVisibleItem, visibleItemCount, totalItemCount = 0, loadingTreshold = 15, previousTotal = 0;

    private ArrayList<Media> items = new ArrayList<>();
    private boolean endOfList = false;

    private MediaProvider provider;
    private int page = 1, retries = 0;
    private MediaProvider.Filters filters = new MediaProvider.Filters();

    private View rootView;
    @Bind(R.id.progressOverlay)
    LinearLayout progressOverlay;
    @Bind(R.id.recyclerView)
    RecyclerView recyclerView;
    @Bind(R.id.emptyView)
    TextView emptyView;
    @Bind(R.id.progress_textview)
    TextView progressTextView;

    LinearLayoutManager layoutManager;
    MediaListRowsAdapter listRowsAdapter;

    public static Fragment newInstance(MediaProvider provider, MediaProvider.Filters.Category category) {
        UserListsFragment fragment = new UserListsFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(EXTRA_PROVIDER, provider);
        bundle.putSerializable(EXTRA_CATEGORY, category);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.user_lists_layout, container, false);
        context = getActivity();

        ButterKnife.bind(this, rootView);

        layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView.setHasFixedSize(true);
        recyclerView.addOnScrollListener(onScrollListener);

        listRowsAdapter = new MediaListRowsAdapter(context, items);
        //listRowsAdapter.setOnItemClickListener(onItemClickListener);
        //recyclerView.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL_LIST));
        recyclerView.setAdapter(listRowsAdapter);

        setupSwipeActions();
    }

    private void setupSwipeActions() {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Media media = items.get(position);

                if(direction == ItemTouchHelper.LEFT) {
                    Log.i("Swipe", "Left Swiped: " + media.title);
                    markWatchedRemoveWatchlist(media.videoId);
                } else if (direction == ItemTouchHelper.RIGHT) {
                    Log.i("Swipe", "Right Swiped: " + media.title);
                    removeFromWatchlist(media.videoId);
                }
            }

            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                Bitmap icon;
                if(actionState == ItemTouchHelper.ACTION_STATE_SWIPE){
                    View itemView = viewHolder.itemView;
                    float height = (float) itemView.getBottom() - (float) itemView.getTop();
                    float width = height / 3;
                    Paint p = new Paint();

                    if(dX > 0){
                        p.setColor(Color.parseColor("#D32F2F"));
                        RectF background = new RectF((float) itemView.getLeft(), (float) itemView.getTop(), dX,(float) itemView.getBottom());
                        c.drawRect(background,p);
                        icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_delete_white);
                        RectF icon_dest = new RectF((float) itemView.getLeft() + width ,(float) itemView.getTop() + width,(float) itemView.getLeft()+ 2*width,(float)itemView.getBottom() - width);
                        c.drawBitmap(icon,null,icon_dest,p);
                    } else {
                        p.setColor(Color.parseColor("#388E3C"));
                        RectF background = new RectF((float) itemView.getRight() + dX, (float) itemView.getTop(),(float) itemView.getRight(), (float) itemView.getBottom());
                        c.drawRect(background,p);
                        icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_mark_watched);
                        RectF icon_dest = new RectF((float) itemView.getRight() - 2*width ,(float) itemView.getTop() + width,(float) itemView.getRight() - width,(float)itemView.getBottom() - width);
                        c.drawBitmap(icon,null,icon_dest,p);
                    }
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        ((WatchTimeApplication)getActivity().getApplication()).getDataChangeHandler().registerListener("user-lists", dataChanged, new int[] {OnDataChangeHandler.ALL});
        //listRowsAdapter.setOnItemClickListener(onItemClickListener);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        ((WatchTimeApplication)getActivity().getApplication()).getDataChangeHandler().unregisterListener("user-lists");
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        provider = getArguments().getParcelable(EXTRA_PROVIDER);
        String language = PrefUtils.get(getActivity(), Prefs.LOCALE, WatchTimeApplication.getSystemLanguage());
        filters.langCode = LocaleUtils.toLocale(language).getLanguage();

        filters.category = (MediaProvider.Filters.Category) getArguments().getSerializable(EXTRA_CATEGORY);
        provider.getList(new ArrayList<Media>(), new MediaProvider.Filters(filters), callback, AccessTokenWT.getCurrentAccessToken().getAccessToken());
        setState(State.LOADING);

        ((WatchTimeApplication)getActivity().getApplication()).getDataChangeHandler().registerListener("user-lists", dataChanged, new int[] {OnDataChangeHandler.ALL});
    }

    private void setState(State state) {
        if (currentState == state) return;
        currentState = state;
        updateUI();
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
                    default:
                        int providerMessage = provider.getLoadingMessage();
                        mLoadingMessage = providerMessage > 0 ? providerMessage : R.string.loading_data;
                        break;
                }

                switch (currentState) {
                    case LOADING_DETAIL:
                    case LOADING:
                        if (listRowsAdapter.isLoading())
                            listRowsAdapter.removeLoading();

                        recyclerView.setVisibility(View.VISIBLE);

                        emptyView.setVisibility(View.GONE);
                        progressOverlay.setVisibility(View.VISIBLE);
                        break;
                    case LOADED:
                        if (listRowsAdapter.isLoading())
                            listRowsAdapter.removeLoading();

                        progressOverlay.setVisibility(View.GONE);
                        boolean hasItems = items.size() > 0;

                        recyclerView.setVisibility(hasItems ? View.VISIBLE : View.INVISIBLE);
                        emptyView.setVisibility(hasItems ? View.GONE : View.VISIBLE);
                        break;
                    case LOADING_PAGE:
                        //if (!listRowsAdapter.isLoading())
                           // listRowsAdapter.addLoading();

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

    private MediaProvider.Callback callback = new MediaProvider.Callback() {
        @Override
        public void onSuccess(MediaProvider.Filters filters, final ArrayList<Media> list, boolean changed) {
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

            if (!isAdded())
                return;

            endOfList = false;

            page = page + 1;
            ThreadUtils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    listRowsAdapter.setItems(list);
                    previousTotal = totalItemCount = listRowsAdapter.getItemCount();
                }
            });
        }

        @Override
        public void onFailure(Exception e) {
            if (e.getMessage().equals("InvalidToken")) {
                if (WatchTimeBaseMethods.getInstance().refreshToken()) {
                    provider.getList(items, new MediaProvider.Filters(filters), this, AccessTokenWT.getCurrentAccessToken().getAccessToken());
                    return;
                }
            }

            if (isDetached() || (e.getMessage().equals("Canceled"))) {
                ThreadUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (listRowsAdapter == null) {
                            return;
                        }

                        listRowsAdapter.removeLoading();
                        setState(State.LOADED);
                    }
                });
            } else if (e.getMessage() != null && e.getMessage().equals(WatchTimeApplication.getAppContext().getString(R.string.movies_error))) {
                endOfList = true;
                ThreadUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (listRowsAdapter == null) {
                            return;
                        }

                        listRowsAdapter.removeLoading();
                        setState(State.LOADED);
                    }
                });
            } else {
                if (retries > 1) {
                    e.printStackTrace();
                    ThreadUtils.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Snackbar.make(rootView, R.string.unknown_error, Snackbar.LENGTH_SHORT).show();
                            setState(State.LOADED);
                        }
                    });
                } else {
                    provider.getList(items, new MediaProvider.Filters(filters), this, AccessTokenWT.getCurrentAccessToken().getAccessToken());
                }
                retries++;
            }
        }
    };

    private RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            visibleItemCount = layoutManager.getChildCount();
            totalItemCount = layoutManager.getItemCount() - (listRowsAdapter.isLoading() ? 1 : 0);
            firstVisibleItem = layoutManager.findFirstVisibleItemPosition();

            if (currentState == State.LOADING_PAGE) {
                if (totalItemCount > previousTotal) {
                    previousTotal = totalItemCount;
                    previousTotal = totalItemCount = layoutManager.getItemCount();
                    setState(State.LOADED);
                }
            }

            if (!endOfList && !(currentState == State.LOADING_PAGE) && !(currentState == State.LOADING) && (totalItemCount - visibleItemCount) <= (firstVisibleItem +
                    loadingTreshold)) {

                filters.page = page;
                //provider.getList(items, new MediaProvider.Filters(filters), callback, AccessTokenWT.getCurrentAccessToken().getAccessToken());

                previousTotal = totalItemCount = layoutManager.getItemCount();
                setState(State.LOADING_PAGE);
            }
        }
    };

    private OnDataChangeHandler.OnDataChangeListener dataChanged = new OnDataChangeHandler.OnDataChangeListener() {
        @Override
        public void onDataChange() {
            provider.getList(new ArrayList<Media>(), new MediaProvider.Filters(filters), callback, AccessTokenWT.getCurrentAccessToken().getAccessToken());
            setState(State.LOADING);
            Log.d("UserListsFrag", "onDataChange: Changed!");
        }
    };

    private void markWatchedRemoveWatchlist(String tmdb) {
        WatchTimeBaseMethods.getInstance().markMovieAsWatched(tmdb);
    }

    private void removeFromWatchlist(String tmdb) {
        WatchTimeBaseMethods.getInstance().removeFromWatchlist(tmdb);
    }
}
