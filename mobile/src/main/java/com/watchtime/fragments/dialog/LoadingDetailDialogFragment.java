package com.watchtime.fragments.dialog;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.watchtime.R;
import com.watchtime.base.providers.media.MediaProvider;
import com.watchtime.base.providers.media.models.Media;

import java.util.ArrayList;

/**
 * Created by João Paulo on 17/02/2017.
 */

public class LoadingDetailDialogFragment extends DialogFragment {
    public interface Callback {
        void onDetailLoadFailure();
        void onDetailLoadSuccess(Media item);
        ArrayList<Media> getCurrentList();
    }

    private Callback callback;
    public static final String EXTRA_MEDIA = "extra_media";
    private MediaProvider provider;
    private boolean savedInstance = false;

    public static LoadingDetailDialogFragment newInstance(int position) {
        Bundle args = new Bundle();
        args.putInt(EXTRA_MEDIA, position);
        LoadingDetailDialogFragment fragment = new LoadingDetailDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return LayoutInflater.from(new ContextThemeWrapper(getActivity(), R.style.Theme_WatchTime)).inflate(R.layout.fragment_loading_details, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        savedInstance = false;
        setStyle(STYLE_NO_FRAME, R.style.Theme_Dialog_Transparent);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (provider != null)
            provider.cancel();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (getTargetFragment() == null)
            throw new IllegalArgumentException("You forgot to set the target Fragment...");

        if (getTargetFragment() instanceof Callback)
            callback = (Callback)getTargetFragment();
        else
            throw new IllegalArgumentException("You forgot to implement callback...");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        savedInstance = true;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ArrayList<Media> list = callback.getCurrentList();
        int position = getArguments().getInt(EXTRA_MEDIA);
        Media short_media = list.get(position);
        provider = short_media.getMediaProvider();
        final int color = short_media.color;

        provider.getDetail(list, position, new MediaProvider.Callback() {
            @Override
            public void onSuccess(MediaProvider.Filters filters, ArrayList<Media> items, boolean changed) {
                final Media media = items.get(0);
                media.color = color;

                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onDetailLoadSuccess(media);
                        if (!savedInstance) dismiss();
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onDetailLoadFailure();
                        if (!savedInstance) dismiss();
                    }
                });
            }
        }, null);

    }
}
