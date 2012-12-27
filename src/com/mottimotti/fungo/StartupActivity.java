package com.mottimotti.fungo;

import android.app.Activity;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Map;

public class StartupActivity extends Activity implements LocationListener {
    final private int METERS = 1;
    final private int TIME = 400;

    private LocationManager locationManager;
    private String provider;
    private Toast toast;
    private ExternalStorageObserver storageObserver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        initNotifier();
        initStorageObserver();
        initLocationManager();

        Location location = locationManager.getLastKnownLocation(provider);
        onLocationChanged(location);
    }

    private void initNotifier() {
        toast = Toast.makeText(this, "", Toast.LENGTH_LONG);
    }

    private void initStorageObserver() {
        storageObserver = new ExternalStorageObserver(this);
        storageObserver.startWatching();
    }

    private void initLocationManager() {
        Criteria criteria = new Criteria();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        provider = locationManager.getBestProvider(criteria, false);
        locationManager.requestLocationUpdates(provider, TIME, METERS, this);
    }

    private void notifyUI(String message) {
        toast.setText(message);
        toast.show();
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location == null) {
            notifyUI("No location");
            return;
        }

        String latitude = String.valueOf(location.getLatitude());
        String longitude = String.valueOf(location.getLongitude());

        String format = "latitude: %s || longitude: %s";
        notifyUI(String.format(format, latitude, longitude));

        LinkedHashMap<String,String> data = new LinkedHashMap<String, String>();
        data.put("latitude", latitude);
        data.put("longitude", longitude);
        data.put("time", Calendar.getInstance().toString());

        try {
            sendLocationData(data);
        } catch (IOException e) {
            notifyUI(e.getMessage());
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
        String format = "status: %s || code: %d";
        notifyUI(String.format(format, s, i));
    }

    @Override
    public void onProviderEnabled(String s) {
        notifyUI("Enabled new provider " + provider);
    }

    @Override
    public void onProviderDisabled(String s) {
        notifyUI("Disabled provider " + provider);
    }

    @Override
    public void onPause() {
        super.onPause();
        storageObserver.stopWatching();
        locationManager.removeUpdates(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        storageObserver.startWatching();
        locationManager.requestLocationUpdates(provider, TIME, METERS, this);
    }

    public void showLocation(View v) {
        locationManager.requestSingleUpdate(provider, this, null);
        Location location = locationManager.getLastKnownLocation(provider);
        onLocationChanged(location);
    }


    private void sendLocationData(final Map<String,String> data) throws IOException {
        storageObserver.updateExternalStorageState();
        if(storageObserver.available()){
            LocationDataSender sender = new LocationDataSender(this);
            sender.send(data);
        }
    }
}
