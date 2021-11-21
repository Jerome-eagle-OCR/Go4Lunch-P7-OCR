package com.jr_eagle_ocr.go4lunch.ui.viewstates;

import android.graphics.Bitmap;

import androidx.annotation.Nullable;

/**
 * @author jrigault
 */
public class RestaurantViewSate {
    private final String id;
    @Nullable
    private final Bitmap photo;
    private final String name;
    private final String distance;
    private final String address;
    private final String joiners;
    private final boolean isJoinersVisible;
    private final int openingPrefix;
    private final String closingTime;
    private final boolean isWarningStyle;
    private final float rating;

    public RestaurantViewSate(
            String id,
            @Nullable Bitmap photo,
            String name,
            String distance,
            String address,
            String joiners,
            boolean isJoinersVisible,
            int openingPrefix,
            String closingTime,
            boolean isWarningStyle,
            float rating
    ) {
        this.id = id;
        this.photo = photo;
        this.name = name;
        this.distance = distance;
        this.address = address;
        this.joiners = joiners;
        this.isJoinersVisible = isJoinersVisible;
        this.openingPrefix = openingPrefix;
        this.closingTime = closingTime;
        this.isWarningStyle = isWarningStyle;
        this.rating = rating;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public Bitmap getPhoto() {
        return photo;
    }

    public String getName() {
        return name;
    }

    public String getDistance() {
        return distance;
    }

    public String getAddress() {
        return address;
    }

    public String getJoiners() {
        return joiners;
    }

    public boolean isJoinersVisible() {
        return isJoinersVisible;
    }

    public int getOpeningPrefix() {
        return openingPrefix;
    }

    public String getClosingTime() {
        return closingTime;
    }

    public boolean isWarningStyle() {
        return isWarningStyle;
    }

    public float getRating() {
        return rating;
    }

    @Override
    public String toString() {
        return "RestaurantViewSate{" +
                "id='" + id + '\'' +
                ", photo?=" + (photo != null) +
                ", name='" + name + '\'' +
                ", distance='" + distance + '\'' +
                ", address='" + address + '\'' +
                ", joiners='" + joiners + '\'' +
                ", isJoinersVisible=" + isJoinersVisible +
                ", openingPrefix=" + openingPrefix +
                ", closingTime='" + closingTime + '\'' +
                ", isWarningStyle=" + isWarningStyle +
                ", rating=" + rating +
                '}';
    }
}
