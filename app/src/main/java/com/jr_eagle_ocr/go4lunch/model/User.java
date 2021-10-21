package com.jr_eagle_ocr.go4lunch.model;

import androidx.annotation.Nullable;

/**
 * @author jrigault
 */
public class User {

    private String mUid;
    private String mUserName;
    @Nullable
    private String mUserUrlPicture;


    public User() {
    }

    public User(String uid, String userName, @Nullable String userUrlPicture) {
        mUid = uid;
        mUserName = userName;
        mUserUrlPicture = userUrlPicture;
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

    public void setUid(String uid) {
        mUid = uid;
    }

    public void setUserName(String userName) {
        mUserName = userName;
    }

    public void setUserUrlPicture(@Nullable String userUrlPicture) {
        mUserUrlPicture = userUrlPicture;
    }
}
