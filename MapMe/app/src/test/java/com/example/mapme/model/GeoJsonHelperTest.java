package com.example.mapme.model;

import com.google.firebase.database.DataSnapshot;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Collections;

public class GeoJsonHelperTest {

    GeoJsonHelper geoJsonHelper;

    @Before
    public void setUp() throws Exception {
        geoJsonHelper = new GeoJsonHelper();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void convertDataToGeoJson() {
        DataSnapshot dataSnapshot = Mockito.mock(DataSnapshot.class);
        DataSnapshot entry = Mockito.mock(DataSnapshot.class);
        DataSnapshot geometry = Mockito.mock(DataSnapshot.class);
        Mockito.when(geometry.getValue()).thenReturn("{\"crs\":{\"type\":\"name\",\"properties\":{\"name\":\"urn:ogc:def:crs:OGC:1.3:CRS84\"}},\"features\":[{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[10.886972399157315,49.89197146019778]},\"properties\":{\"name\":\"Marker\"}}],\"type\":\"FeatureCollection\"}");
        Mockito.when(entry.child("geometry")).thenReturn(geometry);
        Mockito.when(dataSnapshot.getChildren()).thenReturn(Collections.singleton(entry));
        geoJsonHelper.convertDataToGeoJson(dataSnapshot);
    }

    @Test
    public void exportDataAsGeoJson() {
    }
}