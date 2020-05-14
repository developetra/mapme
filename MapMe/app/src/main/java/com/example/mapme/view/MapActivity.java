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
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.example.mapme.R;
import com.example.mapme.model.AppService;
import com.example.mapme.presenter.MapPresenter;
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
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;

import java.util.HashMap;

/**
 * MapActivity - Activity that shows map with geoObjects, user position and menu items.
 */
public class MapActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int LINE_COLOR = Color.parseColor("#F34E2B");
    public static final int FILL_COLOR = Color.parseColor("#90F28A74");
    public AppService appService;
    protected boolean appServiceBound;
    private boolean serviceConnected = false;

    private MapPresenter presenter;

    protected MapView mMapView = null;
    private Marker userMarker;
    private IMapController mapController;
    private ImageButton btnRotateLeft, btnRotateRight;


    /**
     * Initializes layout and starts appServiceConnection.
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenter = new MapPresenter(this);
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

        // enable rotation
        enableRotation();
        // set user marker
        userMarker = new Marker(mMapView);
        userMarker.setIcon(getResources().getDrawable(R.drawable.position));
        presenter.setUserPosition();
        mMapView.getOverlays().add(userMarker);
        Log.d("info", "created Maps");
    }

    @Override
    public void onResume() {
        super.onResume();
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
            appService.registerListener(presenter);
            serviceConnected = true;
            presenter.dataChanged();
            Log.i("info", "Service bound to MapActivity");
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            appServiceBound = false;
            Log.i("info", "Service unbound to MapActivity");
        }
    };

    /**
     * Updates user position on map.
     *
     * @param userGeoPoint
     */
    public void updateUserPosition(GeoPoint userGeoPoint) {
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
    public void startAddMarkerActivity(View view) {
        Intent intent = new Intent(this, AddMarkerActivity.class);
        intent.putExtra("mapCenterLatitude", mMapView.getMapCenter().getLatitude());
        intent.putExtra("mapCenterLongitude", mMapView.getMapCenter().getLongitude());
        intent.putExtra("zoomLevel", mMapView.getZoomLevelDouble());
        intent.putExtra("orientation", mMapView.getMapOrientation());
        intent.putExtra("userGeoPointLatitude", presenter.userGeoPoint.getLatitude());
        intent.putExtra("userGeoPointLongitude", presenter.userGeoPoint.getLongitude());
        startActivity(intent);
    }

    /**
     * Opens new AddPolylineActivity.
     *
     * @param view
     */
    public void startAddPolylineActivity(View view) {
        Intent intent = new Intent(this, AddPolylineActivity.class);
        intent.putExtra("mapCenterLatitude", mMapView.getMapCenter().getLatitude());
        intent.putExtra("mapCenterLongitude", mMapView.getMapCenter().getLongitude());
        intent.putExtra("zoomLevel", mMapView.getZoomLevelDouble());
        intent.putExtra("orientation", mMapView.getMapOrientation());
        intent.putExtra("userGeoPointLatitude", presenter.userGeoPoint.getLatitude());
        intent.putExtra("userGeoPointLongitude", presenter.userGeoPoint.getLongitude());
        startActivity(intent);
    }

    /**
     * Opens new AddPolygonActivity.
     *
     * @param view
     */
    public void startAddPolygonActivity(View view) {
        Intent intent = new Intent(this, AddPolygonActivity.class);
        intent.putExtra("mapCenterLatitude", mMapView.getMapCenter().getLatitude());
        intent.putExtra("mapCenterLongitude", mMapView.getMapCenter().getLongitude());
        intent.putExtra("zoomLevel", mMapView.getZoomLevelDouble());
        intent.putExtra("orientation", mMapView.getMapOrientation());
        intent.putExtra("userGeoPointLatitude", presenter.userGeoPoint.getLatitude());
        intent.putExtra("userGeoPointLongitude", presenter.userGeoPoint.getLongitude());
        startActivity(intent);
    }

    public void startDataActivity(View view) {
        Intent intent = new Intent(this, DataActivity.class);
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
                        presenter.resetDatabase();
                        dialog.cancel();
                    }
                });
        infoDialog.show();
    }

    /**
     * Adds layer with geoObjects from database to map.
     */
    public void addAdditionalLayer(HashMap<String, String> objects) {
        mMapView.getOverlays().clear();
        KmlDocument kmlDocument = new KmlDocument();
        if (!objects.isEmpty()) {
            for (String key : objects.keySet()) {
                kmlDocument.parseGeoJSON(objects.get(key));
                Drawable defaultMarker = getResources().getDrawable(R.drawable.pin);
                Bitmap defaultBitmap = ((BitmapDrawable) defaultMarker).getBitmap();
                Style defaultStyle = new Style(defaultBitmap, LINE_COLOR, 3.0f, FILL_COLOR);
                CustomKmlFolder cKmlFolder = new CustomKmlFolder();
                cKmlFolder.mItems = kmlDocument.mKmlRoot.mItems;
                CustomOverlay myOverLay = cKmlFolder.buildOverlay(mMapView, defaultStyle, null, kmlDocument, key);
                mMapView.getOverlays().add(myOverLay);
            }
            mMapView.invalidate();
            Log.i("info", "Additional layer was added to MapActivity.");
        } else {
            Log.i("info", "Additional layer could not be added to MapActivity.");
        }
    }


}
