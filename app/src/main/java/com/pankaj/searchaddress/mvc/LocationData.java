package com.pankaj.searchaddress.mvc;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;

import java.io.Serializable;

/**
 * Created by s.pankaj on 11-04-2018.
 */

public class LocationData implements Serializable {

    public String locationName = "", locationAddress = "";
    public Double latitude, longitude;
    public int locationId;

    public Location gpslocation;
    public Context currentContext;

    public LocationData(int locationId, String locationName, Double latitude, Double longitude) {
        this.locationName = locationName;
        this.locationId = locationId;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public LocationData() {
    }


    public void setGpslocation(Location gpsLoaction) {
        gpslocation = gpsLoaction;
    }

    public Location getGPSLocation() {
        return gpslocation;
    }

    @SuppressLint("StaticFieldLeak")
    private static LocationData sInstance = null;

    public static LocationData instance() {

        if (sInstance == null) {
            sInstance = new LocationData();
        }
        return sInstance;
    }

    public Context getCurrentContext() {
        return currentContext;
    }

}
