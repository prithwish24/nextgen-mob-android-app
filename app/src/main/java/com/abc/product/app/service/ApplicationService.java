package com.abc.product.app.service;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;

/**
 * Created by prith on 2/16/2018.
 */

public class ApplicationService extends Service implements LocationListener {

    private static final long MIN_DISTANCE_FOR_UPDATE = 10;
    private static final long MIN_TIME_FOR_UPDATE = 1000 * 60 * 2;

    protected LocationManager locationManager;

    public ApplicationService(final Context context) {
        this.locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
    }

    @SuppressLint("MissingPermission")
    public Location getLocation(final String provider) {
        if (locationManager.isProviderEnabled(provider)) {
            locationManager.requestLocationUpdates(provider,
                    MIN_TIME_FOR_UPDATE, MIN_DISTANCE_FOR_UPDATE, this);
            if (locationManager != null) {
                return locationManager.getLastKnownLocation(provider);
            }
        }
        return null;
    }

    public boolean isLocationPermissionGranted() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
        /*if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {*/

            return true;
        }
        return false;
    }

    /*if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        final Location gpsLocation = applicationService.getLocation(LocationManager.GPS_PROVIDER);
        final Location netLocation = applicationService.getLocation(LocationManager.NETWORK_PROVIDER);
        if (gpsLocation !=  null) {
            address = GeoLocationUtil.getAddressFromGeoCoordinates(gpsLocation.getLatitude(), gpsLocation.getLongitude(), HomeActivity.this);

        } else if (netLocation !=  null){
            address = GeoLocationUtil.getAddressFromGeoCoordinates(netLocation.getLatitude(), netLocation.getLongitude(), HomeActivity.this);

        } else {
            showSettingsDialog();
        }
    }*/

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
