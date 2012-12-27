package com.mottimotti.fungo;

import android.content.Context;
import android.os.AsyncTask;
import com.loopj.android.http.AsyncHttpResponseHandler;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Random;

public class LocationDataSender {
    private static JsonFileCache fileCache;
    private static APIClient client;

    public LocationDataSender(Context context) throws IOException {
        String cacheDirPath = JsonFileCache.getCacheDir(context);
        fileCache = JsonFileCache.getFileCache(cacheDirPath);
        client = new APIClient(context);
    }

    public void send(final Map<String, String> data) {
        new AsyncTask<Void, Void, Void>() {
            protected Void doInBackground(Void... voi) {
                writeDataToDisk(data);
                flushExceptions();
                return null;
            }
        }.execute();
    }

    private void writeDataToDisk(Map<String, String> data) {
        int random = new Random().nextInt(99999);
        String key = String.valueOf(random) + data.get("time");
        try {
            fileCache.put(key, new JSONObject(data));
        } catch (IOException e) {
        }

    }


    private static synchronized void flushExceptions() {
        File cacheDirectory = fileCache.getCacheDir();

        if (cacheDirectory.exists() && cacheDirectory.isDirectory()) {
            File[] cachedFiles = cacheDirectory.listFiles();
            for (File file : cachedFiles) {
                if (file.exists() && file.isFile()) {
//                    sendData(file);
                }
            }
        }
    }

    private static void sendData(final File file) {
        if(!client.isOnline()) return;

        AsyncHttpResponseHandler handler = new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(String response) {
                file.delete();
            }
        };
        JSONObject data = fileCache.get(file);
        client.postJSON("/api/v1/location_data/", data.toString(), handler);
    }


}
