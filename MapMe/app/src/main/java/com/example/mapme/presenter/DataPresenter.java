package com.example.mapme.presenter;

import android.location.Location;

import com.example.mapme.model.AppService;
import com.example.mapme.view.DataActivity;
import com.google.firebase.database.DataSnapshot;

import java.util.HashMap;

/**
 * Presenter for DataActivity.
 */
public class DataPresenter implements AppService.AppServiceListener {

    private DataActivity activity;
    private DataSnapshot currentDataSnapshot;

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
     * @param dataSnapshot
     * @param objects
     */
    @Override
    public void dataChanged(DataSnapshot dataSnapshot, HashMap<String, String> objects) {
        currentDataSnapshot = dataSnapshot;
        activity.displayData(currentDataSnapshot);
    }

    /**
     * Gets data and updates view.
     */
    public void getData() {
        currentDataSnapshot = this.activity.appService.getCurrentDataSnapshot();
        activity.displayData(currentDataSnapshot);
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
     * Calls AppService to save file to firebase storage.
     */
    public void saveToCloud() {
        activity.appService.uploadFile();
        activity.showInfoUploadSuccessful();
    }

}
