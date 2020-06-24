package com.example.mapme.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

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
        setContentView(R.layout.activity_add_object);
        mapView = findViewById(R.id.map);
        setMapPosition();
        setUserMarker();
        enableRotation();
        disablePainting();
        IconPlottingOverlay plotter = new IconPlottingOverlay(this, getResources().getDrawable(R.drawable.pin));
        mapView.getOverlayManager().add(plotter);
        Toast toast = Toast.makeText(this, "Press and hold to set a marker", Toast.LENGTH_LONG);
        toast.setGravity(Gravity.TOP, 0, 200);
        toast.show();
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

    /**
     * Adds additional layer with geoObjects & IconPlottingOverlay to map.
     *
     * @param objects
     */
    @Override
    public void addAdditionalLayer(HashMap<String, String> objects) {
        super.addAdditionalLayer(objects);
        IconPlottingOverlay plotter = new IconPlottingOverlay(this, getResources().getDrawable(R.drawable.pin));
        mapView.getOverlayManager().add(plotter);
    }

}
