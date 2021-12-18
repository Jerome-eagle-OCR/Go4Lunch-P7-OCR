package com.jr_eagle_ocr.go4lunch.data.usecases;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.libraries.places.api.model.Place;
import com.jr_eagle_ocr.go4lunch.data.place_autocomplete.PlaceAutocompleteRepository;
import com.jr_eagle_ocr.go4lunch.data.place_autocomplete.models.PredictionsItem;
import com.jr_eagle_ocr.go4lunch.data.usecases.parent.UseCase;
import com.jr_eagle_ocr.go4lunch.ui.AutocompleteRestaurantViewState;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;

/**
 * @author jrigault
 */
public final class PlaceAutocompleteSearch extends UseCase {
    public static final String OK = "OK";
    public static final String NO_SEARCH_MADE = "NO_SEARCH_MADE";
    public static final String ZERO_RESULTS = "ZERO_RESULTS";
    public static final String REQUEST_DENIED = "REQUEST_DENIED";
    private String input = "";
    private final Subject<String> searchInputSubject = BehaviorSubject.createDefault(input).toSerialized();
    private final Observable<List<AutocompleteRestaurantViewState>> autocompleteRestaurantsObservable;

    public PlaceAutocompleteSearch(
            @NonNull PlaceAutocompleteRepository placeAutocompleteRepository
    ) {
        // Create autocomplete observable triggered by search input
        autocompleteRestaurantsObservable = searchInputSubject
                .observeOn(AndroidSchedulers.mainThread())
                .debounce(300, TimeUnit.MILLISECONDS)
                .flatMap(input -> {
                    List<AutocompleteRestaurantViewState> autocompleteRestaurants = new ArrayList<>();
                    final AutocompleteRestaurantViewState[] header = new AutocompleteRestaurantViewState[1];
                    if (!input.isEmpty()) {
                        return placeAutocompleteRepository.getPlaceAutocompleteSearchResponse(input)
                                .subscribeOn(Schedulers.io())
                                .map(response -> {
                                    List<PredictionsItem> predictions = response.getPredictions();
                                    for (PredictionsItem item : predictions) {
                                        String restaurantType = Place.Type.RESTAURANT.name().toLowerCase(Locale.ROOT);
                                        if (item.getTypes().contains(restaurantType)) {
                                            String placeId = item.getPlaceId();
                                            String description = item.getDescription().replace(", France", "");
                                            AutocompleteRestaurantViewState autocompleteRestaurant =
                                                    new AutocompleteRestaurantViewState(description, placeId);
                                            autocompleteRestaurants.add(autocompleteRestaurant);
                                        }
                                    }
                                    header[0] = new AutocompleteRestaurantViewState(response.getStatus(),
                                            String.valueOf(autocompleteRestaurants.size()));
                                    autocompleteRestaurants.add(0, header[0]);
                                    Log.d(TAG, "PlaceAutocompleteSearch: " + header[0].toString());

                                    return autocompleteRestaurants;
                                });
                    } else {
                        return (ObservableSource<List<AutocompleteRestaurantViewState>>) observer -> {
                            header[0] = new AutocompleteRestaurantViewState(NO_SEARCH_MADE, NO_SEARCH_MADE);
                            autocompleteRestaurants.add(header[0]);
                            observer.onNext(autocompleteRestaurants);
                            Log.d(TAG, "PlaceAutocompleteSearch: " + header[0].toString());
                        };
                    }
                });
    }

    /**
     * Gets observable based on Google Places API search response
     *
     * @return RXjava Observable containing an up-to-date array of autocomplete restaurant view states
     */
    public Observable<List<AutocompleteRestaurantViewState>> getAutocompleteRestaurantsObservable() {
        return autocompleteRestaurantsObservable;
    }

    /**
     * Sets search input subject new item only when useful
     *
     * @param input chars from which to search
     */
    public void setSearchInput(@NonNull String input) {
        if (!this.input.equals(input)) {
            this.input = input;
            searchInputSubject.onNext(input);
        }
    }
}

