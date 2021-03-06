package com.jr_eagle_ocr.go4lunch.ui.workmates;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import com.jr_eagle_ocr.go4lunch.data.models.ChosenRestaurant;
import com.jr_eagle_ocr.go4lunch.data.models.User;
import com.jr_eagle_ocr.go4lunch.data.repositories.RestaurantRepository;
import com.jr_eagle_ocr.go4lunch.data.repositories.UserRepository;
import com.jr_eagle_ocr.go4lunch.data.usecases.GetUserViewStates;
import com.jr_eagle_ocr.go4lunch.ui.adapters.UserViewState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author jrigault
 */
public class WorkmatesViewModel extends ViewModel {
    private final String TAG = getClass().getSimpleName();
    private final LiveData<User> currentUserLiveData;
    private final LiveData<Map<String, User>> allLoggedUsersLiveData;
    private final LiveData<Map<ChosenRestaurant, List<String>>> restaurantChosenByUserIdsMapLiveData;
    private final GetUserViewStates getUserViewStates;
    private final MediatorLiveData<List<UserViewState>> allUserViewStatesMediatorLiveData = new MediatorLiveData<>();

    public WorkmatesViewModel(
            @NonNull UserRepository userRepository,
            @NonNull RestaurantRepository restaurantRepository,
            @NonNull GetUserViewStates getUserViewStates
    ) {
        currentUserLiveData = userRepository.getCurrentUser();
        allLoggedUsersLiveData = userRepository.getAllLoggedUsers();
        restaurantChosenByUserIdsMapLiveData = restaurantRepository.getChosenRestaurantByUserIdsMap();
        this.getUserViewStates = getUserViewStates;

        allUserViewStatesMediatorLiveData.addSource(allLoggedUsersLiveData, allUsers ->
                buildAndSetUserViewStates());
        allUserViewStatesMediatorLiveData.addSource(restaurantChosenByUserIdsMapLiveData, restaurantChosenByUserIdsMap ->
                buildAndSetUserViewStates());
    }

    /**
     * Observed by activity to get up-to-date all user view states to display in recyclerview
     */
    public LiveData<List<UserViewState>> getUserViewStates() {
        return allUserViewStatesMediatorLiveData;
    }

    /**
     * Build and set all users to display in recyclerview UserAdapter
     */
    private void buildAndSetUserViewStates() {
        List<UserViewState> userViewStates = new ArrayList<>(); //To be produced to valorize livedata
        User currentUser = currentUserLiveData.getValue();
        Map<String, User> allLoggedUsers = allLoggedUsersLiveData.getValue();
        Map<ChosenRestaurant, List<String>> restaurantChosenByUserIdsMap = restaurantChosenByUserIdsMapLiveData.getValue();
        if (currentUser != null && allLoggedUsers != null && !allLoggedUsers.isEmpty()) {
            Map<String, Pair<String, String>> userChosenRestaurantMap = new HashMap<>(); //To be valorized to get userviewstates
            if (restaurantChosenByUserIdsMap != null) {
                for (Map.Entry<ChosenRestaurant, List<String>> entry : restaurantChosenByUserIdsMap.entrySet()) {
                    String placeId = entry.getKey().getPlaceId();
                    String placeName = entry.getKey().getPlaceName();
                    List<String> chosenByUserIds = entry.getValue();
                    for (int i = 0; i < chosenByUserIds.size(); i++) {
                        String uid = chosenByUserIds.get(i);
                        if (!uid.equals(currentUser.getUid())) {
                            userChosenRestaurantMap.put(uid, new Pair<>(placeId, placeName));
                        }
                    }
                }
            }
            userViewStates = getUserViewStates.getUserViewStates(null,
                    currentUser, allLoggedUsers, userChosenRestaurantMap);
        }
        allUserViewStatesMediatorLiveData.setValue(userViewStates);
        Log.d(TAG, "buildAndSetAllUserViewStates: " + userViewStates.size() + " workmates");
    }
}