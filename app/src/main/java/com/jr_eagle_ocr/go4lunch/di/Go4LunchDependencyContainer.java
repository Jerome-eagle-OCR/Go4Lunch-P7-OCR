package com.jr_eagle_ocr.go4lunch.di;

import android.app.Application;

import com.jr_eagle_ocr.go4lunch.repositories.LocationRepository;
import com.jr_eagle_ocr.go4lunch.repositories.RestaurantRepository;
import com.jr_eagle_ocr.go4lunch.repositories.UserRepository;
import com.jr_eagle_ocr.go4lunch.usecases.GetCurrentUser;
import com.jr_eagle_ocr.go4lunch.usecases.GetCurrentUserChosenRestaurantId;
import com.jr_eagle_ocr.go4lunch.usecases.GetNotificationLinesUseCase;
import com.jr_eagle_ocr.go4lunch.usecases.SetClearChosenRestaurant;

/**
 * @author jrigault
 */
public class Go4LunchDependencyContainer {

    private final UserRepository userRepository;
    private final LocationRepository locationRepository;
    private final RestaurantRepository restaurantRepository;
    private final GetCurrentUser getCurrentUser;
    private final SetClearChosenRestaurant setClearChosenRestaurant;
    private final GetCurrentUserChosenRestaurantId getCurrentUserChosenRestaurantId;
    private final GetNotificationLinesUseCase getNotificationLinesUseCase;

    public Go4LunchDependencyContainer(Application context) {
        userRepository = new UserRepository();
        locationRepository = new LocationRepository();
        restaurantRepository = new RestaurantRepository();

        getCurrentUser =
                new GetCurrentUser(
                        userRepository);

        setClearChosenRestaurant =
                new SetClearChosenRestaurant(
                        restaurantRepository, getCurrentUser);

        getCurrentUserChosenRestaurantId =
                new GetCurrentUserChosenRestaurantId(
                        userRepository, restaurantRepository);

        getNotificationLinesUseCase =
                new GetNotificationLinesUseCase(
                        context, userRepository, restaurantRepository, getCurrentUserChosenRestaurantId);
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

    public GetCurrentUser getUseCaseGetCurrentUser() {
        return getCurrentUser;
    }

    public SetClearChosenRestaurant getUseCaseSetClearChosenRestaurant() {
        return setClearChosenRestaurant;
    }

    public synchronized GetCurrentUserChosenRestaurantId getUseCaseGetCurrentUserChosenRestaurantId() {
        return getCurrentUserChosenRestaurantId;
    }

    public GetNotificationLinesUseCase getGetNotificationTextUseCase() {
        return getNotificationLinesUseCase;
    }
}
