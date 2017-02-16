package com.watchtime.base.providers;

import android.os.AsyncTask;

import com.google.gson.Gson;
import com.watchtime.base.WatchTimeApplication;
import com.watchtime.base.providers.media.MediaProvider;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.concurrent.ExecutionException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public abstract class BaseProvider {
    protected Gson gson = new Gson();
    protected Call currentCall;

    protected OkHttpClient getClient() {
        return WatchTimeApplication.getHttpClient();
    }

    protected Call enqueue(Request request) {
        return enqueue(request, null);
    }

    protected Call enqueue(Request request, Callback requestCallback) {
        currentCall = getClient().newCall(request);
        if (requestCallback != null) currentCall.enqueue(requestCallback);
        return currentCall;
    }

    public void cancel() {
        try {
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    cancelRequest(MediaProvider.MEDIA_CALL);
                    return null;
                }
            }.execute().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    private void cancelRequest(String tag) {
        for (Call call : getClient().dispatcher().queuedCalls()) {
            if (call.request().tag().equals(tag))
                call.cancel();
        }

        for(Call call : getClient().dispatcher().runningCalls())
            if(call.request().tag().equals(tag))
                call.cancel();
    }

    protected String buildQuery(List<NameValuePair> valuePairs) {
        StringBuilder stringBuilder = new StringBuilder();

        try {
            for (int i = 0; i < valuePairs.size(); i++) {
                NameValuePair pair = valuePairs.get(i);
                stringBuilder.append(URLEncoder.encode(pair.getName(), "utf-8"));
                stringBuilder.append("=");
                stringBuilder.append(URLEncoder.encode(pair.getValue(), "utf-8"));
                if (i + 1 != valuePairs.size()) stringBuilder.append("&");
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }


        return stringBuilder.toString();
    }

    public class NameValuePair {
        private String mName;
        private String mValue;

        public NameValuePair(String name, String value) {
            mName = name;
            mValue = value;
        }

        public String getName() {
            return mName;
        }

        public String getValue() {
            return mValue;
        }
    }
}
