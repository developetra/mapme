package com.example.mapme.widgets;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;

import com.example.mapme.activities.EditInformationActivity;

import org.osmdroid.bonuspack.kml.KmlDocument;
import org.osmdroid.bonuspack.kml.KmlFeature;
import org.osmdroid.bonuspack.kml.Style;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.FolderOverlay;
import org.osmdroid.views.overlay.Overlay;

public class CustomOverlay extends FolderOverlay {

    private final String id;

    public CustomOverlay(String id){
        this.id = id;
    }


    @Override
    public boolean onLongPress(final MotionEvent e, final MapView mapView) {
        // TODO:
        if(hitTest(e, mapView)) {
            Activity activity = (Activity) mapView.getContext();
            Intent intent = new Intent(activity, EditInformationActivity.class);
            intent.putExtra("name", "Edit Object");
            intent.putExtra("id", id);
            activity.startActivity(intent);
            Log.d("Overlay", "onLongPress "+activity);
            return true;
        }
        return false;
    }

    protected boolean hitTest(final MotionEvent e, final MapView mapView){
        GeoPoint pt = (GeoPoint) mapView.getProjection().fromPixels((int) e.getX(), (int) e.getY(), null);
        return this.getBounds().contains(pt);
    }
}
