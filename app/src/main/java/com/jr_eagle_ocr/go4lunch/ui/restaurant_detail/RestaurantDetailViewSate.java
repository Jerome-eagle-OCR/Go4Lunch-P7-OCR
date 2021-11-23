package com.jr_eagle_ocr.go4lunch.ui.restaurant_detail;

import android.graphics.Bitmap;

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
    private final int chosenResource;
    private final int likeVisibility;
    private final List<UserViewState> joiningUsers;

    public RestaurantDetailViewSate(
            String id,
            Bitmap photo,
            String name,
            String address,
            int chosenResource,
            int likeVisibility,
            List<UserViewState> joiningUsers
    ) {
        this.id = id;
        this.photo = photo;
        this.name = name;
        this.address = address;
        this.chosenResource = chosenResource;
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

    public int getChooseResource() {
        return chosenResource;
    }

    public int getLikeVisibility() {
        return likeVisibility;
    }

    public List<UserViewState> getJoiningUsers() {
        return joiningUsers;
    }
}
