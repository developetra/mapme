package com.example.mapme.presenter;

import android.location.Location;
import android.util.Log;

import com.example.mapme.model.AppService;
import com.example.mapme.model.OverpassHelper;
import com.example.mapme.view.AddObjectActivity;
import com.google.android.gms.maps.model.LatLng;

import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.OverlayWithIW;

import java.util.HashMap;

import hu.supercluster.overpasser.adapter.OverpassQueryResult;

public class AddObjectPresenter implements AppService.AppServiceListener{

    private AddObjectActivity activity;
    public GeoPoint userGeoPoint;
    private HashMap<String, String> objects = new HashMap<>();
    private OverpassHelper overpassHelper = new OverpassHelper();

    public AddObjectPresenter(final AddObjectActivity activity) {
        this.activity = activity;
    }

    @Override
    public void updateUserPosition(final Location location) {
        userGeoPoint.setLatitude(location.getLatitude());
        userGeoPoint.setLongitude(location.getLongitude());
        this.activity.updateUserPosition(userGeoPoint);
    }

    @Override
    public void dataChanged() {
        objects = activity.appService.getObjects();
        this.activity.addAdditionalLayer(objects);
    }

    public String saveToDatabase(OverlayWithIW geometry) {
        return activity.appService.saveToDatabase(geometry);
    }

    public void computeOverpassResult(OverlayWithIW geometry, String objectId) {
        OverpassQueryResult result = getOverpassResult(geometry);
        int numberOfElements = result.elements.size();
        Log.i("info", "Overpass result number of elements: " + numberOfElements);
        if (numberOfElements == 0){
            activity.showInfoEmptyOverpassResult(objectId);

        } else {
            activity.addLayerWithOverpassResult(result, numberOfElements, objectId);
        }
    }

    public OverpassQueryResult getOverpassResult(OverlayWithIW geometry) {
        OverpassQueryResult result = new OverpassQueryResult();
        if (geometry.getClass().equals(Marker.class)) {
            Marker marker = (Marker) geometry;
            result = overpassHelper.search(new LatLng(marker.getPosition().getLatitude(), marker.getPosition().getLongitude()));
            return result;
        } else{
            BoundingBox bounds = geometry.getBounds();
            result = overpassHelper.search(new LatLng(bounds.getCenterLatitude(), bounds.getCenterLongitude()));
        }
        Log.i("info", "OverpassQueryResult: " + result.toString());
        return result;
    }

    public void addObjectProperties (String id, HashMap<String, String> hashmap){
        activity.appService.addObjectProperties(id, hashmap);
    }
}
