package com.jr_eagle_ocr.go4lunch.data.repositories;

import android.location.Location;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.jr_eagle_ocr.go4lunch.data.repositories.parent.Repository;

/**
 * @author jrigault
 */
public class LocationRepository extends Repository {
    private final MutableLiveData<Boolean> locationPermissionGrantedMutableLiveData = new MutableLiveData<>(Boolean.FALSE);
    private Location location;

    public LocationRepository() {
    }

    public LiveData<Boolean> getLocationPermissionGranted() {
        return locationPermissionGrantedMutableLiveData;
    }

    public Location getLocation() {
        return location;
    }


    public void setLocationPermissionGranted(boolean locationPermissionGranted) {
        locationPermissionGrantedMutableLiveData.setValue(locationPermissionGranted);
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}
