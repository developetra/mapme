package com.example.mapme.backend;

import android.content.Context;
import android.util.Log;

import org.osmdroid.bonuspack.kml.KmlDocument;
import org.osmdroid.views.overlay.Overlay;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;

public class GeoJsonHelper {

    public String readGeoJSON(Context context){
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

    public File writeGeoJSON(Overlay overlay){
        KmlDocument kmlDocument = new KmlDocument();
        kmlDocument.mKmlRoot.addOverlay(overlay, kmlDocument);
        File localFile = kmlDocument.getDefaultPathForAndroid("test.json");
        Writer writer = new StringWriter();
        kmlDocument.saveAsGeoJSON(writer);
        String s = writer.toString();
        Log.d("info", "GeoJSON successfully saved:" + s);
        return localFile;
    }


}
