package com.jr_eagle_ocr.go4lunch.data.place_autocomplete;

import com.jr_eagle_ocr.go4lunch.data.place_autocomplete.models.PlaceAutocompleteApiResponse;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * @author jrigault
 */
public interface PlaceAutocompleteApi {

    @GET("autocomplete/json")
    Observable<PlaceAutocompleteApiResponse> getPlacesFromInput(@Query("input") String input,
                                                                @Query("components") String components,
                                                                @Query("types") String types,
                                                                @Query("location") String location,
                                                                @Query("radius") String radius,
                                                                @Query("strictbounds") String strictbounds,
                                                                @Query("key") String key);
}
