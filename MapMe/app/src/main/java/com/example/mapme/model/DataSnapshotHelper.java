package com.example.mapme.model;

import com.google.firebase.database.DataSnapshot;

import java.util.HashMap;

/**
 * Helper class for converting DataSnapshot to HashMap of GeoObjects.
 */
public class DataSnapshotHelper {

    /**
     * Converts given DataSnapshot to HashMap of GeoObjects.
     *
     * @param dataSnapshot
     * @return objects
     */
    protected static HashMap<String, GeoObject> convertDataSnapshotToGeoObjects(final DataSnapshot dataSnapshot) {
        final HashMap<String, GeoObject> objects = new HashMap<>();
        if (dataSnapshot != null) {
            for (final DataSnapshot entry : dataSnapshot.getChildren()) {
                final String geometry = entry.child("geometry").getValue(String.class);
                final HashMap<String, String> properties = new HashMap<>();
                for (final DataSnapshot property : entry.child("properties").getChildren()) {
                    final String key = property.getKey();
                    final String value = property.getValue(String.class);
                    properties.put(key, value);
                }
                final GeoObject geoObject = new GeoObject(geometry, properties);
                objects.put(entry.getKey(), geoObject);
            }
        }
        return objects;
    }

}
