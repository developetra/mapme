package com.example.mapme.presenter;

import android.location.Location;

import com.example.mapme.model.AppService;
import com.example.mapme.view.DataActivity;
import com.google.firebase.database.DataSnapshot;

/**
 * Presenter for DataActivity.
 */
public class DataPresenter implements AppService.AppServiceListener {

    private DataActivity activity;

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
     */
    @Override
    public void dataChanged(DataSnapshot dataSnapshot) {
        if (dataSnapshot != null || dataSnapshot.getChildrenCount() == 2) {
            activity.displayData(dataSnapshot);
        }
    }

    /**
     * Gets data and updates view.
     */
    public void getData() {
        DataSnapshot dataSnapshot = this.activity.appService.getCurrentDataSnapshot();
        if (dataSnapshot != null || dataSnapshot.getChildrenCount() == 2) {
            activity.displayData(dataSnapshot);
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
     * Calls AppService to save file to firebase storage.
     */
    public void saveToCloud() {
        activity.appService.uploadFile();
        activity.showInfoUploadSuccessful();
    }

}
