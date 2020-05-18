package com.example.mapme.presenter;

import android.location.Location;

import com.example.mapme.view.MapActivity;
import com.example.mapme.model.AppService;
import com.google.firebase.database.DataSnapshot;

import org.osmdroid.util.GeoPoint;

import java.util.HashMap;

/**
 * Presenter for MapActivity.
 */
public class MapPresenter implements AppService.AppServiceListener {

    private MapActivity activity;
    public GeoPoint userGeoPoint;
    private HashMap<String, String> objects = new HashMap<>();

    /**
     * Constructor.
     *
     * @param mapActivity
     */
    public MapPresenter(final MapActivity mapActivity) {
        this.activity = mapActivity;
    }

    /**
     * Updates user position on map.
     *
     * @param location
     */
    @Override
    public void updateUserPosition(final Location location) {
        userGeoPoint.setLatitude(location.getLatitude());
        userGeoPoint.setLongitude(location.getLongitude());
        this.activity.updateUserPosition(userGeoPoint);
    }

    /**
     * Updates additional layer on map when data changes.
     */
    @Override
    public void dataChanged(DataSnapshot dataSnapshot, HashMap<String, String> objects) {
        objects = objects;
        this.activity.addAdditionalLayer(objects);
    }

    /**
     * Gets data and updates additional layer on map.
     */
    public void getData() {
        objects = this.activity.appService.getObjects();
        this.activity.addAdditionalLayer(objects);
    }

    /**
     * Calls AppService to reset database.
     */
    public void resetDatabase() {
        activity.appService.resetDatabase();
    }

    /**
     * Sets user position initially.
     */
    public void setUserPosition() {
        userGeoPoint = new GeoPoint(49.89873, 10.90067);
        this.activity.updateUserPosition(userGeoPoint);
    }

}
