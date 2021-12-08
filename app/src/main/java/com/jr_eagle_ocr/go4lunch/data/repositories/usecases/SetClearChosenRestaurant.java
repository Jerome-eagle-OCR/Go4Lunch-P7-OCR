package com.jr_eagle_ocr.go4lunch.data.repositories.usecases;

import static com.jr_eagle_ocr.go4lunch.data.repositories.RestaurantRepository.BYUSERS_FIELD;
import static com.jr_eagle_ocr.go4lunch.data.repositories.RestaurantRepository.CHOSENBY_COLLECTION_NAME;
import static com.jr_eagle_ocr.go4lunch.data.repositories.RestaurantRepository.USERID_FIELD;
import static com.jr_eagle_ocr.go4lunch.data.repositories.RestaurantRepository.USERNAME_FIELD;

import android.util.Log;

import androidx.lifecycle.LiveData;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.jr_eagle_ocr.go4lunch.data.models.ChosenRestaurant;
import com.jr_eagle_ocr.go4lunch.data.models.Restaurant;
import com.jr_eagle_ocr.go4lunch.data.models.User;
import com.jr_eagle_ocr.go4lunch.data.repositories.RestaurantRepository;
import com.jr_eagle_ocr.go4lunch.data.repositories.UserRepository;
import com.jr_eagle_ocr.go4lunch.data.repositories.usecases.parent.UseCase;

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
    private final LiveData<Map<String, Restaurant>> allRestaurantsLiveData;
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
     * Sets a current user document in Collection "chosen_by"
     * of given restaurant document (itself in Collection "chosen_restaurants")
     * (prior to setting the document, checks that the current chosen restaurant id and the newly
     * chosen one are not the same, if not call the clearing method and do sets the newly chosen
     * restaurant waiting for clearing done, or directly if no clearing has to be done)
     *
     * @param placeId the given restaurant id
     */
    public void setChosenRestaurant(String placeId) {
        String currentUserChosenRestaurantId = currentUserChosenRestaurantIdLiveData.getValue();
        if (placeId != null && !placeId.isEmpty()
                && !placeId.equals(currentUserChosenRestaurantId)) {
            User user = currentUserLiveData.getValue();
            Map<String, Restaurant> allRestaurants = allRestaurantsLiveData.getValue();
            if (user != null && allRestaurants != null) {
                Task<Boolean> clearTask = this.clearChosenRestaurant();
                if (clearTask != null) {
                    clearTask.addOnSuccessListener(aBoolean -> {
                        if (aBoolean) setChosenRestaurant(placeId, user, allRestaurants);
                    });
                } else {
                    setChosenRestaurant(placeId, user, allRestaurants);
                }
            }
        }
    }

    /**
     * Sets a current user document in Collection "chosen_by"
     * of given restaurant document (itself in Collection "chosen_restaurants")
     *
     * @param placeId        the chosen restaurant id
     * @param user           the current user
     * @param allRestaurants all restaurants from Firestore "restaurants" collection
     */
    private void setChosenRestaurant(String placeId, User user, Map<String, Restaurant> allRestaurants) {
        String uid = user.getUid();
        String userName = user.getUserName();
        Restaurant restaurant = allRestaurants.get(placeId);
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
                        task.getResult().collection(CHOSENBY_COLLECTION_NAME).document(uid)
                                .set(userData);
                        task.getResult().update(BYUSERS_FIELD, FieldValue.arrayUnion(uid));
                        return null;
                    })
                    .addOnSuccessListener(o -> Log.d(TAG, "setChosenRestaurant: " + placeId));
        }
    }

    /**
     * Delete current user document in Collection "chosen_by"
     * of current chosen restaurant document (itself in Collection "chosen_restaurants")
     * (if there is a current chosen restaurant)
     *
     * @return a Firestore void task when clearing is successful
     */
    public Task<Boolean> clearChosenRestaurant() {
        String placeId = currentUserChosenRestaurantIdLiveData.getValue();
        User user = currentUserLiveData.getValue();
        if (placeId != null && !placeId.isEmpty() && user != null) {
            String uid = user.getUid();
            return chosenRestaurantsCollection.document(placeId)
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
                        return Tasks.whenAllComplete(tasks);
                    })
                    .continueWith(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "clearChosenRestaurant: " + placeId);
                            return true;
                        } else {
                            return false;
                        }
                    });
        }
        return null;
    }
}
