package com.jr_eagle_ocr.go4lunch.ui;

import androidx.annotation.NonNull;

/**
 * @author jrigault
 */
public class AutocompleteRestaurantViewState {
    @NonNull
    private final String description;
    @NonNull
    private final String placeId;

    public AutocompleteRestaurantViewState(
            @NonNull String description,
            @NonNull String placeId
    ) {
        this.description = description;
        this.placeId = placeId;
    }

    @NonNull
    public String getDescription() {
        return description;
    }

    @NonNull
    public String getPlaceId() {
        return placeId;
    }

    @NonNull
    @Override
    public String toString() {
        return description;
    }
}
