package com.watchtime.utils;

import android.content.Context;
import android.support.v7.widget.Toolbar;

import com.watchtime.R;

/**
 * Created by João Paulo on 23/01/2017.
 */

public class ToolbarUtils {
    public static void updateToolbarHeight(Context context, Toolbar toolbar) {
        toolbar.getLayoutParams().height = context.getResources().getDimensionPixelSize(R.dimen.abc_action_bar_default_height_material);
    }
}
