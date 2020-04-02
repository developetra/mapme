package com.example.mapme.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.example.mapme.R;
import com.example.mapme.backend.AppService;

import java.util.HashMap;

public class EditInformationActivity extends AppCompatActivity implements AppService.AppServiceListener{

    protected AppService appService;
    protected boolean appServiceBound;
    private boolean serviceConnected = false;
    public String currentGeoObjectId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_information);

        // bind to service
        Intent bindIntent = new Intent(this, AppService.class);
        bindService(bindIntent, appServiceConnection, Context.BIND_AUTO_CREATE);
        Log.d("info", "Service bound to EditInformationActivity");

        Intent intent = getIntent();
        currentGeoObjectId = intent.getStringExtra("id");
        String name  = intent.getStringExtra("name");
        ((TextView)findViewById(R.id.textView)).setText(name);
    }

    /**
     * AppService Connection
     */
    public ServiceConnection appServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            AppService.LocalBinder binder = (AppService.LocalBinder) service;
            appService = binder.getService();
            appServiceBound = true;
            appService.registerListener(EditInformationActivity.this);
            serviceConnected = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            appServiceBound = false;
        }
    };

    @Override
    public void updateUserPosition(Location location) { }

    public void save (View view){
        // get user input
        String property01 = ((EditText)findViewById(R.id.property01)).getText().toString();
        String property02 = ((EditText)findViewById(R.id.property02)).getText().toString();
        String property03 = ((EditText)findViewById(R.id.property03)).getText().toString();
        String input01 = ((EditText)findViewById(R.id.input01)).getText().toString();
        String input02 = ((EditText)findViewById(R.id.input02)).getText().toString();
        String input03 = ((EditText)findViewById(R.id.input03)).getText().toString();
        // create HashMap with user input
        HashMap<String,String> properties = new HashMap<>();
        properties.put(property01, input01);
        properties.put(property02, input02);
        properties.put(property03, input03);
        // save properties to database
        appService.editObject(currentGeoObjectId, properties);
    }
}
