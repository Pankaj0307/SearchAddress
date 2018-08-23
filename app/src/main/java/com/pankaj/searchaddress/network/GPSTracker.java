package com.pankaj.searchaddress.network;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;

import com.pankaj.searchaddress.activity.BaseActivity;
import com.pankaj.searchaddress.mvc.LocationData;
import com.pankaj.searchaddress.util.Slog;
import com.pankaj.searchaddress.util.Util;

import java.util.Date;

/**
 * Created by s.pankaj on 11-04-2018.
 */

public class GPSTracker implements LocationListener {

    private Context mContext;

    // flag for GPS status
    private boolean canGetLocation = false;
    private boolean ProgressStatus = false;

    private Location location; // location
    private double latitude; // latitude
    private double longitude; // longitude
    private String gpsTime;
    private Date date;
    private ProgressDialog progDailog;
    private final LocationData locationData;
    private Handler handler;
    private Drawable drawable;
    private Runnable myRunnable;

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 30; // 1/2 minute

    private LocationManager locationManager;

    public GPSTracker(Context context) {
        this.mContext = context;
        locationData = LocationData.instance();
        this.mContext = locationData.getCurrentContext();
//        findLocation();

    }

    @SuppressWarnings("SameParameterValue")
    public GPSTracker(Context context, boolean selectSatus) {
        this.mContext = context;
        locationData = LocationData.instance();
        if (!ProgressStatus && selectSatus) {
            this.mContext = locationData.getCurrentContext();
            getLocation(true);
        }

    }

    private void getLocation(boolean selectSatus) {
        try {
            if (!ProgressStatus && selectSatus) {
                Resources resources = locationData.getCurrentContext().getResources();
                drawable = resources.getDrawable(Util.PROGRESSINDETERMINATE, mContext.getTheme());
                CreateDailog();
            }
            new Thread(new Runnable() {

                @Override
                public void run() {
                    if (handler != null) {
                        if (myRunnable != null)
                            handler.removeCallbacks(myRunnable);
                        myRunnable = new Runnable() {

                            @Override
                            public void run() {
//                                findLocation();
                            }
                        };
                        runOnUiThreaddelay(myRunnable);
                    }
                }
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void findLocation() {
        locationManager = (LocationManager) mContext
                .getSystemService(Context.LOCATION_SERVICE);

        // getting GPS status
        boolean isGPSEnabled = locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER);

        // getting network status
        boolean isNetworkEnabled = locationManager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (!isGPSEnabled && !isNetworkEnabled) {
            // no network provider is enabled
            if (progDailog != null) {
                ProgressStatus = false;
                progDailog.dismiss();
                progDailog.cancel();
                progDailog = null;
                handler = null;
            }
        } else {
            this.canGetLocation = true;
            long time;
            if (isGPSEnabled) {
                if (location == null) {
                    if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    Slog.i(BaseActivity.TAG, "GPS Enabled");
                    if (locationManager != null) {
                        location = locationManager
                                .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                            time = location.getTime();
                            date = new Date(time);
                        }
                    }
                }
            }

            if (location == null) {
                if (isNetworkEnabled && latitude <= 0.0) {
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    Slog.i(BaseActivity.TAG, "Network Available for GPS from Network Provider");
                    if (locationManager != null) {
                        location = locationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                            time = location.getTime();
                            date = new Date(time);
                        }
                    }
                }
            }

            if (location == null) {
                if (progDailog != null) {
                    progDailog.incrementProgressBy(100);
                }
                getLocation(false);

            } else {
                if (progDailog != null) {
                    ProgressStatus = false;
                    progDailog.dismiss();
                    handler = null;
                    progDailog = null;

                }
            }
        }
    }

    private void CreateDailog() {
        ProgressStatus = true;
        handler = new Handler(locationData.getCurrentContext().getMainLooper());
        progDailog = new ProgressDialog(locationData.getCurrentContext());
        progDailog.setMessage("Searching for GPS...");
        progDailog.setIndeterminate(true);
        progDailog.setIndeterminateDrawable(drawable);
        progDailog.setCancelable(false);
        progDailog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        //progDailog.show();
    }

    private void runOnUiThreaddelay(Runnable runnable) {
        handler.postDelayed(runnable, 0);
    }

    private void stopUsingGPS() {
        if (locationManager != null) {
            locationManager.removeUpdates(GPSTracker.this);
            locationManager = null;
        }
    }

    /**
     * Function to get latitude
     */
    public double getLatitude() {
        if (location != null) {
            latitude = location.getLatitude();
        }
        return latitude;
    }

    /**
     * Function to get longitude
     */
    public double getLongitude() {
        if (location != null) {
            longitude = location.getLongitude();
        }
        return longitude;
    }

    public String getTime() {
        if (location != null) {
            date = new Date(location.getTime());
        }
        return gpsTime;
    }

    /**
     * Function to check GPS/wifi enabled
     *
     * @return boolean
     */
    public boolean canGetLocation() {
        return this.canGetLocation;
    }

    @Override
    public void onLocationChanged(Location location) {
    }

    @Override
    public void onProviderDisabled(String provider) {
        Slog.i(BaseActivity.TAG, "GPS TRACKER Disabled");
        this.mContext = locationData.getCurrentContext();
        if (progDailog != null) {
            progDailog.dismiss();
            progDailog.cancel();
        }
        progDailog = null;
        if (handler != null)
            if (myRunnable != null)
                handler.removeCallbacks(myRunnable);
        myRunnable = null;
        handler = null;
    }

    @Override
    public void onProviderEnabled(String provider) {
        Slog.i(BaseActivity.TAG, "GPS TRACKER Enabled");
        if (!ProgressStatus && handler == null) {
            ProgressStatus = true;

            if (progDailog != null) {
                progDailog.dismiss();
                progDailog.cancel();
            }

            handler = null;
            progDailog = null;
            stopUsingGPS();

            new GPSTracker(locationData.getCurrentContext(), true);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Slog.i(BaseActivity.TAG, "GPS TRACKER StatusChanged");
        this.mContext = locationData.getCurrentContext();
    }
}
