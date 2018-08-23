package com.pankaj.searchaddress.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.SyncStateContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.pankaj.searchaddress.R;
import com.pankaj.searchaddress.adapters.LocationAdapter;
import com.pankaj.searchaddress.adapters.RecyclerTouchListener;
import com.pankaj.searchaddress.mvc.LocationData;
import com.pankaj.searchaddress.network.GPSTracker;
import com.pankaj.searchaddress.util.Util;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    private Button btn_addLocation;
    private static final int MULTIPLE_PERMISSION_CODE = 100;
    private static final int ADD_LOCATION_REQUEST = 999;
    private static final int EXISTS_LOCATION_REQUEST = 888;
    private String[] permissions;
    private RecyclerView recyclerView;
    private LocationAdapter locationAdapter;
    private List<LocationData> locationDataList = new ArrayList<>();
    SharedPreferences prefs = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentLayout(R.layout.activity_main);
        prefs = getSharedPreferences(Util.APP_PREFS, MODE_PRIVATE);

        btn_addLocation = findViewById(R.id.btn_addLocation);
        btn_addLocation.setOnClickListener(this);

        if (prefs.getBoolean("firstrun", true)) {
            locationDataList = Util.db.getAllLocationData();
            locationAdapter = new LocationAdapter(locationDataList);

            locationAdapter.notifyDataSetChanged();
            recyclerView.setAdapter(locationAdapter);
        }
        permissions = new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
        };

        if (checkPermissions()) {
            return;
        }


    }

    @Override
    public void setContentLayout(int layout) {
        v = inflater.inflate(layout, null);
        lnr_content.addView(v);
        toolbar.setVisibility(View.VISIBLE);

        toolbar_title.setText("Pickup Points");


        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        locationAdapter = new LocationAdapter(locationDataList);

        recyclerView.setHasFixedSize(false);
        // vertical RecyclerView
        // keep movie_list_row.xml width to `match_parent`
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());

        // horizontal RecyclerView
        // keep movie_list_row.xml width to `wrap_content`
        // RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);

        recyclerView.setLayoutManager(mLayoutManager);

        // adding inbuilt divider line
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));

        // adding custom divider line with padding 16dp
        // recyclerView.addItemDecoration(new MyDividerItemDecoration(this, LinearLayoutManager.HORIZONTAL, 16));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        recyclerView.setAdapter(locationAdapter);

        // row click listener
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                LocationData locationData = locationDataList.get(position);
                Toast.makeText(getApplicationContext(), locationData.locationName + " is selected!", Toast.LENGTH_SHORT).show();
                gotoSelectedLocation(locationData.locationName, position);
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));


    }

    @Override
    protected void onResume() {
        super.onResume();
        if (prefs.getBoolean("firstrun", true)) {
            // Do first run stuff here then set 'firstrun' as false
            // using the following line to edit/commit prefs
            prepareLocationData();
            prefs.edit().putBoolean("firstrun", false).commit();
        } else {
            locationDataList = Util.db.getAllLocationData();
            locationAdapter = new LocationAdapter(locationDataList);
            locationAdapter.notifyDataSetChanged();
            recyclerView.setAdapter(locationAdapter);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_addLocation:
                addLocation();
                break;
            default:
                break;
        }
    }

    private void addLocation() {
        Intent activityIntent = new Intent(MainActivity.this, LocationActivity.class);
        Bundle mBundle = new Bundle();
        mBundle.putString("Add", "ADD");
        activityIntent.putExtras(mBundle);
        startActivityForResult(activityIntent, ADD_LOCATION_REQUEST);
    }


    private void gotoSelectedLocation(String which, int position) {
        Intent activityIntent = new Intent(MainActivity.this, LocationActivity.class);
        Bundle mBundle = new Bundle();
        mBundle.putDouble("latitude", locationDataList.get(position).latitude);
        mBundle.putDouble("longitude", locationDataList.get(position).longitude);
        mBundle.putString("address", locationDataList.get(position).locationName);

        activityIntent.putExtras(mBundle);
        startActivityForResult(activityIntent, EXISTS_LOCATION_REQUEST);
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


    /**
     * Prepares sample data to provide data set to adapter
     */
    private void prepareLocationData() {
        LocationData locationData = new LocationData(1, "Madhapur", 17.4482929, 78.39148509999995);
        locationDataList.add(locationData);

        locationData = new LocationData(2, "Kukatpally", 17.4947934, 78.39964409999993);
        locationDataList.add(locationData);

        locationData = new LocationData(3, "Jubilee Hills", 17.4325235, 78.40701519999993);
        locationDataList.add(locationData);

        locationData = new LocationData(4, "Ameerpet", 17.4374614, 78.4482878);
        locationDataList.add(locationData);

        locationData = new LocationData(5, "Miyapur", 17.5125114, 78.35222780000004);
        locationDataList.add(locationData);

        Util.db.insertLocationDetails(locationDataList);

        // notify adapter about data set changes
        // so that it will render the list with new data
        locationAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_LOCATION_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                locationAdapter.notifyDataSetChanged();
            }
        } else if (requestCode == EXISTS_LOCATION_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                locationAdapter.notifyDataSetChanged();
            }
        }
    }
}