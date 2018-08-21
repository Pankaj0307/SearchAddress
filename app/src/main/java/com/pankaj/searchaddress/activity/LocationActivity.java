package com.pankaj.searchaddress.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.pankaj.searchaddress.R;
import com.pankaj.searchaddress.mvc.LocationData;
import com.pankaj.searchaddress.network.GPSTracker;
import com.pankaj.searchaddress.util.Slog;
import com.pankaj.searchaddress.util.Util;

/**
 * Created by s.pankaj on 11-04-2018.
 */

public class LocationActivity extends BaseActivity implements OnMapReadyCallback {

    PlaceAutocompleteFragment placeAutocompleteFragment;
    AutocompleteFilter typeFilter;
    SupportMapFragment supportMapFragment;
    GoogleMap map;
    private static final int MULTIPLE_PERMISSION_CODE = 100;
    private String[] permissions;
    double longitude, latitude;
    Location location;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentLayout(R.layout.activity_location);

        // check if GPS enabled
        GPSTracker gpsTracker = new GPSTracker(this);

        if (gpsTracker.getIsGPSTrackingEnabled()) {
            latitude = gpsTracker.getLatitude();
            longitude = gpsTracker.getLongitude();
        }

        permissions = new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
        };
        if (checkPermissions()) {
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
//            map.setMyLocationEnabled(true);
        } else {
            // Show rationale and request permission.
        }


        placeAutocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                place.getAddress();
                Slog.i(TAG, "Place Address : " + place.getAddress());
                Slog.i(TAG, "Place LatLong : " + place.getLatLng());
                Slog.i(TAG, "Place Name : " + place.getName());
                Slog.i(TAG, "Place Type : " + place.getPlaceTypes());

                LocationData locationData = new LocationData();
                locationData.locationName = place.getName().toString();
                locationData.locationAddress = place.getAddress().toString();

                String data = place.getLatLng().toString();
                String[] tempArray = data.substring(data.indexOf("(") + 1, data.lastIndexOf(")")).split(",");
                latitude = Double.parseDouble(tempArray[0]);
                longitude = Double.parseDouble(tempArray[1]);

                locationData.latitude = latitude;
                locationData.longitude = longitude;

                Util.db.insertLocationDetails(locationData);

                LatLng latLng = new LatLng(latitude, longitude);
                map.addMarker(new MarkerOptions()
                        .position(latLng)
                        .draggable(true)
                        .title(place.getName().toString()));
                map.moveCamera(CameraUpdateFactory.newLatLng(latLng));

                Toast.makeText(getApplicationContext(), place.getName(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Status status) {

                Toast.makeText(getApplicationContext(), status.toString(), Toast.LENGTH_SHORT).show();

            }
        });
        typeFilter = new AutocompleteFilter.Builder()
                .setCountry("IN")
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_CITIES)
                .build();

        placeAutocompleteFragment.setFilter(typeFilter);
    }

    @Override
    public void setContentLayout(int layout) {
        v = inflater.inflate(layout, null);
        lnr_content.addView(v);
        toolbar.setVisibility(View.GONE);
        placeAutocompleteFragment = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        placeAutocompleteFragment.setText("Mumbai");
        supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        supportMapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            map.setMyLocationEnabled(true);
        } else {
            // Show rationale and request permission.
        }


       /* LatLng latLng = new LatLng(latitude, longitude);
        map.addMarker(new MarkerOptions()
                .position(latLng)
                .draggable(true)
                .title("Marker"));
        map.moveCamera(CameraUpdateFactory.newLatLng(latLng));*/
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {

        if (requestCode == MULTIPLE_PERMISSION_CODE) {
            if (grantResults.length > 0 && ((grantResults[0] != PackageManager.PERMISSION_GRANTED) ||
                    (grantResults[1] != PackageManager.PERMISSION_GRANTED))) {
                Toast.makeText(this, "You should allow permissions. Try again",
                        Toast.LENGTH_LONG).show();
                finish();
            }
        }

    }

    private boolean checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, permissions[0])
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, permissions[1])
                        != PackageManager.PERMISSION_GRANTED) {

            boolean isLocation = ActivityCompat.shouldShowRequestPermissionRationale(this,
                    permissions[0]);
            boolean isStorage = ActivityCompat.shouldShowRequestPermissionRationale(this,
                    permissions[1]);
            if (isLocation && isStorage) {
                isLocation = isStorage = false;
            }

            if (!isLocation || !isStorage) {
                ActivityCompat.requestPermissions(this, permissions,
                        MULTIPLE_PERMISSION_CODE);
            }
            return true;
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LatLng latLng = new LatLng(latitude, longitude);

        map.addMarker(new MarkerOptions()
                .position(latLng)
                .draggable(true)
                .title("Marker"));
        map.moveCamera(CameraUpdateFactory.newLatLng(latLng));

    }
}
