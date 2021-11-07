package com.jr_eagle_ocr.go4lunch.repositories;

import static com.jr_eagle_ocr.go4lunch.repositories.RestaurantRepository.PLACEID_FIELD;

import android.graphics.Bitmap;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.libraries.places.api.model.LocalTime;
import com.google.android.libraries.places.api.model.Period;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.jr_eagle_ocr.go4lunch.R;
import com.jr_eagle_ocr.go4lunch.di.Go4LunchApplication;
import com.jr_eagle_ocr.go4lunch.model.Restaurant;
import com.jr_eagle_ocr.go4lunch.model.User;
import com.jr_eagle_ocr.go4lunch.ui.adaptersviewstates.RestaurantViewSate;
import com.jr_eagle_ocr.go4lunch.ui.adaptersviewstates.UserViewState;
import com.jr_eagle_ocr.go4lunch.usecases.GetCurrentUser;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TempUserRestaurantManager {

    private static final String TAG = "UserRestaurantManager";

    private static final TempUserRestaurantManager instance = new TempUserRestaurantManager();

    private final UserRepository userRepository;
    private final GetCurrentUser mGetCurrentUser;
    private final LiveData<User> currentUserDataLiveData;
    private final LiveData<FirebaseUser> currentFirebaseUserLiveData;
    private final LiveData<Map<String, User>> allUsersLiveData;

    private final LocationRepository locationRepository;

    private final RestaurantRepository restaurantRepository;
    private final LiveData<Map<String, Restaurant>> foundRestaurantsLiveData;
    private final LiveData<List<String>> chosenRestaurantIdsLiveData;

    private String displayedRestaurantId;
    private final MediatorLiveData<List<UserViewState>> joiningUserViewStatesMediatorLiveData;
    private final MediatorLiveData<List<UserViewState>> allUserViewStatesMediatorLiveData;
    private final MutableLiveData<Boolean> isChosenMutableLiveData;
    private final MediatorLiveData<List<RestaurantViewSate>> allRestaurantViewStatesMediatorLiveData;


    private TempUserRestaurantManager() {
        userRepository = Go4LunchApplication.getDependencyContainer().getUserRepository();
        mGetCurrentUser = Go4LunchApplication.getDependencyContainer().getUseCaseGetCurrentUser();
        currentUserDataLiveData = mGetCurrentUser.getCurrentUser();
        currentFirebaseUserLiveData = userRepository.getCurrentFirebaseUser();
        allUsersLiveData = userRepository.getAllUsers();

        locationRepository = Go4LunchApplication.getDependencyContainer().getLocationRepository();

        restaurantRepository = Go4LunchApplication.getDependencyContainer().getRestaurantRepository();
        foundRestaurantsLiveData = restaurantRepository.getFoundRestaurants();
        chosenRestaurantIdsLiveData = restaurantRepository.getChosenRestaurantIds();

        joiningUserViewStatesMediatorLiveData = new MediatorLiveData<>();
        joiningUserViewStatesMediatorLiveData.addSource(allUsersLiveData, uidUserMap ->
                getJoiningUserViewStates(displayedRestaurantId)
        );
        joiningUserViewStatesMediatorLiveData.addSource(chosenRestaurantIdsLiveData, chosenRestaurantIds ->
                getJoiningUserViewStates(displayedRestaurantId)
        );

        isChosenMutableLiveData = new MutableLiveData<>();

        allUserViewStatesMediatorLiveData = new MediatorLiveData<>();
        allUserViewStatesMediatorLiveData.addSource(allUsersLiveData, uidUserMap ->
                getAllUserViewStates()
        );
        allUserViewStatesMediatorLiveData.addSource(chosenRestaurantIdsLiveData, chosenRestaurantIds ->
                getAllUserViewStates()
        );

        allRestaurantViewStatesMediatorLiveData = new MediatorLiveData<>();
        allRestaurantViewStatesMediatorLiveData.addSource(foundRestaurantsLiveData, foundRestaurants ->
                getAllRestaurantViewStates());
        allRestaurantViewStatesMediatorLiveData.addSource(chosenRestaurantIdsLiveData, chosenRestaurantIds ->
                getAllRestaurantViewStates());
    }

    public static TempUserRestaurantManager getInstance() {
        return instance;
    }


    // --- UserRepository section ---

    /**
     * @return
     */
    public LiveData<FirebaseUser> getCurrentFirebaseUser() {
        return currentFirebaseUserLiveData;
    }

    /**
     * Create User in Firestore from FirebaseAuth eventually modified/completed with Firestore db infos
     */
    public LiveData<Boolean> createUser() {
        return userRepository.createUser();
    }

    public LiveData<User> getCurrentUserData() {
        return currentUserDataLiveData;
    }


    // --- User view states generation ---

    /**
     * Get all users to display in recyclerview UserAdapter
     *
     * @return a UserViewState list in a livedata
     */
    public LiveData<List<UserViewState>> getAllUserViewStates() {
        Map<String, User> allUsers = allUsersLiveData.getValue();
        restaurantRepository.getChosenRestaurantsCollection()
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<UserViewState> userViewStates; //To be produced to valorize livedata
                    Map<String, String> userChosenRestaurantMap = new HashMap<>();
                    List<DocumentSnapshot> documents = queryDocumentSnapshots.getDocuments();
                    for (DocumentSnapshot d : documents) {
                        String restaurantId = d.getId();
                        List<String> byUserIds = restaurantRepository.getByUserIds(d);
                        for (int i = 0; i < byUserIds.size(); i++) {
                            String iUser = byUserIds.get(i);
                            userChosenRestaurantMap.put(iUser, restaurantId);
                        }
                    }
                    userViewStates = generateUserViewStates(null, allUsers, userChosenRestaurantMap);
                    allUserViewStatesMediatorLiveData.setValue(userViewStates);
                });

        return allUserViewStatesMediatorLiveData;
    }

//    /**
//     *
//     */
//    private void getByUsersThenUserViewStatesForAllRestaurants() {
//        Map<String, User> allUsers = allUsersLiveData.getValue();
//        restaurantRepository.getChosenRestaurantsCollection()
//                .get()
//                .addOnSuccessListener(queryDocumentSnapshots -> {
//                    List<UserViewState> userViewStates; //To be produced to valorize livedata
//                    Map<String, String> userChosenRestaurantMap = new HashMap<>();
//                    List<DocumentSnapshot> documents = queryDocumentSnapshots.getDocuments();
//                    for (DocumentSnapshot d : documents) {
//                        String restaurantId = d.getId();
//                        List<String> byUserIds = restaurantRepository.getByUserIds(d);
//                        for (int i = 0; i < byUserIds.size(); i++) {
//                            String iUser = byUserIds.get(i);
//                            userChosenRestaurantMap.put(iUser, restaurantId);
//                        }
//                    }
//                    userViewStates = generateUserViewStates(null, allUsers, userChosenRestaurantMap);
//                    allUserViewStatesMediatorLiveData.setValue(userViewStates);
//                });
//
//    }


    /**
     * Get all users or only users joining at a specific restaurant,
     * depending on placeId null or not
     *
     * @param restaurantId the restaurant id for users having chosen this one, or null for all users
     * @return a list of user view state to feed UserAdapter
     */
    public LiveData<List<UserViewState>> getJoiningUserViewStates(String restaurantId) {
        displayedRestaurantId = restaurantId;
        Map<String, User> allUsers = allUsersLiveData.getValue();
        restaurantRepository.getChosenRestaurantsCollection()
                .whereIn(PLACEID_FIELD, Collections.singletonList(displayedRestaurantId))
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<UserViewState> userViewStates; //To be produced to valorize livedata
                    Map<String, String> userChosenRestaurantMap = new HashMap<>();
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                        String placeId = document.getId();
                        if (placeId.equals(restaurantId)) {
                            List<String> byUserIds = restaurantRepository.getByUserIds(document);
                            for (String uid : byUserIds) {
                                userChosenRestaurantMap.put(uid, placeId);
                            }
                        }
                    }
                    userViewStates = generateUserViewStates(displayedRestaurantId, allUsers, userChosenRestaurantMap);
                    joiningUserViewStatesMediatorLiveData.setValue(userViewStates);
                });

        return joiningUserViewStatesMediatorLiveData;
    }

//    /**
//     *
//     */
//    private void getByUsersThenUserViewStatesForGivenRestaurant() {
//        Map<String, User> allUsers = allUsersLiveData.getValue();
//        restaurantRepository.getChosenRestaurantsCollection()
//                .whereIn(PLACEID_FIELD, Collections.singletonList(this.displayedRestaurantIdMutableLivedata.getValue()))
//                .get()
//                .addOnSuccessListener(queryDocumentSnapshots -> {
//                    List<UserViewState> userViewStates; //To be produced to valorize livedata
//                    Map<String, String> userChosenRestaurantMap = new HashMap<>();
//                    if (!queryDocumentSnapshots.isEmpty()) {
//                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
//                        String placeId = document.getId();
//                        if (placeId.equals(displayedRestaurantIdMutableLivedata.getValue())) {
//                            List<String> byUserIds = restaurantRepository.getByUserIds(document);
//                            for (String uid : byUserIds) {
//                                userChosenRestaurantMap.put(uid, placeId);
//                            }
//                        }
//                    }
//                    userViewStates = generateUserViewStates(displayedRestaurantIdMutableLivedata.getValue(), allUsers, userChosenRestaurantMap);
//                    joiningUserViewStatesMediatorLiveData.setValue(userViewStates);
//                });
//    }

    /**
     * @param displayedRestaurantId
     * @param allUsers
     * @param userChosenRestaurantMap
     * @return
     */
    private List<UserViewState> generateUserViewStates(String displayedRestaurantId, Map<String, User> allUsers, Map<String, String> userChosenRestaurantMap) {
        List<UserViewState> userViewStates = new ArrayList<>(); //To be produced to valorize livedata
        if (allUsers != null) {
            for (Map.Entry<String, User> userEntry : allUsers.entrySet()) {
                String uid = userEntry.getKey();
                User user = userEntry.getValue();
                String userName = user.getUserName();
                int appendingString;
                String userUrlPicture = user.getUserUrlPicture();
                String restaurantId;
                String restaurantName = null;
                boolean hasChosen; // has currently iterated user chosen a restaurant ?
                hasChosen = userChosenRestaurantMap.containsKey(uid);
                restaurantId = hasChosen ? userChosenRestaurantMap.get(uid) : null;
                String authUserUid = getCurrentFirebaseUser().getValue().getUid();
                boolean isAuthUser = authUserUid.equals(uid); // is currently iterated user the authenticated user ?
                boolean isChosen = false; // is restaurant displayed in detail activity chosen by authenticated user ?
                if (displayedRestaurantId == null) {
                    appendingString = hasChosen ? R.string.is_eating_at : R.string.not_decided_yet;
                    if (restaurantId != null) {
                        if (this.getFoundRestaurants().getValue() != null) {
                            Restaurant restaurant = this.getFoundRestaurants().getValue().get(restaurantId);
                            if (restaurant != null) restaurantName = restaurant.getName();
                        }
                    }
                } else {
                    appendingString = hasChosen ? R.string.is_joining : R.string.not_decided_yet;
                    if (isAuthUser) {
                        isChosen = displayedRestaurantId.equals(restaurantId);
                        isChosenMutableLiveData.setValue(isChosen); //TODO: isChosenMLD should be a switchmap from getAuthChosenRestaurantMLD
                    }
                }
                float textAlpha = hasChosen ? 1 : 0.3f;
                float imageAlpha = hasChosen ? 1 : 0.6f;

                if (!(displayedRestaurantId != null && !hasChosen) && !(displayedRestaurantId == null && isAuthUser)) {
                    UserViewState userViewState = new UserViewState(
                            userName, appendingString, userUrlPicture,
                            restaurantId, restaurantName, textAlpha, imageAlpha);
                    if (!isChosen) {
                        userViewStates.add(userViewState);
                    }
                }
            }
        }
        return userViewStates;
    }


    // --- Maps section ---

    public Location getLocation() {
        return locationRepository.getLocation();
    }

    public void setLocation(Location location) {
        locationRepository.setLocation(location);
    }


    // --- RestaurantRepository section

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
        restaurantRepository.addFoundRestaurant(restaurant);
    }


    // --- Restaurant view states generation ---

    /**
     * @return
     */
    public LiveData<List<RestaurantViewSate>> getAllRestaurantViewStates() {
        restaurantRepository.getChosenRestaurantsCollection()
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<RestaurantViewSate> restaurantViewSates; //To be produced to valorize livedata
                    Map<String, Integer> restaurantByUsersCountMap = new HashMap<>();
                    List<DocumentSnapshot> documents = queryDocumentSnapshots.getDocuments();
                    for (DocumentSnapshot d : documents) {
                        String restaurantId = d.getId();
                        List<String> byUserIds = restaurantRepository.getByUserIds(d);
                        Integer byUsersCount = byUserIds.size();
                        restaurantByUsersCountMap.put(restaurantId, byUsersCount);
                    }
                    restaurantViewSates = generateRestaurantViewStates(restaurantByUsersCountMap);
                    allRestaurantViewStatesMediatorLiveData.setValue(restaurantViewSates);
                });

        return allRestaurantViewStatesMediatorLiveData;
    }

//    private void getByUsersSizeThenRestaurantViewStates() {
//        restaurantRepository.getChosenRestaurantsCollection()
//                .get()
//                .addOnSuccessListener(queryDocumentSnapshots -> {
//                    List<RestaurantViewSate> restaurantViewSates; //To be produced to valorize livedata
//                    Map<String, Integer> restaurantByUsersCountMap = new HashMap<>();
//                    List<DocumentSnapshot> documents = queryDocumentSnapshots.getDocuments();
//                    for (DocumentSnapshot d : documents) {
//                        String restaurantId = d.getId();
//                        List<String> byUserIds = restaurantRepository.getByUserIds(d);
//                        Integer byUsersCount = byUserIds.size();
//                        restaurantByUsersCountMap.put(restaurantId, byUsersCount);
//                    }
//                    restaurantViewSates = generateRestaurantViewStates(restaurantByUsersCountMap);
//                    allRestaurantViewStatesMediatorLiveData.setValue(restaurantViewSates);
//                });
//    }

    private List<RestaurantViewSate> generateRestaurantViewStates(Map<String, Integer> restaurantByUsersCountMap) {
        List<RestaurantViewSate> restaurantViewSates = new ArrayList<>();
        Map<String, Restaurant> foundRestaurants = foundRestaurantsLiveData.getValue();
        if (foundRestaurants != null) {
            for (Map.Entry<String, Restaurant> restaurantEntry : foundRestaurants.entrySet()) {
                String placeId = restaurantEntry.getKey();
                Restaurant restaurant = restaurantEntry.getValue();
                // Restaurant photo
                Bitmap photo = restaurant.getPhoto();
                // Restaurant name
                String name = restaurant.getName();
                // Restaurant distance from current maps location
                String distance = this.getDistanceText(restaurant);
                // Restaurant address
                String address = restaurant.getAddress();
                if (address.endsWith(", France"))
                    address = address.substring(0, address.length() - 8);
                // Restaurant number of joining users and text visibility
                Object[] joinersArray = this.getJoinersArray(restaurantByUsersCountMap, placeId);
                String joiners = (String) joinersArray[0];
                boolean isJoinersVisible = (boolean) joinersArray[1];
                // Restaurant opening
                Object[] openingArray = this.getOpeningArray(restaurant);
                int openingPrefix = (int) openingArray[0];
                String closingTime = (String) openingArray[1];
                boolean isWarningStyle = (boolean) openingArray[2];
                // Restaurant rating range reduced to 3 stars
                int rating;
                rating = (int) (restaurant.getRating() * 3 / 5);

                // Restaurant view state creation
                RestaurantViewSate restaurantViewSate = new RestaurantViewSate(
                        placeId, photo, name, distance, address,
                        joiners, isJoinersVisible, openingPrefix, closingTime, isWarningStyle, rating);

                // Add view state in the list
                restaurantViewSates.add(restaurantViewSate);
            }
        }

        return restaurantViewSates;
    }

    @NonNull
    private String getDistanceText(Restaurant restaurant) {
        String distance;
        double startLat = restaurant.getLatLng().latitude;
        double startLng = restaurant.getLatLng().longitude;
        double endLat = this.getLocation().getLatitude();
        double endLng = this.getLocation().getLongitude();
        float[] results = new float[1];
        Location.distanceBetween(startLat, startLng, endLat, endLng, results);
        double result = Math.rint(results[0]);
        distance = String.valueOf(result).split("\\.")[0] + "m";
        return distance;
    }

    @NonNull
    private Object[] getOpeningArray(Restaurant restaurant) {
        Object[] openingArray = new Object[3];
        int openingPrefix;
        String closingTime = "";
        boolean isWarningStyle = true;

        Calendar nowCalendar = Calendar.getInstance();
        int todayDay = nowCalendar.get(Calendar.DAY_OF_WEEK) - 1;

        Period todayOpenings = restaurant.getOpeningHours().getPeriods().get(todayDay);

        if (todayOpenings.getClose() != null) {
            LocalTime closeTime = todayOpenings.getClose().getTime();
            int closeMn = closeTime.getHours() * 60 + closeTime.getMinutes();
            int nowMn = nowCalendar.get(Calendar.HOUR_OF_DAY) * 60 + nowCalendar.get(Calendar.MINUTE);
            int compareMn = closeMn - nowMn;
            if (compareMn <= 0) {
                openingPrefix = R.string.closed;
            } else if (compareMn < 60) {
                openingPrefix = R.string.closing_soon;
            } else {
                openingPrefix = R.string.open_until;
                closingTime = closeTime.getHours() + ":" + closeTime.getMinutes();
                isWarningStyle = false;
            }
        } else {
            openingPrefix = R.string.always_open;
            isWarningStyle = false;
        }
        openingArray[0] = openingPrefix;
        openingArray[1] = closingTime;
        openingArray[2] = isWarningStyle;

        return openingArray;
    }

    @NonNull
    private Object[] getJoinersArray(Map<String, Integer> restaurantByUsersNumberMap, String placeId) {
        Object[] joinersArray = new Object[2];
        String joiners;
        boolean isJoinersVisible;
        if (restaurantByUsersNumberMap.isEmpty()) {
            joiners = "";
            isJoinersVisible = false;
        } else {
            //noinspection ConstantConditions: placeId is a Key got from entrySet in generateRestaurantViewStates()
            int byUsersNumber = restaurantByUsersNumberMap.get(placeId);
            joiners = "(" + byUsersNumber + ")";
            isJoinersVisible = true;
        }
        joinersArray[0] = joiners;
        joinersArray[1] = isJoinersVisible;

        return joinersArray;
    }


    // --- RESTAURANT CHOOSING ---

    /**
     * Get all chosen restaurants all users combined
     *
     * @return a list of chosen restaurants place ids
     */
    public LiveData<List<String>> getChosenRestaurantIds() {
        return chosenRestaurantIdsLiveData;
    }


//    /**
//     * Get authenticated user chosen restaurant id
//     *
//     * @return the place id of the chosen restaurant
//     */
//    public LiveData<String> getCurrentUserChosenRestaurant() {
//        FirebaseUser firebaseUser = currentFirebaseUserLiveData.getValue();
//        String userId = firebaseUser != null ? firebaseUser.getUid() : "";
//        restaurantRepository.getChosenRestaurantsCollection()
//                .whereArrayContains(BYUSERS_FIELD, userId)
//                .get()
//                .addOnSuccessListener(queryDocumentSnapshots -> {
//                    List<DocumentSnapshot> documents = queryDocumentSnapshots.getDocuments();
//                    if (!documents.isEmpty()) {
//                        currentUserChosenRestaurantIdMediatorLiveData.setValue(documents.get(0).getId());
//                    } else {
//                        currentUserChosenRestaurantIdMediatorLiveData.setValue(null);
//                    }
//                });
//        return currentUserChosenRestaurantIdMediatorLiveData;
//    }

    /**
     * @return
     */
    public LiveData<Boolean> getIsChosen() { //TODO: to be moved in RestaurantDetailVM
        return isChosenMutableLiveData;
    }


    // --- Manage restaurant liking --- //TODO: to be moved in RestaurantDetailVM

    /**
     * @param restaurantId
     * @return
     */
    public LiveData<Boolean> setLikedRestaurant(String restaurantId) {
        return restaurantRepository.setLikedRestaurant(restaurantId);
    }

    /**
     * @param restaurantId
     * @return
     */
    public LiveData<Boolean> clearLikedRestaurant(String restaurantId) {
        return restaurantRepository.clearLikedRestaurant(restaurantId);
    }

    /**
     * @param restaurantId
     * @return
     */
    public LiveData<Boolean> getIsLiked(String restaurantId) {
        return restaurantRepository.getIsLikedRestaurant(restaurantId);
    }
}