package com.example.mapme.view;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import com.example.mapme.R;
import com.example.mapme.view.overlays.PaintingSurface;

import org.osmdroid.views.overlay.OverlayWithIW;

import hu.supercluster.overpasser.adapter.OverpassQueryResult;

/**
 * AddPolylineActivity - Activity to draw polylines on the map.
 */
public class AddPolylineActivity extends AddObjectActivity {

    /**
     * Initializes layout and starts appServiceConnection.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_object);
        mapView = findViewById(R.id.map);
        setMapPosition();
        setUserMarker();
        enableRotation();
        enablePainting(PaintingSurface.Mode.Polyline);
    }

    /**
     * Opens new EditInformationActivity for last saved geoObject.
     */
    @Override
    public void startEditObjectActivity() {
        Intent intent = new Intent(this, EditInformationActivity.class);
        intent.putExtra("name", "Edit Polyline");
        intent.putExtra("id", currentGeoObjectId);
        startActivity(intent);
    }

    /**
     * Shows info dialog to add reference or edit geoObject.
     *
     * @param view
     * @param objectId
     */
    public void showInfoAddReferenceOrEditObject(View view, final String objectId, final OverlayWithIW geometry) {
        super.showInfoAddReferenceOrEditObject(view, objectId, geometry);
        paintingSurface.setVisibility(View.GONE);
        panning.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        painting.setBackgroundColor(Color.TRANSPARENT);
    }
    
    /**
     * Shows info dialog when OverpassResult was empty and sets mode back to mapmode.
     *
     * @param id
     */
    @Override
    public void showInfoEmptyOverpassResult(String id) {
        super.showInfoEmptyOverpassResult(id);
        paintingSurface.setVisibility(View.GONE);
        panning.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        painting.setBackgroundColor(Color.TRANSPARENT);
    }

    /**
     * Adds additional layer with OverpassResult to map and removes PainingSurface.
     *
     * @param nodes
     * @param numberOfNodes
     * @param objectId
     */
    @Override
    public void addLayerWithOverpassResult(OverpassQueryResult nodes, int numberOfNodes, final String objectId) {
        super.addLayerWithOverpassResult(nodes, numberOfNodes, objectId);
        paintingSurface.setVisibility(View.GONE);
    }
}
