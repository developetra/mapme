package com.example.mapme.presenter;

import android.location.Location;
import android.util.Log;

import com.example.mapme.model.AppService;
import com.example.mapme.model.GeoJsonHelper;
import com.example.mapme.model.GeoObject;
import com.example.mapme.model.OverpassHelper;
import com.example.mapme.view.AddObjectActivity;
import com.google.android.gms.maps.model.LatLng;

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

    private final AddObjectActivity activity;
    public GeoPoint userGeoPoint;

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
    public void dataChanged(HashMap<String, GeoObject> objects) {
        if (objects != null) {
            HashMap<String, String> geoJsonObjects = GeoJsonHelper.insertPropertiesToGeoJson(objects);
            this.activity.addAdditionalLayer(geoJsonObjects);
        }
    }

    /**
     * Gets data and updates additional layer on map.
     */
    public void getData() {
        HashMap<String, GeoObject> objects = this.activity.appService.getObjects();
        if (objects != null) {
            HashMap<String, String> geoJsonObjects = GeoJsonHelper.insertPropertiesToGeoJson(objects);
            this.activity.addAdditionalLayer(geoJsonObjects);
        }
    }

    /**
     * Creates GeoObject with given geometry and calls AppService to save object to database.
     *
     * @param geometry
     * @return id
     */
    public String saveToDatabase(OverlayWithIW geometry) {
        GeoObject object = new GeoObject(geometry);
        HashMap<String, String> properties = new HashMap<>();
        properties.put("type", geometry.getTitle());
        object.setProperties(properties);
        return activity.appService.saveToDatabase(object);
    }

    /**
     * Computes Overpass result.
     *
     * @param geometry
     * @param objectId
     */
    public void computeOverpassResult(OverlayWithIW geometry, String objectId) {
        OverpassQueryResult resultNodes = getOverpassResultNodes(geometry);
        int numberOfNodes = resultNodes.elements.size();
        Log.i("info", "Overpass result number of nodes: " + numberOfNodes);
        if (numberOfNodes == 0) {
            activity.showInfoEmptyOverpassResult(objectId);
        } else {
            activity.addLayerWithOverpassResult(resultNodes, numberOfNodes, objectId);
        }
    }

    /**
     * Get Overpass result for nodes.
     *
     * @param geometry
     * @return OverpassQueryResult
     */
    public OverpassQueryResult getOverpassResultNodes(OverlayWithIW geometry) {
        OverpassQueryResult result = new OverpassQueryResult();
        if (geometry.getClass().equals(Marker.class)) {
            Marker marker = (Marker) geometry;
            result = OverpassHelper.searchNodes(new LatLng(marker.getPosition().getLatitude(), marker.getPosition().getLongitude()));
            return result;
        } else {
            BoundingBox bounds = geometry.getBounds();
            result = OverpassHelper.searchNodes(new LatLng(bounds.getCenterLatitude(), bounds.getCenterLongitude()));
        }
        Log.i("info", "OverpassQueryResult: " + result);
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
