package com.jr_eagle_ocr.go4lunch.ui.adapters;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * @author jrigault
 */
public class UserViewState {
    @NonNull
    private final String name;
    private final int appendingString;
    @Nullable
    private final String urlPicture;
    @Nullable
    private final String chosenRestaurantId;
    @Nullable
    private final String chosenRestaurantName;
    private final float textAlpha;
    private final float imageAlpha;

    public UserViewState(
            @NonNull String name,
            int appendingString,
            @Nullable String urlPicture,
            @Nullable String chosenRestaurantId,
            @Nullable String chosenRestaurantName,
            float textAlpha,
            float imageAlpha
    ) {
        this.name = name;
        this.appendingString = appendingString;
        this.urlPicture = urlPicture;
        this.chosenRestaurantId = chosenRestaurantId;
        this.chosenRestaurantName = chosenRestaurantName;
        this.textAlpha = textAlpha;
        this.imageAlpha = imageAlpha;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public int getAppendingString() {
        return appendingString;
    }

    @Nullable
    public String getUrlPicture() {
        return urlPicture;
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

    @Nullable
    public String getChosenRestaurantName() {
        return chosenRestaurantName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserViewState that = (UserViewState) o;

        if (appendingString != that.appendingString) return false;
        if (Float.compare(that.textAlpha, textAlpha) != 0) return false;
        if (Float.compare(that.imageAlpha, imageAlpha) != 0) return false;
        if (!name.equals(that.name)) return false;
        if (urlPicture != null ? !urlPicture.equals(that.urlPicture) : that.urlPicture != null)
            return false;
        if (chosenRestaurantId != null ? !chosenRestaurantId.equals(that.chosenRestaurantId) : that.chosenRestaurantId != null)
            return false;
        return chosenRestaurantName != null ? chosenRestaurantName.equals(that.chosenRestaurantName) : that.chosenRestaurantName == null;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + appendingString;
        result = 31 * result + (urlPicture != null ? urlPicture.hashCode() : 0);
        result = 31 * result + (chosenRestaurantId != null ? chosenRestaurantId.hashCode() : 0);
        result = 31 * result + (chosenRestaurantName != null ? chosenRestaurantName.hashCode() : 0);
        result = 31 * result + (textAlpha != +0.0f ? Float.floatToIntBits(textAlpha) : 0);
        result = 31 * result + (imageAlpha != +0.0f ? Float.floatToIntBits(imageAlpha) : 0);
        return result;
    }

    @Override
    public String toString() {
        return "UserViewState{" +
                "name='" + name + '\'' +
                ", appendingString=" + appendingString +
                ", urlPicture='" + urlPicture + '\'' +
                ", chosenRestaurantId='" + chosenRestaurantId + '\'' +
                ", chosenRestaurantName='" + chosenRestaurantName + '\'' +
                ", textAlpha=" + textAlpha +
                ", imageAlpha=" + imageAlpha +
                '}';
    }
}