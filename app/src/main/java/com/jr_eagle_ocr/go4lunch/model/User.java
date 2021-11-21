package com.jr_eagle_ocr.go4lunch.model;

import androidx.annotation.Nullable;

/**
 * @author jrigault
 */
public class User {

    private String uid;
    private String userName;
    private String userEmail;
    @Nullable
    private String userUrlPicture;


    public User() {
    }

    public User(
            String uid,
            String userName,
            String userEmail,
            @Nullable String userUrlPicture
    ) {
        this.uid = uid;
        this.userName = userName;
        this.userEmail = userEmail;
        this.userUrlPicture = userUrlPicture;
    }


    public String getUid() {
        return uid;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    @Nullable
    public String getUserUrlPicture() {
        return userUrlPicture;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public void setUserUrlPicture(@Nullable String userUrlPicture) {
        this.userUrlPicture = userUrlPicture;
    }
}
