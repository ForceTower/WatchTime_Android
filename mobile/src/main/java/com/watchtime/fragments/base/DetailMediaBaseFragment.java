package com.watchtime.fragments.base;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;

import com.watchtime.activities.MediaDetailsActivity;

/**
 * Created by João Paulo on 17/02/2017.
 */

public class DetailMediaBaseFragment extends Fragment {
    protected MediaDetailsActivity activity;
    protected View rootView;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof MediaDetailsActivity) {
            activity = (MediaDetailsActivity) context;
            Log.d("DetailMediaFragBase", "Yes");
        }
    }
}
