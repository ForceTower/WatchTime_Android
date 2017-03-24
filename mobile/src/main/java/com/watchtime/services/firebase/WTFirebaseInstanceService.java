package com.watchtime.services.firebase;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.watchtime.base.ApiEndPoints;
import com.watchtime.sdk.AccessTokenWT;
import com.watchtime.sdk.WatchTimeBaseMethods;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Created by João Paulo on 23/03/2017.
 */

public class WTFirebaseInstanceService extends FirebaseInstanceIdService {
    private static final String TAG = WTFirebaseInstanceService.class.getSimpleName();

    public void onTokenRefresh() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.i(TAG, "Refreshed token: " + refreshedToken);

        WatchTimeBaseMethods.getInstance().setFirebaseToken(refreshedToken);
    }
}
