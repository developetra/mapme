package com.example.mapme.backend;

import com.google.gson.JsonObject;

import org.osmdroid.bonuspack.kml.KmlDocument;
import org.osmdroid.views.overlay.OverlayWithIW;

import java.util.HashMap;

public class GeoObject {

    private static int idCounter = 0;
    private final int id;
    private String geometry;
    private HashMap<String, String> properties;

    public GeoObject(final OverlayWithIW geometry) {
        this.id = idCounter++;
        this.geometry = convertOverlayToString(geometry);
    }

    public int getId() {
        return this.id;
    }

    public String getGeometry() {
        return this.geometry;
    }

    public void setGeometry(final OverlayWithIW geometry) {
        this.geometry = convertOverlayToString(geometry);
    }

    public HashMap<String, String> getProperties() {
        return this.properties;
    }

    public void setProperties(final HashMap<String, String> properties) {
        this.properties = properties;
    }

    public String convertOverlayToString(OverlayWithIW overlayWithIW){
        // Create a KML Document and add the overlay
        KmlDocument kmlDocument = new KmlDocument();
        kmlDocument.mKmlRoot.addOverlay(overlayWithIW, kmlDocument);
        // Convert KML to GeoJson
        JsonObject geojson = kmlDocument.mKmlRoot.asGeoJSON(true);
        // Convert GeoJson to String
        String geometry = geojson.toString();
        return geometry;
    }

}
