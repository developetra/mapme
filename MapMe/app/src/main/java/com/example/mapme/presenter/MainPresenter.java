package com.example.mapme.presenter;

import com.example.mapme.view.MainActivity;

/**
 * Presenter for MainActivity.
 */
public class MainPresenter {

    private final MainActivity activity;
    private final String infoText = "\nMapMe is a collaborative tool for the acquisition and mapping of geospatial data. \n\n" +
            "To create new geoobjects, you can draw markers, polylines or polygons directly on the screen and add more information " +
            "about them. Additionally you can add a reference to a nearby object already existing in Open Street Maps data.\n\n" +
            "Let's get started! ";

    /**
     * Constructor.
     *
     * @param mainActivity
     */
    public MainPresenter(MainActivity mainActivity) {
        this.activity = mainActivity;
    }

    /**
     * Get info text.
     *
     * @return infoText
     */
    public String getInfoText() {
        return this.infoText;
    }
}
