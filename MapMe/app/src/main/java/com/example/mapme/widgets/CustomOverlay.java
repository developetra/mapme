package com.example.mapme.widgets;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.MotionEvent;

import com.example.mapme.activities.EditInformationActivity;

import org.osmdroid.bonuspack.kml.KmlDocument;
import org.osmdroid.bonuspack.kml.KmlFeature;
import org.osmdroid.bonuspack.kml.Style;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.FolderOverlay;
import org.osmdroid.views.overlay.Overlay;

public class CustomOverlay extends FolderOverlay {



    @Override
    public boolean onLongPress(final MotionEvent e, final MapView mapView) {
        // TODO:
        Activity activity = (Activity) mapView.getContext();
        Intent intent = new Intent(activity, EditInformationActivity.class);
        activity.startActivity(intent);
        Log.d("Overlay", "onLongPress "+activity);
        return true;
    }
}
