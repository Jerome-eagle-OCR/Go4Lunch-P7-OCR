package com.jr_eagle_ocr.go4lunch.data.models;

/**
 * @author jrigault
 */
public class ChosenRestaurant {
    private String placeId;
    private String placeName;
    private String placeAddress;
    private String timestamp;

    public ChosenRestaurant() {
    }

    public ChosenRestaurant(
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ChosenRestaurant that = (ChosenRestaurant) o;

        if (placeId != null ? !placeId.equals(that.placeId) : that.placeId != null) return false;
        if (placeName != null ? !placeName.equals(that.placeName) : that.placeName != null)
            return false;
        if (placeAddress != null ? !placeAddress.equals(that.placeAddress) : that.placeAddress != null)
            return false;
        return timestamp != null ? timestamp.equals(that.timestamp) : that.timestamp == null;
    }

    @Override
    public int hashCode() {
        int result = placeId != null ? placeId.hashCode() : 0;
        result = 31 * result + (placeName != null ? placeName.hashCode() : 0);
        result = 31 * result + (placeAddress != null ? placeAddress.hashCode() : 0);
        result = 31 * result + (timestamp != null ? timestamp.hashCode() : 0);
        return result;
    }
}