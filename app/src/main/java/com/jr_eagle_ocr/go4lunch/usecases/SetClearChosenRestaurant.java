package com.jr_eagle_ocr.go4lunch.usecases;

import static com.jr_eagle_ocr.go4lunch.repositories.RestaurantRepository.BYUSERS_FIELD;
import static com.jr_eagle_ocr.go4lunch.repositories.RestaurantRepository.CHOSENBY_COLLECTION_NAME;
import static com.jr_eagle_ocr.go4lunch.repositories.RestaurantRepository.USERID_FIELD;
import static com.jr_eagle_ocr.go4lunch.repositories.RestaurantRepository.USERNAME_FIELD;

import android.util.Log;

import androidx.lifecycle.LiveData;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.jr_eagle_ocr.go4lunch.model.ChosenRestaurant;
import com.jr_eagle_ocr.go4lunch.model.Restaurant;
import com.jr_eagle_ocr.go4lunch.model.User;
import com.jr_eagle_ocr.go4lunch.repositories.RestaurantRepository;
import com.jr_eagle_ocr.go4lunch.repositories.UserRepository;

import java.util.HashMap;
import java.util.Map;

/**
 * @author jrigault
 */
public final class SetClearChosenRestaurant extends UseCase {
    private final LiveData<User> currentUserLiveData;
    private final LiveData<Map<String, Restaurant>> foundRestaurantsLiveData;
    private final CollectionReference chosenRestaurantsCollection;

    public SetClearChosenRestaurant(
            UserRepository userRepository,
            RestaurantRepository restaurantRepository
    ) {
        currentUserLiveData = userRepository.getCurrentUser();
        foundRestaurantsLiveData = restaurantRepository.getFoundRestaurants();
        chosenRestaurantsCollection = restaurantRepository.getChosenRestaurantsCollection();
    }

    /**
     * Set current user document in Collection "chosen_by"
     * of given restaurant document (itself in Collection "chosen_restaurants")
     *
     * @param placeId the given restaurant id
     */
    public void setChosenRestaurant(String placeId) {
        User user = currentUserLiveData.getValue();
        if (user != null && foundRestaurantsLiveData.getValue() != null) {
            String uid = user.getUid();
            String userName = user.getUserName();
            Restaurant restaurant = foundRestaurantsLiveData.getValue().get(placeId);
            if (restaurant != null) {
                String placeName = restaurant.getName();
                String placeAddress = restaurant.getAddress();
                ChosenRestaurant chosenRestaurant = new ChosenRestaurant(
                        placeId, placeName, placeAddress,
                        String.valueOf(System.currentTimeMillis()));

                chosenRestaurantsCollection.document(placeId)
                        .get()
                        .continueWith(task -> {
                            DocumentSnapshot document = task.getResult();
                            if (!document.exists()) {
                                document.getReference().set(chosenRestaurant);
                            }
                            return document.getReference();
                        })
                        .continueWith(task -> {
                            Map<String, String> userData = new HashMap<>();
                            userData.put(USERID_FIELD, uid);
                            userData.put(USERNAME_FIELD, userName);
                            task.getResult().update(BYUSERS_FIELD, FieldValue.arrayUnion(uid));
                            task.getResult().collection(CHOSENBY_COLLECTION_NAME).document(uid)
                                    .set(userData);
                            return null;
                        })
                        .addOnSuccessListener(o -> {
                            Log.d(TAG, "setChosenRestaurant: " + placeId);
                        });
            }
        }
    }

    /**
     * Delete current user document in Collection "chosen_by"
     * of given restaurant document (itself in Collection "chosen_restaurants")
     *
     * @param placeId placeId the given restaurant id
     */
    public void clearChosenRestaurant(String placeId) {
        User user = currentUserLiveData.getValue();
        if (user != null) {
            String uid = user.getUid();
            chosenRestaurantsCollection.document(placeId)
                    .collection(CHOSENBY_COLLECTION_NAME).document(uid)
                    .get()
                    .continueWith(task -> {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            document.getReference().delete();
                            chosenRestaurantsCollection.document(placeId)
                                    .update(BYUSERS_FIELD, FieldValue.arrayRemove(uid));
                        }
                        return document.getReference();
                    })
                    .addOnSuccessListener(documentReference -> {
                        Log.d(TAG, "clearChosenRestaurant: " + placeId);
                    });
        }
    }
}
