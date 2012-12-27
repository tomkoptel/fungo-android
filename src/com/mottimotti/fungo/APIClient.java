package com.mottimotti.fungo;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import org.apache.http.entity.StringEntity;

import java.io.UnsupportedEncodingException;

public class APIClient {
    private AsyncHttpClient client;

    private Context context;
    private static final int TIMEOUT = 5000;

    public APIClient(Context context) {
        this.context = context;
        this.client = new AsyncHttpClient();
        this.client.setTimeout(TIMEOUT);
    }

    private String getAbsoluteUrl(String relativeUrl) {
        return context.getString(R.string.BASE_URL) + relativeUrl;
    }

    public boolean isOnline() {
        return APIClient.networkEnabled(context);
    }

    public static Boolean networkEnabled(Context context) {
        String service = Context.CONNECTIVITY_SERVICE;
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(service);
        NetworkInfo activeNetwork = connectivity.getActiveNetworkInfo();

        if (activeNetwork == null)
            return false;

        boolean isConnectedOrConnecting = activeNetwork.isConnectedOrConnecting();
        boolean isActiveAndAvailable = activeNetwork.isConnected() && activeNetwork.isAvailable();

        if (isConnectedOrConnecting || isActiveAndAvailable)
            return true;

        return false;
    }

    public void postJSON(String uri, String data, AsyncHttpResponseHandler handler) {
        StringEntity entity = null;
        try {
            entity = new StringEntity(data);
        } catch (UnsupportedEncodingException e) {
            return;
        }
        client.post(context, getAbsoluteUrl(uri), entity, "application/json", handler);
    }

}
