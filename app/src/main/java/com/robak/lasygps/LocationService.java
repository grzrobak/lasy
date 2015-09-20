package com.robak.lasygps;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.AndroidException;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

/**
 * Created by Grzesiek on 20-09-2015.
 */
public class LocationService {
    private static final String TAG = MainActivity.class.getSimpleName();

    private final static float DISPLACEMENT = 10.0F; // 10 meters

    private GoogleApiClient mGoogleApiClient;
    private final Context context;

    public LocationService(Context context, GoogleApiClient googleApiClient) {
        this.context = context;
        this.mGoogleApiClient = googleApiClient;
    }

    /**
     * Creating location request object
     * */
    protected LocationRequest createLocationRequest() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(getUpdateInterval());
        mLocationRequest.setFastestInterval(getUpdateInterval());
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT); // 10 meters
        return mLocationRequest;
    }

    /**
     * Starting the location updates
     * */
    public void startLocationUpdates(com.google.android.gms.location.LocationListener listener) {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, createLocationRequest(), listener);
        Log.d(TAG, "Periodic location updates started!");
    }

    /**
     * Stopping location updates
     */
    public void stopLocationUpdates(com.google.android.gms.location.LocationListener listener) {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, listener);
        Log.d(TAG, "Periodic location updates stopped!");
    }

    private int getUpdateInterval()
    {
        return Integer.valueOf(
                PreferenceManager.getDefaultSharedPreferences(context)
                .getString(SettingsActivity.KEY_TIME_INTERVAL, "5")
        );
    }

    public Location getLastLocation()
    {
        return LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
    }

}
