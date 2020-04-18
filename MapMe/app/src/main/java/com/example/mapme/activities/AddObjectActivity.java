package com.example.mapme.activities;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.example.mapme.R;
import com.example.mapme.backend.AppService;
import com.example.mapme.backend.PaintingSurface;

import org.osmdroid.api.IMapController;
import org.osmdroid.api.IMapView;
import org.osmdroid.bonuspack.kml.KmlDocument;
import org.osmdroid.bonuspack.kml.Style;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.FolderOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.OverlayWithIW;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;

import java.util.ArrayList;

/**
 * AddObjectActivity - this abstract Activity provides all common methods for AddMarker, AddPolygon and AddPolyline Activities.
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
    public String currentGeoObjectId;
    private ArrayList<String> objects = new ArrayList<String>();

    @Override
    protected void onRestart() {
        super.onRestart();
        // bind to service
        Intent bindIntent = new Intent(this, AppService.class);
        bindService(bindIntent, appServiceConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * Processes extras from intent and sets map position and user marker accordingly.
     */
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

    /**
     * Enables rotation using icons or multitouch.
     */
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

    /**
     * Enables painting by initialising painting surface and setting mode.
     */
    public void enablePainting(PaintingSurface.Mode mode) {
        panning = findViewById(R.id.enablePanning);
        panning.setOnClickListener(this);
        panning.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        painting = findViewById(R.id.enablePainting);
        painting.setOnClickListener(this);
        paintingSurface = findViewById(R.id.paintingSurface);
        paintingSurface.init(this, mMapView);
        paintingSurface.setMode(mode);
    }

    /**
     * Disables painting.
     */
    public void disablePainting() {
        panning = findViewById(R.id.enablePanning);
        panning.setVisibility(View.GONE);
        painting = findViewById(R.id.enablePainting);
        painting.setVisibility(View.GONE);
    }

    /**
     * OnClick listener for rotate and painting/panning icons.
     *
     * @param view
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
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
     * AppService Connection.
     */
    public ServiceConnection appServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            AppService.LocalBinder binder = (AppService.LocalBinder) service;
            appService = binder.getService();
            appServiceBound = true;
            appService.registerListener(AddObjectActivity.this);
            serviceConnected = true;
            addAdditionalLayer();
            Log.d("info", "Service bound to Activity");
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            appServiceBound = false;
        }
    };

    /**
     * Updates user position on map.
     *
     * @param location
     */
    @Override
    public void updateUserPosition(Location location) {
        userGeoPoint.setLatitude(location.getLatitude());
        userGeoPoint.setLongitude(location.getLongitude());
        userMarker.setPosition(userGeoPoint);
        mMapView.invalidate();
        Log.d("info", "Updating user position");
    }

    /**
     * Shows info dialog to edit geoObject.
     *
     * @param view
     * @param id
     */
    public void showInfo(View view, String id) {
        currentGeoObjectId = id;
        AlertDialog.Builder infoDialog = new AlertDialog.Builder(AddObjectActivity.this);
        infoDialog.setTitle("GeoObject (Id: " + id + ")");
        infoDialog.setMessage("The GeoObject has already been saved to the database. You can edit the information about the GeoObject.");
        infoDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        infoDialog.setNeutralButton("Edit",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        editObject();
                    }
                });
        infoDialog.show();
    }

    /**
     * Opens new EditInformationActivity for last saved geoObject.
     */
    public void editObject() {
        Intent intent = new Intent(this, EditInformationActivity.class);
        intent.putExtra("id", currentGeoObjectId);
        startActivity(intent);
    }

    /**
     * Calls appService to save geoObject to database.
     *
     * @param geometry
     * @return
     */
    public String saveToDatabase(OverlayWithIW geometry) {
        return appService.saveToDatabase(geometry);
    }

    /**
     * Go back to previous activity.
     * @param view
     */
    public void back(View view){
        this.finish();
    }

    public void addAdditionalLayer() {
        mMapView.getOverlays().clear();
        objects = appService.getObjects();
        KmlDocument kmlDocument = new KmlDocument();
        if (!objects.isEmpty()) {
            for (String s : objects) {
                Log.d("info", "GeoObject from database: " + s);
                kmlDocument.parseGeoJSON(s);
                Drawable defaultMarker = getResources().getDrawable(R.drawable.pin);
                Bitmap defaultBitmap = ((BitmapDrawable) defaultMarker).getBitmap();
                Style defaultStyle = new Style(defaultBitmap, 0x901010AA, 3.0f, 0x20AA1010);
                FolderOverlay myOverLay = (FolderOverlay) kmlDocument.mKmlRoot.buildOverlay(mMapView, defaultStyle, null, kmlDocument);
                mMapView.getOverlays().add(myOverLay);
            }
            mMapView.invalidate();
            Log.d("info", "Additional layer was added");
        } else {
            Log.d("info", "Additional layer could not be added");
        }
    }
}
