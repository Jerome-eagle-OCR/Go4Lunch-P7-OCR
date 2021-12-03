package com.jr_eagle_ocr.go4lunch.ui.listview;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import com.jr_eagle_ocr.go4lunch.data.models.ChosenRestaurant;
import com.jr_eagle_ocr.go4lunch.data.models.User;
import com.jr_eagle_ocr.go4lunch.data.repositories.LocationRepository;
import com.jr_eagle_ocr.go4lunch.data.repositories.RestaurantRepository;
import com.jr_eagle_ocr.go4lunch.data.repositories.UserRepository;
import com.jr_eagle_ocr.go4lunch.data.repositories.usecases.GetRestaurantViewStates;
import com.jr_eagle_ocr.go4lunch.ui.adapters.RestaurantViewSate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListViewViewModel extends ViewModel {
    private final String TAG = getClass().getSimpleName();
    private final LiveData<User> currentUserLiveData;
    private final LiveData<Map<ChosenRestaurant, List<String>>> restaurantChosenByUserIdsMapLiveData;
    private final MediatorLiveData<List<RestaurantViewSate>> allRestaurantViewStatesMediatorLiveData = new MediatorLiveData<>();
    private final GetRestaurantViewStates getRestaurantViewStates;

    public ListViewViewModel(
            LocationRepository locationRepository,
            UserRepository userRepository,
            RestaurantRepository restaurantRepository
    ) {
        currentUserLiveData = userRepository.getCurrentUser();
        restaurantChosenByUserIdsMapLiveData = restaurantRepository.getChosenRestaurantByUserIdsMap();
        getRestaurantViewStates = new GetRestaurantViewStates(locationRepository, restaurantRepository);

        allRestaurantViewStatesMediatorLiveData.addSource(currentUserLiveData, currentUser ->
                buildAndSetAllRestaurantViewStates());
        allRestaurantViewStatesMediatorLiveData.addSource(restaurantChosenByUserIdsMapLiveData, restaurantChosenByUserIdsMap ->
                buildAndSetAllRestaurantViewStates());
    }

    /**
     * Observed by activity to get up-to-date all restaurant view states to display in recyclerview
     */
    public LiveData<List<RestaurantViewSate>> getAllRestaurantViewStates() {
        return allRestaurantViewStatesMediatorLiveData;
    }

    /**
     * Build and set all restaurants to display in recyclerview UserAdapter
     */
    private void buildAndSetAllRestaurantViewStates() {
        List<RestaurantViewSate> restaurantViewSates = new ArrayList<>(); //To be produced to valorize livedata
        User currentUser = currentUserLiveData.getValue();
        if (currentUser != null) {
            Map<String, Integer> restaurantChosenByUsersCountMap = new HashMap<>();
            Map<ChosenRestaurant, List<String>> restaurantChosenByUserIdsMap = restaurantChosenByUserIdsMapLiveData.getValue();
            if (restaurantChosenByUserIdsMap != null) {
                for (Map.Entry<ChosenRestaurant, List<String>> entry : restaurantChosenByUserIdsMap.entrySet()) {
                    String placeId = entry.getKey().getPlaceId();
                    List<String> chosenByUserIds = entry.getValue();
                    int chosenByUserCount = chosenByUserIds.size();
                    String currentUserUid = currentUser.getUid();

                    // Current user choice must not be count
                    if (chosenByUserIds.contains(currentUserUid)) chosenByUserCount--;

                    restaurantChosenByUsersCountMap.put(placeId, chosenByUserCount);
                }
            }
            restaurantViewSates = getRestaurantViewStates.getRestaurantViewStates(restaurantChosenByUsersCountMap);
        }
        allRestaurantViewStatesMediatorLiveData.setValue(restaurantViewSates);
        Log.d(TAG, "buildAndSetAllRestaurantViewStates: " + restaurantViewSates.size() + "restaurants");
    }
}
