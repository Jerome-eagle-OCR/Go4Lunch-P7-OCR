package com.jr_eagle_ocr.go4lunch.ui.adapters;

import androidx.annotation.Nullable;

/**
 * @author jrigault
 */
public class UserViewState {
    private final String name;
    private final int appendingString;
    private final String urlPicture;
    @Nullable
    private final String email;
    @Nullable
    private final String chosenRestaurantId;
    private final String chosenRestaurantName;
    private final float textAlpha;
    private final float imageAlpha;

    public UserViewState(
            String name,
            int appendingString,
            String urlPicture,
            @Nullable String email,
            @Nullable String chosenRestaurantId,
            String chosenRestaurantName, float textAlpha,
            float imageAlpha
    ) {
        this.name = name;
        this.appendingString = appendingString;
        this.urlPicture = urlPicture;
        this.email = email;
        this.chosenRestaurantId = chosenRestaurantId;
        this.chosenRestaurantName = chosenRestaurantName;
        this.textAlpha = textAlpha;
        this.imageAlpha = imageAlpha;
    }

    public String getName() {
        return name;
    }

    public int getAppendingString() {
        return appendingString;
    }

    public String getUrlPicture() {
        return urlPicture;
    }

    @Nullable
    public String getEmail() {
        return email;
    }

    public float getTextAlpha() {
        return textAlpha;
    }

    public float getImageAlpha() {
        return imageAlpha;
    }

    @Nullable
    public String getChosenRestaurantId() {
        return chosenRestaurantId;
    }

    public String getChosenRestaurantName() {
        return chosenRestaurantName;
    }

    @Override
    public String toString() {
        return "UserViewState{" +
                "name='" + name + '\'' +
                ", appendingString=" + appendingString +
                ", urlPicture='" + urlPicture + '\'' +
                ", email='" + email + '\'' +
                ", chosenRestaurantId='" + chosenRestaurantId + '\'' +
                ", chosenRestaurantName='" + chosenRestaurantName + '\'' +
                ", textAlpha=" + textAlpha +
                ", imageAlpha=" + imageAlpha +
                '}';
    }
}