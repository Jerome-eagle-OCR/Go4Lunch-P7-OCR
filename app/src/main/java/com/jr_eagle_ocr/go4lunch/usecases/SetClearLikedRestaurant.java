package com.jr_eagle_ocr.go4lunch.usecases;

import static com.jr_eagle_ocr.go4lunch.repositories.RestaurantRepository.BYUSERS_FIELD;
import static com.jr_eagle_ocr.go4lunch.repositories.RestaurantRepository.LIKEDBY_COLLECTION_NAME;
import static com.jr_eagle_ocr.go4lunch.repositories.RestaurantRepository.PLACEID_FIELD;
import static com.jr_eagle_ocr.go4lunch.repositories.RestaurantRepository.USERID_FIELD;

import androidx.lifecycle.LiveData;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.jr_eagle_ocr.go4lunch.model.User;
import com.jr_eagle_ocr.go4lunch.repositories.RestaurantRepository;
import com.jr_eagle_ocr.go4lunch.repositories.UserRepository;
import com.jr_eagle_ocr.go4lunch.usecases.parent.UseCase;

import java.util.HashMap;
import java.util.Map;

/**
 * @author jrigault
 */
public final class SetClearLikedRestaurant extends UseCase {
    private final LiveData<User> currentUserLiveData;
    private final CollectionReference likedRestaurantsCollection;

    public SetClearLikedRestaurant(
            UserRepository userRepository,
            RestaurantRepository restaurantRepository
    ) {
        currentUserLiveData = userRepository.getCurrentUser();
        likedRestaurantsCollection = restaurantRepository.getLikedRestaurantsCollection();
    }

    /**
     * Set current user document in Collection "liked_by"
     * of given restaurant document (itself in Collection "liked_restaurants")
     *
     * @param placeId the given restaurant id
     */
    public void setLikedRestaurant(String placeId) {
        User user = currentUserLiveData.getValue();
        if (user != null) {
            String uid = user.getUid();
            Map<String, String> userData = new HashMap<>();
            userData.put(USERID_FIELD, uid);
            likedRestaurantsCollection.document(placeId)
                    .get()
                    .continueWith(task -> {
                        DocumentSnapshot document = task.getResult();
                        if (!document.exists()) {
                            Map<String, String> placeData = new HashMap<>();
                            placeData.put(PLACEID_FIELD, placeId);
                            placeData.put(BYUSERS_FIELD, uid);
                            document.getReference().set(placeData);
                        }
                        return document.getReference();
                    })
                    .continueWith(task -> {
                        task.getResult()
                                .collection(LIKEDBY_COLLECTION_NAME)
                                .document(uid)
                                .set(userData);
                        task.getResult().update(BYUSERS_FIELD, FieldValue.arrayUnion(uid));
                        return null;
                    });
        }
    }

    /**
     * Delete current user document in Collection "liked_by"
     * of given restaurant document (itself in Collection "liked_restaurants")
     *
     * @param placeId the given restaurant id
     */
    public void clearLikedRestaurant(String placeId) {
        User user = currentUserLiveData.getValue();
        if (user != null) {
            String uid = user.getUid();
            likedRestaurantsCollection.document(placeId)
                    .collection(LIKEDBY_COLLECTION_NAME).document(uid)
                    .get()
                    .continueWith(task -> {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            document.getReference().delete();
                            likedRestaurantsCollection.document(placeId)
                                    .update(BYUSERS_FIELD, FieldValue.arrayRemove(uid));
                        }
                        return null;
                    });
        }
    }
}
