package com.jr_eagle_ocr.go4lunch.repositories;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.jr_eagle_ocr.go4lunch.model.Restaurant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author jrigault
 */
public final class RestaurantRepository extends Repository {
    private final MutableLiveData<Map<String, Restaurant>> foundRestaurantsMutableLiveData;
    private final MutableLiveData<List<String>> chosenRestaurantIdsMutableLiveData;


    public RestaurantRepository() {
        foundRestaurantsMutableLiveData = new MutableLiveData<>(new HashMap<>());
        chosenRestaurantIdsMutableLiveData = new MutableLiveData<>();

        setChosenRestaurantIdsAndCleanCollection();
    }

    /**
     * Get restaurants found in MapsView
     *
     * @return the restaurant HashMap where key = placeId and value = restaurant
     */
    public LiveData<Map<String, Restaurant>> getFoundRestaurants() {
        return foundRestaurantsMutableLiveData;
    }

    /**
     * Add a restaurant to the restaurant HashMap
     *
     * @param restaurant the restaurant to add
     */
    public void addFoundRestaurant(Restaurant restaurant) {
        String restaurantId = restaurant.getId();
        Map<String, Restaurant> modifiedRestaurants = foundRestaurantsMutableLiveData.getValue();
        if (modifiedRestaurants != null) modifiedRestaurants.put(restaurantId, restaurant);
        foundRestaurantsMutableLiveData.setValue(modifiedRestaurants);
    }


    // --- FIRESTORE ---

    public static final String CHOSEN_COLLECTION_NAME = "chosen_restaurants";
    public static final String LIKED_COLLECTION_NAME = "liked_restaurants";
    public static final String PLACEID_FIELD = "placeId";
    public static final String PLACENAME_FIELD = "placeName";
    public static final String PLACEADDRESS_FIELD = "placeAddress";
    private static final String TIMESTAMP_FIELD = "timestamp";
    public static final String BYUSERS_FIELD = "byUsers";
    public static final String CHOSENBY_COLLECTION_NAME = "chosen_by";
    public static final String LIKEDBY_COLLECTION_NAME = "liked_by";
    public static final String USERID_FIELD = "uid";
    public static final String USERNAME_FIELD = "userName";

    // --- RESTAURANT CHOOSING ---

    /**
     * Get the list of chosen restaurant ids from the listened "chosen_restaurants" Firestore collection
     *
     * @return a livedata of up-to-date list of chosen restaurant id
     */
    public LiveData<List<String>> getChosenRestaurantIds() {
        return chosenRestaurantIdsMutableLiveData;
    }

    /**
     * Set the Firestore "chosen_restaurants" collection listener and build an up-to-date list
     * of chosen restaurant id and clean the collection by testing each restaurant document:
     * if the document is today's AND if the (chosen) by user list is not empty,
     * add restaurant id in a list, if not delete the restaurant document
     */
    private void setChosenRestaurantIdsAndCleanCollection() {
        this.getChosenRestaurantsCollection()
                .addSnapshotListener((value, error) -> {
                    List<String> chosenRestaurantIds = new ArrayList<>(); // Déclenche toujours un changement ?
                    if (value != null && !value.isEmpty()) {
                        List<DocumentSnapshot> documents = value.getDocuments();
                        for (DocumentSnapshot document : documents) {
                            document.getReference().collection(CHOSENBY_COLLECTION_NAME)
                                    .get()
                                    .addOnSuccessListener(queryDocumentSnapshots -> {
                                        boolean isToday = getIsToday(document);
                                        boolean isByUsersEmpty = getIsByUsersEmpty(document);
                                        if (isToday && !isByUsersEmpty) {
                                            chosenRestaurantIds.add(document.getId());
                                            Log.d(TAG, "getChosenRestaurantIds: restaurantId " + document.getId() + " added");
                                        } else if (!isToday) {
                                            document.getReference().collection(CHOSENBY_COLLECTION_NAME)
                                                    .get()
                                                    .addOnSuccessListener(queryDocumentSnapshots1 -> {
                                                        if (!queryDocumentSnapshots1.isEmpty()) {
                                                            List<DocumentSnapshot> documents1 = queryDocumentSnapshots1.getDocuments();
                                                            for (DocumentSnapshot d : documents1) {
                                                                d.getReference().delete();
                                                            }
                                                        }
                                                        document.getReference().delete();
                                                        Log.d(TAG, "getChosenRestaurantIds: document " + document.getId() + " deleted");
                                                    });
                                        }
                                    });
                        }
                    }
                    chosenRestaurantIdsMutableLiveData.setValue(chosenRestaurantIds);
                    if (error != null) {
                        chosenRestaurantIdsMutableLiveData.setValue(null);
                        Log.e(TAG, "onEvent: ", error);
                    }
                });
    }

    /**
     * Determine if a restaurant document is today
     *
     * @param document the document to "analyse"
     * @return true if document is today, false if it is yesterday or older
     */
    private boolean getIsToday(DocumentSnapshot document) {
        boolean isToday;
        Calendar calendar = Calendar.getInstance();

        long currentTimeMillis = System.currentTimeMillis();
        calendar.setTimeInMillis(currentTimeMillis);
        int currentDay = calendar.get(Calendar.DAY_OF_WEEK);

        String documentTimestamp = document.getString(TIMESTAMP_FIELD);
        long documentTimeMillis = documentTimestamp != null ? Long.parseLong(documentTimestamp) : 0;
        calendar.setTimeInMillis(documentTimeMillis);
        int documentDay = calendar.get(Calendar.DAY_OF_WEEK);

        long diffTimeHour = (currentTimeMillis - documentTimeMillis) / 3600000;
        isToday = (diffTimeHour < 24) && (currentDay == documentDay);

        return isToday;
    }

    /**
     * @param document
     * @return
     */
    private boolean getIsByUsersEmpty(DocumentSnapshot document) {
        boolean isByUsersEmpty;
        String byUsersString = "";
        Map<String, Object> data = document.getData();
        if (data != null) {
            Object byUsersData = data.get(BYUSERS_FIELD);
            if (byUsersData != null) {
                byUsersString = byUsersData.toString();
            }
        }
        isByUsersEmpty = byUsersString.equals("[]");

        return isByUsersEmpty;
    }

    /**
     * Convert the byUsers field from a restaurant document to a list of user id
     *
     * @param document the restaurant document
     * @return a list of user id
     */
    public List<String> getByUserIds(DocumentSnapshot document) {
        List<String> byUserIds = new ArrayList<>();
        Map<String, Object> data = document.getData();
        if (data != null) {
            String[] unpackedDoc = data.entrySet().toArray()[0].toString().split("=")[1].split(",");
            Log.d(TAG, "getByUsers: " + Arrays.toString(unpackedDoc));
            Object byUsersData = document.getData().get(BYUSERS_FIELD);
            if (byUsersData != null) {
                String inLine = byUsersData.toString();
                inLine = inLine.replaceAll("[\\[\\]]", "").trim();
                byUserIds = Arrays.asList(inLine.split(", ", -1));
            }
        }
        return byUserIds;
    }

    /**
     * Get the Collection Reference for the chosen restaurants
     *
     * @return the Collection Reference
     */
    public CollectionReference getChosenRestaurantsCollection() {
        return db.collection(CHOSEN_COLLECTION_NAME);
    }


    // --- RESTAURANT LIKING ---

    /**
     * Get the Collection Reference for the liked restaurants
     *
     * @return the Collection Reference
     */
    public CollectionReference getLikedRestaurantsCollection() {
        return db.collection(LIKED_COLLECTION_NAME);
    }
}
