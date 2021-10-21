package com.jr_eagle_ocr.go4lunch.repositories;

import static com.jr_eagle_ocr.go4lunch.repositories.RestaurantRepository.PLACEID_FIELD;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.jr_eagle_ocr.go4lunch.R;
import com.jr_eagle_ocr.go4lunch.di.Go4LunchApplication;
import com.jr_eagle_ocr.go4lunch.model.Restaurant;
import com.jr_eagle_ocr.go4lunch.model.User;
import com.jr_eagle_ocr.go4lunch.model.UserViewState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TempUserRestaurantManager {

    private static final String TAG = "UserRestaurantManager";
    private static final TempUserRestaurantManager instance = new TempUserRestaurantManager();

    private final UserRepository userRepository;
    private final LiveData<Map<String, User>> allUsersLiveData;

    private final RestaurantRepository restaurantRepository;
    private final Map<String, Restaurant> mRestaurants;
    private final LiveData<List<String>> chosenRestaurantIdsLiveData;

    private final MutableLiveData<User> currentUserMutableLiveData;
    private final MediatorLiveData<List<UserViewState>> joiningUserViewStatesMediatorLiveData;
    private final MediatorLiveData<List<UserViewState>> allUserViewStatesMediatorLiveData;
    private final MutableLiveData<String> restaurantIdMutableLivedata;
    private final MutableLiveData<Boolean> isChosenMutableLiveData;

    private TempUserRestaurantManager() {
        userRepository = Go4LunchApplication.getDependencyContainer().getUserRepository();
        allUsersLiveData = userRepository.getAllUsers();

        restaurantRepository = Go4LunchApplication.getDependencyContainer().getRestaurantRepository();
        mRestaurants = restaurantRepository.getFoundRestaurants();
        chosenRestaurantIdsLiveData = restaurantRepository.getChosenRestaurantIds();

        currentUserMutableLiveData = new MutableLiveData<>();

        restaurantIdMutableLivedata = new MutableLiveData<>();
        joiningUserViewStatesMediatorLiveData = new MediatorLiveData<>();
        joiningUserViewStatesMediatorLiveData.addSource(allUsersLiveData, uidUserMap ->
                getByUsersThenUserViewStatesForGivenRestaurant()
        );
        joiningUserViewStatesMediatorLiveData.addSource(chosenRestaurantIdsLiveData, chosenRestaurantIds ->
                getByUsersThenUserViewStatesForGivenRestaurant()
        );
        joiningUserViewStatesMediatorLiveData.addSource(restaurantIdMutableLivedata, restaurantId ->
                getByUsersThenUserViewStatesForGivenRestaurant());
        isChosenMutableLiveData = new MutableLiveData<>();

        allUserViewStatesMediatorLiveData = new MediatorLiveData<>();
        allUserViewStatesMediatorLiveData.addSource(allUsersLiveData, uidUserMap ->
                getByUsersThenUserViewStatesForAllRestaurants()
        );
        allUserViewStatesMediatorLiveData.addSource(chosenRestaurantIdsLiveData, chosenRestaurantIds ->
                getByUsersThenUserViewStatesForAllRestaurants()
        );
    }

    public static TempUserRestaurantManager getInstance() {
        return instance;
    }

    // --- UserRepository section ---

    /**
     * @return
     */
    public FirebaseUser getCurrentFirebaseUser() {
        return userRepository.getCurrentFirebaseUser();
    }

    /**
     * Create User in Firestore from FirebaseAuth eventually modified/completed with Firestore db infos
     */
    public LiveData<Boolean> createUser() {
        return userRepository.createUser();
    }

    /**
     * Get User Data from Firestore (livedata valorized by getUserDataFromFirestore())
     *
     * @return the current user in a livedata
     */
    public LiveData<User> getUserData() {
        Task<DocumentSnapshot> getUserData = userRepository.getUserData();
        if (getUserData != null) {
            getUserData.continueWith(task ->
                    task.getResult().getReference()
                            .addSnapshotListener((value, error) -> {
                                if (value != null && value.exists()) {
                                    User currentUser = value.toObject(User.class);
                                    currentUserMutableLiveData.setValue(currentUser);
                                } else if (error != null) {
                                    Log.e(TAG, "getUserDataFromFirestore: ", error);
                                    currentUserMutableLiveData.setValue(null);
                                }
                            }));
        } else {
            currentUserMutableLiveData.setValue(null);
        }
        return currentUserMutableLiveData;
    }


    // --- RestaurantRepository section

    /**
     * @return
     */
    public Map<String, Restaurant> getFoundRestaurants() {
        return restaurantRepository.getFoundRestaurants();
    }

    /**
     * @param restaurant
     */
    public void addFoundRestaurant(Restaurant restaurant) {
        restaurantRepository.addFoundRestaurant(restaurant);
    }

    /**
     * Get all chosen restaurants all users combined
     *
     * @return a list of chosen restaurants place ids
     */
    public LiveData<List<String>> getChosenRestaurantIds() {
        return chosenRestaurantIdsLiveData;
    }

    /**
     * Set the chosen restaurant for the current user
     *
     * @param restaurantId the chosen restaurant place id
     * @return true if setting has succeeded, false if not
     */
    public LiveData<Boolean> setChosenRestaurant(String restaurantId) {
        return restaurantRepository.setChosenRestaurant(restaurantId);
    }

    /**
     * Clear the chosen restaurant for the current user
     *
     * @param restaurantId the chosen restaurant place id
     * @return true if clearing has succeeded, false if not
     */
    public LiveData<Boolean> clearChosenRestaurant(String restaurantId) {
        return restaurantRepository.clearChosenRestaurant(restaurantId);
    }

    /**
     * Get all users or only users joining at a specific restaurant,
     * depending on placeId null or not
     *
     * @param restaurantId the restaurant id for users having chosen this one, or null for all users
     * @return a list of user view state to feed UserAdapter
     */
    public LiveData<List<UserViewState>> getJoiningUsers(String restaurantId) {
        restaurantIdMutableLivedata.setValue(restaurantId);
        return joiningUserViewStatesMediatorLiveData;
    }

    /**
     * @return
     */
    public LiveData<List<UserViewState>> getAllUsers() {
        return allUserViewStatesMediatorLiveData;
    }

    /**
     *
     */
    private void getByUsersThenUserViewStatesForGivenRestaurant() {
        Map<String, User> allUsers = allUsersLiveData.getValue();
        restaurantRepository.getChosenRestaurantsCollection()
                .whereIn(PLACEID_FIELD, Collections.singletonList(this.restaurantIdMutableLivedata.getValue()))
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<UserViewState> userViewStates; //To be produced to valorize livedata
                    Map<String, String> userChosenRestaurantMap = new HashMap<>();
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                        String placeId = document.getId();
                        if (placeId.equals(restaurantIdMutableLivedata.getValue())) {
                            List<String> byUserIds = restaurantRepository.getByUserIds(document);
                            for (String uid : byUserIds) {
                                userChosenRestaurantMap.put(uid, placeId);
                            }
                        }
                    }
                    userViewStates = generateUserViewStates(restaurantIdMutableLivedata.getValue(), allUsers, userChosenRestaurantMap);
                    joiningUserViewStatesMediatorLiveData.setValue(userViewStates);
                });
    }

    /**
     *
     */
    private void getByUsersThenUserViewStatesForAllRestaurants() {
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
    }

//    /**
//     *
//     */
//    private void getByUsersThenUserViewStates() {
//        if (this.restaurantIdMutableLivedata == null) {
//            Map<String, User> allUsers = allUsersLiveData.getValue();
//            restaurantRepository.getChosenRestaurantsCollection()
//                    .get()
//                    .addOnSuccessListener(queryDocumentSnapshots -> {
//                        List<UserViewState> userViewStates; //To be produced to valorize livedata
//                        Map<String, String> userChosenRestaurantMap = new HashMap<>();
//                        List<DocumentSnapshot> documents = queryDocumentSnapshots.getDocuments();
//                        for (DocumentSnapshot d : documents) {
//                            String restaurantId = d.getId();
//                            List<String> byUserIds = restaurantRepository.getByUserIds(d);
//                            for (int i = 0; i < byUserIds.size(); i++) {
//                                String iUser = byUserIds.get(i);
//                                userChosenRestaurantMap.put(iUser, restaurantId);
//                            }
//                        }
//                        userViewStates = generateUserViewStates(allUsers, userChosenRestaurantMap);
//                        joiningUserViewStatesMediatorLiveData.setValue(userViewStates);
//                    });
//        } else {
//            Map<String, User> allUsers = allUsersLiveData.getValue();
//            restaurantRepository.getChosenRestaurantsCollection()
//                    .whereIn(PLACEID_FIELD, Collections.singletonList(this.restaurantIdMutableLivedata))
//                    .get()
//                    .addOnSuccessListener(queryDocumentSnapshots -> {
//                        List<UserViewState> userViewStates; //To be produced to valorize livedata
//                        Map<String, String> userChosenRestaurantMap = new HashMap<>();
//                        if (!queryDocumentSnapshots.isEmpty()) {
//                            DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
//                            String placeId = document.getId();
//                            if (placeId.equals(this.restaurantIdMutableLivedata.getValue())) {
//                                List<String> byUserIds = restaurantRepository.getByUserIds(document);
//                                for (String uid : byUserIds) {
//                                    userChosenRestaurantMap.put(uid, placeId);
//                                }
//                            }
//                        }
//                        userViewStates = generateUserViewStates(allUsers, userChosenRestaurantMap);
//                        joiningUserViewStatesMediatorLiveData.setValue(userViewStates);
//                    });
//        }
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
                boolean hasChosen;
                boolean isChosen = false;
                hasChosen = userChosenRestaurantMap.containsKey(uid);
                restaurantId = hasChosen ? userChosenRestaurantMap.get(uid) : null;
                if (displayedRestaurantId == null) {
                    appendingString = hasChosen ? R.string.is_eating_at : R.string.not_decided_yet;
                    if (restaurantId != null) {
                        Restaurant restaurant = mRestaurants.get(restaurantId);
                        if (restaurant != null) restaurantName = restaurant.getName();
                    }
                } else {
                    appendingString = hasChosen ? R.string.is_joining : R.string.not_decided_yet;
                    String connectedUserUid = this.getCurrentFirebaseUser().getUid();
                    if (connectedUserUid.equals(uid)) {
                        isChosen = displayedRestaurantId.equals(restaurantId);
                        isChosenMutableLiveData.setValue(isChosen);
                        userName = "";
                        appendingString = R.string.you_lunch_here;
                    }
                }
                float textAlpha = hasChosen ? 1 : 0.3f;
                float imageAlpha = hasChosen ? 1 : 0.6f;

                if (!(displayedRestaurantId != null && !hasChosen)) {
                    UserViewState userViewState = new UserViewState(
                            userName, appendingString, userUrlPicture,
                            restaurantId, restaurantName, textAlpha, imageAlpha);
                    if (isChosen) {
                        userViewStates.add(0, userViewState);
                    } else {
                        userViewStates.add(userViewState);
                    }
                }
            }
        }
        return userViewStates;
    }

    /**
     * @return
     */
    public LiveData<Boolean> getIsChosen() {
        return isChosenMutableLiveData;
    }

    /**
     * @param restaurantId
     * @return
     */
    public LiveData<Boolean> getIsLiked(String restaurantId) {
        return restaurantRepository.getIsLikedRestaurant(restaurantId);
    }

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
}