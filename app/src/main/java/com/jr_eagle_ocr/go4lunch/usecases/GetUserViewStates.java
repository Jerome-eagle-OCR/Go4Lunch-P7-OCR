package com.jr_eagle_ocr.go4lunch.usecases;

import android.util.Log;

import androidx.core.util.Pair;
import androidx.lifecycle.LiveData;

import com.google.firebase.auth.FirebaseUser;
import com.jr_eagle_ocr.go4lunch.R;
import com.jr_eagle_ocr.go4lunch.model.User;
import com.jr_eagle_ocr.go4lunch.repositories.UserRepository;
import com.jr_eagle_ocr.go4lunch.ui.viewstates.UserViewState;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author jrigault
 */
public final class GetUserViewStates extends UseCase {
    private final LiveData<FirebaseUser> currentFirebaseUserLiveData;

    public GetUserViewStates(
            UserRepository userRepository
    ) {
        currentFirebaseUserLiveData = userRepository.getCurrentFirebaseUser();
    }

    /**
     * Generate a list of UserViewState
     *
     * @param displayedRestaurantId   extra from intent for RestaurantDetailActivity, or null for WorkmatesFragment
     * @param allUsers                all users map<user id, user> from UserRepository
     * @param userChosenRestaurantMap a map<user id, chosen restaurant id> to get the restaurant id for each user having chosen a restaurant
     * @return a list of UserViewState
     */
    public List<UserViewState> getUserViewStates(String displayedRestaurantId, Map<String, User> allUsers, Map<String, Pair<String, String>> userChosenRestaurantMap) {
        List<UserViewState> userViewStates = new ArrayList<>(); //To be produced to valorize livedata
        if (allUsers != null) {
            for (Map.Entry<String, User> userEntry : allUsers.entrySet()) {
                String uid = userEntry.getKey();
                User user = userEntry.getValue();
                // UserViewState parameters to set:
                String userName = user.getUserName();
                int appendingString;
                String userUrlPicture = user.getUserUrlPicture();
                String chosenRestaurantId = null;
                String chosenRestaurantName = null;
                float textAlpha;
                float imageAlpha;

                boolean hasChosen; // has currently iterated user chosen a restaurant ?
                hasChosen = userChosenRestaurantMap.containsKey(uid);
                boolean isCurrentUser; // is currently iterated user the authenticated user ?
                FirebaseUser currentUser = currentFirebaseUserLiveData.getValue();
                String currentUserUid = currentUser != null ? currentUser.getUid() : null;
                isCurrentUser = uid.equals(currentUserUid);
                // valorize parameters:
                if (displayedRestaurantId == null) {
                    appendingString = hasChosen ? R.string.is_eating_at : R.string.not_decided_yet;
                    Pair<String, String> restaurantPair;
                    if (hasChosen) {
                        restaurantPair = userChosenRestaurantMap.get(uid);
                        chosenRestaurantId = restaurantPair != null ? restaurantPair.first : null;
                        chosenRestaurantName = restaurantPair != null ? restaurantPair.second : null;
                    }
                } else {
                    appendingString = hasChosen ? R.string.is_joining : R.string.not_decided_yet;
                }
                textAlpha = hasChosen ? 1 : 0.3f;
                imageAlpha = hasChosen ? 1 : 0.6f;

                // create UserViewState and add it in list
                if ((displayedRestaurantId != null && hasChosen && !isCurrentUser) || (displayedRestaurantId == null && !isCurrentUser)) {
                    UserViewState userViewState = new UserViewState(
                            userName, appendingString, userUrlPicture,
                            null, chosenRestaurantId, chosenRestaurantName, textAlpha, imageAlpha);

                    userViewStates.add(userViewState);
                    Log.d(TAG, "getUserViewStates: userViewState added in list: " + userViewState.toString());
                }
            }
        }
        return userViewStates;
    }
}
