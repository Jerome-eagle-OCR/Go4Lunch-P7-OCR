package com.jr_eagle_ocr.go4lunch.model;

import androidx.annotation.Nullable;

public class User {

    private String mUid;
    private String mUserName;
    @Nullable
    private String mUserUrlPicture;
    @Nullable
    private String mChosenRestaurantId;


    public User() {
    }

    public User(String uId, String userName, @Nullable String userUrlPicture) {
        mUid = uId;
        mUserName = userName;
        mUserUrlPicture = userUrlPicture;
        mChosenRestaurantId = null;
    }


    public String getUid() {
        return mUid;
    }

    public String getUserName() {
        return mUserName;
    }

    @Nullable
    public String getUserUrlPicture() {
        return mUserUrlPicture;
    }

    @Nullable
    public String getChosenRestaurantId() {
        return mChosenRestaurantId;
    }


    public void setUserName(String userName) {
        mUserName = userName;
    }

    public void setUserUrlPicture(@Nullable String userUrlPicture) {
        mUserUrlPicture = userUrlPicture;
    }

    public void setChosenRestaurantId(@Nullable String chosenRestaurantId) {
        mChosenRestaurantId = chosenRestaurantId;
    }
}
