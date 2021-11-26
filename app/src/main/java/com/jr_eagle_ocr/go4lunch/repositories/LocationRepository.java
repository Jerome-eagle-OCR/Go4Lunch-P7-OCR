package com.jr_eagle_ocr.go4lunch.repositories;

import android.location.Location;

import com.jr_eagle_ocr.go4lunch.repositories.parent.Repository;

/**
 * @author jrigault
 */
public class LocationRepository extends Repository {
    private Location location;

    public LocationRepository() {
    }

    public Location getLocation() {
        return location;
    }


    public void setLocation(Location location) {
        this.location = location;
    }
}
