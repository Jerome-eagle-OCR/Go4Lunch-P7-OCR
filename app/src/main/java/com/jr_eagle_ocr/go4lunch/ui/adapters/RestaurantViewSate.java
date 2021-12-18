package com.jr_eagle_ocr.go4lunch.ui.adapters;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * @author jrigault
 */
public class RestaurantViewSate {
    @NonNull
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
            @NonNull String id,
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

    @NonNull
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RestaurantViewSate that = (RestaurantViewSate) o;

        if (isJoinersVisible != that.isJoinersVisible) return false;
        if (openingPrefix != that.openingPrefix) return false;
        if (isWarningStyle != that.isWarningStyle) return false;
        if (Float.compare(that.rating, rating) != 0) return false;
        if (!id.equals(that.id)) return false;
        if (photo != null ? !photo.equals(that.photo) : that.photo != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (distance != null ? !distance.equals(that.distance) : that.distance != null)
            return false;
        if (address != null ? !address.equals(that.address) : that.address != null) return false;
        if (joiners != null ? !joiners.equals(that.joiners) : that.joiners != null) return false;
        return closingTime != null ? closingTime.equals(that.closingTime) : that.closingTime == null;
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + (photo != null ? photo.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (distance != null ? distance.hashCode() : 0);
        result = 31 * result + (address != null ? address.hashCode() : 0);
        result = 31 * result + (joiners != null ? joiners.hashCode() : 0);
        result = 31 * result + (isJoinersVisible ? 1 : 0);
        result = 31 * result + openingPrefix;
        result = 31 * result + (closingTime != null ? closingTime.hashCode() : 0);
        result = 31 * result + (isWarningStyle ? 1 : 0);
        result = 31 * result + (rating != +0.0f ? Float.floatToIntBits(rating) : 0);
        return result;
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
