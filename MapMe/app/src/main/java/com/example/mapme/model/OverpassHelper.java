package com.example.mapme.model;

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
     * Creates new overpass query to search for nodes with name within 50m radius.
     *
     * @param center
     * @return OverpassQueryResult
     */
    public OverpassQueryResult searchNodes(LatLng center) {
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
        Log.i("info", "Overpass result - number of nodes: " + numberOfElements);
        for (int i = 0; i < numberOfElements; i++) {
            OverpassQueryResult.Element e = result.elements.get(i);
            String type = e.type;
            String name = e.tags.name;
            double lat = e.lat;
            double lon = e.lon;
            Log.i("info", "Overpass result - node: " + type + name + lat + lon);
        }
        return result;
    }


    /**
     * Creates new overpass query to search for ways with name within 50m radius.
     *
     * @param center
     * @return OverpassQueryResult
     */
    public OverpassQueryResult searchWays(LatLng center) {
        LatLngBounds bounds = toBounds(center, 50);
        OverpassQuery query = new OverpassQuery()
                .format(JSON)
                .timeout(30)
                .filterQuery()
                .way()
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
        Log.i("info", "Overpass result - number of ways: " + numberOfElements);
        for (int i = 0; i < numberOfElements; i++) {
            OverpassQueryResult.Element e = result.elements.get(i);
            String type = e.type;
            String name = e.tags.name;
            double lat = e.lat;
            double lon = e.lon;
            Log.i("info", "Overpass result - way: " + type + name + lat + lon);
        }
        return result;
    }


    /**
     * Creates new overpass query to search for nodes with name within 50m radius.
     *
     * @param center
     * @return OverpassQueryResult
     */
    public OverpassQueryResult searchRelations(LatLng center) {
        LatLngBounds bounds = toBounds(center, 50);
        OverpassQuery query = new OverpassQuery()
                .format(JSON)
                .timeout(30)
                .filterQuery()
                .rel()
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
        Log.i("info", "Overpass result - number of relations: " + numberOfElements);
        for (int i = 0; i < numberOfElements; i++) {
            OverpassQueryResult.Element e = result.elements.get(i);
            String type = e.type;
            String name = e.tags.name;
            double lat = e.lat;
            double lon = e.lon;
            Log.i("info", "Overpass result - relation: " + type + name + lat + lon);
        }
        return result;
    }

    /**
     * Interprets overpass query.
     *
     * @param query
     * @return
     */
    private OverpassQueryResult interpret(String query) {
        // fix Overpasser bug
        query = query.replace("\"", "");
        query = query.substring(0, query.length() - 1) + ";";
        Log.i("info", "Overpass query: " + query);
        try {
            return OverpassServiceProvider.get().interpreter(query).execute().body();
        } catch (Exception e) {
            Log.w("info", "Could not interpret OverpassQuery.");
            return new OverpassQueryResult();
        }
    }

    /**
     * Creates LatLngBounds from given LatLng with a given radius.
     *
     * @param center
     * @param radiusInMeters
     * @return
     */
    private LatLngBounds toBounds(LatLng center, double radiusInMeters) {
        double distanceFromCenterToCorner = radiusInMeters * Math.sqrt(2.0);
        LatLng southwestCorner = SphericalUtil.computeOffset(center, distanceFromCenterToCorner, 225.0);
        LatLng northeastCorner = SphericalUtil.computeOffset(center, distanceFromCenterToCorner, 45.0);
        return new LatLngBounds(southwestCorner, northeastCorner);
    }

}