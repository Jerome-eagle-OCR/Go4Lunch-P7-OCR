package com.jr_eagle_ocr.go4lunch.data.models;

import android.graphics.Bitmap;

import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.OpeningHours;

/**
 * @author jrigault
 */
public class FoundRestaurant {
    private final String mId;
    private String mName;
    private String mAddress;
    private LatLng mLatLng;
    @Nullable
    private Double mRating;
    private OpeningHours mOpeningHours;
    private String mPhoneNumber;
    @Nullable
    private Bitmap mPhoto;
    private String mWebsiteUrl;

    public FoundRestaurant(
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

    @Nullable
    public Double getRating() {
        return mRating;
    }

    public OpeningHours getOpeningHours() {
        return mOpeningHours;
    }

    public String getPhoneNumber() {
        return mPhoneNumber;
    }

    @Nullable
    public Bitmap getPhoto() {
        return mPhoto;
    }

    public String getWebsiteUrl() {
        return mWebsiteUrl;
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

    public void setRating(@Nullable Double rating) {
        mRating = rating;
    }

    public void setOpeningHours(OpeningHours openingHours) {
        mOpeningHours = openingHours;
    }

    public void setPhoneNumber(String phoneNumber) {
        mPhoneNumber = phoneNumber;
    }

    public void setPhoto(@Nullable Bitmap photo) {
        mPhoto = photo;
    }

    public void setWebsiteUrl(String websiteUrl) {
        mWebsiteUrl = websiteUrl;
    }
}
