package com.example.mapme.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.IBinder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.mapme.R;
import com.example.mapme.backend.AppService;
import com.example.mapme.backend.IconPlottingOverlay;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.api.IMapView;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;


public class AddMarkerActivity extends AddObjectActivity implements View.OnClickListener, View.OnLongClickListener, AppService.AppServiceListener {

    protected AppService appService;
    protected boolean appServiceBound;
    private boolean serviceConnected = false;
    private GeoPoint userGeoPoint;
    private Marker userMarker;
    ImageButton painting, panning;
    ImageButton btnRotateLeft, btnRotateRight;
    protected MapView mMapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_object);

        mMapView = findViewById(R.id.map);
        setMapPositionAndUserMarker();
        enableRotation();
        disablePainting();

        IconPlottingOverlay plotter = new IconPlottingOverlay(this, this.getResources().getDrawable(R.drawable.marker_default));
        mMapView.getOverlayManager().add(plotter);

        // bind to service
        Intent bindIntent = new Intent(AddMarkerActivity.this, AppService.class);
        bindService(bindIntent, appServiceConnection, Context.BIND_AUTO_CREATE);
        Log.d("info", "Service bound to AddMarkerActivity");

    }

    private void setMapPositionAndUserMarker() {
        IMapController mapController = mMapView.getController();
        // set map position
        Intent intent = getIntent();
        double mapCenterLatitude = intent.getDoubleExtra("mapCenterLatitude", 49.89873);
        double mapCenterLongitude = intent.getDoubleExtra("mapCenterLongitude", 10.90067);
        double zoomLevel = intent.getDoubleExtra("zoomLevel", 17.0);
        double userGeoPointLatitude = intent.getDoubleExtra("userGeoPointLatitude", 0);
        double userGeoPointLongitude = intent.getDoubleExtra("userGeoPointLongitude", 0);
        GeoPoint startPoint = new GeoPoint(mapCenterLatitude, mapCenterLongitude);
        mapController.setCenter(startPoint);
        mapController.setZoom(zoomLevel);
        // set user marker
        userGeoPoint = new GeoPoint(userGeoPointLatitude, userGeoPointLongitude);
        userMarker = new Marker(mMapView);
        userMarker.setIcon(getResources().getDrawable(R.drawable.position));
        userMarker.setPosition(userGeoPoint);
        mMapView.getOverlays().add(userMarker);
    }

    private void enableRotation() {
        btnRotateLeft = findViewById(R.id.btnRotateLeft);
        btnRotateRight = findViewById(R.id.btnRotateRight);
        btnRotateRight.setOnClickListener(this);
        btnRotateLeft.setOnClickListener(this);
        mMapView = findViewById(R.id.map);
        RotationGestureOverlay mRotationGestureOverlay = new RotationGestureOverlay(mMapView);
        mRotationGestureOverlay.setEnabled(true);
        mMapView.setMultiTouchControls(true);
        mMapView.setMapListener(new MapListener() {
            @Override
            public boolean onScroll(ScrollEvent event) {
                Log.i(IMapView.LOGTAG, System.currentTimeMillis() + " onScroll " + event.getX() + "," + event.getY());
                return true;
            }

            @Override
            public boolean onZoom(ZoomEvent event) {
                Log.i(IMapView.LOGTAG, System.currentTimeMillis() + " onZoom " + event.getZoomLevel());
                return true;
            }
        });
        mMapView.getOverlayManager().add(mRotationGestureOverlay);
    }

    private void disablePainting() {
        panning = findViewById(R.id.enablePanning);
        panning.setVisibility(View.GONE);
        painting = findViewById(R.id.enablePainting);
        painting.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnRotateLeft: {
                float angle = mMapView.getMapOrientation() + 10;
                if (angle > 360)
                    angle = 360 - angle;
                mMapView.setMapOrientation(angle);
            }
            break;
            case R.id.btnRotateRight: {
                float angle = mMapView.getMapOrientation() - 10;
                if (angle < 0)
                    angle += 360f;
                mMapView.setMapOrientation(angle);
            }
        }
    }

    @Override
    public boolean onLongClick(View v) {
        this.showPopupWindow(v);
        return true;
    }

    /**
     * App Service Connection
     */
    private ServiceConnection appServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            AppService.LocalBinder binder = (AppService.LocalBinder) service;
            appService = binder.getService();
            appServiceBound = true;
            appService.registerListener(AddMarkerActivity.this);
            serviceConnected = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            appServiceBound = false;
        }
    };

    @Override
    public void updateUserPosition(Location location) {
        userGeoPoint.setLatitude(location.getLatitude());
        userGeoPoint.setLongitude(location.getLongitude());
        userMarker.setPosition(userGeoPoint);
        Log.d("info", "AddMarkerActivity is updating user position");
    }

    @Override
    public void editObject(View view){
        Intent intent = new Intent(this, EditInformationActivity.class);
        intent.putExtra("name", "Edit Marker");
        startActivity(intent);
    }

}
