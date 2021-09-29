package com.jr_eagle_ocr.go4lunch.repositories;

import com.jr_eagle_ocr.go4lunch.model.Restaurant;

import java.util.HashMap;
import java.util.Map;

public final class RestaurantRepository {
    private static volatile RestaurantRepository instance;

    private final Map<String, Restaurant> mRestaurants = new HashMap<>();

    private RestaurantRepository() {
    }

    public static RestaurantRepository getInstance() {
        RestaurantRepository result = instance;
        if (result != null) {
            return result;
        }
        synchronized (RestaurantRepository.class) {
            if (instance == null) {
                instance = new RestaurantRepository();
            }
        }
        return instance;
    }

    public Map<String, Restaurant> getRestaurants() {
        return mRestaurants;
    }

    public void addRestaurant(Restaurant restaurant) {
        String id = restaurant.getId();
        mRestaurants.put(id, restaurant);
    }
}
