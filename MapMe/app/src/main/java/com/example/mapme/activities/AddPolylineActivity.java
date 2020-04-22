package com.example.mapme.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.example.mapme.R;
import com.example.mapme.backend.AppService;
import com.example.mapme.backend.PaintingSurface;

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
        // initialize layout
        setContentView(R.layout.activity_add_object);
        mMapView = findViewById(R.id.map);
        setMapPositionAndUserMarker();
        enableRotation();
        enablePainting(PaintingSurface.Mode.Polyline);
    }

    /**
     * Opens new EditInformationActivity for last saved geoObject.
     */
    @Override
    public void editObject() {
        Intent intent = new Intent(this, EditInformationActivity.class);
        intent.putExtra("name", "Edit Polyline");
        intent.putExtra("id", currentGeoObjectId);
        startActivity(intent);
    }
}
