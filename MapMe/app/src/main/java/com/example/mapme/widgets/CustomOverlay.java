package com.example.mapme.widgets;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.MotionEvent;

import com.example.mapme.view.EditInformationActivity;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.FolderOverlay;

public class CustomOverlay extends FolderOverlay {

    private final String currentGeoObjectId;

    public CustomOverlay(String id){
        this.currentGeoObjectId = id;
    }


    @Override
    public boolean onLongPress(final MotionEvent e, final MapView mapView) {
        // TODO:
        if(hitTest(e, mapView)) {

            AlertDialog.Builder infoDialog = new AlertDialog.Builder(mapView.getContext());
            infoDialog.setTitle("GeoObject (Id: " + currentGeoObjectId + ")");
            infoDialog.setMessage("Do you want to edit the GeoObject?");
            infoDialog.setNegativeButton("Cancel",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            infoDialog.setNeutralButton("Edit",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Activity activity = (Activity) mapView.getContext();
                            Intent intent = new Intent(activity, EditInformationActivity.class);
                            intent.putExtra("name", "Edit Object");
                            intent.putExtra("id", currentGeoObjectId);
                            activity.startActivity(intent);
                            Log.d("Overlay", "onLongPress "+activity);
                        }
                    });
            infoDialog.show();
            return true;
        }
        return false;
    }

    protected boolean hitTest(final MotionEvent e, final MapView mapView){
        GeoPoint pt = (GeoPoint) mapView.getProjection().fromPixels((int) e.getX(), (int) e.getY(), null);
        return this.getBounds().contains(pt);
    }
}
