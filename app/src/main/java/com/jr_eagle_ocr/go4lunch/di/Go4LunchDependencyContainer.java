package com.jr_eagle_ocr.go4lunch.di;

import android.app.Application;

import com.jr_eagle_ocr.go4lunch.data.place_autocomplete.PlaceAutocompleteRepository;
import com.jr_eagle_ocr.go4lunch.data.repositories.LocationRepository;
import com.jr_eagle_ocr.go4lunch.data.repositories.RestaurantRepository;
import com.jr_eagle_ocr.go4lunch.data.repositories.UserRepository;
import com.jr_eagle_ocr.go4lunch.data.repositories.usecases.GetCurrentUserChosenRestaurantId;
import com.jr_eagle_ocr.go4lunch.data.repositories.usecases.GetNotificationKit;
import com.jr_eagle_ocr.go4lunch.data.repositories.usecases.IsLikedRestaurant;
import com.jr_eagle_ocr.go4lunch.data.repositories.usecases.PlaceAutocompleteSearch;
import com.jr_eagle_ocr.go4lunch.data.repositories.usecases.SetClearChosenRestaurant;
import com.jr_eagle_ocr.go4lunch.data.repositories.usecases.SetClearLikedRestaurant;

/**
 * @author jrigault
 */
public final class Go4LunchDependencyContainer {
    private final Application context;
    private final UserRepository userRepository;
    private final LocationRepository locationRepository;
    private final RestaurantRepository restaurantRepository;
    private final PlaceAutocompleteRepository placeAutocompleteRepository;
    private final PlaceAutocompleteSearch placeAutocompleteSearch;
    private final SetClearChosenRestaurant setClearChosenRestaurant;
    private final GetCurrentUserChosenRestaurantId getCurrentUserChosenRestaurantId;
    private final SetClearLikedRestaurant setClearLikedRestaurant;
    private final IsLikedRestaurant isLikedRestaurant;
    private final GetNotificationKit getNotificationKit;

    public Go4LunchDependencyContainer(
            Application context
    ) {
        this.context = context;
        userRepository = new UserRepository();
        locationRepository = new LocationRepository();
        restaurantRepository = new RestaurantRepository();
        placeAutocompleteRepository = new PlaceAutocompleteRepository();

        placeAutocompleteSearch = new PlaceAutocompleteSearch(
                locationRepository, placeAutocompleteRepository);

        getCurrentUserChosenRestaurantId =
                new GetCurrentUserChosenRestaurantId(
                        userRepository, restaurantRepository);

        setClearChosenRestaurant =
                new SetClearChosenRestaurant(
                        userRepository, restaurantRepository,
                        getCurrentUserChosenRestaurantId);

        setClearLikedRestaurant =
                new SetClearLikedRestaurant(
                        userRepository, restaurantRepository);

        isLikedRestaurant =
                new IsLikedRestaurant(
                        userRepository, restaurantRepository);


        getNotificationKit =
                new GetNotificationKit(
                        userRepository, restaurantRepository);
    }

    public Application getContext() {
        return context;
    }

    public UserRepository getUserRepository() {
        return userRepository;
    }

    public LocationRepository getLocationRepository() {
        return locationRepository;
    }

    public RestaurantRepository getRestaurantRepository() {
        return restaurantRepository;
    }

    public PlaceAutocompleteRepository getPlaceAutocompleteRepository() {
        return placeAutocompleteRepository;
    }

    public PlaceAutocompleteSearch getPlaceAutocompleteSearch() {
        return placeAutocompleteSearch;
    }

    public SetClearChosenRestaurant setClearChosenRestaurant() {
        return setClearChosenRestaurant;
    }

    public GetCurrentUserChosenRestaurantId getCurrentUserChosenRestaurantId() {
        return getCurrentUserChosenRestaurantId;
    }

    public SetClearLikedRestaurant setClearLikedRestaurant() {
        return setClearLikedRestaurant;
    }

    public IsLikedRestaurant getIsLikedRestaurant() {
        return isLikedRestaurant;
    }

    public GetNotificationKit getNotificationKit() {
        return getNotificationKit;
    }
}
