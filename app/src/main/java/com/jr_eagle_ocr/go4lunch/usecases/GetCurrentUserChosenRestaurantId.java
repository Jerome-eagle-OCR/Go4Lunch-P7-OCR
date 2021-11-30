package com.jr_eagle_ocr.go4lunch.usecases;

import static com.jr_eagle_ocr.go4lunch.repositories.RestaurantRepository.BYUSERS_FIELD;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.jr_eagle_ocr.go4lunch.model.User;
import com.jr_eagle_ocr.go4lunch.repositories.RestaurantRepository;
import com.jr_eagle_ocr.go4lunch.repositories.UserRepository;
import com.jr_eagle_ocr.go4lunch.usecases.parent.UseCase;

import java.util.List;

/**
 * @author jrigault
 */
public final class GetCurrentUserChosenRestaurantId extends UseCase {
    private final LiveData<User> currentUserLiveData;
    private final CollectionReference chosenRestaurantsCollection;
    private final MediatorLiveData<String> currentUserChosenRestaurantIdMediatorLiveData;

    public GetCurrentUserChosenRestaurantId(
            UserRepository userRepository,
            RestaurantRepository restaurantRepository
    ) {
        currentUserLiveData = userRepository.getCurrentUser();
        chosenRestaurantsCollection = restaurantRepository.getChosenRestaurantsCollection();
        LiveData<List<String>> chosenRestaurantIdsLiveData = restaurantRepository.getChosenRestaurantIds();

        currentUserChosenRestaurantIdMediatorLiveData = new MediatorLiveData<>();
        currentUserChosenRestaurantIdMediatorLiveData.addSource(currentUserLiveData, currentUser ->
                setCurrentUserChosenRestaurantId());
        currentUserChosenRestaurantIdMediatorLiveData.addSource(chosenRestaurantIdsLiveData, chosenRestaurantIds ->
                setCurrentUserChosenRestaurantId());
    }

    /**
     * Get authenticated user chosen restaurant id
     *
     * @return the place id of the chosen restaurant
     */
    public LiveData<String> getCurrentUserChosenRestaurantId() {
        return currentUserChosenRestaurantIdMediatorLiveData;
    }

    /**
     * Set authenticated user chosen restaurant id from Firestore query
     */
    private void setCurrentUserChosenRestaurantId() {
        User user = currentUserLiveData.getValue();
        String uid = user != null ? user.getUid() : "";
        chosenRestaurantsCollection
                .whereArrayContains(BYUSERS_FIELD, uid)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<DocumentSnapshot> documents = queryDocumentSnapshots.getDocuments();
                    if (documents.size() == 1) {
                        currentUserChosenRestaurantIdMediatorLiveData.setValue(documents.get(0).getId());
                    } else {
                        currentUserChosenRestaurantIdMediatorLiveData.setValue(null);
                    }
                });
    }
}
