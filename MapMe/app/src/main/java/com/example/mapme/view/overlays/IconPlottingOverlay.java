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
     * @param activity
     * @param m
     */
    public IconPlottingOverlay(AddObjectActivity activity,Drawable m) {
        super();
        markerIcon = m;
        currentActivity = activity;

    }

    /**
     * Add overlay with marker to map.
     * @param e
     * @param mapView
     * @return
     */
    @Override
    public boolean onLongPress(final MotionEvent e, final MapView mapView) {
        if (markerIcon != null) {
            GeoPoint pt = (GeoPoint) mapView.getProjection().fromPixels((int) e.getX(), (int) e.getY(), null);
            /*
             * <b>Note</b></b: when plotting a point off the mMapView, the conversion from
             * screen coordinates to mMapView coordinates will return values that are invalid from a latitude,longitude
             * perspective. Sometimes this is a wanted behavior and sometimes it isn't. We are leaving it up to you,
             * the developer using osmdroid to decide on what is right for your application. See
             * <a href="https://github.com/osmdroid/osmdroid/pull/722">https://github.com/osmdroid/osmdroid/pull/722</a>
             * for more information and the discussion associated with this.
             */

            //just in case the point is off the mMapView, let's fix the coordinates
            if (pt.getLongitude() < -180)
                pt.setLongitude(pt.getLongitude() + 360);
            if (pt.getLongitude() > 180)
                pt.setLongitude(pt.getLongitude() - 360);
            //latitude is a bit harder. see https://en.wikipedia.org/wiki/Mercator_projection
            if (pt.getLatitude() > mapView.getTileSystem().getMaxLatitude())
                pt.setLatitude(mapView.getTileSystem().getMaxLatitude());
            if (pt.getLatitude() < mapView.getTileSystem().getMinLatitude())
                pt.setLatitude(mapView.getTileSystem().getMinLatitude());

            Marker marker = new Marker(mapView);
            marker.setPosition(pt);
            marker.setIcon(markerIcon);
            marker.setImage(markerIcon);
            marker.setTitle("Marker");
            marker.setSnippet("To add information and save to database press long on marker.");
            marker.setSubDescription(pt.getLatitude() + "," + pt.getLongitude());

            mapView.getOverlayManager().add(marker);
            mapView.invalidate();
            marker.setId(currentActivity.saveToDatabase(marker));
            currentActivity.showInfoAddReferenceOrEditObject(mapView, marker.getId(), marker);
            return true;
        }
        return false;
    }
}