package com.jr_eagle_ocr.go4lunch.di;

import com.jr_eagle_ocr.go4lunch.repositories.RestaurantRepository;
import com.jr_eagle_ocr.go4lunch.repositories.UserRepository;

/**
 * @author jrigault
 */
public class Go4LunchDependencyContainer {

    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;

    public Go4LunchDependencyContainer() {
        userRepository = UserRepository.getInstance();
        restaurantRepository = RestaurantRepository.getInstance();
    }

    public UserRepository getUserRepository() {
        return userRepository;
    }

    public RestaurantRepository getRestaurantRepository() {
        return restaurantRepository;
    }
}
