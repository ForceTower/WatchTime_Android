package com.watchtime.fragments.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.watchtime.R;
import com.watchtime.base.WatchTimeApplication;

public class MessageDialogFragment extends DialogFragment {

    public static final String TITLE = "title";
    public static final String MESSAGE = "message";
    public static final String CANCELABLE = "cancelable";

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (!getArguments().containsKey(TITLE) || !getArguments().containsKey(MESSAGE)) {
            return super.onCreateDialog(savedInstanceState);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(getArguments().getString(TITLE))
                .setMessage(getArguments().getString(MESSAGE));

        if(getArguments().getBoolean(CANCELABLE, true)) {
            builder.setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            setCancelable(true);
        } else {
            setCancelable(false);
        }

        return builder.create();
    }

    public static void show(FragmentManager fm, String title, String message) {
        show(fm, title, message, true);
    }

    public static void show(FragmentManager fm, String title, String message, Boolean cancelable) {
        MessageDialogFragment dialogFragment = new MessageDialogFragment();
        Bundle args = new Bundle();
        args.putString(TITLE, title);
        args.putString(MESSAGE, message);
        args.putBoolean(CANCELABLE, cancelable);
        dialogFragment.setArguments(args);
        dialogFragment.show(fm, "overlay_fragment");
    }

    public static void show(FragmentManager fm, int titleRes, int messageRes) {
        show(fm, WatchTimeApplication.getAppContext().getString(titleRes), WatchTimeApplication.getAppContext().getString(messageRes));
    }

    public static void show(FragmentManager fm, int titleRes, int messageRes, Boolean cancelable) {
        show(fm, WatchTimeApplication.getAppContext().getString(titleRes), WatchTimeApplication.getAppContext().getString(messageRes), cancelable);
    }
}
