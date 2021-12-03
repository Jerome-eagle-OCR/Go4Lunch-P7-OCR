package com.jr_eagle_ocr.go4lunch.ui.mapview;

import android.location.Location;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.maps.model.LatLng;
import com.jr_eagle_ocr.go4lunch.R;
import com.jr_eagle_ocr.go4lunch.data.models.FoundRestaurant;
import com.jr_eagle_ocr.go4lunch.data.models.Restaurant;
import com.jr_eagle_ocr.go4lunch.data.repositories.LocationRepository;
import com.jr_eagle_ocr.go4lunch.data.repositories.RestaurantRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author jrigault
 */
public class MapViewViewModel extends ViewModel {
    public static final String NAME = "NAME";
    public static final String LATLNG = "LATLNG";
    public static final String DRAWABLE_RESOURCE = "DRAWABLE_RESOURCE";
    private final LocationRepository locationRepository;
    private final RestaurantRepository restaurantRepository;
    private final LiveData<Map<String, Restaurant>> allRestaurantsLiveData;
    private final LiveData<List<String>> chosenRestaurantIdsLiveData;
    private final MediatorLiveData<Map<String, Map<String, Object>>> markersDetailsMediatorLiveData = new MediatorLiveData<>();

    public MapViewViewModel(
            LocationRepository locationRepository,
            RestaurantRepository restaurantRepository
    ) {
        this.locationRepository = locationRepository;
        this.restaurantRepository = restaurantRepository;
        allRestaurantsLiveData = restaurantRepository.getAllRestaurants();
        chosenRestaurantIdsLiveData = restaurantRepository.getChosenRestaurantIds();

        markersDetailsMediatorLiveData.addSource(chosenRestaurantIdsLiveData, chosenRestaurantIds ->
                setMarkerDetails());
    }

    public void setLocation(Location lastKnownLocation) {
        locationRepository.setLocation(lastKnownLocation);
    }

    /**
     * Set the list of restaurant place ids found in MapViewFragment
     *
     * @param foundRestaurantIds the list of found restaurant place ids
     */
    public void setFoundRestaurantIds(List<String> foundRestaurantIds) {
        restaurantRepository.setFoundRestaurantIds(foundRestaurantIds);
        setMarkerDetails();
    }

    /**
     * Get restaurants from the listened "restaurants" Firestore collection
     *
     * @return a restaurant HashMap (placeId, restaurant (POJO)) in a livedata
     */
    public LiveData<Map<String, Restaurant>> getAllRestaurants() {
        return allRestaurantsLiveData;
    }

    /**
     * Add a foundRestaurant to the "restaurants" Firestore collection
     *
     * @param foundRestaurant the foundRestaurant to add
     */
    public void addFoundRestaurant(FoundRestaurant foundRestaurant) {
        restaurantRepository.addFoundRestaurant(foundRestaurant);
    }

    /**
     * Get the list of chosen restaurant ids from the listened "chosen_restaurants" Firestore collection
     *
     * @return a livedata of up-to-date list of chosen restaurant id
     */
    public LiveData<List<String>> getChosenRestaurantIds() {
        return chosenRestaurantIdsLiveData;
    }

    /**
     * @return
     */
    public LiveData<Map<String, Map<String, Object>>> getMarkerDetails() {
        return markersDetailsMediatorLiveData;
    }

    /**
     *
     */
    private void setMarkerDetails() {
        Map<String, Map<String, Object>> markerDetails = new HashMap<>();
        Map<String, Restaurant> allRestaurants = allRestaurantsLiveData.getValue();
        List<String> chosenRestaurantIds = chosenRestaurantIdsLiveData.getValue();
        List<String> foundRestaurantIds = restaurantRepository.getFoundRestaurantIds();
        if (foundRestaurantIds != null && chosenRestaurantIds != null
                && allRestaurants != null && !allRestaurants.isEmpty()) {
            for (String id : foundRestaurantIds) {
                Restaurant restaurant = allRestaurants.get(id);
                if (restaurant != null) {
                    Map<String, Object> details = new HashMap<>();
                    String name = restaurant.getName();
                    double lat = restaurant.getGeoPoint().getLatitude();
                    double lng = restaurant.getGeoPoint().getLongitude();
                    LatLng latLng = new LatLng(lat, lng);
                    boolean isChosen = chosenRestaurantIds.contains(id);
                    int drawableResource = isChosen ? R.drawable.green_marker : R.drawable.orange_marker;
                    details.put(NAME, name);
                    details.put(LATLNG, latLng);
                    details.put(DRAWABLE_RESOURCE, drawableResource);
                    markerDetails.put(id, details);
                }
            }
            markersDetailsMediatorLiveData.setValue(markerDetails);
        }
    }
}
