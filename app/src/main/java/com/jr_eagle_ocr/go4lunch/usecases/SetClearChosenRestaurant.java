package com.jr_eagle_ocr.go4lunch.usecases;

import static com.jr_eagle_ocr.go4lunch.repositories.RestaurantRepository.BYUSERS_FIELD;
import static com.jr_eagle_ocr.go4lunch.repositories.RestaurantRepository.CHOSENBY_COLLECTION_NAME;
import static com.jr_eagle_ocr.go4lunch.repositories.RestaurantRepository.USERID_FIELD;
import static com.jr_eagle_ocr.go4lunch.repositories.RestaurantRepository.USERNAME_FIELD;

import android.util.Log;

import androidx.lifecycle.LiveData;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.jr_eagle_ocr.go4lunch.model.User;
import com.jr_eagle_ocr.go4lunch.model.pojo.ChosenRestaurantPojo;
import com.jr_eagle_ocr.go4lunch.model.pojo.RestaurantPojo;
import com.jr_eagle_ocr.go4lunch.repositories.RestaurantRepository;
import com.jr_eagle_ocr.go4lunch.repositories.UserRepository;
import com.jr_eagle_ocr.go4lunch.usecases.parent.UseCase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author jrigault
 */
public final class SetClearChosenRestaurant extends UseCase {
    private final LiveData<User> currentUserLiveData;
    private final LiveData<Map<String, RestaurantPojo>> allRestaurantsLiveData;
    private final LiveData<String> currentUserChosenRestaurantIdLiveData;
    private final CollectionReference chosenRestaurantsCollection;

    public SetClearChosenRestaurant(
            UserRepository userRepository,
            RestaurantRepository restaurantRepository,
            GetCurrentUserChosenRestaurantId getCurrentUserChosenRestaurantId
    ) {
        currentUserLiveData = userRepository.getCurrentUser();
        allRestaurantsLiveData = restaurantRepository.getAllRestaurants();
        currentUserChosenRestaurantIdLiveData = getCurrentUserChosenRestaurantId.getCurrentUserChosenRestaurantId();
        chosenRestaurantsCollection = restaurantRepository.getChosenRestaurantsCollection();
    }

    /**
     * Manages to set the current user document in Collection "chosen_by"
     * of the given restaurant document (itself in Collection "chosen_restaurants"),
     * but previously tests if current user has already chosen a restaurant,
     * if so, clears it and afterwards, really do set the newly chosen restaurant
     *
     * @param placeId the given restaurant id
     */
    public void setChosenRestaurant(String placeId) {
        String currentUserChosenRestaurantId = currentUserChosenRestaurantIdLiveData.getValue();
        if (currentUserChosenRestaurantId != null) {
            this.clearChosenRestaurant(currentUserChosenRestaurantId);
        }
        this.doSetChosenRestaurant(placeId);
    }

    /**
     * Set current user document in Collection "chosen_by"
     * of given restaurant document (itself in Collection "chosen_restaurants")
     *
     * @param placeId the given restaurant id
     */
    private void doSetChosenRestaurant(String placeId) {
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
     * @return a Firestore void task when clearing is successful
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
                        List<Task<Void>> tasks = new ArrayList<>();
                        if (document.exists()) {
                            Task<Void> task1 = document.getReference().delete();
                            Task<Void> task2 = chosenRestaurantsCollection.document(placeId)
                                    .update(BYUSERS_FIELD, FieldValue.arrayRemove(uid));
                            tasks.addAll(Arrays.asList(task1, task2));
                        }
                        return Tasks.whenAllSuccess(tasks);
                    })
                    .addOnSuccessListener(task -> {
                        Log.d(TAG, "clearChosenRestaurant: " + placeId);
                    });
        }
    }
}
