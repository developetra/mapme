package com.example.mapme.model;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.StrictMode;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.osmdroid.views.overlay.OverlayWithIW;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * AppService - responsible for location manager, connectivity manager, database & storage access.
 */
public class AppService extends Service {

    // ===== Service
    private static final long MINIMUM_TIME_BETWEEN_UPDATE = 1000;
    private static final long MINIMUM_DISTANCING_FOR_UPDATE = 1;
    private final IBinder binder = new LocalBinder();
    private final List<AppServiceListener> listeners = new ArrayList<AppServiceListener>();

    // ===== Location
    protected LocationManager locationManager;

    // ===== Internet Connection
    private ConnectivityManager connectivityManager;

    // ===== Firebase Database
    private long counter = 0;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference counterRef = database.getReference("counter");
    private DatabaseReference objectRef = database.getReference("objects");
    private DataSnapshot currentDataSnapshot;

    // ===== Firebase Storage
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private StorageReference fileRef;
    private UploadTask uploadTask;

    // ===== Helper
    private GeoJsonHelper geoJsonHelper = new GeoJsonHelper();

    /**
     * Get current dataSnapshot.
     *
     * @return currentDataSnapshot
     */
    public DataSnapshot getCurrentDataSnapshot() {
        return this.currentDataSnapshot;
    }

    /**
     * onCreate.
     */
    @Override
    public void onCreate() {
        super.onCreate();

        // TODO: Do not run request in main thread:
        // https://stackoverflow.com/questions/6343166/how-to-fix-android-os-networkonmainthreadexception
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    /**
     * Initializes location manager, connectivity manager, firebase storage & realtime database.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        initLocationManager();
        connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        getDataFromDatabase();
        updateInRealtime();
        storage = FirebaseStorage.getInstance();
        Log.i("info", "AppService started.");
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
     * App Service Listener & listener methods.
     */
    public interface AppServiceListener {
        void updateUserPosition(Location location);

        void dataChanged(DataSnapshot dataSnapshot);
    }

    public void registerListener(AppServiceListener listener) {
        listeners.add(listener);
    }

    public void unregisterListener(AppServiceListener listener) {
        listeners.remove(listener);
    }

    /**
     * Initializes locationManager.
     */
    private void initLocationManager() {
        Log.i("info", "LocationManager initialized.");
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        LocationListener locListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.i("info", "Location changed.");
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
            Log.i("info", "Permission to access location not given.");
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MINIMUM_TIME_BETWEEN_UPDATE, MINIMUM_DISTANCING_FOR_UPDATE, locListener);
    }

    /**
     * Checks internet connection.
     *
     * @return boolean
     */
    public boolean checkInternetConnection() {
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        return (activeNetwork != null && activeNetwork.isConnectedOrConnecting());
    }

    /**
     * Gets data from database.
     */
    public void getDataFromDatabase() {
        objectRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    currentDataSnapshot = dataSnapshot;
                    for (AppServiceListener listener : listeners) {
                        listener.dataChanged(currentDataSnapshot);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("info", "Failed to load data from database.", databaseError.toException());
            }
        });
    }

    /**
     * Initializes realtime database.
     */
    public void updateInRealtime() {
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
                if (dataSnapshot != null) {
                    currentDataSnapshot = dataSnapshot;
                    for (AppServiceListener listener : listeners) {
                        listener.dataChanged(dataSnapshot);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w("info", "Failed to update map.", error.toException());
            }
        });
    }

    /**
     * Saves given geometry to database.
     *
     * @param geometry
     * @return id
     */
    public String saveToDatabase(OverlayWithIW geometry) {
        String id = String.valueOf(counter + 1);
        GeoObject object = new GeoObject(geometry);
        objectRef.child(id).setValue(object);
        HashMap<String, String> properties = new HashMap<>();
        properties.put("type", geometry.getTitle());
        editObjectProperties(id, properties);
        incrementCounter();
        return id;
    }

    /**
     * Saves given properties to object.
     *
     * @param id
     * @param hashmap
     */
    public void editObjectProperties(String id, HashMap<String, String> hashmap) {
        try {
            objectRef.child(id).child("properties").setValue(hashmap);
        } catch (Exception e) {
            Log.e("info", "Object does not exist.");
        }
    }

    /**
     * Adds given properties to object.
     *
     * @param id
     * @param hashmap
     */
    public void addObjectProperties(String id, HashMap<String, String> hashmap) {
        try {
            for (String key : hashmap.keySet()) {
                objectRef.child(id).child("properties").child(key).setValue(hashmap.get(key));
            }
        } catch (Exception e) {
            Log.e("info", "Object does not exist.");
        }

    }

    /**
     * Deletes object from database.
     *
     * @param id
     */
    public void deleteObject(String id) {
        try {
            objectRef.child(id).removeValue();
        } catch (Exception e) {
            Log.e("info", "Object does not exist.");
        }
    }

    /**
     * Resets database.
     */
    public void resetDatabase() {
        counter = 0;
        counterRef.child("counter").setValue(counter);
        objectRef.setValue(null);
        updateInRealtime();
    }

    /**
     * Increments counter in database.
     */
    public void incrementCounter() {
        counterRef.child("counter").setValue(counter + 1);
    }

    /**
     * Uploads file to firebase storage.
     */
    public void uploadFile() {
        String file = geoJsonHelper.convertDataToGeoJsonString(this, currentDataSnapshot);
        byte[] data = file.getBytes();
        StorageReference storageRef = storage.getReference();
        StorageReference fileRef = storageRef.child("database.geojson");
        uploadTask = fileRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.i("info", "File upload not successful. :-(");
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.i("info", "File upload successful! :-)");
            }
        });
    }

}