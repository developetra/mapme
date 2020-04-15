package com.example.mapme.backend;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import hu.supercluster.overpasser.adapter.OverpassQueryResult;

import static org.junit.Assert.*;

public class OverpassHelperTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void search() {
        LatLngBounds bounds = new LatLngBounds(new LatLng(49.895987, 10.880654), new LatLng(49.899328, 10.885956));
        OverpassHelper helper = new OverpassHelper();
        OverpassQueryResult result = helper.search(bounds);
        Assert.assertNotNull(result);
    }
}