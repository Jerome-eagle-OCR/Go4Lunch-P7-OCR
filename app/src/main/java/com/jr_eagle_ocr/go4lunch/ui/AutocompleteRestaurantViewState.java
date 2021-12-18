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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AutocompleteRestaurantViewState viewState = (AutocompleteRestaurantViewState) o;

        if (!description.equals(viewState.description)) return false;
        return placeId.equals(viewState.placeId);
    }

    @Override
    public int hashCode() {
        int result = description.hashCode();
        result = 31 * result + placeId.hashCode();
        return result;
    }

    @NonNull
    @Override
    public String toString() {
        return description;
    }
}
