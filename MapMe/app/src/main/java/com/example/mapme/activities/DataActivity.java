package com.example.mapme.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Typeface;
import android.location.Location;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.mapme.R;
import com.example.mapme.backend.AppService;
import com.google.firebase.database.DataSnapshot;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polyline;

public class DataActivity extends AppCompatActivity implements AppService.AppServiceListener {

    protected AppService appService;
    protected boolean appServiceBound;
    private boolean serviceConnected = false;
    private DataSnapshot currentDataSnapshot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);
    }

    @Override
    public void onResume() {
        super.onResume();
        Intent bindIntent = new Intent(DataActivity.this, AppService.class);
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
            appService.registerListener(DataActivity.this);
            serviceConnected = true;
            Log.d("info", "Service bound to DataActivity");
            displayData();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            appServiceBound = false;
            Log.d("info", "Service unbound to DataActivity");
        }
    };

    /**
     * Cancel and go back to previous activity.
     * @param view
     */
    public void cancel(View view){
        this.finish();
    }

    @Override
    public void updateUserPosition(Location location) {

    }

    @Override
    public void addAdditionalLayer() {
        displayData();
    }

    public void displayData(){
        currentDataSnapshot = appService.getCurrentDataSnapshot();
        TableLayout inputFields = findViewById(R.id.inputFields);
        inputFields.removeAllViews();
        for (final DataSnapshot entry : currentDataSnapshot.getChildren()) {
            // id and type
            TableRow tableRowObject = new TableRow(this);
            TextView textViewObject = new TextView(this);
            textViewObject.setText(entry.getKey() + "   " +entry.child("properties").child("type").getValue() + "                                                      ");
            textViewObject.setTypeface(null, Typeface.BOLD);
            tableRowObject.addView(textViewObject);
            // edit button
            ImageButton edit = new ImageButton(this);
            edit.setImageDrawable(getResources().getDrawable(R.drawable.edit));
            edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                 editObject(entry.getKey());
                }
            });
            tableRowObject.addView(edit);
            // delete button
            ImageButton delete = new ImageButton(this);
            delete.setImageDrawable(getResources().getDrawable(R.drawable.delete));
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    appService.deleteObject(entry.getKey());
                }
            });
            tableRowObject.addView(delete);
            inputFields.addView(tableRowObject);
            // properties
            for (DataSnapshot property : entry.child("properties").getChildren()) {
                    TableRow tableRowProperties = new TableRow(this);
                    String key = property.getKey().toString();
                    String value = property.getValue(String.class);
                    TextView textViewProperties = new TextView(this);
                    textViewProperties.setText("     " + key + " - " + value);
                    tableRowProperties.addView(textViewProperties);
                    inputFields.addView(tableRowProperties);
                }
            // empty row
            TableRow emptyRow1 = new TableRow(this);
            TextView emptytextView1 = new TextView(this);
            emptytextView1.setText(" ");
            emptyRow1.addView(emptytextView1);
            inputFields.addView(emptyRow1);
            TableRow emptyRow2 = new TableRow(this);
            TextView emptytextView2 = new TextView(this);
            emptytextView2.setText(" ");
            emptyRow2.addView(emptytextView2);
            inputFields.addView(emptyRow2);

        }
    }

    public void editObject(String id){
        Intent intent = new Intent(this, EditInformationActivity.class);
        intent.putExtra("name", "Edit Object");
        intent.putExtra("id", id);
        startActivity(intent);
    }
}
