package com.jr_eagle_ocr.go4lunch.ui.restaurant_detail;

import android.graphics.Bitmap;

import androidx.annotation.Nullable;

import com.jr_eagle_ocr.go4lunch.ui.adapters.UserViewState;

import java.util.List;

/**
 * @author jrigault
 */
public class RestaurantDetailViewSate {
    private final String id;
    private final Bitmap photo;
    private final String name;
    private final String address;
    @Nullable
    private String phoneNumber;
    @Nullable
    private String websiteUrl;
    private final int chooseResource;
    private final int likeVisibility;
    private final List<UserViewState> joiningUsers;

    public RestaurantDetailViewSate(
            String id,
            Bitmap photo,
            String name,
            String address,
            @Nullable String phoneNumber,
            @Nullable String websiteUrl,
            int chooseResource,
            int likeVisibility,
            List<UserViewState> joiningUsers
    ) {
        this.id = id;
        this.photo = photo;
        this.name = name;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.websiteUrl = websiteUrl;
        this.chooseResource = chooseResource;
        this.likeVisibility = likeVisibility;
        this.joiningUsers = joiningUsers;
    }

    public String getId() {
        return id;
    }

    public Bitmap getPhoto() {
        return photo;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    @Nullable
    public String getPhoneNumber() {
        return phoneNumber;
    }

    @Nullable
    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public int getChooseResource() {
        return chooseResource;
    }

    public int getLikeVisibility() {
        return likeVisibility;
    }

    public List<UserViewState> getJoiningUsers() {
        return joiningUsers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RestaurantDetailViewSate that = (RestaurantDetailViewSate) o;

        if (chooseResource != that.chooseResource) return false;
        if (likeVisibility != that.likeVisibility) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (photo != null ? !photo.equals(that.photo) : that.photo != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (address != null ? !address.equals(that.address) : that.address != null) return false;
        if (phoneNumber != null ? !phoneNumber.equals(that.phoneNumber) : that.phoneNumber != null)
            return false;
        if (websiteUrl != null ? !websiteUrl.equals(that.websiteUrl) : that.websiteUrl != null)
            return false;
        return joiningUsers != null ? joiningUsers.equals(that.joiningUsers) : that.joiningUsers == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (photo != null ? photo.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (address != null ? address.hashCode() : 0);
        result = 31 * result + (phoneNumber != null ? phoneNumber.hashCode() : 0);
        result = 31 * result + (websiteUrl != null ? websiteUrl.hashCode() : 0);
        result = 31 * result + chooseResource;
        result = 31 * result + likeVisibility;
        result = 31 * result + (joiningUsers != null ? joiningUsers.hashCode() : 0);
        return result;
    }
}
