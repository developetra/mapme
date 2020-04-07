package com.example.mapme.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.example.mapme.R;
import com.example.mapme.backend.AppService;
import com.example.mapme.backend.GeoJsonHelper;
import com.example.mapme.backend.OverpassHelper;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

/**
 * MainActivity - Start screen when opening the app.
 */
public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 5;
    private OverpassHelper overpassHelper = new OverpassHelper();

    /**
     * Initializes layout, requests permissions and starts appService.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermission();
        startAppService();
    }

    /**
     * Opens new MapActivity.
     *
     * @param view
     */
    public void startMap(View view) {
        startAppService();
        Intent intent = new Intent(this, MapActivity.class);
        startActivity(intent);
    }

    /**
     * Shows info dialog about app.
     *
     * @param view
     */
    public void showInfo(View view) {
        // TEST
        LatLng southwest = new LatLng(49.891999, 10.887844);
        LatLng northeast = new LatLng(49.895648, 10.889604);
        LatLngBounds bounds = new LatLngBounds(southwest, northeast);
        overpassHelper.search(bounds);

        AlertDialog.Builder infoDialog = new AlertDialog.Builder(MainActivity.this);
        infoDialog.setTitle("How MapMe works:");
        infoDialog.setMessage("MapMe is a collaborative tool for the acquisition and mapping of geospatial data. \n" +
                "You can draw markers, polylines or polygons directly on the screen. ");
        infoDialog.setNeutralButton("Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        infoDialog.show();
    }

    /**
     * Requests permission to access fine location.
     */
    private void requestPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }
        } else {
            // Permission has already been granted
        }
    }

    /**
     * Starts AppService.
     */
    private void startAppService() {
        Log.d("info", "Starting AppService");
        Intent serviceIntent = new Intent(getApplicationContext(), AppService.class);
        startService(serviceIntent);
    }
}
