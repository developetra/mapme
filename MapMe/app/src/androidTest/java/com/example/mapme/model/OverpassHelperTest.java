package com.example.mapme.model;

import android.support.test.runner.AndroidJUnit4;

import com.google.android.gms.maps.model.LatLng;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class OverpassHelperTest {

    OverpassHelper overpassHelper;

    @Before
    public void setUp() throws Exception {
        overpassHelper = new OverpassHelper();
    }

    @Test
    public void searchNodes() {
        LatLng latlng = new LatLng(49.89873, 10.90067);
        Assert.assertNotNull(overpassHelper.searchNodes(latlng));
    }

}