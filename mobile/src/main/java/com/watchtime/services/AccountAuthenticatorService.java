package com.watchtime.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.watchtime.account_manager.AccountAuthenticator;

public class AccountAuthenticatorService extends Service{

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i("AccMgr - AccActorSvc", "Binding");
        AccountAuthenticator authenticator = new AccountAuthenticator(this);
        return authenticator.getIBinder();
    }
}
