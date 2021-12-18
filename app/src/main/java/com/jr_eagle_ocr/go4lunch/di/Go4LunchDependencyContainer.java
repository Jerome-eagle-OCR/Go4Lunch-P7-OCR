package com.jr_eagle_ocr.go4lunch.di;

import android.app.Application;

import com.jr_eagle_ocr.go4lunch.data.place_autocomplete.PlaceAutocompleteRepository;
import com.jr_eagle_ocr.go4lunch.data.repositories.LocationRepository;
import com.jr_eagle_ocr.go4lunch.data.repositories.RestaurantRepository;
import com.jr_eagle_ocr.go4lunch.data.repositories.UserRepository;
import com.jr_eagle_ocr.go4lunch.data.usecases.GetCurrentUserChosenRestaurantId;
import com.jr_eagle_ocr.go4lunch.data.usecases.GetIsLikedRestaurant;
import com.jr_eagle_ocr.go4lunch.data.usecases.GetNotificationPair;
import com.jr_eagle_ocr.go4lunch.data.usecases.PlaceAutocompleteSearch;
import com.jr_eagle_ocr.go4lunch.data.usecases.SetClearChosenRestaurant;
import com.jr_eagle_ocr.go4lunch.data.usecases.SetClearLikedRestaurant;
import com.jr_eagle_ocr.go4lunch.util.BitmapUtil;

/**
 * @author jrigault
 */
public final class Go4LunchDependencyContainer {
    private final Application context;
    private final BitmapUtil bitmapUtil;
    private final UserRepository userRepository;
    private final LocationRepository locationRepository;
    private final RestaurantRepository restaurantRepository;
    private final PlaceAutocompleteRepository placeAutocompleteRepository;
    private final PlaceAutocompleteSearch placeAutocompleteSearch;
    private final SetClearChosenRestaurant setClearChosenRestaurant;
    private final GetCurrentUserChosenRestaurantId getCurrentUserChosenRestaurantId;
    private final SetClearLikedRestaurant setClearLikedRestaurant;
    private final GetIsLikedRestaurant getIsLikedRestaurant;
    private final GetNotificationPair getNotificationPair;

    public Go4LunchDependencyContainer(
            Application context
    ) {
        this.context = context;
        this.bitmapUtil = new BitmapUtil();
        userRepository = new UserRepository();
        locationRepository = new LocationRepository();
        restaurantRepository = new RestaurantRepository(bitmapUtil);
        placeAutocompleteRepository = new PlaceAutocompleteRepository(locationRepository);

        placeAutocompleteSearch = new PlaceAutocompleteSearch(placeAutocompleteRepository);

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

        getIsLikedRestaurant =
                new GetIsLikedRestaurant(
                        userRepository, restaurantRepository);


        getNotificationPair =
                new GetNotificationPair(
                        userRepository, restaurantRepository);
    }

    public Application getContext() {
        return context;
    }

    public BitmapUtil getBitmapUtil() {
        return bitmapUtil;
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

    public GetIsLikedRestaurant getIsLikedRestaurant() {
        return getIsLikedRestaurant;
    }

    public GetNotificationPair getNotificationKit() {
        return getNotificationPair;
    }
}
