package com.example.mapme.view;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import com.example.mapme.R;
import com.example.mapme.view.overlays.PaintingSurface;

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
        setContentView(R.layout.activity_add_object);
        mapView = findViewById(R.id.map);
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
     * @param nodes
     * @param numberOfNodes
     * @param ways
     * @param numberOfWays
     * @param relations
     * @param numberOfRelations
     * @param objectId
     */
    @Override
    public void addLayerWithOverpassResult(OverpassQueryResult nodes, int numberOfNodes, OverpassQueryResult ways, int numberOfWays, OverpassQueryResult relations, int numberOfRelations, final String objectId) {
        super.addLayerWithOverpassResult(nodes, numberOfNodes, ways, numberOfWays, relations, numberOfRelations, objectId);
        paintingSurface.setVisibility(View.GONE);
    }

}
