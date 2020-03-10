package com.example.mapme.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.location.Location;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.example.mapme.R;
import com.example.mapme.backend.AppService;
import com.example.mapme.backend.PaintingSurface;


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

public class AddPolygonActivity extends AppCompatActivity implements View.OnClickListener, AppService.AppServiceListener {

    protected AppService appService;
    protected boolean appServiceBound;
    private boolean serviceConnected = false;
    private GeoPoint userGeoPoint;
    private Marker userMarker;
    ImageButton painting, panning;
    TextView textViewCurrentLocation;
    PaintingSurface paintingSurface;
    ImageButton btnRotateLeft, btnRotateRight;
    protected MapView mMapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_object);

        mMapView = findViewById(R.id.map);
        setMapPositionAndUserMarker();
        enableRotation();
        enablePainting();

        // bind to service
        Intent bindIntent = new Intent(AddPolygonActivity.this, AppService.class);
        bindService(bindIntent, appServiceConnection, Context.BIND_AUTO_CREATE);
        Log.d("info", "Service bound to AddPolygonActivity");
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
        textViewCurrentLocation = findViewById(R.id.textViewCurrentLocation);
        mMapView = findViewById(R.id.map);
        RotationGestureOverlay mRotationGestureOverlay = new RotationGestureOverlay(mMapView);
        mRotationGestureOverlay.setEnabled(true);
        mMapView.setMultiTouchControls(true);
        mMapView.setMapListener(new MapListener() {
            @Override
            public boolean onScroll(ScrollEvent event) {
                Log.i(IMapView.LOGTAG, System.currentTimeMillis() + " onScroll " + event.getX() + "," + event.getY());
                updateInfo();
                return true;
            }
            @Override
            public boolean onZoom(ZoomEvent event) {
                Log.i(IMapView.LOGTAG, System.currentTimeMillis() + " onZoom " + event.getZoomLevel());
                updateInfo();
                return true;
            }
        });
        mMapView.getOverlayManager().add(mRotationGestureOverlay);
    }

    private void enablePainting() {
        panning = findViewById(R.id.enablePanning);
        panning.setOnClickListener(this);
        panning.setBackgroundColor(Color.BLACK);
        painting = findViewById(R.id.enablePainting);
        painting.setOnClickListener(this);
        paintingSurface = findViewById(R.id.paintingSurface);
        paintingSurface.init(this, mMapView);
        paintingSurface.setMode(PaintingSurface.Mode.Polygon);
    }

    private void updateInfo() {
        IGeoPoint mapCenter = mMapView.getMapCenter();
        textViewCurrentLocation.setText(mapCenter.getLatitude() + "," +
                mapCenter.getLongitude()
                + ",zoom=" + mMapView.getZoomLevelDouble() + ",angle=" + mMapView.getMapOrientation() + "\nBounds: " + mMapView.getBoundingBox().toString());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.enablePanning:
                paintingSurface.setVisibility(View.GONE);
                panning.setBackgroundColor(Color.BLACK);
                painting.setBackgroundColor(Color.TRANSPARENT);
                break;
            case R.id.enablePainting:
                paintingSurface.setVisibility(View.VISIBLE);
                painting.setBackgroundColor(Color.BLACK);
                panning.setBackgroundColor(Color.TRANSPARENT);
                break;

            case R.id.btnRotateLeft: {
                float angle = mMapView.getMapOrientation() + 10;
                if (angle > 360)
                    angle = 360 - angle;
                mMapView.setMapOrientation(angle);
                updateInfo();
            }
            break;
            case R.id.btnRotateRight: {
                float angle = mMapView.getMapOrientation() - 10;
                if (angle < 0)
                    angle += 360f;
                mMapView.setMapOrientation(angle);
                updateInfo();
            }
            break;
        }
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
            appService.registerListener(AddPolygonActivity.this);
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
        Log.d("info", "AddPolygonActivity is updating user position");
    }

    public void showPopupWindow(View view) {

        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_add_information, null);

        // create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        // show the popup window
        // which view you pass in doesn't matter, it is only used for the window tolken
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        // dismiss the popup window when touched
        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                popupWindow.dismiss();
                return true;
            }
        });
    }
}
