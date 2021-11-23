package com.jr_eagle_ocr.go4lunch.ui.listview;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.DocumentSnapshot;
import com.jr_eagle_ocr.go4lunch.repositories.RestaurantRepository;
import com.jr_eagle_ocr.go4lunch.ui.adapters.RestaurantViewSate;
import com.jr_eagle_ocr.go4lunch.usecases.GetRestaurantViewStates;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListViewViewModel extends ViewModel {
    private final RestaurantRepository restaurantRepository;
    private final LiveData<List<String>> chosenRestaurantIdsLiveData;

    private final MediatorLiveData<List<RestaurantViewSate>> allRestaurantViewStatesMediatorLiveData = new MediatorLiveData<>();

    public ListViewViewModel(
            RestaurantRepository restaurantRepository,
            GetRestaurantViewStates getRestaurantViewStates
    ) {
        this.restaurantRepository = restaurantRepository;
        chosenRestaurantIdsLiveData = restaurantRepository.getChosenRestaurantIds();

        allRestaurantViewStatesMediatorLiveData.addSource(chosenRestaurantIdsLiveData, chosenRestaurantIds ->
                buildAndSetAllRestaurantViewStates(getRestaurantViewStates));
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
    private void buildAndSetAllRestaurantViewStates(GetRestaurantViewStates getRestaurantViewStates) {
        restaurantRepository.getChosenRestaurantsCollection()
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<RestaurantViewSate> restaurantViewSates; //To be produced to valorize livedata
                    Map<String, Integer> restaurantByUsersCountMap = new HashMap<>();
                    List<DocumentSnapshot> documents = queryDocumentSnapshots.getDocuments();
                    for (DocumentSnapshot document : documents) {
                        String restaurantId = document.getId();
                        List<String> byUserIds = restaurantRepository.getByUserIds(document);
                        Integer byUsersCount = byUserIds.size();
                        restaurantByUsersCountMap.put(restaurantId, byUsersCount);
                    }
                    restaurantViewSates = getRestaurantViewStates.getRestaurantViewStates(restaurantByUsersCountMap);
                    allRestaurantViewStatesMediatorLiveData.setValue(restaurantViewSates);
                });
    }
}
