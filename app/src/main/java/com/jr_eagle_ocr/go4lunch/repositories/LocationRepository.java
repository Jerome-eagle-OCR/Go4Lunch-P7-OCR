package com.jr_eagle_ocr.go4lunch.repositories;

import android.location.Location;

public class LocationRepository extends Repository {
    //    private static volatile LocationRepository instance;
    private Location location;

    public LocationRepository() {
    }

//    public static LocationRepository getInstance() {
//        LocationRepository result = instance;
//        if (result != null) {
//            return result;
//        }
//        synchronized (LocationRepository.class) {
//            if (instance == null) {
//                instance = new LocationRepository();
//            }
//        }
//        return instance;
//    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}
