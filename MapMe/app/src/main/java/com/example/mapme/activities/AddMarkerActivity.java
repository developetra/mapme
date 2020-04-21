package com.example.mapme.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.mapme.R;
import com.example.mapme.backend.AppService;
import com.example.mapme.backend.IconPlottingOverlay;

import org.osmdroid.bonuspack.kml.KmlDocument;
import org.osmdroid.bonuspack.kml.Style;
import org.osmdroid.views.overlay.FolderOverlay;

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
        // bind to service
        Intent bindIntent = new Intent(this, AppService.class);
        bindService(bindIntent, appServiceConnection, Context.BIND_AUTO_CREATE);
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
    public void editObject() {
        Intent intent = new Intent(this, EditInformationActivity.class);
        intent.putExtra("name", "Edit Marker");
        intent.putExtra("id", currentGeoObjectId);
        startActivity(intent);
    }

    @Override
    public void addAdditionalLayer() {
        super.addAdditionalLayer();
        IconPlottingOverlay plotter = new IconPlottingOverlay(this, getResources().getDrawable(R.drawable.marker_default));
        mMapView.getOverlayManager().add(plotter);
    }

}
