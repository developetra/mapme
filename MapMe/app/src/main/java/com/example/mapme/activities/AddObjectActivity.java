package com.example.mapme.activities;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.example.mapme.R;
import com.example.mapme.backend.AppService;
import com.example.mapme.backend.PaintingSurface;

import org.osmdroid.api.IMapController;
import org.osmdroid.api.IMapView;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;

/**
 * AddObjectActivity - this abstract Activity provides all common methods for the AddMarker, AddPolygon and AddPolyline Activities.
 */
public abstract class AddObjectActivity extends AppCompatActivity implements View.OnClickListener, AppService.AppServiceListener {

    protected MapView mMapView;
    private GeoPoint userGeoPoint;
    private Marker userMarker;
    private ImageButton btnRotateLeft, btnRotateRight;
    private ImageButton painting, panning;
    private PaintingSurface paintingSurface;
    protected AppService appService;
    protected boolean appServiceBound;
    private boolean serviceConnected = false;
    private PopupWindow popupWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_object);
    }

    public void setMapPositionAndUserMarker() {
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

    public void enableRotation() {
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

    public void enablePainting() {
        panning = findViewById(R.id.enablePanning);
        panning.setOnClickListener(this);
        panning.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        painting = findViewById(R.id.enablePainting);
        painting.setOnClickListener(this);
        paintingSurface = findViewById(R.id.paintingSurface);
        paintingSurface.init(this, mMapView);
        paintingSurface.setMode(PaintingSurface.Mode.Polygon);
    }

    public void disablePainting() {
        panning = findViewById(R.id.enablePanning);
        panning.setVisibility(View.GONE);
        painting = findViewById(R.id.enablePainting);
        painting.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.enablePanning:
                paintingSurface.setVisibility(View.GONE);
                panning.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                painting.setBackgroundColor(Color.TRANSPARENT);
                break;
            case R.id.enablePainting:
                paintingSurface.setVisibility(View.VISIBLE);
                painting.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                panning.setBackgroundColor(Color.TRANSPARENT);
                break;
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
            break;
        }
    }

    /**
     * AppService Connection
     */
    public ServiceConnection appServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            AppService.LocalBinder binder = (AppService.LocalBinder) service;
            appService = binder.getService();
            appServiceBound = true;
            appService.registerListener(AddObjectActivity.this);
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
        mMapView.invalidate();
        Log.d("info", "Updating user position");
    }

    public void showPopupWindow(View view) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_edit_information, null);
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true;
        popupWindow = new PopupWindow(popupView, width, height, focusable);
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
    }

    public void closePopupWindow(View view) {
        popupWindow.dismiss();
    }

    public void editObject(View view) {
        Intent intent = new Intent(this, EditInformationActivity.class);
        startActivity(intent);
    }
}
