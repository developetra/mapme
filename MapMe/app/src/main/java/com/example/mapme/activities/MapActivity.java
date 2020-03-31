package com.example.mapme.activities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
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
import android.view.View;
import android.widget.ImageButton;

import com.example.mapme.R;
import com.example.mapme.backend.AppService;
import com.example.mapme.backend.GeoJsonHelper;
import com.example.mapme.backend.OverpassHelper;

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

public class MapActivity extends Activity implements View.OnClickListener, AppService.AppServiceListener {

    protected MapView mMapView = null;
    protected AppService appService;
    protected boolean appServiceBound;
    private boolean serviceConnected = false;
    private GeoPoint userGeoPoint;
    private Marker userMarker;
    private IMapController mapController;
    private ImageButton btnRotateLeft, btnRotateRight;
    private GeoJsonHelper geoJsonHelper = new GeoJsonHelper();
    private OverpassHelper overpassHelper = new OverpassHelper();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        // inflate and create the mMapView
        setContentView(R.layout.activity_map);
        mMapView = (MapView) findViewById(R.id.map);

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

        // bind to service
        Intent bindIntent = new Intent(MapActivity.this, AppService.class);
        bindService(bindIntent, appServiceConnection, Context.BIND_AUTO_CREATE);
        Log.d("info", "created Maps");
        Log.d("info", "Service bound to Maps");

        // test
        addAdditionalLayer();

    }

    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    public void onPause() {
        super.onPause();
        mMapView.onPause();
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

    /**
     * App Service Connection
     */
    private ServiceConnection appServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            AppService.LocalBinder binder = (AppService.LocalBinder) service;
            appService = binder.getService();
            appServiceBound = true;
            appService.registerListener(MapActivity.this);
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
        mapController.setCenter(userGeoPoint);
        mMapView.invalidate();
        Log.d("info", "MapActivity is updating user position");
    }

    public void addMarker(View view) {

        // TEST
        //File file = geoJsonHelper.writeGeoJSON(myOverLay);
        appService.uploadFile();


        Intent intent = new Intent(this, AddMarkerActivity.class);
        intent.putExtra("mapCenterLatitude", mMapView.getMapCenter().getLatitude());
        intent.putExtra("mapCenterLongitude", mMapView.getMapCenter().getLongitude());
        intent.putExtra("zoomLevel", mMapView.getZoomLevelDouble());
        intent.putExtra("orientation", mMapView.getMapOrientation());
        intent.putExtra("userGeoPointLatitude", userGeoPoint.getLatitude());
        intent.putExtra("userGeoPointLongitude", userGeoPoint.getLongitude());
        startActivity(intent);
    }

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

    private void addAdditionalLayer() {
        String jsonString = geoJsonHelper.readGeoJSON(this);
        KmlDocument kmlDocument = new KmlDocument();
        kmlDocument.parseGeoJSON(jsonString);
        Drawable defaultMarker = getResources().getDrawable(R.drawable.pin);
        Bitmap defaultBitmap = ((BitmapDrawable) defaultMarker).getBitmap();
        Style defaultStyle = new Style(defaultBitmap, 0x901010AA, 3.0f, 0x20AA1010);
        FolderOverlay myOverLay = (FolderOverlay) kmlDocument.mKmlRoot.buildOverlay(mMapView, defaultStyle, null, kmlDocument);
        mMapView.getOverlays().add(myOverLay);
        mMapView.invalidate();
        Log.d("info", "Additional layer was added");
    }
}
