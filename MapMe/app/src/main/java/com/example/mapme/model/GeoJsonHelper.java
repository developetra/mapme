package com.example.mapme.model;

import android.content.Context;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Helper class for converting data to GeoJson.
 */
public class GeoJsonHelper {

    /**
     * Converts given dataSnapshot into hashmap with geoJson objects.
     *
     * @param dataSnapshot
     * @return geoJsonHashmap
     */
    public HashMap<String, String> convertDataToGeoJson(DataSnapshot dataSnapshot) {
        HashMap<String, String> objects = new HashMap<>();
        if (dataSnapshot.getChildren() != null) {
            for (DataSnapshot entry : dataSnapshot.getChildren()) {
                String geometry = entry.child("geometry").getValue(String.class);
                try {
                    JSONObject geojson = new JSONObject(geometry);
                    JSONObject feature = (JSONObject) geojson.getJSONArray("features").get(0);
                    JSONObject featureProperies = feature.getJSONObject("properties");
                    for (DataSnapshot property : entry.child("properties").getChildren()) {
                        String key = property.getKey();
                        String value = property.getValue(String.class);
                        featureProperies.put(key, value);
                    }
                    objects.put(entry.getKey(), geojson.toString());
                } catch (JSONException e) {
                    Log.w("info", "Data entry could not be converted to geoJson.");
                }
            }
        }
        return objects;
    }

    /**
     * Converts given dataSnapshot into geoJson String.
     *
     * @param context
     * @param dataSnapshot
     * @return geoJsonString
     */
    public String convertDataToGeoJsonString(Context context, DataSnapshot dataSnapshot) {
        HashMap<String, String> objects = convertDataToGeoJson(dataSnapshot);
        JSONObject combined = new JSONObject();
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
