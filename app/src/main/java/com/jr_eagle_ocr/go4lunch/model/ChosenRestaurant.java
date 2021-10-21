package com.jr_eagle_ocr.go4lunch.model;

import java.util.List;

/**
 * @author jrigault
 */
public class ChosenRestaurant {

    private String placeId;
    private Long timestamp;
    private List<String> byUsers;

    public ChosenRestaurant() {
    }

    public ChosenRestaurant(
            String placeId,
            Long timestamp,
            List<String> byUsers
    ) {
        this.placeId = placeId;
        this.timestamp = timestamp;
        this.byUsers = byUsers;
    }

    public String getPlaceId() {
        return placeId;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public List<String> getByUsers() {
        return byUsers;
    }
}