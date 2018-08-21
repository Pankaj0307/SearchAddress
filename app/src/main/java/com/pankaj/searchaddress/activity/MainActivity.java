package com.pankaj.searchaddress.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.pankaj.searchaddress.R;
import com.pankaj.searchaddress.network.GPSTracker;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    private ListView lv_pickup_points;
    private Button btn_addLocation;
    private static final int MULTIPLE_PERMISSION_CODE = 100;
    private String[] permissions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentLayout(R.layout.activity_main);


        permissions = new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
        };

        if (checkPermissions()) {
            return;
        }

        btn_addLocation.setOnClickListener(this);

    }

    @Override
    public void setContentLayout(int layout) {
        v = inflater.inflate(layout, null);
        lnr_content.addView(v);
        toolbar.setVisibility(View.GONE);

        lv_pickup_points = (ListView) lnr_content.findViewById(R.id.lv_pickup_points);
        btn_addLocation = (Button) lnr_content.findViewById(R.id.btn_addLocation);


    }

    @Override
    protected void onResume() {
        super.onResume();

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
        startActivity(activityIntent);

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

}
