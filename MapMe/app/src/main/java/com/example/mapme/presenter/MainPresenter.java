package com.example.mapme.presenter;

import com.example.mapme.view.MainActivity;

public class MainPresenter {

    private MainActivity activity;
    private String infoText = "MapMe is a collaborative tool for the acquisition and mapping of geospatial data. \n" +
            "You can draw markers, polylines or polygons directly on the screen. ";

    public MainPresenter(MainActivity mainActivity) {
        this.activity = mainActivity;
    }

    public String getInfoText() {
        return this.infoText;
    }
}
