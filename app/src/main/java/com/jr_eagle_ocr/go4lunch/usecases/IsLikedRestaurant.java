package com.jr_eagle_ocr.go4lunch.usecases;

import static com.jr_eagle_ocr.go4lunch.repositories.RestaurantRepository.LIKEDBY_COLLECTION_NAME;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.ListenerRegistration;
import com.jr_eagle_ocr.go4lunch.model.User;
import com.jr_eagle_ocr.go4lunch.repositories.RestaurantRepository;
import com.jr_eagle_ocr.go4lunch.repositories.UserRepository;

public class IsLikedRestaurant extends UseCase {
    private final LiveData<User> currentUserLiveData;
    private final CollectionReference likedRestaurantsCollection;
    private ListenerRegistration listenerRegistration;
    private final MutableLiveData<Boolean> isLikedRestaurantMutableLiveData = new MutableLiveData<>();

    public IsLikedRestaurant(
            UserRepository userRepository,
            RestaurantRepository restaurantRepository
    ) {
        currentUserLiveData = userRepository.getCurrentUser();
        likedRestaurantsCollection = restaurantRepository.getLikedRestaurantsCollection();
    }


    /**
     * Get if restaurant set is liked by current user
     *
     * @return a boolean in a livedata
     */
    public LiveData<Boolean> isLikedRestaurant() {
        return isLikedRestaurantMutableLiveData;
    }

    /**
     * Add a Firestore listener registration to lively determine
     * if restaurant set is liked by current user
     *
     * @param restaurantId the possibly liked restaurant id
     */
    public void addListenerRegistration(String restaurantId) {
        // clean the previous listener in case something went wrong on removing it in
        // the RestaurantDetailViewModel onCleared()
        if (listenerRegistration != null) listenerRegistration.remove();
        if (restaurantId != null && currentUserLiveData.getValue() != null) {
            String uid = currentUserLiveData.getValue().getUid();
            listenerRegistration = likedRestaurantsCollection
                    .document(restaurantId)
                    .collection(LIKEDBY_COLLECTION_NAME)
                    .document(uid)
                    .addSnapshotListener((value, error) -> {
                        if (value != null) {
                            isLikedRestaurantMutableLiveData.setValue(value.exists());
                        } else if (error != null) {
                            isLikedRestaurantMutableLiveData.setValue(null);
                            Log.e(TAG, "setListenerRegistration: ", error);
                        }
                    });
        }
    }

    public void removeListenerRegistration() {
        listenerRegistration.remove();
        isLikedRestaurantMutableLiveData.setValue(false);
    }
}
