package com.example.mapme.presenter;

import android.location.Location;

import com.example.mapme.model.AppService;
import com.example.mapme.view.EditInformationActivity;
import com.google.firebase.database.DataSnapshot;

import java.util.HashMap;

/**
 * Presenter for EditInformationActivity.
 */
public class EditInformationPresenter implements AppService.AppServiceListener {

    private EditInformationActivity activity;

    /**
     * Constructor.
     *
     * @param editInformationActivity
     */
    public EditInformationPresenter(final EditInformationActivity editInformationActivity) {
        this.activity = editInformationActivity;
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
     * Fills properties when database changes.
     *
     * @param dataSnapshot
     */
    @Override
    public void dataChanged(DataSnapshot dataSnapshot) {
        if (dataSnapshot != null || dataSnapshot.getChildrenCount() == 2) {
            activity.fillProperties(dataSnapshot);
        }
    }

    /**
     * Calls AppService to edit properties of object.
     *
     * @param id
     * @param hashmap
     */
    public void editObjectProperties(String id, HashMap<String, String> hashmap) {
        activity.appService.editObjectProperties(id, hashmap);
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
     * Calls AppService to get the current data and fills properties in the view.
     */
    public void fillProperties() {
        DataSnapshot dataSnapshot = activity.appService.getCurrentDataSnapshot();
        if (dataSnapshot != null || dataSnapshot.getChildrenCount() == 2) {
            activity.fillProperties(dataSnapshot);
        }
    }
}
