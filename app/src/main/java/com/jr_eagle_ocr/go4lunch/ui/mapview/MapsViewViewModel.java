package com.jr_eagle_ocr.go4lunch.ui.mapview;

import android.location.Location;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.jr_eagle_ocr.go4lunch.model.Restaurant;
import com.jr_eagle_ocr.go4lunch.repositories.LocationRepository;
import com.jr_eagle_ocr.go4lunch.repositories.RestaurantRepository;

import java.util.List;
import java.util.Map;

/**
 * @author jrigault
 */
public class MapsViewViewModel extends ViewModel {
    private final LocationRepository locationRepository;
    private final RestaurantRepository restaurantRepository;
    private final LiveData<Map<String, Restaurant>> foundRestaurantsLiveData;
    private final LiveData<List<String>> chosenRestaurantIdsLiveData;

    public MapsViewViewModel(
            LocationRepository locationRepository,
            RestaurantRepository restaurantRepository
    ) {
        this.locationRepository = locationRepository;
        this.restaurantRepository = restaurantRepository;
        foundRestaurantsLiveData = restaurantRepository.getFoundRestaurants();
        chosenRestaurantIdsLiveData = restaurantRepository.getChosenRestaurantIds();
    }

    public void setLocation(Location lastKnownLocation) {
        locationRepository.setLocation(lastKnownLocation);
    }

    /**
     * Get restaurants found in MapsView
     *
     * @return a livedata of restaurant HashMap where key = placeId and value = restaurant
     */
    public LiveData<Map<String, Restaurant>> getFoundRestaurants() {
        return foundRestaurantsLiveData;
    }

    /**
     * Add a restaurant to the restaurant HashMap
     *
     * @param restaurant the restaurant to add
     */
    public void addFoundRestaurant(Restaurant restaurant) {
        restaurantRepository.addFoundRestaurant(restaurant);
    }

    /**
     * Get the list of chosen restaurant ids from the listened "chosen_restaurants" Firestore collection
     *
     * @return a livedata of up-to-date list of chosen restaurant id
     */
    public LiveData<List<String>> getChosenRestaurantIds() {
        return chosenRestaurantIdsLiveData;
    }
}
