package com.jr_eagle_ocr.go4lunch.ui.listview;

import static com.jr_eagle_ocr.go4lunch.ui.MainViewModel.LISTVIEW;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.jr_eagle_ocr.go4lunch.data.models.ChosenRestaurant;
import com.jr_eagle_ocr.go4lunch.data.models.User;
import com.jr_eagle_ocr.go4lunch.data.repositories.RestaurantRepository;
import com.jr_eagle_ocr.go4lunch.data.repositories.UserRepository;
import com.jr_eagle_ocr.go4lunch.data.usecases.GetRestaurantViewStates;
import com.jr_eagle_ocr.go4lunch.ui.AutocompleteRestaurantViewState;
import com.jr_eagle_ocr.go4lunch.ui.MainViewModel;
import com.jr_eagle_ocr.go4lunch.ui.adapters.RestaurantViewSate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ListViewViewModel extends ViewModel {
    private final String TAG = getClass().getSimpleName();
    private final LiveData<User> currentUserLiveData;
    private final LiveData<Map<ChosenRestaurant, List<String>>> chosenRestaurantByUserIdsMapLiveData;
    private LiveData<List<String>> filteredRestaurantIdsLivedata;
    private LiveData<AutocompleteRestaurantViewState> selectedItemLiveData;
    private final MediatorLiveData<List<RestaurantViewSate>> allRestaurantViewStatesMediatorLiveData = new MediatorLiveData<>();
    private final GetRestaurantViewStates getRestaurantViewStates;

    public ListViewViewModel(
            @NonNull UserRepository userRepository,
            @NonNull RestaurantRepository restaurantRepository,
            @NonNull GetRestaurantViewStates getRestaurantViewStates
    ) {
        currentUserLiveData = userRepository.getCurrentUser();
        chosenRestaurantByUserIdsMapLiveData = restaurantRepository.getChosenRestaurantByUserIdsMap();
        this.getRestaurantViewStates = getRestaurantViewStates;

        allRestaurantViewStatesMediatorLiveData.addSource(currentUserLiveData, currentUser ->
                buildAndSetAllRestaurantViewStates());
        allRestaurantViewStatesMediatorLiveData.addSource(chosenRestaurantByUserIdsMapLiveData, restaurantChosenByUserIdsMap ->
                buildAndSetAllRestaurantViewStates());
    }

    /**
     * Observed by activity to get up-to-date all restaurant view states to display in recyclerview
     */
    public LiveData<List<RestaurantViewSate>> getRestaurantViewStates() {
        return allRestaurantViewStatesMediatorLiveData;
    }

    /**
     * Build and set restaurants to display in recyclerview UserAdapter
     */
    private void buildAndSetAllRestaurantViewStates() {
        List<RestaurantViewSate> restaurantViewSates = new ArrayList<>(); //To be produced to valorize livedata
        User currentUser = currentUserLiveData.getValue();
        if (currentUser != null) {
            Map<String, Integer> restaurantChosenByUsersCountMap = new HashMap<>();
            Map<ChosenRestaurant, List<String>> restaurantChosenByUserIdsMap = chosenRestaurantByUserIdsMapLiveData.getValue();
            if (restaurantChosenByUserIdsMap != null) {
                for (Map.Entry<ChosenRestaurant, List<String>> entry : restaurantChosenByUserIdsMap.entrySet()) {
                    String placeId = entry.getKey().getPlaceId();
                    List<String> chosenByUserIds = entry.getValue();
                    int chosenByUserCount = chosenByUserIds.size();
                    // Current user choice must not be count
                    String currentUserUid = currentUser.getUid();
                    if (chosenByUserIds.contains(currentUserUid)) chosenByUserCount--;

                    restaurantChosenByUsersCountMap.put(placeId, chosenByUserCount);
                }
            }
            // Get the unfiltered list
            List<RestaurantViewSate> unfilteredRestaurantViewSates = getRestaurantViewStates.getRestaurantViewStates(restaurantChosenByUsersCountMap);

            // Filter list depending on search results and selection
            List<String> filteredIds = filteredRestaurantIdsLivedata.getValue();
            AutocompleteRestaurantViewState selectedItem = selectedItemLiveData.getValue();
            for (RestaurantViewSate restaurantViewSate : unfilteredRestaurantViewSates) {
                String placeId = restaurantViewSate.getId();
                // If filter list not null we keep only place ids in list
                // If selected item is not null we keep only this id
                if ((selectedItem == null || placeId.equals(selectedItem.getPlaceId()))
                        && (filteredIds == null || filteredIds.contains(placeId))) {
                    restaurantViewSates.add(restaurantViewSate);
                    if (selectedItem != null && placeId.equals(selectedItem.getPlaceId())) break;
                }
            }
        }
        allRestaurantViewStatesMediatorLiveData.setValue(restaurantViewSates);
        Log.d(TAG, "buildAndSetAllRestaurantViewStates: " + restaurantViewSates.size() + "restaurants");
    }

    /**
     * Set current MainViewModel instance and,
     * set current fragment (listviewfragment)
     * valorize livedatas
     *
     * @param mainViewModel the current MainViewModel instance
     */
    public void setMainViewModel(MainViewModel mainViewModel) {
        // Set current displayed fragment (mapview) in mainviewmodel
        mainViewModel.setCurrentFragment(LISTVIEW);
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
        // Add filtered restaurant ids livedata as source to trigger restaurants view states update
        allRestaurantViewStatesMediatorLiveData.addSource(filteredRestaurantIdsLivedata, filteredRestaurantIds ->
                buildAndSetAllRestaurantViewStates());
        // Valorize search selected item livedata
        selectedItemLiveData = mainViewModel.getSelectedItem();
        // Add selected item livedata as source to trigger restaurants view states update
        allRestaurantViewStatesMediatorLiveData.addSource(selectedItemLiveData, selectedItem ->
                buildAndSetAllRestaurantViewStates());
    }

    @VisibleForTesting
    public LiveData<List<String>> getFilteredRestaurantIds() {
        return filteredRestaurantIdsLivedata;
    }
}
