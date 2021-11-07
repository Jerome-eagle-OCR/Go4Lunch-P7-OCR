package com.jr_eagle_ocr.go4lunch.usecases;

import static com.jr_eagle_ocr.go4lunch.repositories.RestaurantRepository.BYUSERS_FIELD;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.jr_eagle_ocr.go4lunch.repositories.RestaurantRepository;
import com.jr_eagle_ocr.go4lunch.repositories.UserRepository;

import java.util.List;

public class GetCurrentUserChosenRestaurantId {

    private final LiveData<FirebaseUser> currentFirebaseUserLiveData;
    private final CollectionReference chosenRestaurantsCollection;

    private final MediatorLiveData<String> currentUserChosenRestaurantIdMediatorLiveData;

    public GetCurrentUserChosenRestaurantId(
            UserRepository userRepository,
            RestaurantRepository restaurantRepository
    ) {
        currentFirebaseUserLiveData = userRepository.getCurrentFirebaseUser();
        chosenRestaurantsCollection = restaurantRepository.getChosenRestaurantsCollection();
        LiveData<List<String>> chosenRestaurantIdsLiveData = restaurantRepository.getChosenRestaurantIds();

        currentUserChosenRestaurantIdMediatorLiveData = new MediatorLiveData<>();
        currentUserChosenRestaurantIdMediatorLiveData.addSource(currentFirebaseUserLiveData, firebaseUser ->
                getCurrentUserChosenRestaurantId());
        currentUserChosenRestaurantIdMediatorLiveData.addSource(chosenRestaurantIdsLiveData, chosenRestaurantIds ->
                getCurrentUserChosenRestaurantId());
    }

    /**
     * Get authenticated user chosen restaurant id
     *
     * @return the place id of the chosen restaurant
     */
    public LiveData<String> getCurrentUserChosenRestaurantId() {
        FirebaseUser firebaseUser = currentFirebaseUserLiveData.getValue();
        String uid = firebaseUser != null ? firebaseUser.getUid() : "";
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
        return currentUserChosenRestaurantIdMediatorLiveData;
    }
}
