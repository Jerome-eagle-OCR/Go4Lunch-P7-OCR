package com.jr_eagle_ocr.go4lunch.usecases;

import static com.jr_eagle_ocr.go4lunch.repositories.RestaurantRepository.BYUSERS_FIELD;
import static com.jr_eagle_ocr.go4lunch.repositories.RestaurantRepository.CHOSENBY_COLLECTION_NAME;
import static com.jr_eagle_ocr.go4lunch.repositories.RestaurantRepository.USERID_FIELD;
import static com.jr_eagle_ocr.go4lunch.repositories.RestaurantRepository.USERNAME_FIELD;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.jr_eagle_ocr.go4lunch.model.ChosenRestaurant;
import com.jr_eagle_ocr.go4lunch.model.Restaurant;
import com.jr_eagle_ocr.go4lunch.model.User;
import com.jr_eagle_ocr.go4lunch.repositories.RestaurantRepository;

import java.util.HashMap;
import java.util.Map;

public class SetClearChosenRestaurant {

    private static final String TAG = SetClearChosenRestaurant.class.getSimpleName();
    private final LiveData<User> currentUserLiveData;
    private final LiveData<Map<String, Restaurant>> foundRestaurantsLiveData;
    private final CollectionReference chosenRestaurantsCollection;


    public SetClearChosenRestaurant(
            RestaurantRepository restaurantRepository,
            GetCurrentUser getCurrentUser
    ) {
        currentUserLiveData = getCurrentUser.getCurrentUser();
        foundRestaurantsLiveData = restaurantRepository.getFoundRestaurants();
        chosenRestaurantsCollection = restaurantRepository.getChosenRestaurantsCollection();
    }

    /**
     * Set current user document in Collection "chosen_by"
     * of given restaurant document (itself in Collection "chosen_restaurants")
     *
     * @param placeId the given restaurant id
     * @return boolean livedata when successful
     */
    public LiveData<Boolean> setChosenRestaurant(String placeId) {
        MutableLiveData<Boolean> isSetLiveData = new MutableLiveData<>();
        User user = currentUserLiveData.getValue();
        if (user != null && foundRestaurantsLiveData.getValue() != null) {
            String userId = user.getUid();
            String userName = user.getUserName();
            Restaurant restaurant = foundRestaurantsLiveData.getValue().get(placeId);
            if (restaurant != null) {
                String placeName = restaurant.getName();
                String placeAddress = restaurant.getAddress();
                ChosenRestaurant chosenRestaurant = new ChosenRestaurant(
                        placeId, placeName, placeAddress,
                        System.currentTimeMillis());

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
                            userData.put(USERID_FIELD, userId);
                            userData.put(USERNAME_FIELD, userName);
                            task.getResult().update(BYUSERS_FIELD, FieldValue.arrayUnion(userId));
                            task.getResult().collection(CHOSENBY_COLLECTION_NAME).document(userId)
                                    .set(userData);
                            return null;
                        })
                        .addOnSuccessListener(o -> {
                            isSetLiveData.setValue(true);
                            Log.d(TAG, "setChosenRestaurant: " + placeId);
                        });
            }
        }
        return isSetLiveData;
    }

    /**
     * Delete current user document in Collection "chosen_by"
     * of given restaurant document (itself in Collection "chosen_restaurants")
     *
     * @param placeId placeId the given restaurant id
     * @return boolean livedata when successful
     */
    public LiveData<Boolean> clearChosenRestaurant(String placeId) {
        MutableLiveData<Boolean> isClearedLiveData = new MutableLiveData<>();
        User user = currentUserLiveData.getValue();
        if (user != null) {
            String userId = user.getUid();
            chosenRestaurantsCollection.document(placeId)
                    .collection(CHOSENBY_COLLECTION_NAME).document(userId)
                    .get()
                    .continueWith(task -> {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            document.getReference().delete();
                            chosenRestaurantsCollection.document(placeId)
                                    .update(BYUSERS_FIELD, FieldValue.arrayRemove(userId));
                        }
                        return document.getReference();
                    })
                    .addOnSuccessListener(documentReference -> {
                        isClearedLiveData.setValue(true);
                        Log.d(TAG, "clearChosenRestaurant: " + placeId);
                    });
        }
        return isClearedLiveData;
    }
}
