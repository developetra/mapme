package com.example.mapme.model;

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
     * Default Constructor.
     */
    public GeoObject() {
    }

    /**
     * Constructor.
     *
     * @param geometry
     * @param properties
     */
    public GeoObject(String geometry, HashMap<String, String> properties) {
        this.geometry = geometry;
        this.properties = properties;
    }

    /**
     * Constructor.
     *
     * @param geometry
     */
    public GeoObject(final OverlayWithIW geometry) {
        this.geometry = convertGeometryToGeoJson(geometry);
    }

    /**
     * Get Geometry.
     *
     * @return geometry
     */
    public String getGeometry() {
        return this.geometry;
    }

    /**
     * Set Geometry.
     *
     * @param geometry
     */
    public void setGeometry(final OverlayWithIW geometry) {
        this.geometry = convertGeometryToGeoJson(geometry);
    }

    /**
     * Get properties.
     *
     * @return properties
     */
    public HashMap<String, String> getProperties() {
        return this.properties;
    }

    /**
     * Set properties.
     *
     * @param properties
     */
    public void setProperties(final HashMap<String, String> properties) {
        this.properties = properties;
    }

    /**
     * Convert given geometry to a geoJson String.
     *
     * @param overlayWithIW
     * @return geometry as geoJsonString
     */
    private String convertGeometryToGeoJson(OverlayWithIW overlayWithIW) {
        KmlDocument kmlDocument = new KmlDocument();
        kmlDocument.mKmlRoot.addOverlay(overlayWithIW, kmlDocument);
        JsonObject geojson = kmlDocument.mKmlRoot.asGeoJSON(true);
        String geometry = geojson.toString();
        return geometry;
    }

}