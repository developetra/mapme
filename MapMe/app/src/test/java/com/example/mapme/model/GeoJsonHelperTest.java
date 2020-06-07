package com.example.mapme.model;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

public class GeoJsonHelperTest {


    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testInsertPropertiesToGeoJson() {
        HashMap<String, GeoObject> objects = new HashMap<>();
        HashMap<String, String> properties = new HashMap<>();
        properties.put("test", "test");
        GeoObject geoObject = new GeoObject("{\n" +
                "  \"type\": \"Feature\",\n" +
                "  \"geometry\": {\n" +
                "    \"type\": \"Point\",\n" +
                "    \"coordinates\": [49.891667, 10.886944]\n" +
                "  },\n" +
                "  \"properties\": {\n" +
                "    \"name\": \"Bamberger Rathaus\"\n" +
                "  }\n" +
                "}\n", properties);
        objects.put("test", geoObject);
        GeoJsonHelper.insertPropertiesToGeoJson(objects);

    }

    @Test
    public void testConvertObjectsToGeoJsonString() {
    }
}