package com.example.mapme.backend;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.OverlayWithIW;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.Polyline;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class AppService extends Service {

    // ===== Service
    private static final long MINIMUM_TIME_BETWEEN_UPDATE = 1000;
    private static final long MINIMUM_DISTANCING_FOR_UPDATE = 1;
    private final IBinder binder = new LocalBinder();
    private final List<AppServiceListener> listeners = new ArrayList<AppServiceListener>();

    // ===== Location
    protected LocationManager locationManager;
    private Location userPosition = null;

    // ===== Firebase Storage
    private StorageReference storageRef;
    private StorageReference fileRef;
    private UploadTask uploadTask;

    // ===== Firebase Database
    private long counter = 0;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference counterRef = database.getReference("counter");
    private DatabaseReference objectRef = database.getReference("objects");
    private ArrayList<String> objects = new ArrayList<String>();

    private GeoJsonHelper geoJsonHelper = new GeoJsonHelper();

    // ===== Getter & Setter

    public Location getUserPosition() {
        return userPosition;
    }

    public void setUserPosition(Location userPosition) {
        this.userPosition = userPosition;
    }

    public ArrayList<String> getObjects() {
        getDataFromDatabase();
        return this.objects;
    }

    @Override
    public void onCreate() {
        initLocationManager();
        updateInRealtime();
        Log.d("info", "AppService started");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        initLocationManager();
        Log.d("info", "AppService started");
        return Service.START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    /**
     * Local Binder.
     */
    public class LocalBinder extends Binder {
        public AppService getService() {
            return AppService.this;
        }
    }

    /**
     * App Service Listener Methods.
     *
     * @param listener
     */
    public void registerListener(AppServiceListener listener) {
        listeners.add(listener);
    }

    public void unregisterListener(AppServiceListener listener) {
        listeners.remove(listener);
    }

    public interface AppServiceListener {
        void updateUserPosition(Location location);
    }

    /**
     * Initialises LocationManager.
     *
     */
    private void initLocationManager() {
        Log.d("info", "LocationManager initialized");
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        LocationListener locListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.d("info", "Location changed");
                userPosition = location;
                for (AppServiceListener listener : listeners) {
                    listener.updateUserPosition(location);
                }
            }
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }
            @Override
            public void onProviderEnabled(String provider) {
            }
            @Override
            public void onProviderDisabled(String provider) {
            }
        };
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("info", "Permission not given!");
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MINIMUM_TIME_BETWEEN_UPDATE, MINIMUM_DISTANCING_FOR_UPDATE, locListener);
    }

    public void uploadFile(){
        try {
            InputStream is = getAssets().open("database.geojson");
            StorageReference fileRef = storageRef.child("database.geojson");

            uploadTask = fileRef.putStream(is);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle unsuccessful uploads
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                    // ...
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void downloadFile(){
        File localFile = null;
        try {
            localFile = File.createTempFile("test", "geojson");
        } catch (IOException e) {
            e.printStackTrace();
        }
        final File finalLocalFile = localFile;
        fileRef.getFile(localFile)
                .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        Log.d("info", "Successfully downloaded file: " + finalLocalFile.getAbsolutePath());
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle failed download
                // ...
            }
        });
    }

    public void resetDatabase(){
        // delete old database entries
        counter = 0;
        counterRef.child("counter").setValue(counter);
        objectRef.setValue(null);
        updateInRealtime();
    }

    public void updateInRealtime(){
        counterRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                counter = (long) dataSnapshot.child("counter").getValue();
            }
            @Override
            public void onCancelled(DatabaseError error) {
                Log.w("info", "Failed to read value.", error.toException());
            }
        });
        objectRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // update map!
            }
            @Override
            public void onCancelled(DatabaseError error) {
                Log.w("info", "Failed to read value.", error.toException());
            }
        });
    }

    public String saveToDatabase(OverlayWithIW geometry){
        String id = String.valueOf(counter +1);
        GeoObject object = new GeoObject(geometry);
        objectRef.child(id).setValue(object);
        incrementCounter();
        return id;
    }

    public void editObject(String id, HashMap<String,String> hashmap){
        objectRef.child(id).child("properties").setValue(hashmap);
    }

    public void incrementCounter(){
        counterRef.child("counter").setValue(counter+1);
    }

    public void getDataFromDatabase(){
        objectRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                objects.clear();
                for (int i = 0; i < counter; i++){
                    String s = (String) dataSnapshot.child("object").child(String.valueOf(i)).child("geometry").getValue();
                    objects.add(s);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // ...
            }
        });
    }


}
