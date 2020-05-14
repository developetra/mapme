package com.example.mapme.presenter;

import android.location.Location;

import com.example.mapme.model.AppService;
import com.example.mapme.view.DataActivity;
import com.google.firebase.database.DataSnapshot;

public class DataPresenter implements AppService.AppServiceListener {

    private DataActivity activity;
    private DataSnapshot currentDataSnapshot;

    public DataPresenter(final DataActivity dataActivity) {
        this.activity = dataActivity;
    }

    @Override
    public void updateUserPosition(Location location) {
    }

    @Override
    public void dataChanged() {
        currentDataSnapshot = this.activity.appService.getCurrentDataSnapshot();
        activity.displayData(currentDataSnapshot);
    }

    public void deleteObject(String id){
        activity.appService.deleteObject(id);
    }

    public void saveToCloud(){
        activity.appService.uploadFile();
        activity.showInfoUploadSuccessful();
    }
}
