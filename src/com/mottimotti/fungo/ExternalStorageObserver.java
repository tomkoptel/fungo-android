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
    private OnUpdateStorageListener updateListener;

    public ExternalStorageObserver(Context context) {
        this.context = context;
    }

    public void updateExternalStorageState() {
        String state = Environment.getExternalStorageState();
        Toast toast = Toast.makeText(context, "", Toast.LENGTH_LONG);

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            externalStorageAvailable = externalStorageWriteable = true;
            toast.setText("MEDIA_MOUNTED !");
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            externalStorageAvailable = true;
            externalStorageWriteable = false;
            toast.setText("MEDIA_MOUNTED_READ_ONLY !");
        } else {
            externalStorageAvailable = externalStorageWriteable = false;
            toast.setText("MEDIA_UNREACHABLE !");
        }
        toast.show();

        updateListener.onUpdateStorage(externalStorageAvailable && externalStorageWriteable);
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

    public void setOnUpdateStorageListener(OnUpdateStorageListener listener) {
        this.updateListener = listener;
    }

    public interface OnUpdateStorageListener {
        public void onUpdateStorage(boolean storageAvailable);
    }
}
