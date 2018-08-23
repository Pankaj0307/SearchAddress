package com.pankaj.searchaddress.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
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

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by s.pankaj on 11-04-2018.
 */

public class LocationActivity extends BaseActivity implements LocationListener, OnMapReadyCallback, View.OnClickListener {

    PlaceAutocompleteFragment placeAutocompleteFragment;
    AutocompleteFilter typeFilter;
    SupportMapFragment supportMapFragment;
    GoogleMap googleMap;
    private static final int MULTIPLE_PERMISSION_CODE = 100;
    private String[] permissions;
    double longitude, latitude;
    double existsLatitude, existsLongitude;
    Location location;
    GPSTracker gpsTracker;
    LatLng latLng;
    LocationManager locationManager;
    String _Location = "";
    Button btn_addLocationIndb;
    Boolean isLocationSelected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentLayout(R.layout.activity_location);

        permissions = new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
        };

        if (checkPermissions()) {
            return;
        }

        try {
            supportMapFragment =
                    (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            supportMapFragment.getMapAsync(this);
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            Criteria criteria = new Criteria();
            String bestProvider = locationManager.getBestProvider(criteria, true);
            location = locationManager.getLastKnownLocation(bestProvider);
            if (location != null) {
                onLocationChanged(location);
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 0, this);
        } catch (SecurityException se) {
            se.printStackTrace();
        }


        // check if GPS enabled
        gpsTracker = new GPSTracker(this);

        if (Util.isGPSEnabled()) {
            latitude = gpsTracker.getLatitude();
            longitude = gpsTracker.getLongitude();
        }


        placeAutocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(final Place place) {
                place.getAddress();
                Slog.i(TAG, "Place Address : " + place.getAddress());
                Slog.i(TAG, "Place LatLong : " + place.getLatLng());
                Slog.i(TAG, "Place Name : " + place.getName());
                Slog.i(TAG, "Place Type : " + place.getPlaceTypes());
                isLocationSelected = true;
//                btn_addLocationIndb.setEnabled(true);
                btn_addLocationIndb.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        addLocation(place);
                    }
                });

                Toast.makeText(getApplicationContext(), place.getName(), Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onError(Status status) {

                Toast.makeText(getApplicationContext(), status.toString(), Toast.LENGTH_SHORT).show();

            }
        });
        typeFilter = new AutocompleteFilter.Builder()
                .setCountry("IN")
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
                .build();

        placeAutocompleteFragment.setFilter(typeFilter);
        if (getIntent() != null) {
            if (getIntent().hasExtra("Add")) {
                setCurrentLocationName("Add");
            } else {
                _Location = getIntent().getExtras().getString("address");
                existsLatitude = getIntent().getExtras().getDouble("latitude");
                existsLongitude = getIntent().getExtras().getDouble("longitude");

                setCurrentLocationName("Exists");

            }

        }
    }

    private void addLocation(Place place) {
        double longitude = 0, latitude = 0;

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
        googleMap.addMarker(new MarkerOptions()
                .position(latLng)
                .draggable(true)
                .title(place.getName().toString()));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));

        Intent returnIntent = new Intent();
        Bundle mBundle = new Bundle();
        mBundle.putDouble("latitude", latitude);
        mBundle.putDouble("longitude", longitude);
        mBundle.putString("address", _Location);
        returnIntent.putExtras(mBundle);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();


    }


    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public void setContentLayout(int layout) {
        v = inflater.inflate(layout, null);
        lnr_content.addView(v);
        toolbar.setVisibility(View.GONE);
        placeAutocompleteFragment = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        supportMapFragment.getMapAsync(this);

        btn_addLocationIndb = (Button) lnr_content.findViewById(R.id.btn_addLocationIndb);
        btn_addLocationIndb.setOnClickListener(this);
//        btn_addLocationIndb.setEnabled(false);
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            this.googleMap.setMyLocationEnabled(true);
        } else {
            // Show rationale and request permission.
        }

        latitude = location.getLatitude();
        longitude = location.getLongitude();
        LatLng latLng = new LatLng(latitude, longitude);

        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        try {
            List<Address> listAddresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (null != listAddresses && listAddresses.size() > 0) {
                _Location = listAddresses.get(0).getPremises();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.googleMap.addMarker(new MarkerOptions()
                .position(latLng)
                .draggable(true)
                .title(_Location));
        this.googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        this.googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
    }

    @Override
    public void onLocationChanged(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        latLng = new LatLng(latitude, longitude);
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

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent returnIntent = new Intent();
        Bundle mBundle = new Bundle();
        mBundle.putDouble("latitude", latitude);
        mBundle.putDouble("longitude", longitude);
        mBundle.putString("address", _Location);
        returnIntent.putExtras(mBundle);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();

    }

    private void setCurrentLocationName(String value) {
        if (value.equalsIgnoreCase("Add")) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();

            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
            try {
                List<Address> listAddresses = geocoder.getFromLocation(latitude, longitude, 1);
                if (null != listAddresses && listAddresses.size() > 0) {
                    _Location = listAddresses.get(0).getPremises();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            placeAutocompleteFragment.setText(_Location);
        } else {
            try {
                supportMapFragment.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(GoogleMap googleMap) {
                        LatLng latLng = new LatLng(existsLatitude, existsLongitude);
                        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                        try {
                            List<Address> listAddresses = geocoder.getFromLocation(existsLatitude, existsLongitude, 1);
                            if (null != listAddresses && listAddresses.size() > 0) {
                                _Location = listAddresses.get(0).getPremises();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        googleMap.addMarker(new MarkerOptions()
                                .position(latLng)
                                .draggable(true)
                                .title(_Location));
                        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));

                        placeAutocompleteFragment.setText(_Location);

                    }
                });

//            googleMap.setMyLocationEnabled(true);
            } catch (SecurityException se) {
                se.printStackTrace();
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_addLocationIndb:
                if (isLocationSelected == false) {
                    Toast.makeText(LocationActivity.this, "Please select location ", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }
}