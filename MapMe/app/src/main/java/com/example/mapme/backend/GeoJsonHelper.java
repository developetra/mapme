package com.example.mapme.backend;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.osmdroid.bonuspack.kml.KmlDocument;
import org.osmdroid.views.overlay.Overlay;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Helper class for converting data to GeoJson.
 */
public class GeoJsonHelper {

    /**
     * Converts given geoJson file into a String.
     *
     * @param context
     * @return
     */
    public String readGeoJSON(Context context) {
        String geoJSONString = null;
        try {
            InputStream is = context.getAssets().open("database.geojson");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            geoJSONString = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            Log.d("info", "Could not read file.");
            ex.printStackTrace();
        }
        Log.d("info", "geoJSONString: " + geoJSONString);
        return geoJSONString;
    }

    public File writeGeoJSON(Context context, Overlay overlay) {
        return writeGeoJSON(context, overlay, new HashMap<String, String>());
    }

    /**
     * Converts given overlay and properties into a geoJson file.
     *
     * @param context
     * @param overlay
     * @param properties
     * @return
     */
    public File writeGeoJSON(Context context, Overlay overlay, HashMap<String, String> properties) {

        // Create a KML Document and add the overlay
        KmlDocument kmlDocument = new KmlDocument();
        kmlDocument.mKmlRoot.addOverlay(overlay, kmlDocument);

        // Convert KML to GeoJson
        JsonObject geojson = kmlDocument.mKmlRoot.asGeoJSON(true);

        // Add additional properties to the geojson
        JsonObject feature = (JsonObject) geojson.getAsJsonArray("features").get(0);
        JsonObject featureProperies = feature.getAsJsonObject("properties");
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            featureProperies.addProperty(entry.getKey(), entry.getValue());
        }

        // Write the Geojson to a file called "geoJsonFile.txt"
        File file = new File(context.getExternalFilesDir(null), "geoJsonFile.txt");
        try {
            FileWriter writer = new FileWriter(file);
            new Gson().toJson(geojson, writer);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d("info", "GeoJSON successfully saved");

        return file;
    }

}
