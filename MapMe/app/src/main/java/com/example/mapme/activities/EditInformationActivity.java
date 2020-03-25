package com.example.mapme.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.example.mapme.R;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polygon;

public class EditInformationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_information);

        Intent intent = getIntent();
        String name  = intent.getStringExtra("name");
        ((TextView)findViewById(R.id.textView)).setText(name);
    }

    private void save (View view){
        String property01 = ((EditText)findViewById(R.id.property01)).getText().toString();
        String property02 = ((EditText)findViewById(R.id.property02)).getText().toString();
        String property03 = ((EditText)findViewById(R.id.property03)).getText().toString();
        String input01 = ((EditText)findViewById(R.id.input01)).getText().toString();
        String input02 = ((EditText)findViewById(R.id.input02)).getText().toString();
        String input03 = ((EditText)findViewById(R.id.input03)).getText().toString();

    }
}
