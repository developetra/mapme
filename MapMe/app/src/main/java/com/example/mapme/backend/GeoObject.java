package com.example.mapme.backend;

import com.google.gson.JsonObject;

import org.osmdroid.bonuspack.kml.KmlDocument;
import org.osmdroid.views.overlay.OverlayWithIW;

import java.util.HashMap;

/**
 * Class GeoObject - Represents a geoObject that is defined by its geometry and properties.
 */
public class GeoObject {

    private String geometry;
    private HashMap<String, String> properties;

    /**
     * Constructor
     */

    public GeoObject() {
        // Default constructor required for calls to DataSnapshot.getValue(GeoObject.class)
    }

    public GeoObject(final OverlayWithIW geometry) {
        this.geometry = convertGeometryToGeoJson(geometry);
    }

    /**
     * Getter and Setter
     */

    public String getGeometry() {
        return this.geometry;
    }

    public void setGeometry(final OverlayWithIW geometry) {
        this.geometry = convertGeometryToGeoJson(geometry);
    }

    public HashMap<String, String> getProperties() {
        return this.properties;
    }

    public void setProperties(final HashMap<String, String> properties) {
        this.properties = properties;
    }

    /**
     * Convert given geometry to a geoJson String.
     *
     * @param overlayWithIW
     * @return
     */
    public String convertGeometryToGeoJson(OverlayWithIW overlayWithIW) {
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
