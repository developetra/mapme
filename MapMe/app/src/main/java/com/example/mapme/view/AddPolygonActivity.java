package com.example.mapme.view;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.mapme.R;
import com.example.mapme.view.overlays.PaintingSurface;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.HashMap;

import hu.supercluster.overpasser.adapter.OverpassQueryResult;

/**
 * AddPolygonActivity - Activity to draw polygons on the map.
 */
public class AddPolygonActivity extends AddObjectActivity {

    /**
     * Initializes layout and starts appServiceConnection.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // initialize layout
        setContentView(R.layout.activity_add_object);
        mMapView = findViewById(R.id.map);
        setMapPositionAndUserMarker();
        enableRotation();
        enablePainting(PaintingSurface.Mode.Polygon);
    }

    /**
     * Opens new EditInformationActivity for last saved geoObject.
     */
    @Override
    public void startEditObjectActivity() {
        Intent intent = new Intent(this, EditInformationActivity.class);
        intent.putExtra("name", "Edit Polygon");
        intent.putExtra("id", currentGeoObjectId);
        startActivity(intent);
    }

    @Override
    public void addLayerWithOverpassResult(OverpassQueryResult result, int numberOfElements, final String objectId) {
        for (int i = 0; i < numberOfElements; i++) {
            final OverpassQueryResult.Element e = result.elements.get(i);
            GeoPoint geoPoint = new GeoPoint(e.lat, e.lon);

            Marker marker = new Marker(mMapView);
            marker.setPosition(geoPoint);
            marker.setTitle(e.tags.name);
            marker.setTextIcon(e.tags.name);
            //marker.setIcon(getDrawable(R.drawable.pin));
            marker.setOnMarkerClickListener(
                    new Marker.OnMarkerClickListener() {
                        @Override
                        public boolean onMarkerClick(Marker marker, MapView mapView) {
                            // add reference to database
                            HashMap<String, String> properties = new HashMap<>();
                            properties.put("reference", String.valueOf(e.id));
                            presenter.addObjectProperties(objectId, properties);
                            // show info
                            showInfoReferenceAdded(mMapView, objectId);
                            return false;
                        }
                    });
            mMapView.getOverlays().add(marker);
            marker.showInfoWindow();
            Log.d("info", "Layer with OverpassQueryResult was added");
        }
        // enable panning
        paintingSurface.setVisibility(View.GONE);
        panning.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        painting.setBackgroundColor(Color.TRANSPARENT);
    }

}
