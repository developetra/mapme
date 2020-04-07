package com.example.mapme.backend;

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
     * @param southwest
     * @param northeast
     * @return
     */
    public OverpassQueryResult search(LatLng southwest, LatLng northeast) {

        LatLngBounds bounds = new LatLngBounds(southwest, northeast);

        OverpassQuery query = new OverpassQuery()
                .format(JSON)
                .timeout(30)
                .filterQuery()
                .node()
                .amenity("parking")
                .tagNot("access", "private")
                .boundingBox(
                        bounds.southwest.latitude,
                        bounds.southwest.longitude,
                        bounds.northeast.latitude,
                        bounds.northeast.longitude
                )
                .end()
                .output(100);

        return interpret(query.build());
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

