package com.example.mapme.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.mapme.R;
import com.example.mapme.backend.AppService;

public class AddPolygonActivity extends AddObjectActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_object);

        mMapView = findViewById(R.id.map);
        setMapPositionAndUserMarker();
        enableRotation();
        enablePainting();

        // bind to service
        Intent bindIntent = new Intent(this, AppService.class);
        bindService(bindIntent, appServiceConnection, Context.BIND_AUTO_CREATE);
        Log.d("info", "Service bound to AddPolygonActivity");
    }

    @Override
    public void editObject(View view){
        Intent intent = new Intent(this, EditInformationActivity.class);
        intent.putExtra("name", "Edit Polygon");
        intent.putExtra("id", currentGeoObjectId);
        startActivity(intent);
    }

}
