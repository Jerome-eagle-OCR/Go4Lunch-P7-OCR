package com.jr_eagle_ocr.go4lunch.data.place_autocomplete;


import android.location.Location;

import androidx.annotation.NonNull;

import com.jr_eagle_ocr.go4lunch.BuildConfig;
import com.jr_eagle_ocr.go4lunch.data.place_autocomplete.models.PlaceAutocompleteApiResponse;
import com.jr_eagle_ocr.go4lunch.data.repositories.LocationRepository;

import io.reactivex.Observable;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * @author jrigault
 */
public final class PlaceAutocompleteRepository {
    public static final String BASE_URL = BuildConfig.IS_TESTING.get() ?
            "https://d3ba77ff-2477-4ff5-b127-81c2f865529c.mock.pstmn.io/"
            : "https://maps.googleapis.com/maps/api/place/";
    public static final String COMPONENTS_VALUE = "country:fr";
    public static final String TYPES_VALUE = "establishment";
    public static final String RADIUS_VALUE = "500";
    public static final String STRICTBOUNDS_VALUE = "true";
    public static final String KEY_VALUE = BuildConfig.GOOGLE_PLACES_KEY;
    private final LocationRepository locationRepository;

    private final PlaceAutocompleteApi service;

    public PlaceAutocompleteRepository(
            @NonNull LocationRepository locationRepository
    ) {
        this.locationRepository = locationRepository;

        // Create retrofit and valorize API service
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();

        service = retrofit.create(PlaceAutocompleteApi.class);
    }

    /**
     * Get place autocomplete API response observable
     *
     * @param input       chars from which to search
     * @param mapLocation geographic location to search around
     * @return place autocomplete API response in Rxjava observable
     */
    public Observable<PlaceAutocompleteApiResponse> getPlaceAutocompleteSearchResponse(String input) {
        Location mapLocation = locationRepository.getMapLocation();
        String location = mapLocation.getLatitude() + "," + mapLocation.getLongitude();

        return this.service.getPlacesFromInput(input,
                                               COMPONENTS_VALUE,
                                               TYPES_VALUE,
                                               location,
                                               RADIUS_VALUE,
                                               STRICTBOUNDS_VALUE,
                                               KEY_VALUE);
    }
}