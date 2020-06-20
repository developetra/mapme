package com.example.mapme.presenter;

import android.arch.lifecycle.Lifecycle;
import android.location.Location;

import com.example.mapme.model.AppService;
import com.example.mapme.model.GeoJsonHelper;
import com.example.mapme.model.GeoObject;
import com.example.mapme.view.MapActivity;

import org.osmdroid.util.GeoPoint;

import java.util.HashMap;

/**
 * Presenter for MapActivity.
 */
public class MapPresenter implements AppService.AppServiceListener {

    private final MapActivity activity;
    public GeoPoint userGeoPoint = new GeoPoint(49.89873, 10.90067);
    private HashMap<String, GeoObject> objects = new HashMap<>();

    /**
     * Constructor.
     *
     * @param mapActivity
     */
    public MapPresenter(MapActivity mapActivity) {
        activity = mapActivity;
    }

    /**
     * Get the current UserGeoPoint.
     *
     * @return
     */
    public GeoPoint getUserGeoPoint() {
        return userGeoPoint;
    }

    /**
     * Updates user position on map.
     *
     * @param location
     */
    @Override
    public void updateUserPosition(Location location) {
        this.userGeoPoint.setLatitude(location.getLatitude());
        this.userGeoPoint.setLongitude(location.getLongitude());
        activity.updateUserPosition(this.userGeoPoint);
    }

    /**
     * Updates additional layer on map when data changes.
     */
    @Override
    public void dataChanged(final HashMap<String, GeoObject> objects) {
        this.objects = objects;
        if (objects != null) {
            HashMap<String, String> geoJsonObjects = GeoJsonHelper.insertPropertiesToGeoJson(objects);
            if (this.activity.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
                this.activity.addAdditionalLayer(geoJsonObjects);
            }
        }
    }

    /**
     * Gets data and updates additional layer on map.
     */
    public void getData() {
        this.objects = activity.appService.getObjects();
        if (objects != null) {
            HashMap<String, String> geoJsonObjects = GeoJsonHelper.insertPropertiesToGeoJson(objects);
            if (this.activity.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
                this.activity.addAdditionalLayer(geoJsonObjects);
            }
        }
    }

    /**
     * Calls AppService to reset database.
     */
    public void resetDatabase() {
        this.activity.appService.resetDatabase();
        this.activity.mapView.invalidate();
    }

    /**
     * Sets user position initially.
     */
    public void setUserPosition() {
        activity.updateUserPosition(this.userGeoPoint);
    }

}
