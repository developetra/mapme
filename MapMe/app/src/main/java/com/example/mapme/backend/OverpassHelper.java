package com.example.mapme.backend;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.SphericalUtil;

import hu.supercluster.overpasser.adapter.OverpassQueryResult;
import hu.supercluster.overpasser.adapter.OverpassServiceProvider;
import hu.supercluster.overpasser.library.query.OverpassQuery;

import static hu.supercluster.overpasser.library.output.OutputFormat.JSON;

/**
 * Helper class for overpass queries.
 */
public class OverpassHelper {

    /**
     * Creates new overpass query.
     *
     * @param center
     * @return
     */
    public OverpassQueryResult search(LatLng center) {
        LatLngBounds bounds = toBounds(center, 50);
        OverpassQuery query = new OverpassQuery()
                .format(JSON)
                .timeout(30)
                .filterQuery()
                .node()
                .tag("name")
                .boundingBox(
                        bounds.southwest.latitude,
                        bounds.southwest.longitude,
                        bounds.northeast.latitude,
                        bounds.northeast.longitude
                )
                .end()
                .output(500);

        OverpassQueryResult result = interpret(query.build());
        int numberOfElements = result.elements.size();
        Log.d("info", "overpass result number of elements: " + numberOfElements);
        for (int i = 0; i < numberOfElements; i++) {
            OverpassQueryResult.Element e = result.elements.get(i);
            String type = e.type;
            String name = e.tags.name;
            double lat = e.lat;
            double lon = e.lon;
            Log.d("info", "overpass result element: " + type + name + lat + lon);
        }
        Log.d("info", "overpass result: " + String.valueOf(result));
        return result;
    }

    /**
     * Interprets overpass query.
     *
     * @param query
     * @return
     */
    private OverpassQueryResult interpret(String query) {
        // Fix Overpasser bug
        query = query.replace("\"", "");
        query = query.substring(0, query.length() - 1) + ";"; // TOOD: Important + ";"
        Log.d("query","Query:" + query);
        try {
            return OverpassServiceProvider.get().interpreter(query).execute().body();
        } catch (Exception e) {
            Log.d("info", "Could not interpret OverpassQuery.");
            return new OverpassQueryResult();
        }
    }

    /**
     * Creates LatLngBounds from given LatLng with a given radius.
     * @param center
     * @param radiusInMeters
     * @return
     */
    public LatLngBounds toBounds(LatLng center, double radiusInMeters) {
        double distanceFromCenterToCorner = radiusInMeters * Math.sqrt(2.0);
        LatLng southwestCorner = SphericalUtil.computeOffset(center, distanceFromCenterToCorner, 225.0);
        LatLng northeastCorner = SphericalUtil.computeOffset(center, distanceFromCenterToCorner, 45.0);
        return new LatLngBounds(southwestCorner, northeastCorner);
    }
}