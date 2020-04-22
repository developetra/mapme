package com.example.mapme.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;

import com.example.mapme.R;
import com.example.mapme.backend.AppService;
import com.example.mapme.widgets.CustomKmlFolder;
import com.example.mapme.widgets.CustomOverlay;

import org.osmdroid.api.IMapController;
import org.osmdroid.api.IMapView;
import org.osmdroid.bonuspack.kml.KmlDocument;
import org.osmdroid.bonuspack.kml.Style;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.FolderOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;

import java.util.HashMap;

/**
 * MapActivity - Activity that shows map with geoObjects, user position and menu items.
 */
public class MapActivity extends Activity implements View.OnClickListener, AppService.AppServiceListener {

    protected MapView mMapView = null;
    protected AppService appService;
    protected boolean appServiceBound;
    private boolean serviceConnected = false;
    private GeoPoint userGeoPoint;
    private Marker userMarker;
    private IMapController mapController;
    private ImageButton btnRotateLeft, btnRotateRight;
    private HashMap<String, String> objects = new HashMap<>();

    /**
     * Initializes layout and starts appServiceConnection.
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        // inflate and create the mMapView
        setContentView(R.layout.activity_map);
        mMapView = (MapView) findViewById(R.id.map);
        // set tile source
        mMapView.setTileSource(TileSourceFactory.MAPNIK);
//        mMapView.setTileSource(new OnlineTileSourceBase("USGS Topo", 0, 18, 256, "",
//                new String[] { "http://a.tile.stamen.com/toner/" }) {
//            @Override
//            public String getTileURLString(long pMapTileIndex) {
//                return getBaseUrl()
//                        + MapTileIndex.getZoom(pMapTileIndex)
//                        + "/" + MapTileIndex.getX(pMapTileIndex)
//                        + "/" + MapTileIndex.getY(pMapTileIndex)
//                        + mImageFilenameEnding;
//            }
//        });
        mMapView.setMultiTouchControls(true);
        mapController = mMapView.getController();
        mapController.setZoom(17.0);
        userGeoPoint = new GeoPoint(49.89873, 10.90067);
        mapController.setCenter(userGeoPoint);
        // enable rotation
        enableRotation();
        // set user marker
        userMarker = new Marker(mMapView);
        userMarker.setIcon(getResources().getDrawable(R.drawable.position));
        userMarker.setPosition(userGeoPoint);
        mMapView.getOverlays().add(userMarker);

        Log.d("info", "created Maps");
        Log.d("info", "Service bound to Maps");
    }

    @Override
    public void onResume() {
        super.onResume();

        // bind to service
        Intent bindIntent = new Intent(MapActivity.this, AppService.class);
        bindService(bindIntent, appServiceConnection, Context.BIND_AUTO_CREATE);

        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();

        unbindService(appServiceConnection);

        mMapView.onPause();
    }

    /**
     * Enables rotation using icons or multitouch.
     */
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

    /**
     * App Service Connection.
     */
    private ServiceConnection appServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            AppService.LocalBinder binder = (AppService.LocalBinder) service;
            appService = binder.getService();
            appServiceBound = true;
            appService.registerListener(MapActivity.this);
            serviceConnected = true;
            addAdditionalLayer();
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
        mapController.setCenter(userGeoPoint);
        mMapView.invalidate();
        Log.d("info", "MapActivity is updating user position");
    }

    /**
     * Opens new AddMarkerActivity.
     *
     * @param view
     */
    public void addMarker(View view) {
        Intent intent = new Intent(this, AddMarkerActivity.class);
        intent.putExtra("mapCenterLatitude", mMapView.getMapCenter().getLatitude());
        intent.putExtra("mapCenterLongitude", mMapView.getMapCenter().getLongitude());
        intent.putExtra("zoomLevel", mMapView.getZoomLevelDouble());
        intent.putExtra("orientation", mMapView.getMapOrientation());
        intent.putExtra("userGeoPointLatitude", userGeoPoint.getLatitude());
        intent.putExtra("userGeoPointLongitude", userGeoPoint.getLongitude());
        startActivity(intent);
    }

    /**
     * Opens new AddPolylineActivity.
     *
     * @param view
     */
    public void addPolyline(View view) {
        Intent intent = new Intent(this, AddPolylineActivity.class);
        intent.putExtra("mapCenterLatitude", mMapView.getMapCenter().getLatitude());
        intent.putExtra("mapCenterLongitude", mMapView.getMapCenter().getLongitude());
        intent.putExtra("zoomLevel", mMapView.getZoomLevelDouble());
        intent.putExtra("orientation", mMapView.getMapOrientation());
        intent.putExtra("userGeoPointLatitude", userGeoPoint.getLatitude());
        intent.putExtra("userGeoPointLongitude", userGeoPoint.getLongitude());
        startActivity(intent);
    }

    /**
     * Opens new AddPolygonActivity.
     *
     * @param view
     */
    public void addPolygon(View view) {
        Intent intent = new Intent(this, AddPolygonActivity.class);
        intent.putExtra("mapCenterLatitude", mMapView.getMapCenter().getLatitude());
        intent.putExtra("mapCenterLongitude", mMapView.getMapCenter().getLongitude());
        intent.putExtra("zoomLevel", mMapView.getZoomLevelDouble());
        intent.putExtra("orientation", mMapView.getMapOrientation());
        intent.putExtra("userGeoPointLatitude", userGeoPoint.getLatitude());
        intent.putExtra("userGeoPointLongitude", userGeoPoint.getLongitude());
        startActivity(intent);
    }

    /**
     * OnClick listener for rotate icons.
     *
     * @param view
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
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

    /**
     * Shows info dialog to reset database.
     *
     * @param view
     */
    public void showInfoResetDatabase(View view) {
        AlertDialog.Builder infoDialog = new AlertDialog.Builder(MapActivity.this);
        infoDialog.setTitle("Reset Database");
        infoDialog.setMessage("Do you really want to reset the database? All previously saved data will be lost. ");
        infoDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        infoDialog.setNeutralButton("Reset",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        resetDatabase();
                        dialog.cancel();
                    }
                });
        infoDialog.show();
    }

    /**
     * Calls appService to reset database.
     */
    public void resetDatabase() {
        appService.resetDatabase();
    }

    /**
     * Adds layer with geoObjects from database to map.
     */
    public void addAdditionalLayer() {
        mMapView.getOverlays().clear();
        objects = appService.getObjects();

        KmlDocument kmlDocument = new KmlDocument();

        if (!objects.isEmpty()) {
            for (String key : objects.keySet()) {
                kmlDocument.parseGeoJSON(objects.get(key));
                Drawable defaultMarker = getResources().getDrawable(R.drawable.pin);
                Bitmap defaultBitmap = ((BitmapDrawable) defaultMarker).getBitmap();
                Style defaultStyle = new Style(defaultBitmap, 0x901010AA, 3.0f, 0x20AA1010);
                CustomOverlay myOverLay = (CustomOverlay) kmlDocument.mKmlRoot.buildOverlay(mMapView, defaultStyle, null, kmlDocument);

                mMapView.getOverlays().add(myOverLay);
            }
            mMapView.invalidate();
            Log.d("info", "Additional layer was added");
        } else {
            Log.d("info", "Additional layer could not be added");
        }
    }

    final GestureDetector gestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
        public void onLongPress(MotionEvent e) {
            Log.e("", "Longpress detected");
        }
    });

    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

}
