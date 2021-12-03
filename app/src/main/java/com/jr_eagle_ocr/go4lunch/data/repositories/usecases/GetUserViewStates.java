package com.jr_eagle_ocr.go4lunch.data.repositories.usecases;

import android.util.Log;

import androidx.core.util.Pair;

import com.jr_eagle_ocr.go4lunch.R;
import com.jr_eagle_ocr.go4lunch.data.models.User;
import com.jr_eagle_ocr.go4lunch.data.repositories.usecases.parent.UseCase;
import com.jr_eagle_ocr.go4lunch.ui.adapters.UserViewState;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author jrigault
 */
public final class GetUserViewStates extends UseCase {
    private final User currentUser;

    public GetUserViewStates(
            User currentUser
    ) {
        this.currentUser = currentUser;
    }

    /**
     * Generate a list of UserViewState
     *
     * @param displayedRestaurantId   extra from intent for RestaurantDetailActivity, or null for WorkmatesFragment
     * @param allLoggedUsers          all users map<user id, user> from UserRepository
     * @param userChosenRestaurantMap a map<user id, chosen restaurant id> to get the restaurant id for each user having chosen a restaurant
     * @return a list of UserViewState
     */
    public List<UserViewState> getUserViewStates(String displayedRestaurantId, Map<String, User> allLoggedUsers,
                                                 Map<String, Pair<String, String>> userChosenRestaurantMap) {
        List<UserViewState> userViewStates = new ArrayList<>(); //To be produced to valorize livedata
        if (allLoggedUsers != null) {
            for (Map.Entry<String, User> userEntry : allLoggedUsers.entrySet()) {
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
                // has currently iterated user chosen a restaurant ?
                boolean hasChosen;
                hasChosen = userChosenRestaurantMap.containsKey(uid);
                // is currently iterated user the authenticated user ?
                boolean isCurrentUser;
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
                if ((displayedRestaurantId != null && hasChosen && !isCurrentUser)
                        || (displayedRestaurantId == null && !isCurrentUser)) {

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
