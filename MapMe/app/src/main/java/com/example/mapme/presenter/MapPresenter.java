package com.example.mapme.presenter;

import android.location.Location;

import com.example.mapme.view.MapActivity;
import com.example.mapme.model.AppService;

import org.osmdroid.util.GeoPoint;

import java.util.HashMap;

public class MapPresenter implements AppService.AppServiceListener {

    private MapActivity activity;
    public GeoPoint userGeoPoint;
    private HashMap<String, String> objects = new HashMap<>();

    public MapPresenter(final MapActivity mapActivity) {
        this.activity = mapActivity;
    }


    public void resetDatabase() {
        activity.appService.resetDatabase();
    }

    public void setUserPosition() {
        userGeoPoint = new GeoPoint(49.89873, 10.90067);
        this.activity.updateUserPosition(userGeoPoint);
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


}
