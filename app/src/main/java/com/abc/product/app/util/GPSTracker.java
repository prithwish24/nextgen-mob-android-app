package com.abc.product.app.util;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.abc.product.app.android.HomeActivity;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by prith on 2/18/2018.
 */

public class GPSTracker implements LocationListener {
    private static final String TAG = GPSTracker.class.getName();

    private static final long MIN_TIME_FOR_UPDATE = 6000;
    private static final long MIN_DISTANCE_FOR_UPDATE = 10;

    private final Context context;
    public GPSTracker(Context context) {
        this.context = context;
    }

    public Location getLocation() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "Permission Refused by user !", Toast.LENGTH_SHORT).show();
            return null;
        }
        final LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (isGPSEnabled) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_FOR_UPDATE, MIN_DISTANCE_FOR_UPDATE, this);
            final Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            return location;
        } else {
            showSettingsDialog();;
        }
        return null;
    }

    public Address getAddress(final double latitude, final double longitude) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            final List<Address> addressList = geocoder.getFromLocation(latitude, longitude, 1);
            if (addressList != null && addressList.size() > 0) {
                return addressList.get(0);
            }
        } catch (IOException e) {
            Log.e(TAG, "Unable connect to Geocoder", e);
        }

        return null;
    }


    private void showSettingsDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setTitle("SETTINGS");
        alertDialog.setMessage("Enable Location Provider! Go to settings menu?");
        alertDialog.setPositiveButton("Settings",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(
                                Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        context.startActivity(intent);
                    }
                });
        alertDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        alertDialog.show();
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
