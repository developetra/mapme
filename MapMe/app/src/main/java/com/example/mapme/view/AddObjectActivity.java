package com.example.mapme.view;

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
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.example.mapme.R;
import com.example.mapme.model.AppService;
import com.example.mapme.presenter.AddObjectPresenter;
import com.example.mapme.view.overlays.PaintingSurface;
import com.example.mapme.widgets.CustomKmlFolder;
import com.example.mapme.widgets.CustomOverlay;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.kml.KmlDocument;
import org.osmdroid.bonuspack.kml.Style;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.OverlayWithIW;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;

import java.util.HashMap;

import hu.supercluster.overpasser.adapter.OverpassQueryResult;

/**
 * AddObjectActivity - this abstract Activity provides all common methods for AddMarker, AddPolygon and AddPolyline Activities.
 */
public abstract class AddObjectActivity extends AppCompatActivity implements View.OnClickListener {

    public AddObjectPresenter presenter;
    public AppService appService;
    protected boolean appServiceBound;
    private boolean serviceConnected;
    protected MapView mapView;
    private Marker userMarker;
    private ImageButton btnRotateLeft, btnRotateRight;
    public ImageButton painting, panning;
    public PaintingSurface paintingSurface;
    public String currentGeoObjectId;
    public static final int LINE_COLOR = Color.parseColor("#F34E2B");
    public static final int FILL_COLOR = Color.parseColor("#90F28A74");

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenter = new AddObjectPresenter(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        Intent bindIntent = new Intent(this, AppService.class);
        bindService(bindIntent, appServiceConnection, Context.BIND_AUTO_CREATE);
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        unbindService(appServiceConnection);
        mapView.onPause();
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
            appService.registerListener(presenter);
            serviceConnected = true;
            presenter.getData();
            Log.i("info", "Service bound to AddObjectActivity.");
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            appServiceBound = false;
            Log.i("info", "Service unbound to AddObjectActivity.");
        }
    };

    /**
     * Processes extras from intent and sets map position and user marker accordingly.
     */
    public void setMapPositionAndUserMarker() {
        IMapController mapController = mapView.getController();
        Intent intent = getIntent();
        double mapCenterLatitude = intent.getDoubleExtra("mapCenterLatitude", 49.89873);
        double mapCenterLongitude = intent.getDoubleExtra("mapCenterLongitude", 10.90067);
        double zoomLevel = intent.getDoubleExtra("zoomLevel", 17.0);
        double userGeoPointLatitude = intent.getDoubleExtra("userGeoPointLatitude", 0);
        double userGeoPointLongitude = intent.getDoubleExtra("userGeoPointLongitude", 0);
        GeoPoint startPoint = new GeoPoint(mapCenterLatitude, mapCenterLongitude);
        mapController.setCenter(startPoint);
        mapController.setZoom(zoomLevel);
        userMarker = new Marker(mapView);
        userMarker.setIcon(getResources().getDrawable(R.drawable.position));
        userMarker.setPosition(new GeoPoint(userGeoPointLatitude, userGeoPointLongitude));
        mapView.getOverlays().add(userMarker);
    }

    /**
     * Enables rotation using icons or multitouch.
     */
    public void enableRotation() {
        btnRotateLeft = findViewById(R.id.btnRotateLeft);
        btnRotateRight = findViewById(R.id.btnRotateRight);
        btnRotateRight.setOnClickListener(this);
        btnRotateLeft.setOnClickListener(this);
        mapView = findViewById(R.id.map);
        RotationGestureOverlay rotationGestureOverlay = new RotationGestureOverlay(mapView);
        rotationGestureOverlay.setEnabled(true);
        mapView.setMultiTouchControls(true);
        mapView.setMapListener(new MapListener() {
            @Override
            public boolean onScroll(ScrollEvent event) {
                return true;
            }

            @Override
            public boolean onZoom(ZoomEvent event) {
                return true;
            }
        });
        mapView.getOverlayManager().add(rotationGestureOverlay);
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
        paintingSurface.init(this, mapView);
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
                float angle = mapView.getMapOrientation() + 10;
                if (angle > 360)
                    angle = 360 - angle;
                mapView.setMapOrientation(angle);
            }
            break;
            case R.id.btnRotateRight: {
                float angle = mapView.getMapOrientation() - 10;
                if (angle < 0)
                    angle += 360f;
                mapView.setMapOrientation(angle);
            }
            break;
        }
    }

    /**
     * Updates user position on map.
     *
     * @param userGeoPoint
     */
    public void updateUserPosition(GeoPoint userGeoPoint) {
        userMarker.setPosition(userGeoPoint);
        mapView.invalidate();
        Log.i("info", "Updating user position.");
    }

    /**
     * Opens new EditInformationActivity for last saved geoObject.
     */
    public void startEditObjectActivity() {
        Intent intent = new Intent(this, EditInformationActivity.class);
        intent.putExtra("id", currentGeoObjectId);
        startActivity(intent);
    }

    /**
     * Calls presenter to save geoObject to database.
     *
     * @param geometry
     * @return
     */
    public String saveToDatabase(OverlayWithIW geometry) {
        return presenter.saveToDatabase(geometry);
    }

    /**
     * Go back to previous activity.
     *
     * @param view
     */
    public void back(View view) {
        this.finish();
    }

    /**
     * Shows info dialog to edit geoObject.
     *
     * @param view
     * @param id
     */
    public void showInfoEditObject(View view, String id) {
        currentGeoObjectId = id;
        AlertDialog.Builder infoDialog = new AlertDialog.Builder(this);
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
                        startEditObjectActivity();
                    }
                });
        infoDialog.show();
    }

    /**
     * Shows info dialog to add reference or edit geoObject.
     *
     * @param view
     * @param objectId
     */
    public void showInfoAddReferenceOrEditObject(View view, final String objectId, final OverlayWithIW geometry) {
        currentGeoObjectId = objectId;
        AlertDialog.Builder infoDialog = new AlertDialog.Builder(this);
        infoDialog.setTitle("GeoObject (Id: " + objectId + ")");
        infoDialog.setMessage("The GeoObject has already been saved to the database. You can either add a reference to an existing object or edit the information about the GeoObject.");
        infoDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        infoDialog.setNeutralButton("Edit",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        startEditObjectActivity();
                    }
                });
        infoDialog.setPositiveButton("Add Reference",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        presenter.computeOverpassResult(geometry, objectId);
                    }
                });
        infoDialog.show();
    }

    /**
     * Shows info dialog when reference was added.
     *
     * @param view
     * @param id
     */
    public void showInfoReferenceAdded(View view, String id) {
        currentGeoObjectId = id;
        AlertDialog.Builder infoDialog = new AlertDialog.Builder(this);
        infoDialog.setTitle("GeoObject (Id: " + id + ")");
        infoDialog.setMessage("The reference was added to the GeoObject. You can now edit the information about the GeoObject.");
        infoDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        infoDialog.setNeutralButton("Edit",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        startEditObjectActivity();
                    }
                });
        infoDialog.show();
    }

    /**
     * Shows info dialog when OverpassResult was empty.
     *
     * @param id
     */
    public void showInfoEmptyOverpassResult(String id) {
        currentGeoObjectId = id;
        AlertDialog.Builder infoDialog = new AlertDialog.Builder(this);
        infoDialog.setTitle("GeoObject (Id: " + id + ")");
        infoDialog.setMessage("There were no objects to reference found nearby.");
        infoDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        infoDialog.setNeutralButton("Edit GeoObject",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        startEditObjectActivity();
                    }
                });
        infoDialog.show();
    }

    /**
     * Adds additional layer with geoObjects to map.
     *
     * @param objects
     */
    public void addAdditionalLayer(HashMap<String, String> objects) {
        mapView.getOverlays().clear();
        KmlDocument kmlDocument = new KmlDocument();

        if (!objects.isEmpty()) {
            for (String key : objects.keySet()) {
                kmlDocument.parseGeoJSON(objects.get(key));
                Drawable defaultMarker = getResources().getDrawable(R.drawable.pin);
                Bitmap defaultBitmap = ((BitmapDrawable) defaultMarker).getBitmap();
                Style defaultStyle = new Style(defaultBitmap, LINE_COLOR, 3.0f, FILL_COLOR);
                CustomKmlFolder cKmlFolder = new CustomKmlFolder();
                cKmlFolder.mItems = kmlDocument.mKmlRoot.mItems;
                CustomOverlay myOverLay = cKmlFolder.buildOverlay(mapView, defaultStyle, null, kmlDocument, key);
                mapView.getOverlays().add(myOverLay);
            }
            mapView.invalidate();
            Log.i("info", "Additional layer was added.");
        } else {
            Log.i("info", "Additional layer could not be added or is empty.");
        }
    }

    /**
     * Adds additional layer with OverpassResult to map.
     *
     */
    public void addLayerWithOverpassResult(OverpassQueryResult nodes, int numberOfNodes, final String objectId) {
        for (int i = 0; i < numberOfNodes; i++) {
            final OverpassQueryResult.Element e = nodes.elements.get(i);
            GeoPoint geoPoint = new GeoPoint(e.lat, e.lon);
            Marker marker = new Marker(mapView);
            marker.setPosition(geoPoint);
            marker.setTitle(e.tags.name);
            marker.setTextIcon(e.tags.name);
            marker.setOnMarkerClickListener(
                    new Marker.OnMarkerClickListener() {
                        @Override
                        public boolean onMarkerClick(Marker marker, MapView mapView) {
                            HashMap<String, String> properties = new HashMap<>();
                            properties.put("reference", String.valueOf(e.id));
                            AddObjectActivity.this.presenter.addObjectProperties(objectId, properties);
                            AddObjectActivity.this.showInfoReferenceAdded(AddObjectActivity.this.mapView, objectId);
                            return false;
                        }
                    });
            mapView.getOverlays().add(marker);
        }
        mapView.invalidate();
        Log.i("info", "Layer with Overpass Result was added.");
        panning.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        painting.setBackgroundColor(Color.TRANSPARENT);
    }

}