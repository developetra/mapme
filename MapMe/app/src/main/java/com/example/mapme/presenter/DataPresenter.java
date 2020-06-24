package com.example.mapme.presenter;

import android.location.Location;

import com.example.mapme.model.AppService;
import com.example.mapme.model.GeoObject;
import com.example.mapme.view.DataActivity;

import java.util.HashMap;

/**
 * Presenter for DataActivity.
 */
public class DataPresenter implements AppService.AppServiceListener {

    private final DataActivity activity;
    private HashMap<String, GeoObject> objects = new HashMap<>();

    /**
     * Constructor.
     *
     * @param dataActivity
     */
    public DataPresenter(final DataActivity dataActivity) {
        this.activity = dataActivity;
    }

    /**
     * Does nothing when user position changes.
     *
     * @param location
     */
    @Override
    public void updateUserPosition(Location location) {
    }

    /**
     * Updates data on display when database changes.
     *
     * @param objects
     */
    @Override
    public void dataChanged(HashMap<String, GeoObject> objects) {
        if (objects != null) {
            activity.displayData(objects);
        }
    }

    /**
     * Gets data and updates view.
     */
    public void getData() {
        objects = this.activity.appService.getObjects();
        if (objects != null) {
            activity.displayData(objects);
        }
    }

    /**
     * Calls AppService to delete object from database.
     *
     * @param id
     */
    public void deleteObject(String id) {
        activity.appService.deleteObject(id);
    }

    /**
     * Calls AppService to reset database.
     */
    public void resetDatabase() {
        this.activity.appService.resetDatabase();
    }

    /**
     * Calls AppService to save file to firebase storage.
     */
    public void saveToCloud() {
        activity.appService.uploadFile();
        activity.showInfoUploadSuccessful();
    }

}
