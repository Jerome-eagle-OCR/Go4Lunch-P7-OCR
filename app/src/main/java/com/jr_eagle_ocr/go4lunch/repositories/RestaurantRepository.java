package com.jr_eagle_ocr.go4lunch.repositories;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jr_eagle_ocr.go4lunch.model.ChosenRestaurant;
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
    private static volatile RestaurantRepository instance;

    private final FirebaseFirestore db;
    private final FirebaseAuth auth;
    private final MutableLiveData<Map<String, Restaurant>> foundRestaurantsLiveData;
    private final MutableLiveData<List<String>> chosenRestaurantIdsMutableLiveData;
    private final MutableLiveData<String> authUserChosenRestaurantLiveData;


    private RestaurantRepository() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        foundRestaurantsLiveData = new MutableLiveData<>(new HashMap<>());
        chosenRestaurantIdsMutableLiveData = new MutableLiveData<>(new ArrayList<>());
        authUserChosenRestaurantLiveData = new MutableLiveData<>(null);
    }

    public static RestaurantRepository getInstance() {
        RestaurantRepository result = instance;
        if (result != null) {
            return result;
        }
        synchronized (RestaurantRepository.class) {
            if (instance == null) {
                instance = new RestaurantRepository();
            }
        }
        return instance;
    }


    /**
     * Get restaurant Map found in MapsView
     *
     * @return the restaurant Map where key = placeId and value = restaurant
     */
    public LiveData<Map<String, Restaurant>> getFoundRestaurants() {
        return foundRestaurantsLiveData;
    }

    /**
     * Add a restaurant to the restaurant Map
     *
     * @param restaurant the restaurant to add
     */
    public void addFoundRestaurant(Restaurant restaurant) {
        String restaurantId = restaurant.getId();
        Map<String, Restaurant> modifiedRestaurants = foundRestaurantsLiveData.getValue();
        if (modifiedRestaurants != null) modifiedRestaurants.put(restaurantId, restaurant);
        foundRestaurantsLiveData.setValue(modifiedRestaurants);
    }


    // --- FIRESTORE ---

    private static final String CHOSEN_COLLECTION_NAME = "chosen_restaurants";
    private static final String LIKED_COLLECTION_NAME = "liked_restaurants";
    public static final String PLACEID_FIELD = "placeId";
    private static final String TIMESTAMP_FIELD = "timestamp";
    public static final String BYUSERS_FIELD = "byUsers";
    private static final String LIKEDBY_COLLECTION_NAME = "liked_by";
    public static final String USERID_FIELD = "uid";

    // --- CHOOSE RESTAURANT ---

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
                    List<String> chosenRestaurantIds = new ArrayList<>();
                    if (value != null && !value.isEmpty()) {
                        List<DocumentSnapshot> documents = value.getDocuments();
                        for (DocumentSnapshot document : documents) {
                            boolean isGood = getIsGood(document);
                            if (isGood) {
                                chosenRestaurantIds.add(document.getId());
                                Log.d(TAG, "getChosenRestaurantIds: restaurantId " + document.getId() + " added");
                            } else {
                                document.getReference().delete();
                                Log.d(TAG, "getChosenRestaurantIds: document " + document.getId() + " deleted");
                            }
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
     * Determine if a restaurant document must be considered or deleted
     *
     * @param document the document to "analyse"
     * @return true if document must be considered and false is must be deleted
     */
    private boolean getIsGood(DocumentSnapshot document) {
        // 1- Determine if document is today's
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
        Log.d(TAG, "getIsGood: timestamp = " + documentTimeMillis + "ms, dayOfTheWeek = " + documentDay + " , isToday ? " + isToday);

        // 2- Determine if chosen by user array is empty
        String byUsersString = "";
        Map<String, Object> data = document.getData();
        if (data != null) {
            Object byUsersData = data.get(BYUSERS_FIELD);
            if (byUsersData != null) {
                byUsersString = byUsersData.toString();
            }
        }
        boolean isByUsersEmpty = byUsersString.equals("[]");
        Log.d(TAG, "getIsGood: byUsers = " + byUsersString + " , empty ? " + isByUsersEmpty);

        return isToday && !isByUsersEmpty;
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
        String userId = auth.getUid();
        ChosenRestaurant chosenRestaurant = new ChosenRestaurant(placeId, System.currentTimeMillis(), new ArrayList<>());
        if (userId != null) {
            getChosenRestaurantsCollection().document(placeId)
                    .get()
                    .continueWith(task -> {
                        DocumentSnapshot document = task.getResult();
                        if (!document.exists()) {
                            chosenRestaurant.getByUsers().add(userId);
                            document.getReference().set(chosenRestaurant);
                        }
                        if (document.exists()) {
                            document.getReference().update(BYUSERS_FIELD, FieldValue.arrayUnion(userId));
                        }
                        return null;
                    })
                    .addOnSuccessListener(o -> {
                        isSetLiveData.setValue(true);
                        authUserChosenRestaurantLiveData.setValue(placeId);
                    });
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
        String userId = auth.getUid();
        if (userId != null) {
            getChosenRestaurantsCollection().document(placeId)
                    .get()
                    .continueWith(task -> {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            document.getReference().update(BYUSERS_FIELD, FieldValue.arrayRemove(userId));
                        }
                        return null;
                    })
                    .addOnSuccessListener(o -> {
                        isClearedLiveData.setValue(true);
                        authUserChosenRestaurantLiveData.setValue(null);
                    });
        }
        return isClearedLiveData;
    }

    /**
     * Get authenticated user chosen restaurant id
     *
     * @return the place id of the chosen restaurant
     */
    public LiveData<String> getAuthUserChosenRestaurantLiveData() {
        return authUserChosenRestaurantLiveData;
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


    // --- LIKE RESTAURANT ---

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
