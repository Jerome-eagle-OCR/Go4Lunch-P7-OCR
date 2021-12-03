package com.jr_eagle_ocr.go4lunch.data.repositories.usecases;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.jr_eagle_ocr.go4lunch.data.models.ChosenRestaurant;
import com.jr_eagle_ocr.go4lunch.data.models.User;
import com.jr_eagle_ocr.go4lunch.data.repositories.RestaurantRepository;
import com.jr_eagle_ocr.go4lunch.data.repositories.UserRepository;
import com.jr_eagle_ocr.go4lunch.data.repositories.usecases.parent.UseCase;

import java.util.List;
import java.util.Map;

/**
 * @author jrigault
 */
public final class GetCurrentUserChosenRestaurantId extends UseCase {
    private final LiveData<User> currentUserLiveData;
    private final LiveData<Map<ChosenRestaurant, List<String>>> chosenRestaurantByUserIdsMapLiveData;
    private final MediatorLiveData<String> currentUserChosenRestaurantIdMediatorLiveData = new MediatorLiveData<>();

    public GetCurrentUserChosenRestaurantId(
            UserRepository userRepository,
            RestaurantRepository restaurantRepository
    ) {
        currentUserLiveData = userRepository.getCurrentUser();
        chosenRestaurantByUserIdsMapLiveData = restaurantRepository.getChosenRestaurantByUserIdsMap();

        currentUserChosenRestaurantIdMediatorLiveData.addSource(currentUserLiveData, currentUser ->
                getCurrentUserChosenRestaurantId());
        currentUserChosenRestaurantIdMediatorLiveData.addSource(chosenRestaurantByUserIdsMapLiveData, chosenRestaurantByUserIdsMap ->
                getCurrentUserChosenRestaurantId());
    }

    /**
     * Get authenticated user chosen restaurant id
     *
     * @return the place id of the chosen restaurant
     */
    public LiveData<String> getCurrentUserChosenRestaurantId() {
        String currentUserChosenRestaurantId = "";
        User currentUser = currentUserLiveData.getValue();
        if (currentUser != null) {
            String currentUserId = currentUser.getUid();
            Map<ChosenRestaurant, List<String>> chosenRestaurantByUserIdsMap = chosenRestaurantByUserIdsMapLiveData.getValue();
            if (chosenRestaurantByUserIdsMap != null) {
                for (Map.Entry<ChosenRestaurant, List<String>> entry : chosenRestaurantByUserIdsMap.entrySet()) {
                    List<String> chosenByUserIds = entry.getValue();
                    if (chosenByUserIds.contains(currentUserId)) {
                        ChosenRestaurant chosenRestaurant = entry.getKey();
                        currentUserChosenRestaurantId = chosenRestaurant.getPlaceId();
                        break;
                    }
                }
            }
        } else {
            currentUserChosenRestaurantId = null;
        }
        currentUserChosenRestaurantIdMediatorLiveData.setValue(currentUserChosenRestaurantId);

        return currentUserChosenRestaurantIdMediatorLiveData;
    }
}