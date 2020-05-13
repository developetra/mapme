package com.example.mapme.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.mapme.R;
import com.example.mapme.view.overlays.IconPlottingOverlay;

import java.util.HashMap;

/**
 * AddMarkerActivity - Activity to add marker to the map.
 */
public class AddMarkerActivity extends AddObjectActivity implements View.OnLongClickListener {

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
        disablePainting();
        IconPlottingOverlay plotter = new IconPlottingOverlay(this, getResources().getDrawable(R.drawable.marker_default));
        mMapView.getOverlayManager().add(plotter);
    }

    /**
     * Drop marker onLongClick.
     *
     * @param view
     * @return
     */
    @Override
    public boolean onLongClick(View view) {
        return true;
    }

    /**
     * Opens new EditInformationActivity for last saved geoObject.
     */
    @Override
    public void startEditObjectActivity() {
        Intent intent = new Intent(this, EditInformationActivity.class);
        intent.putExtra("name", "Edit Marker");
        intent.putExtra("id", currentGeoObjectId);
        startActivity(intent);
    }

    @Override
    public void addAdditionalLayer(HashMap<String, String> objects) {
        super.addAdditionalLayer(objects);
        IconPlottingOverlay plotter = new IconPlottingOverlay(this, getResources().getDrawable(R.drawable.marker_default));
        mMapView.getOverlayManager().add(plotter);
    }

}
