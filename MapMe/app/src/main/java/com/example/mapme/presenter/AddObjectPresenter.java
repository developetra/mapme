package com.example.mapme.presenter;

import android.location.Location;
import android.util.Log;

import com.example.mapme.model.AppService;
import com.example.mapme.model.OverpassHelper;
import com.example.mapme.view.AddObjectActivity;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;

import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.OverlayWithIW;

import java.util.HashMap;

import hu.supercluster.overpasser.adapter.OverpassQueryResult;

/**
 * Presenter for AddObjectActivity.
 */
public class AddObjectPresenter implements AppService.AppServiceListener {

    private AddObjectActivity activity;
    private OverpassHelper overpassHelper = new OverpassHelper();
    public GeoPoint userGeoPoint;
    private HashMap<String, String> objects = new HashMap<>();

    /**
     * Constructor.
     *
     * @param activity
     */
    public AddObjectPresenter(final AddObjectActivity activity) {
        this.activity = activity;
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
     * Calls AppService to save geometry to database.
     *
     * @param geometry
     * @return id
     */
    public String saveToDatabase(OverlayWithIW geometry) {
        return activity.appService.saveToDatabase(geometry);
    }

    /**
     * Computes Overpass result.
     *
     * @param geometry
     * @param objectId
     */
    public void computeOverpassResult(OverlayWithIW geometry, String objectId) {
        OverpassQueryResult result = getOverpassResult(geometry);
        int numberOfElements = result.elements.size();
        Log.i("info", "Overpass result number of elements: " + numberOfElements);
        if (numberOfElements == 0) {
            activity.showInfoEmptyOverpassResult(objectId);
        } else {
            activity.addLayerWithOverpassResult(result, numberOfElements, objectId);
        }
    }

    /**
     * Get Overpass result for marker or other geoObjects.
     *
     * @param geometry
     * @return OverpassQueryResult
     */
    public OverpassQueryResult getOverpassResult(OverlayWithIW geometry) {
        OverpassQueryResult result = new OverpassQueryResult();
        if (geometry.getClass().equals(Marker.class)) {
            Marker marker = (Marker) geometry;
            result = overpassHelper.search(new LatLng(marker.getPosition().getLatitude(), marker.getPosition().getLongitude()));
            return result;
        } else {
            BoundingBox bounds = geometry.getBounds();
            result = overpassHelper.search(new LatLng(bounds.getCenterLatitude(), bounds.getCenterLongitude()));
        }
        Log.i("info", "OverpassQueryResult: " + result.toString());
        return result;
    }

    /**
     * Calls AppService to add properties to object.
     *
     * @param id
     * @param hashmap
     */
    public void addObjectProperties(String id, HashMap<String, String> hashmap) {
        activity.appService.addObjectProperties(id, hashmap);
    }

}
