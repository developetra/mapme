package com.example.mapme.presenter;

import android.location.Location;
import android.util.Log;

import com.example.mapme.model.AppService;
import com.example.mapme.model.GeoJsonHelper;
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
    private GeoJsonHelper geoJsonHelper = new GeoJsonHelper();
    public GeoPoint userGeoPoint;
    private DataSnapshot currentDataSnapshot;
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
    public void dataChanged(DataSnapshot dataSnapshot) {
        currentDataSnapshot = dataSnapshot;
        if (currentDataSnapshot != null) {
            objects = geoJsonHelper.convertDataToGeoJson(currentDataSnapshot);
            this.activity.addAdditionalLayer(objects);
        } else {
            Log.w("info", "Database is empty.");
        }
    }

    /**
     * Gets data and updates additional layer on map.
     */
    public void getData() {
        currentDataSnapshot = this.activity.appService.getCurrentDataSnapshot();
        if (currentDataSnapshot != null) {
            objects = geoJsonHelper.convertDataToGeoJson(currentDataSnapshot);
            this.activity.addAdditionalLayer(objects);
        } else {
            Log.w("info", "Database is empty.");
        }
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
        OverpassQueryResult resultNodes = getOverpassResultNodes(geometry);
        int numberOfNodes = resultNodes.elements.size();
        Log.i("info", "Overpass result number of nodes: " + numberOfNodes);
        OverpassQueryResult resultWays = getOverpassResultWays(geometry);
        int numberOfWays = resultWays.elements.size();
        Log.i("info", "Overpass result number of ways: " + numberOfWays);
        OverpassQueryResult resultRelations = getOverpassResultRelations(geometry);
        int numberOfRelations = resultRelations.elements.size();
        Log.i("info", "Overpass result number of relations: " + numberOfRelations);
        if (numberOfNodes == 0 && numberOfWays == 0 && numberOfRelations == 0) {
            activity.showInfoEmptyOverpassResult(objectId);
        } else {
            activity.addLayerWithOverpassResult(resultNodes, numberOfNodes, resultWays, numberOfWays, resultRelations, numberOfRelations, objectId);
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
            result = overpassHelper.searchNodes(new LatLng(marker.getPosition().getLatitude(), marker.getPosition().getLongitude()));
            return result;
        } else {
            BoundingBox bounds = geometry.getBounds();
            result = overpassHelper.searchNodes(new LatLng(bounds.getCenterLatitude(), bounds.getCenterLongitude()));
        }
        Log.i("info", "OverpassQueryResult: " + result.toString());
        return result;
    }

    /**
     * Get Overpass result for ways.
     *
     * @param geometry
     * @return OverpassQueryResult
     */
    public OverpassQueryResult getOverpassResultWays(OverlayWithIW geometry) {
        OverpassQueryResult result = new OverpassQueryResult();
        if (geometry.getClass().equals(Marker.class)) {
            Marker marker = (Marker) geometry;
            result = overpassHelper.searchWays(new LatLng(marker.getPosition().getLatitude(), marker.getPosition().getLongitude()));
            return result;
        } else {
            BoundingBox bounds = geometry.getBounds();
            result = overpassHelper.searchWays(new LatLng(bounds.getCenterLatitude(), bounds.getCenterLongitude()));
        }
        Log.i("info", "OverpassQueryResult: " + result.toString());
        return result;
    }

    /**
     * Get Overpass result for relations.
     *
     * @param geometry
     * @return OverpassQueryResult
     */
    public OverpassQueryResult getOverpassResultRelations(OverlayWithIW geometry) {
        OverpassQueryResult result = new OverpassQueryResult();
        if (geometry.getClass().equals(Marker.class)) {
            Marker marker = (Marker) geometry;
            result = overpassHelper.searchRelations(new LatLng(marker.getPosition().getLatitude(), marker.getPosition().getLongitude()));
            return result;
        } else {
            BoundingBox bounds = geometry.getBounds();
            result = overpassHelper.searchRelations(new LatLng(bounds.getCenterLatitude(), bounds.getCenterLongitude()));
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
