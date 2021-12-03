package com.jr_eagle_ocr.go4lunch.data.models;

import androidx.annotation.Nullable;

import com.google.firebase.firestore.GeoPoint;

import java.util.HashMap;

/**
 * @author jrigault
 */
public class Restaurant {
    private String id;
    @Nullable
    private String photoString;
    private String name;
    private GeoPoint geoPoint;
    private String address;
    @Nullable
    private String phoneNumber;
    @Nullable
    private String webSiteUrl;
    private HashMap<String, String> closeTimes;
    private float rating;
    private String timestamp;

    public Restaurant() {
    }

    public Restaurant(
            String id,
            @Nullable String photoString,
            String name,
            GeoPoint geoPoint,
            String address,
            @Nullable String phoneNumber,
            @Nullable String webSiteUrl,
            HashMap<String, String> closeTimes,
            float rating,
            String timestamp
    ) {
        this.id = id;
        this.photoString = photoString;
        this.name = name;
        this.geoPoint = geoPoint;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.webSiteUrl = webSiteUrl;
        this.closeTimes = closeTimes;
        this.rating = rating;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public String getPhotoString() {
        return photoString;
    }

    public String getName() {
        return name;
    }

    public GeoPoint getGeoPoint() {
        return geoPoint;
    }

    public String getAddress() {
        return address;
    }

    @Nullable
    public String getPhoneNumber() {
        return phoneNumber;
    }

    @Nullable
    public String getWebSiteUrl() {
        return webSiteUrl;
    }

    public HashMap<String, String> getCloseTimes() {
        return closeTimes;
    }

    public float getRating() {
        return rating;
    }

    public String getTimestamp() {
        return timestamp;
    }


    public void setId(String id) {
        this.id = id;
    }

    public void setPhotoString(@Nullable String photoString) {
        this.photoString = photoString;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setGeoPoint(GeoPoint geoPoint) {
        this.geoPoint = geoPoint;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setPhoneNumber(@Nullable String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setWebSiteUrl(@Nullable String webSiteUrl) {
        this.webSiteUrl = webSiteUrl;
    }

    public void setCloseTimes(HashMap<String, String> closeTimes) {
        this.closeTimes = closeTimes;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
