package com.mottimotti.fungo;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import android.widget.Toast;

public class ExternalStorageObserver {
    BroadcastReceiver mExternalStorageReceiver;
    boolean externalStorageAvailable = false;
    boolean externalStorageWriteable = false;
    private Context context;

    public ExternalStorageObserver(Context context) {
        this.context = context;
    }

    public void updateExternalStorageState() {
        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            externalStorageAvailable = externalStorageWriteable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            externalStorageAvailable = true;
            externalStorageWriteable = false;
        } else {
            externalStorageAvailable = externalStorageWriteable = false;
        }
    }

    public void startWatching() {
        mExternalStorageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Toast.makeText(context, ("Storage: " + intent.getData()), Toast.LENGTH_LONG);
                updateExternalStorageState();
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        filter.addAction(Intent.ACTION_MEDIA_REMOVED);
        context.registerReceiver(mExternalStorageReceiver, filter);
        updateExternalStorageState();
    }

    public void stopWatching() {
        context.unregisterReceiver(mExternalStorageReceiver);
    }

    public boolean available() {
        return (externalStorageAvailable && externalStorageWriteable);
    }
}
