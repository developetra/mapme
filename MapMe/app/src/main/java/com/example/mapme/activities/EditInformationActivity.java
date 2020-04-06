package com.example.mapme.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.mapme.R;
import com.example.mapme.backend.AppService;

import java.util.HashMap;

public class EditInformationActivity extends AppCompatActivity implements AppService.AppServiceListener{

    protected AppService appService;
    protected boolean appServiceBound;
    private boolean serviceConnected = false;
    public String currentGeoObjectId = "";
    private int inputCounter = 0;

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

        addInputField(findViewById(R.id.textView));
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
        // create HashMap
        HashMap<String,String> properties = new HashMap<>();
        // fill HashMap with user input
        for (int i = 0; i< inputCounter; i++){
            TableRow tableRow = findViewById(i);
            String property = ((EditText)tableRow.getChildAt(0)).getText().toString();
            String input = ((EditText)tableRow.getChildAt(1)).getText().toString();
            properties.put(property, input);
            Log.d("info", "property + input " + property + input);
        }
        // save properties to database
        appService.editObject(currentGeoObjectId, properties);
    }

    public void addInputField(View view){
        TableLayout inputFields = findViewById(R.id.inputFields);
        TableRow tableRow = new TableRow(this);
        tableRow.setId(inputCounter++);
        // property field
        EditText editTextProperty = new EditText(this);
        editTextProperty.setWidth(500);
        TableRow.LayoutParams paramsProperty = new TableRow.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
        paramsProperty.setMargins(8, 8, 8,8);
        tableRow.addView(editTextProperty, paramsProperty);
        //input field
        EditText editTextInput = new EditText(this);
        editTextInput.setWidth(500);
        TableRow.LayoutParams paramsInput = new  TableRow.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
        paramsInput.setMargins(8, 8, 8,8);
        tableRow.addView(editTextInput, paramsInput);
        // add tableRow to tableLayout
        inputFields.addView(tableRow);
    }

    public void removeInputField (View view){
        TableLayout inputFields = findViewById(R.id.inputFields);
        TableRow tableRow = findViewById(inputCounter-1);
        inputFields.removeView(tableRow);
        inputCounter--;
    }
}
