package com.example.mapme.presenter;

import android.location.Location;

import com.example.mapme.model.AppService;
import com.example.mapme.view.EditInformationActivity;
import com.google.firebase.database.DataSnapshot;

import java.util.HashMap;

public class EditInformationPresenter implements AppService.AppServiceListener{

    private EditInformationActivity activity;
    private DataSnapshot currentDataSnapshot;

    public EditInformationPresenter(final EditInformationActivity editInformationActivity) {
        this.activity = editInformationActivity;
    }

    @Override
    public void updateUserPosition(Location location) {
    }

    @Override
    public void dataChanged() {
    }

    public void editObjectProperties (String id, HashMap<String, String> hashmap){
        activity.appService.editObjectProperties(id, hashmap);
    }

    public void deleteObject(String id){
        activity.appService.deleteObject(id);
    }

    public void fillProperties(){
        currentDataSnapshot = activity.appService.getCurrentDataSnapshot();
        activity.fillProperties(currentDataSnapshot);
    }
}
