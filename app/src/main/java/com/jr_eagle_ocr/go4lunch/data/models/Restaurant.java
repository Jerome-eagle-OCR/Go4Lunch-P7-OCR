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
    private String websiteUrl;
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
            @Nullable String websiteUrl,
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
        this.websiteUrl = websiteUrl;
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
    public String getWebsiteUrl() {
        return websiteUrl;
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

    public void setWebsiteUrl(@Nullable String websiteUrl) {
        this.websiteUrl = websiteUrl;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Restaurant that = (Restaurant) o;

        if (Float.compare(that.rating, rating) != 0) return false;
        if (!id.equals(that.id)) return false;
        if (photoString != null ? !photoString.equals(that.photoString) : that.photoString != null)
            return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (geoPoint != null ? !geoPoint.equals(that.geoPoint) : that.geoPoint != null)
            return false;
        if (address != null ? !address.equals(that.address) : that.address != null) return false;
        if (phoneNumber != null ? !phoneNumber.equals(that.phoneNumber) : that.phoneNumber != null)
            return false;
        if (websiteUrl != null ? !websiteUrl.equals(that.websiteUrl) : that.websiteUrl != null)
            return false;
        if (closeTimes != null ? !closeTimes.equals(that.closeTimes) : that.closeTimes != null)
            return false;
        return timestamp != null ? timestamp.equals(that.timestamp) : that.timestamp == null;
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + (photoString != null ? photoString.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (geoPoint != null ? geoPoint.hashCode() : 0);
        result = 31 * result + (address != null ? address.hashCode() : 0);
        result = 31 * result + (phoneNumber != null ? phoneNumber.hashCode() : 0);
        result = 31 * result + (websiteUrl != null ? websiteUrl.hashCode() : 0);
        result = 31 * result + (closeTimes != null ? closeTimes.hashCode() : 0);
        result = 31 * result + (rating != +0.0f ? Float.floatToIntBits(rating) : 0);
        result = 31 * result + (timestamp != null ? timestamp.hashCode() : 0);
        return result;
    }
}
