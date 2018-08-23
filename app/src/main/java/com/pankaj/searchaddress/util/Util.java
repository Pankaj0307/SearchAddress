package com.pankaj.searchaddress.util;

import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.location.LocationManager;
import android.os.Environment;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.pankaj.searchaddress.R;
import com.pankaj.searchaddress.SearchAddressApplication;
import com.pankaj.searchaddress.database.DBHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

import static com.pankaj.searchaddress.activity.BaseActivity.TAG;

/**
 * Created by s.pankaj on 11-04-2018.
 */

public class Util {
    public static final boolean debugMode = true/*BuildConfig.DEBUG*/;
    public static DBHelper db;
    private static final int REQUEST_CHECK_SETTINGS = 10;
    public static String APP_PREFS = "MyPrefs";
    public static String FIRST_LAUNCH = "firstLaunch";

    public static final int PROGRESSINDETERMINATE = R.drawable.progress_indeterminate;

    public static void BackupDatabase() {
        try {
            Slog.i(TAG, "BackupDatabase Completed ");
            File sd = new File(Environment.getExternalStorageDirectory()
                    + "/Download");
            if (!sd.exists()) {
                sd.mkdir();
            }
            if (sd.canWrite()) {
                String currentDBPath = DBHelper.DB_PATH + DBHelper.DB_NAME;
                String backupDBPath = "SearchAddress.db";
                File currentDB = new File(currentDBPath);
                File backupDB = new File(sd, backupDBPath);

                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB)
                            .getChannel();
                    FileChannel dst = new FileOutputStream(backupDB)
                            .getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static boolean isGPSEnabled() {
        LocationManager locationManager = (LocationManager) SearchAddressApplication.getContext()
                .getSystemService(Context.LOCATION_SERVICE);
        return locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER) && locationManager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    public static void displayLocationSettingsRequest(Context context, final Activity activity) {
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API).build();
        googleApiClient.connect();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(30000);
        locationRequest.setFastestInterval(5000);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:

                        try {
                            status.startResolutionForResult(activity, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            e.printStackTrace();
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        break;
                }
            }
        });
    }
}
