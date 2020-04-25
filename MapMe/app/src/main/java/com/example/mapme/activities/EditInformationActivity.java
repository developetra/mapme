package com.example.mapme.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.autofill.AutofillValue;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.mapme.R;
import com.example.mapme.backend.AppService;
import com.google.firebase.database.DataSnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * EditInformationActivity - Activity to edit information for specific geoObject.
 */
public class EditInformationActivity extends AppCompatActivity implements AppService.AppServiceListener {

    protected AppService appService;
    protected boolean appServiceBound;
    private boolean serviceConnected = false;
    public String currentGeoObjectId = "";
    private int inputCounter = 0;
    private DataSnapshot currentDataSnapshot;

    /**
     * Initializes layout and starts appServiceConnection.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // initialize layout
        setContentView(R.layout.activity_edit_information);
        Intent intent = getIntent();
        currentGeoObjectId = intent.getStringExtra("id");
        Log.d("info", "Editing object with id: "+ currentGeoObjectId);
        String name = intent.getStringExtra("name");
        ((TextView) findViewById(R.id.textView)).setText(name + "(Id: " + currentGeoObjectId + ")");

    }

    @Override
    public void onResume() {
        super.onResume();
        Intent bindIntent = new Intent(EditInformationActivity.this, AppService.class);
        bindService(bindIntent, appServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onPause() {
        super.onPause();
        unbindService(appServiceConnection);
    }
    /**
     * AppService Connection.
     */
    public ServiceConnection appServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            AppService.LocalBinder binder = (AppService.LocalBinder) service;
            appService = binder.getService();
            appServiceBound = true;
            appService.registerListener(EditInformationActivity.this);
            serviceConnected = true;
            Log.d("info", "Service bound to EditInformationActivity");
            fillProperties();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            appServiceBound = false;
            Log.d("info", "Service unbound to EditInformationActivity");
        }
    };

    /**
     * No action when updating user position.
     *
     * @param location
     */
    @Override
    public void updateUserPosition(Location location) {
    }

    /**
     * Creates hashmap from user input and saves it to database.
     *
     * @param view
     */
    public void save(View view) {
        // create HashMap
        HashMap<String, String> properties = new HashMap<>();
        // fill HashMap with user input
        for (int i = 0; i < inputCounter; i++) {
            TableRow tableRow = findViewById(i);
            String property = ((EditText) tableRow.getChildAt(0)).getText().toString();
            String input = ((EditText) tableRow.getChildAt(1)).getText().toString();
            properties.put(property, input);
            Log.d("info", "property + input " + property + input);
        }
        // save properties to database
        appService.editObject(currentGeoObjectId, properties);
        this.finish();
    }

    /**
     * Cancel and go back to previous activity.
     * @param view
     */
    public void cancel(View view){
        this.finish();
    }

    /**
     * Deletes the GeoObject.
     * @param view
     */
    public void delete(View view){
        appService.deleteObject(currentGeoObjectId);
        this.finish();
    }

    /**
     * Adds new row with input fields to layout.
     *
     * @param view
     */
    public void addInputField(View view, String property, String input) {
        TableLayout inputFields = findViewById(R.id.inputFields);
        TableRow tableRow = new TableRow(this);
        tableRow.setId(inputCounter++);
        // property field
        EditText editTextProperty = new EditText(this);
        editTextProperty.setText(property);
        editTextProperty.setWidth(500);
        TableRow.LayoutParams paramsProperty = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
        paramsProperty.setMargins(8, 8, 8, 8);
        tableRow.addView(editTextProperty, paramsProperty);
        //input field
        EditText editTextInput = new EditText(this);
        editTextInput.setText(input);
        editTextInput.setWidth(500);
        TableRow.LayoutParams paramsInput = new TableRow.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
        paramsInput.setMargins(8, 8, 8, 8);
        tableRow.addView(editTextInput, paramsInput);
        // add tableRow to tableLayout
        inputFields.addView(tableRow);
    }

    /**
     * Removes one row of input fields from layout.
     *
     * @param view
     */
    public void removeInputField(View view) {
        TableLayout inputFields = findViewById(R.id.inputFields);
        TableRow tableRow = findViewById(inputCounter - 1);
        inputFields.removeView(tableRow);
        inputCounter--;
    }

    public void addAdditionalLayer() {}

    public void fillProperties(){
        currentDataSnapshot = appService.getCurrentDataSnapshot();

        for (DataSnapshot entry : currentDataSnapshot.getChildren()) {
            if (entry.getKey().equals(currentGeoObjectId)) {
                for (DataSnapshot property : entry.child("properties").getChildren()) {
                    String key = property.getKey().toString();
                    String value = property.getValue(String.class);
                    addInputField(findViewById(R.id.textView), key, value);
                }
            }
        }
        addInputField(findViewById(R.id.textView), "", "");

    }
}
