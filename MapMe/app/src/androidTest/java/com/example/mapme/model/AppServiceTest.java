package com.example.mapme.model;

import android.location.Location;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;


public class AppServiceTest {

    AppService appService;

    @Before
    public void setUp() throws Exception {
        appService = new AppService();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testSetGetObjects() {
        HashMap<String, GeoObject> objects = new HashMap<>();
        objects.put("test", new GeoObject());
        appService.setObjects(objects);
        Assert.assertEquals(objects, appService.getObjects());
    }

    @Test
    public void testRegisterListener() {
        AppService.AppServiceListener listener = new AppService.AppServiceListener() {
            @Override
            public void updateUserPosition(Location location) {
            }

            @Override
            public void dataChanged(HashMap<String, GeoObject> objects) {
            }
        };
        appService.registerListener(listener);
        Assert.assertEquals(true, appService.listeners.contains(listener));
    }

    @Test
    public void testUnRegisterListener() {
        AppService.AppServiceListener listener = new AppService.AppServiceListener() {
            @Override
            public void updateUserPosition(Location location) {
            }

            @Override
            public void dataChanged(HashMap<String, GeoObject> objects) {
            }
        };
        appService.registerListener(listener);
        appService.unregisterListener(listener);
        Assert.assertEquals(false, appService.listeners.contains(listener));
    }

    @Test
    public void testResetDatabase() {
        appService.resetDatabase();
    }


    @Test
    public void testSaveToDatabase() {
        GeoObject object = new GeoObject();
        final String id = appService.saveToDatabase(object);
        Assert.assertNotNull(appService.objectRef.child(id));
    }

    @Test
    public void testDeleteObject() {
    }

    @Test
    public void testEditObjectProperties() {
    }

    @Test
    public void testAddObjectProperties() {
    }


}