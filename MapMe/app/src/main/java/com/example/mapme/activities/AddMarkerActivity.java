package com.example.mapme.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.mapme.R;
import com.example.mapme.backend.AppService;
import com.example.mapme.backend.IconPlottingOverlay;


public class AddMarkerActivity extends AddObjectActivity implements View.OnLongClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        Log.d("info", "Service bound to AddMarkerActivity");
    }

    @Override
    public boolean onLongClick(View view) {
        return true;
    }

    @Override
    public void editObject(View view){
        Intent intent = new Intent(this, EditInformationActivity.class);
        intent.putExtra("name", "Edit Marker");
        intent.putExtra("id", currentGeoObjectId);
        startActivity(intent);
    }

}
