package com.jr_eagle_ocr.go4lunch.model;

/**
 * @author jrigault
 */
public class ChosenRestaurant {

    private String placeId;
    private String placeName;
    private String placeAddress;
    private Long timestamp;


    public ChosenRestaurant() {
    }

    public ChosenRestaurant(
            String placeId,
            String placeName,
            String placeAddress,
            Long timestamp
    ) {
        this.placeId = placeId;
        this.placeName = placeName;
        this.placeAddress = placeAddress;
        this.timestamp = timestamp;
    }


    public String getPlaceId() {
        return placeId;
    }

    public String getPlaceName() {
        return placeName;
    }

    public String getPlaceAddress() {
        return placeAddress;
    }

    public Long getTimestamp() {
        return timestamp;
    }
}