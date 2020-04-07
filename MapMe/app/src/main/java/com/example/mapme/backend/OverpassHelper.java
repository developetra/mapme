package com.example.mapme.backend;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

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
     * @param bounds
     * @return
     */
    public OverpassQueryResult search(LatLngBounds bounds) {
        OverpassQuery query = new OverpassQuery()
                .format(JSON)
                .timeout(30)
                .filterQuery()
                    .node()
                    .boundingBox(
                            bounds.southwest.latitude,
                            bounds.southwest.longitude,
                            bounds.northeast.latitude,
                            bounds.northeast.longitude
                )
                .end()
                .output(100);

        OverpassQueryResult result = interpret(query.build());
        int numberOfElements = result.elements.size();
        Log.d("info", "overpass result number of elements: " + numberOfElements);
        for (int i = 0; i< numberOfElements; i++){
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
        try {
            return OverpassServiceProvider.get().interpreter(query).execute().body();
        } catch (Exception e) {
            e.printStackTrace();
            return new OverpassQueryResult();
        }
    }
}

