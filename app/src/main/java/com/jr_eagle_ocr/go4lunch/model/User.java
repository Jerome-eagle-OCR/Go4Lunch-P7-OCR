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
    private boolean isNoonReminderEnabled;

    public User() {
    }

    public User(
            String uid,
            String userName,
            String userEmail,
            @Nullable String userUrlPicture,
            boolean isNoonReminderEnabled
    ) {
        this.uid = uid;
        this.userName = userName;
        this.userEmail = userEmail;
        this.userUrlPicture = userUrlPicture;
        this.isNoonReminderEnabled = isNoonReminderEnabled;
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

    public boolean isNoonReminderEnabled() {
        return isNoonReminderEnabled;
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

    public void setNoonReminderEnabled(boolean noonReminderEnabled) {
        isNoonReminderEnabled = noonReminderEnabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        if (isNoonReminderEnabled != user.isNoonReminderEnabled) return false;
        if (uid != null ? !uid.equals(user.uid) : user.uid != null) return false;
        if (userName != null ? !userName.equals(user.userName) : user.userName != null)
            return false;
        if (userEmail != null ? !userEmail.equals(user.userEmail) : user.userEmail != null)
            return false;
        return userUrlPicture != null ? userUrlPicture.equals(user.userUrlPicture) : user.userUrlPicture == null;
    }

    @Override
    public int hashCode() {
        int result = uid != null ? uid.hashCode() : 0;
        result = 31 * result + (userName != null ? userName.hashCode() : 0);
        result = 31 * result + (userEmail != null ? userEmail.hashCode() : 0);
        result = 31 * result + (userUrlPicture != null ? userUrlPicture.hashCode() : 0);
        result = 31 * result + (isNoonReminderEnabled ? 1 : 0);
        return result;
    }
}
