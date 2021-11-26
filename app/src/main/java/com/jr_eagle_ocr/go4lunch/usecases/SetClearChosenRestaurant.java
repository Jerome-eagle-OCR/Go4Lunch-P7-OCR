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
import com.jr_eagle_ocr.go4lunch.model.User;
import com.jr_eagle_ocr.go4lunch.model.pojo.ChosenRestaurantPojo;
import com.jr_eagle_ocr.go4lunch.model.pojo.RestaurantPojo;
import com.jr_eagle_ocr.go4lunch.repositories.RestaurantRepository;
import com.jr_eagle_ocr.go4lunch.repositories.UserRepository;
import com.jr_eagle_ocr.go4lunch.usecases.parent.UseCase;

import java.util.HashMap;
import java.util.Map;

/**
 * @author jrigault
 */
public final class SetClearChosenRestaurant extends UseCase {
    private final LiveData<User> currentUserLiveData;
    private final LiveData<Map<String, RestaurantPojo>> allRestaurantsLiveData;
    private final CollectionReference chosenRestaurantsCollection;

    public SetClearChosenRestaurant(
            UserRepository userRepository,
            RestaurantRepository restaurantRepository
    ) {
        currentUserLiveData = userRepository.getCurrentUser();
        allRestaurantsLiveData = restaurantRepository.getAllRestaurants();
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
        if (user != null && allRestaurantsLiveData.getValue() != null) {
            String uid = user.getUid();
            String userName = user.getUserName();
            RestaurantPojo restaurant = allRestaurantsLiveData.getValue().get(placeId);
            if (restaurant != null) {
                String placeName = restaurant.getName();
                String placeAddress = restaurant.getAddress();
                ChosenRestaurantPojo chosenRestaurant = new ChosenRestaurantPojo(
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
                            task.getResult().collection(CHOSENBY_COLLECTION_NAME).document(uid)
                                    .set(userData);
                            task.getResult().update(BYUSERS_FIELD, FieldValue.arrayUnion(uid));
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
