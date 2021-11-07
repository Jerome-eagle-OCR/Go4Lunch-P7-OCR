package com.jr_eagle_ocr.go4lunch.repositories;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
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
public final class RestaurantRepository {
    private static final String TAG = "RestaurantRepository";
//    private static volatile RestaurantRepository instance;

    private final FirebaseFirestore db;
    private final FirebaseAuth auth;
    private final MutableLiveData<Map<String, Restaurant>> foundRestaurantsMutableLiveData;
    private final MutableLiveData<List<String>> chosenRestaurantIdsMutableLiveData;


    public RestaurantRepository() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        foundRestaurantsMutableLiveData = new MutableLiveData<>(new HashMap<>());
        chosenRestaurantIdsMutableLiveData = new MutableLiveData<>();
    }

//    public static RestaurantRepository getInstance() {
//        RestaurantRepository result = instance;
//        if (result != null) {
//            return result;
//        }
//        synchronized (RestaurantRepository.class) {
//            if (instance == null) {
//                instance = new RestaurantRepository();
//            }
//        }
//        return instance;
//    }


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

    private static final String CHOSEN_COLLECTION_NAME = "chosen_restaurants";
    private static final String LIKED_COLLECTION_NAME = "liked_restaurants";
    public static final String PLACEID_FIELD = "placeId";
    public static final String PLACENAME_FIELD = "placeName";
    public static final String PLACEADDRESS_FIELD = "placeAddress";
    private static final String TIMESTAMP_FIELD = "timestamp";
    public static final String BYUSERS_FIELD = "byUsers";
    public static final String CHOSENBY_COLLECTION_NAME = "chosen_by";
    private static final String LIKEDBY_COLLECTION_NAME = "liked_by";
    public static final String USERID_FIELD = "uid";
    public static final String USERNAME_FIELD = "userName";

    // --- CHOOSE RESTAURANT MANAGEMENT ---

    /**
     * Listen to Collection "chosen_restaurants" and for each restaurant document
     * test if the document is today's AND if the (chosen) by user list is not empty :
     * - if yes, add restaurant id in a list
     * - if not, delete the restaurant document
     *
     * @return a fresh list of restaurant ids in a livedata
     */
    public LiveData<List<String>> getChosenRestaurantIds() {
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
                                            document.getReference().delete();
                                            Log.d(TAG, "getChosenRestaurantIds: document " + document.getId() + " deleted");
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
        return chosenRestaurantIdsMutableLiveData;
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

        long documentTimeMillis = document.getLong(TIMESTAMP_FIELD);
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
    public boolean getIsByUsersEmpty(DocumentSnapshot document) {
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

//    /**
//     * Set current user document in Collection "chosen_by"
//     * of given restaurant document (itself in Collection "chosen_restaurants")
//     *
//     * @param placeId the given restaurant id
//     * @return boolean livedata when successful
//     */
//    public LiveData<Boolean> setChosenRestaurant(String placeId) {
//        MutableLiveData<Boolean> isSetLiveData = new MutableLiveData<>();
//        String userId = auth.getUid();
//        ChosenRestaurant chosenRestaurant = new ChosenRestaurant(placeId, System.currentTimeMillis(), new ArrayList<>());
//        if (userId != null) {
//            getChosenRestaurantsCollection().document(placeId)
//                    .get()
//                    .continueWith(task -> {
//                        DocumentSnapshot document = task.getResult();
//                        if (!document.exists()) {
//                            chosenRestaurant.getByUsers().add(userId);
//                            document.getReference().set(chosenRestaurant);
//                        }
//                        if (document.exists()) {
//                            document.getReference().update(BYUSERS_FIELD, FieldValue.arrayUnion(userId));
//                        }
//                        return null;
//                    })
//                    .addOnSuccessListener(o -> {
//                        isSetLiveData.setValue(true);
//                    });
//        }
//        return isSetLiveData;
//    }
//
//    /**
//     * Delete current user document in Collection "chosen_by"
//     * of given restaurant document (itself in Collection "chosen_restaurants")
//     *
//     * @param placeId placeId the given restaurant id
//     * @return boolean livedata when successful
//     */
//    public LiveData<Boolean> clearChosenRestaurant(String placeId) {
//        MutableLiveData<Boolean> isClearedLiveData = new MutableLiveData<>();
//        String userId = auth.getUid();
//        if (userId != null) {
//            getChosenRestaurantsCollection().document(placeId)
//                    .get()
//                    .continueWith(task -> {
//                        DocumentSnapshot document = task.getResult();
//                        if (document.exists()) {
//                            document.getReference().update(BYUSERS_FIELD, FieldValue.arrayRemove(userId));
//                        }
//                        return null;
//                    })
//                    .addOnSuccessListener(o -> {
//                        isClearedLiveData.setValue(true);
//                    });
//        }
//        return isClearedLiveData;
//    }
//

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


    // --- LIKE RESTAURANT MANAGEMENT ---

    /**
     * Listen if current user document exists in Collection "liked_by"
     * of given restaurant document (itself in Collection "liked_restaurants")
     *
     * @return boolean livedata
     */
    public LiveData<Boolean> getIsLikedRestaurant(String placeId) {
        String userId = auth.getUid();
        MutableLiveData<Boolean> isLikedRestaurantMutableLiveData = new MutableLiveData<>();
        if (userId != null) {
            this.getLikedRestaurantsCollection().document(placeId)
                    .collection(LIKEDBY_COLLECTION_NAME).document(userId)
                    .addSnapshotListener((value, error) -> {
                        if (value != null) {
                            isLikedRestaurantMutableLiveData.setValue(value.exists());
                        } else if (error != null) {
                            isLikedRestaurantMutableLiveData.setValue(null);
                            Log.e(TAG, "getLikedRestaurants: ", error);
                        }
                    });
        }
        return isLikedRestaurantMutableLiveData;
    }


    /**
     * Set current user document in Collection "liked_by"
     * of given restaurant document (itself in Collection "liked_restaurants")
     *
     * @param placeId the given restaurant id
     * @return boolean livedata when successful
     */
    public MutableLiveData<Boolean> setLikedRestaurant(String placeId) {
        MutableLiveData<Boolean> isSetLiveData = new MutableLiveData<>();
        String userId = auth.getUid();
        Map<String, String> userData = new HashMap<>();
        userData.put(USERID_FIELD, userId);
        if (userId != null) {
            getLikedRestaurantsCollection().document(placeId)
                    .get()
                    .continueWith(task -> {
                        DocumentSnapshot document = task.getResult();
                        if (!document.exists()) {
                            Map<String, String> placeData = new HashMap<>();
                            placeData.put(PLACEID_FIELD, placeId);
                            document.getReference().set(placeData);
                        }
                        return document.getReference();
                    })
                    .continueWith(task -> {
                        task.getResult().collection(LIKEDBY_COLLECTION_NAME).document(userId)
                                .set(userData);

                        return null;
                    })
                    .addOnSuccessListener(o -> isSetLiveData.setValue(true));
        }
        return isSetLiveData;
    }

    /**
     * Delete current user document in Collection "liked_by"
     * of given restaurant document (itself in Collection "liked_restaurants")
     *
     * @param placeId placeId the given restaurant id
     * @return boolean livedata when successful
     */
    public MutableLiveData<Boolean> clearLikedRestaurant(String placeId) {
        MutableLiveData<Boolean> isClearedLiveData = new MutableLiveData<>();
        String userId = auth.getUid();
        if (userId != null) {
            getLikedRestaurantsCollection().document(placeId)
                    .collection(LIKEDBY_COLLECTION_NAME).document(userId)
                    .get()
                    .continueWith(task -> {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            document.getReference().delete();
                        }
                        return null;
                    })
                    .addOnSuccessListener(o -> isClearedLiveData.setValue(true));
        }
        return isClearedLiveData;
    }

    /**
     * Get the Collection Reference for the liked restaurants
     *
     * @return the Collection Reference
     */
    private CollectionReference getLikedRestaurantsCollection() {
        return db.collection(LIKED_COLLECTION_NAME);
    }
}
