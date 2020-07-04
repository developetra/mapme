package com.example.mapme.model;

import android.content.Context;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Method;
import java.util.HashMap;

@RunWith(AndroidJUnit4.class)
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
        HashMap<String, String> result = GeoJsonHelper.insertPropertiesToGeoJson(objects);
        Assert.assertNotNull(result);
    }

    @Test
    public void convertObjectsToGeoJsonString() {
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
        String result = GeoJsonHelper.convertObjectsToGeoJsonString(this.getTestContext(), objects);
        Assert.assertNotNull(result);
    }

    /**
     * @return The {@link Context} of the test project.
     */
    private Context getTestContext()
    {
        try
        {
            Method getTestContext = GeoJsonHelperTest.class.getMethod("getTestContext");
            return (Context) getTestContext.invoke(this);
        }
        catch (final Exception exception)
        {
            exception.printStackTrace();
            return null;
        }
    }
}