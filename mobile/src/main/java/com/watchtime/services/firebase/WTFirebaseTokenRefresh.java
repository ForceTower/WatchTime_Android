package com.watchtime.services.firebase;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;

import java.io.IOException;

public class WTFirebaseTokenRefresh extends IntentService {
    private static final String ACTION_REFRESH_FIREBASE_TOKEN = "com.watchtime.services.firebase.action.RefreshTokenFirebase";

    public WTFirebaseTokenRefresh() {
        super("WTFirebaseTokenRefresh");
    }

    public static void startActionRefreshFirebase(Context context) {
        Intent intent = new Intent(context, WTFirebaseTokenRefresh.class);
        intent.setAction(ACTION_REFRESH_FIREBASE_TOKEN);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_REFRESH_FIREBASE_TOKEN.equals(action)) {
                handleActionRefreshFirebaseToken();
            }
        }
    }


    private void handleActionRefreshFirebaseToken() {
        try {
            FirebaseInstanceId.getInstance().deleteInstanceId();
        } catch (IOException e) {
            Log.i("FirebaseRFT", "Failed Generating a new Firebase token. Have you created a token before?");
        }
    }
}
