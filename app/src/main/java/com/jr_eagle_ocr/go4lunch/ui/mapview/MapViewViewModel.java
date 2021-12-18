package com.jr_eagle_ocr.go4lunch.ui.mapview;

import android.location.Location;

import androidx.annotation.VisibleForTesting;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.maps.model.LatLng;
import com.jr_eagle_ocr.go4lunch.R;
import com.jr_eagle_ocr.go4lunch.data.models.FoundRestaurant;
import com.jr_eagle_ocr.go4lunch.data.models.Restaurant;
import com.jr_eagle_ocr.go4lunch.data.repositories.LocationRepository;
import com.jr_eagle_ocr.go4lunch.data.repositories.RestaurantRepository;
import com.jr_eagle_ocr.go4lunch.ui.AutocompleteRestaurantViewState;
import com.jr_eagle_ocr.go4lunch.ui.MainViewModel;

import java.util.ArrayList;
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
    private LiveData<List<String>> filteredRestaurantIdsLivedata;
    private final MediatorLiveData<Map<String, Map<String, Object>>> markersDetailsMediatorLiveData = new MediatorLiveData<>();
    private LiveData<AutocompleteRestaurantViewState> selectedItemLiveData;

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
        locationRepository.setMapLocation(lastKnownLocation);
    }

    public void setLocationPermissionGranted(boolean locationPermissionGranted) {
        locationRepository.setLocationPermissionGranted(locationPermissionGranted);
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

    public LiveData<AutocompleteRestaurantViewState> getSelectedItem() {
        return selectedItemLiveData;
    }

    /**
     * Get marker details
     *
     * @return a hashmap (placeId, hashmap(detailKey, detailValue)) in a livedata
     */
    public LiveData<Map<String, Map<String, Object>>> getMarkerDetails() {
        return markersDetailsMediatorLiveData;
    }

    /**
     * Set marker details depending on chosen and search filtered restaurants
     */
    private void setMarkerDetails() {
        Map<String, Map<String, Object>> markerDetails = new HashMap<>();
        Map<String, Restaurant> allRestaurants = allRestaurantsLiveData.getValue();
        List<String> chosenRestaurantIds = chosenRestaurantIdsLiveData.getValue();
        List<String> foundRestaurantIds = restaurantRepository.getFoundRestaurantIds();
        List<String> filteredRestaurantIds = filteredRestaurantIdsLivedata.getValue();
        if (foundRestaurantIds != null && chosenRestaurantIds != null
                && allRestaurants != null && !allRestaurants.isEmpty()) {
            for (String id : foundRestaurantIds) {
                boolean isToBeSet = filteredRestaurantIds == null || filteredRestaurantIds.contains(id);
                if (isToBeSet) {
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
            }
            markersDetailsMediatorLiveData.setValue(markerDetails);
        }
    }

    /**
     * Set current MainViewModel instance and,
     * set current fragment (mapviewfragment)
     * valorize livedatas
     *
     * @param mainViewModel the current MainViewModel instance
     */
    public void setMainViewModel(MainViewModel mainViewModel) {
        // Set current displayed fragment (mapview) in mainviewmodel
        mainViewModel.setCurrentFragment(MainViewModel.MAPVIEW);
        // Valorize filtered restaurant ids livedata with transformation to map autocomplete restaurants to a list of ids
        filteredRestaurantIdsLivedata = Transformations.map(mainViewModel.getAutocompleteRestaurantArray(), viewStates -> {
            List<String> restaurantIds = null;
            if (viewStates != null) {
                restaurantIds = new ArrayList<>();
                for (AutocompleteRestaurantViewState viewState : viewStates) {
                    restaurantIds.add(viewState.getPlaceId());
                }
            }
            return restaurantIds;
        });
        // Add filtered restaurant ids livedata as source to trigger marker details update
        markersDetailsMediatorLiveData.addSource(filteredRestaurantIdsLivedata, restaurantIdsFilter ->
                setMarkerDetails());
        // Valorize search selected item livedata
        selectedItemLiveData = mainViewModel.getSelectedItem();
    }

    @VisibleForTesting
    public LiveData<List<String>> getFilteredRestaurantIds() {
        return filteredRestaurantIdsLivedata;
    }
}
