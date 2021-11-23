package com.jr_eagle_ocr.go4lunch.model;

import android.graphics.Bitmap;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.OpeningHours;

/**
 * @author jrigault
 */
public class Restaurant {
    private final String mId;
    private String mName;
    private String mAddress;
    private LatLng mLatLng;
    private double mRating;
    private OpeningHours mOpeningHours;
    private String mPhoneNumber;
    private Bitmap mPhoto;
    private String mWebSiteUrl;

    public Restaurant(
            String id
    ) {
        mId = id;
    }

    public String getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public String getAddress() {
        return mAddress;
    }

    public LatLng getLatLng() {
        return mLatLng;
    }

    public double getRating() {
        return mRating;
    }

    public OpeningHours getOpeningHours() {
        return mOpeningHours;
    }

    public String getPhoneNumber() {
        return mPhoneNumber;
    }

    public Bitmap getPhoto() {
        return mPhoto;
    }

    public String getWebSiteUrl() {
        return mWebSiteUrl;
    }


    public void setName(String name) {
        mName = name;
    }

    public void setAddress(String address) {
        mAddress = address;
    }

    public void setLatLng(LatLng latLng) {
        mLatLng = latLng;
    }

    public void setRating(double rating) {
        mRating = rating;
    }

    public void setOpeningHours(OpeningHours openingHours) {
        mOpeningHours = openingHours;
    }

    public void setPhoneNumber(String phoneNumber) {
        mPhoneNumber = phoneNumber;
    }

    public void setPhoto(Bitmap photo) {
        mPhoto = photo;
    }

    public void setWebSiteUrl(String webSiteUrl) {
        mWebSiteUrl = webSiteUrl;
    }
}
