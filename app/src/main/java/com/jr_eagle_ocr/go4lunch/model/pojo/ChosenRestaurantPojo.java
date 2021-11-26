package com.jr_eagle_ocr.go4lunch.model.pojo;

/**
 * @author jrigault
 */
public class ChosenRestaurantPojo {
    private String placeId;
    private String placeName;
    private String placeAddress;
    private String timestamp;

    public ChosenRestaurantPojo() {
    }

    public ChosenRestaurantPojo(
            String placeId,
            String placeName,
            String placeAddress,
            String timestamp
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

    public String getTimestamp() {
        return timestamp;
    }


    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public void setPlaceAddress(String placeAddress) {
        this.placeAddress = placeAddress;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}