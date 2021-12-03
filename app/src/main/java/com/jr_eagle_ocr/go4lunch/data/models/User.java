package com.jr_eagle_ocr.go4lunch.data.models;

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
    private boolean isLogged;

    public User() {
    }

    public User(
            String uid,
            String userName,
            String userEmail,
            @Nullable String userUrlPicture,
            boolean isNoonReminderEnabled,
            boolean isLogged) {
        this.uid = uid;
        this.userName = userName;
        this.userEmail = userEmail;
        this.userUrlPicture = userUrlPicture;
        this.isNoonReminderEnabled = isNoonReminderEnabled;
        this.isLogged = isLogged;
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

    public boolean isLogged() {
        return isLogged;
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

    public void setLogged(boolean logged) {
        isLogged = logged;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        if (isNoonReminderEnabled() != user.isNoonReminderEnabled()) return false;
        if (isLogged() != user.isLogged()) return false;
        if (!getUid().equals(user.getUid())) return false;
        if (getUserName() != null ? !getUserName().equals(user.getUserName()) : user.getUserName() != null)
            return false;
        if (getUserEmail() != null ? !getUserEmail().equals(user.getUserEmail()) : user.getUserEmail() != null)
            return false;
        return getUserUrlPicture() != null ? getUserUrlPicture().equals(user.getUserUrlPicture()) : user.getUserUrlPicture() == null;
    }

    @Override
    public int hashCode() {
        int result = getUid().hashCode();
        result = 31 * result + (getUserName() != null ? getUserName().hashCode() : 0);
        result = 31 * result + (getUserEmail() != null ? getUserEmail().hashCode() : 0);
        result = 31 * result + (getUserUrlPicture() != null ? getUserUrlPicture().hashCode() : 0);
        result = 31 * result + (isNoonReminderEnabled() ? 1 : 0);
        result = 31 * result + (isLogged() ? 1 : 0);
        return result;
    }
}
