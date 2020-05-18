package com.example.mapme.model;

import com.google.firebase.database.DataSnapshot;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Collections;

import static org.junit.Assert.*;

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
        Mockito.when(entry.child("geometry").getValue()).thenReturn("test");
        Mockito.when(dataSnapshot.getChildren()).thenReturn(Collections.singleton(entry));
        geoJsonHelper.convertDataToGeoJson(dataSnapshot);
    }

    @Test
    public void exportDataAsGeoJson() {
    }
}