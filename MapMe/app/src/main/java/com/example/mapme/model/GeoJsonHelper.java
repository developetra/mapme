package com.example.mapme.model;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Helper class for converting data to GeoJson.
 */
public class GeoJsonHelper {

    /**
     * Inserts the property information of given objects into their geoJson-String.
     *
     * @param objects
     * @return geoJsonHashmap
     */
    public static HashMap<String, String> insertPropertiesToGeoJson(HashMap<String, GeoObject> objects) {
        HashMap<String, String> geoJsonHashmap = new HashMap<>();
        for (String objectKey : objects.keySet()) {
            GeoObject object = objects.get(objectKey);
            HashMap<String, String> properties = object.getProperties();
            try {
                JSONObject geojson = new JSONObject(object.getGeometry());
                JSONObject feature = (JSONObject) geojson.getJSONArray("features").get(0);
                JSONObject featureProperies = feature.getJSONObject("properties");
                for (String propertyKey : properties.keySet()) {
                    String key = propertyKey;
                    String value = properties.get(propertyKey);
                    featureProperies.put(key, value);
                }
                geoJsonHashmap.put(objectKey, geojson.toString());
            } catch (JSONException e) {
                Log.w("info", "Data entry could not be converted to geoJson.");
            }
        }
        return geoJsonHashmap;
    }

    /**
     * Converts given HashMap of objects into GeoJson-String.
     *
     * @param context
     * @param objects
     * @return geoJsonString
     */
    protected static String convertObjectsToGeoJsonString(Context context, HashMap<String, GeoObject> objects) {
        JSONObject combined = new JSONObject();
        HashMap<String, String> geoJsonHashmap = insertPropertiesToGeoJson(objects);
        int counter = 1;
        if (!objects.isEmpty()) {
            for (String key : objects.keySet()) {
                try {
                    combined.put(String.valueOf(counter++), objects.get(key));
                } catch (JSONException e) {
                    Log.w("info", "Data entries could not be combined to geoJson.");
                }
            }
        }
        return combined.toString();
    }
}
