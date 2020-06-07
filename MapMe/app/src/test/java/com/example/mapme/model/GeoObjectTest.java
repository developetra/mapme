package com.example.mapme.model;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.osmdroid.views.overlay.OverlayWithIW;

import java.util.HashMap;

public class GeoObjectTest {

    GeoObject geoObject;

    @Before
    public void setUp() throws Exception {
        geoObject = new GeoObject();
    }

    @Test
    public void testConstructor() {
        HashMap<String, String> properties = new HashMap<>();
        properties.put("test", "test");
        geoObject = new GeoObject("test", properties);
        Assert.assertNotNull(geoObject);
    }

    @Test
    public void testConstructorOverlay() {
        geoObject = new GeoObject(Mockito.mock(OverlayWithIW.class));
        Assert.assertNotNull(geoObject);
    }

    @Test
    public void testSetGetGeometry() {
        geoObject.setGeometry(Mockito.mock(OverlayWithIW.class));
        Assert.assertNotNull(geoObject.getGeometry());
    }

    @Test
    public void testSetGetProperties() {
        HashMap<String, String> test = new HashMap<>();
        test.put("test", "test");
        geoObject.setProperties(test);
        Assert.assertEquals(test, geoObject.getProperties());
    }

}