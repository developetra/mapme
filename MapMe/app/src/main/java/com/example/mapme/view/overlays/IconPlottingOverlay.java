package com.example.mapme.view.overlays;

import android.graphics.drawable.Drawable;
import android.view.MotionEvent;

import com.example.mapme.view.AddObjectActivity;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;

/**
 * Helper class for AddMarkerActivity.
 */
public class IconPlottingOverlay extends Overlay {
    Drawable markerIcon;
    AddObjectActivity currentActivity;

    /**
     * Constructor.
     *
     * @param activity
     * @param markerIcon
     */
    public IconPlottingOverlay(AddObjectActivity activity, Drawable markerIcon) {
        this.markerIcon = markerIcon;
        currentActivity = activity;
    }

    /**
     * Add overlay with marker when pressing on map.
     *
     * @param motionEvent
     * @param mapView
     * @return boolean
     */
    @Override
    public boolean onLongPress(final MotionEvent motionEvent, final MapView mapView) {
        if (markerIcon != null) {
            GeoPoint geoPoint = (GeoPoint) mapView.getProjection().fromPixels((int) motionEvent.getX(), (int) motionEvent.getY(), null);
            if (geoPoint.getLongitude() < -180)
                geoPoint.setLongitude(geoPoint.getLongitude() + 360);
            if (geoPoint.getLongitude() > 180)
                geoPoint.setLongitude(geoPoint.getLongitude() - 360);
            if (geoPoint.getLatitude() > MapView.getTileSystem().getMaxLatitude())
                geoPoint.setLatitude(MapView.getTileSystem().getMaxLatitude());
            if (geoPoint.getLatitude() < MapView.getTileSystem().getMinLatitude())
                geoPoint.setLatitude(MapView.getTileSystem().getMinLatitude());
            Marker marker = new Marker(mapView);
            marker.setPosition(geoPoint);
            marker.setIcon(markerIcon);
            marker.setImage(markerIcon);
            marker.setTitle("Marker");
            mapView.getOverlayManager().add(marker);
            mapView.invalidate();
            marker.setId(currentActivity.saveToDatabase(marker));
            currentActivity.showInfoAddReferenceOrEditObject(mapView, marker.getId(), marker);
            return true;
        }
        return false;
    }
}
