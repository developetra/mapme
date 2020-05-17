package com.example.mapme.view;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.example.mapme.R;
import com.example.mapme.model.AppService;
import com.example.mapme.presenter.MainPresenter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * MainActivity - Start screen when opening the app.
 */
public class MainActivity extends AppCompatActivity {

    private MainPresenter presenter;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 5;
    private FirebaseAuth mAuth;

    /**
     * Initializes layout, requests permissions and starts appService.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        presenter = new MainPresenter(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermission();
        startAppService();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    /**
     * Opens new MapActivity.
     *
     * @param view
     */
    public void startMap(View view) {
        startAppService();
        Intent intent = new Intent(this, MapActivity.class);
        startActivity(intent);
    }

    /**
     * Shows info dialog about app.
     *
     * @param view
     */
    public void showInfo(View view) {
        AlertDialog.Builder infoDialog = new AlertDialog.Builder(MainActivity.this);
        infoDialog.setTitle("How MapMe works:");
        infoDialog.setMessage(presenter.getInfoText());
        infoDialog.setNeutralButton("Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        infoDialog.show();
    }

    /**
     * Requests permission to access fine location.
     */
    private void requestPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }
        } else {
            // Permission has already been granted
        }
    }

    /**
     * Starts AppService.
     */
    private void startAppService() {
        Log.d("info", "Starting AppService");
        Intent serviceIntent = new Intent(getApplicationContext(), AppService.class);
        startService(serviceIntent);
    }
    
}
