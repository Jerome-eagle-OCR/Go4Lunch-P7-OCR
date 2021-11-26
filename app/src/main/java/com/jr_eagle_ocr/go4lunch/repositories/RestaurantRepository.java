package com.jr_eagle_ocr.go4lunch.repositories;

import android.annotation.SuppressLint;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.libraries.places.api.model.LocalTime;
import com.google.android.libraries.places.api.model.Period;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ListenerRegistration;
import com.jr_eagle_ocr.go4lunch.model.Restaurant;
import com.jr_eagle_ocr.go4lunch.model.pojo.RestaurantPojo;
import com.jr_eagle_ocr.go4lunch.repositories.parent.Repository;
import com.jr_eagle_ocr.go4lunch.util.BitmapUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author jrigault
 */
public final class RestaurantRepository extends Repository {
    private List<String> foundRestaurantIds;
    private final MutableLiveData<Map<String, RestaurantPojo>> allRestaurantsMutableLiveData;
    private final MutableLiveData<List<String>> chosenRestaurantIdsMutableLiveData;
    private ListenerRegistration restaurantsListenerRegistration;
    private ListenerRegistration chosenRestaurantsListenerRegistration;

    public RestaurantRepository() {
        allRestaurantsMutableLiveData = new MutableLiveData<>(new HashMap<>());
//        mapsFoundRestaurantsMutableLiveData = new MutableLiveData<>(new HashMap<>());
        chosenRestaurantIdsMutableLiveData = new MutableLiveData<>();
    }

    // --- FIRESTORE ---

    public static final String RESTAURANTS_COLLECTION_NAME = "restaurants";
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


    /**
     * @param foundRestaurantIds
     */
    public void setFoundRestaurantIds(List<String> foundRestaurantIds) {
        this.foundRestaurantIds = foundRestaurantIds;
    }

    /**
     * @return
     */
    public List<String> getFoundRestaurantIds() {
        return foundRestaurantIds;
    }

    /**
     * Get restaurants from the listened "restaurants" Firestore collection
     *
     * @return a restaurant HashMap (placeId, restaurant (POJO)) in a livedata
     */
    public LiveData<Map<String, RestaurantPojo>> getAllRestaurants() {
        return allRestaurantsMutableLiveData;
    }

    /**
     * Add a restaurant to the "restaurants" Firestore collection
     *
     * @param restaurant the restaurant to add
     */
    @SuppressLint("DefaultLocale") // format close time to 24h
    public void addFoundRestaurant(Restaurant restaurant) {
        // Set all variables:
        // Id
        String id = restaurant.getId();
        // Bitmap base64 string
        String photoString = null;
        if (restaurant.getPhoto() != null) {
            photoString = BitmapUtil.encodeToBase64(restaurant.getPhoto());
        }
        // Name
        String name = restaurant.getName();
        // GeoPoint
        GeoPoint geoPoint = new GeoPoint(restaurant.getLatLng().latitude, restaurant.getLatLng().longitude);
        // Address
        String address = restaurant.getAddress();
        // Phone number
        String phoneNumber = restaurant.getPhoneNumber();
        // Web site URL
        String webSiteUrl = restaurant.getWebSiteUrl();
        // Close times
        HashMap<String, String> closeTimes = new LinkedHashMap<>();
        List<Period> periods = restaurant.getOpeningHours().getPeriods();
        for (Period period : periods) {
            String dayOfWeek = period.getOpen().getDay().name();
            String closeTime;
            if (period.getClose() != null) {
                LocalTime closeLocalTime = period.getClose().getTime();
                closeTime = String.format("%02d", closeLocalTime.getHours()) +
                        ":" + String.format("%02d", closeLocalTime.getMinutes());
            } else {
                closeTime = null;
            }
            closeTimes.put(dayOfWeek, closeTime);
        }
        // Rating
        float rating = 0;
        if (restaurant.getRating() != null) {
            rating = (int) (restaurant.getRating() * 3 / 5);
        }
        // Timestamp
        String timestamp = String.valueOf(System.currentTimeMillis());

        RestaurantPojo foundRestaurant =
                new RestaurantPojo(id, photoString, name, geoPoint,
                        address, phoneNumber, webSiteUrl, closeTimes, rating, timestamp);

        this.getRestaurantsCollection().document(id).set(foundRestaurant);
    }

    /**
     * Set the Firestore "restaurants" collection listener and build an up-to-date
     * HashMap (id, restaurant (POJO)) and clean the collection by testing each restaurant document:
     * if the document is not older than 30 days, add restaurant id in a list,
     * if not delete the restaurant document
     */
    public void setAllRestaurants() {
        restaurantsListenerRegistration = this.getRestaurantsCollection()
                .addSnapshotListener((value, error) -> {
                    Map<String, RestaurantPojo> foundRestaurants = new HashMap<>();
                    if (value != null && !value.isEmpty()) {
                        List<DocumentSnapshot> documents = value.getDocuments();
                        for (DocumentSnapshot document : documents) {
                            boolean isNotTooOld = getIsNotTooOld(document);
                            if (isNotTooOld) {
                                RestaurantPojo foundRestaurant = document.toObject(RestaurantPojo.class);
                                foundRestaurants.put(document.getId(), foundRestaurant);
                            } else {
                                document.getReference().delete();
                            }
                        }
                        allRestaurantsMutableLiveData.setValue(foundRestaurants);
                    }
                });
    }

    /**
     * Determine if a restaurant document is not too old (less than 30 days)
     *
     * @param document the document to "analyse"
     * @return a boolean
     */
    private boolean getIsNotTooOld(DocumentSnapshot document) {
        Calendar calendar = Calendar.getInstance();
        int todayDayOfYear = calendar.get(Calendar.DAY_OF_YEAR);

        String documentTimestamp = document.getString(TIMESTAMP_FIELD);
        long documentTimeMillis = documentTimestamp != null ? Long.parseLong(documentTimestamp) : 0;
        calendar.setTimeInMillis(documentTimeMillis);
        int documentDayOfYear = calendar.get(Calendar.DAY_OF_YEAR);

        long diffDayOfYear = documentDayOfYear - todayDayOfYear;

        return diffDayOfYear < 30;
    }


    /**
     * Unset the Firestore "restaurants" collection listener
     */
    public void unsetFoundRestaurants() {
        if (restaurantsListenerRegistration != null)
            restaurantsListenerRegistration.remove();
    }

    /**
     * Get the Collection Reference for the chosen restaurants
     *
     * @return the Collection Reference
     */
    public CollectionReference getRestaurantsCollection() {
        return db.collection(RESTAURANTS_COLLECTION_NAME);
    }


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
    public void setChosenRestaurantIdsAndCleanCollection() {
        chosenRestaurantsListenerRegistration = this.getChosenRestaurantsCollection()
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
     * Unset the Firestore "chosen_restaurants" collection listener
     */
    public void unsetChosenRestaurantIdsAndCleanCollection() {
        if (chosenRestaurantsListenerRegistration != null)
            chosenRestaurantsListenerRegistration.remove();
    }

    /**
     * Determine if a chosen restaurant document is today
     *
     * @param document the document to "analyse"
     * @return a boolean
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
     * Determine if document by users array is empty
     *
     * @param document the document to "analyse"
     * @return a boolean
     */
    private boolean getIsByUsersEmpty(DocumentSnapshot document) {
        boolean isByUsersEmpty = true;
        if (document.getData() != null) {
            Object byUsersData = document.getData().get(BYUSERS_FIELD);
            if (byUsersData != null) {
                String inLine = byUsersData.toString();
                isByUsersEmpty = inLine.equals("[]");
            }
        }
        return isByUsersEmpty;
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


    // --- UTIL ---

    /**
     * Convert the byUsers field from a restaurant document to a list of user id
     *
     * @param document the restaurant document
     * @return a list of user id
     */
    public List<String> getByUserIds(DocumentSnapshot document) {
        List<String> byUserIds = new ArrayList<>();
        if (document.getData() != null) {
            Object byUsersData = document.getData().get(BYUSERS_FIELD);
            if (byUsersData != null) {
                String inLine = byUsersData.toString();
                if (!inLine.equals("[]")) {
                    inLine = inLine.replaceAll("[\\[\\]]", "").trim();
                    byUserIds = Arrays.asList(inLine.split(", ", 0));
                }
            }
        }
        return byUserIds;
    }
}
