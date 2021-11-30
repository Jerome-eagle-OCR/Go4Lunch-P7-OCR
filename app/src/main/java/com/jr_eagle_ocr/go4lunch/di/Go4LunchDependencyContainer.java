package com.jr_eagle_ocr.go4lunch.di;

import android.app.Application;

import com.jr_eagle_ocr.go4lunch.repositories.LocationRepository;
import com.jr_eagle_ocr.go4lunch.repositories.RestaurantRepository;
import com.jr_eagle_ocr.go4lunch.repositories.UserRepository;
import com.jr_eagle_ocr.go4lunch.usecases.GetCurrentUserChosenRestaurantId;
import com.jr_eagle_ocr.go4lunch.usecases.GetNotificationKit;
import com.jr_eagle_ocr.go4lunch.usecases.GetRestaurantViewStates;
import com.jr_eagle_ocr.go4lunch.usecases.GetUserViewStates;
import com.jr_eagle_ocr.go4lunch.usecases.IsLikedRestaurant;
import com.jr_eagle_ocr.go4lunch.usecases.SetClearChosenRestaurant;
import com.jr_eagle_ocr.go4lunch.usecases.SetClearLikedRestaurant;

/**
 * @author jrigault
 */
public final class Go4LunchDependencyContainer {
    private final Application context;
    private final UserRepository userRepository;
    private final LocationRepository locationRepository;
    private final RestaurantRepository restaurantRepository;
    private final GetUserViewStates getUserViewStates;
    private final GetRestaurantViewStates getRestaurantViewStates;
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

        getUserViewStates =
                new GetUserViewStates(
                        userRepository);

        getRestaurantViewStates =
                new GetRestaurantViewStates(
                        locationRepository, restaurantRepository);

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

    public GetUserViewStates getUserViewStates() {
        return getUserViewStates;
    }

    public GetRestaurantViewStates getRestaurantViewStates() {
        return getRestaurantViewStates;
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
