package com.jr_eagle_ocr.go4lunch.ui.restaurant_detail;


import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.view.View;

import androidx.core.util.Pair;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import com.jr_eagle_ocr.go4lunch.R;
import com.jr_eagle_ocr.go4lunch.data.models.ChosenRestaurant;
import com.jr_eagle_ocr.go4lunch.data.models.Restaurant;
import com.jr_eagle_ocr.go4lunch.data.models.User;
import com.jr_eagle_ocr.go4lunch.data.repositories.RestaurantRepository;
import com.jr_eagle_ocr.go4lunch.data.repositories.UserRepository;
import com.jr_eagle_ocr.go4lunch.data.usecases.GetCurrentUserChosenRestaurantId;
import com.jr_eagle_ocr.go4lunch.data.usecases.GetIsLikedRestaurant;
import com.jr_eagle_ocr.go4lunch.data.usecases.GetUserViewStates;
import com.jr_eagle_ocr.go4lunch.data.usecases.SetClearChosenRestaurant;
import com.jr_eagle_ocr.go4lunch.data.usecases.SetClearLikedRestaurant;
import com.jr_eagle_ocr.go4lunch.ui.adapters.UserViewState;
import com.jr_eagle_ocr.go4lunch.util.BitmapUtil;
import com.jr_eagle_ocr.go4lunch.util.Event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author jrigault
 */
public class RestaurantDetailViewModel extends ViewModel {
    private final String TAG = getClass().getSimpleName();
    private final String LIKE = "LIKE";
    private final String CHOOSE = "CHOOSE";
    private final LiveData<User> currentUserLiveData;
    private final LiveData<Map<String, User>> allLoggedUsersLiveData;
    private final LiveData<Map<String, Restaurant>> allRestaurantsLiveData;
    private final LiveData<Map<ChosenRestaurant, List<String>>> restaurantChosenByUserIdsMapLiveData;
    private final LiveData<String> currentUserChosenRestaurantIdLiveData;
    private final GetIsLikedRestaurant getIsLikedRestaurant;
    private final LiveData<Boolean> isLikedRestaurantLiveData;
    private final GetUserViewStates getUserViewStates;
    private final SetClearChosenRestaurant setClearChosenRestaurant;
    private final SetClearLikedRestaurant setClearLikedRestaurant;
    private final BitmapUtil bitmapUtil;
    private final MediatorLiveData<RestaurantDetailViewSate> restaurantDetailViewStateMediatorLiveData = new MediatorLiveData<>();
    private final MediatorLiveData<List<UserViewState>> joiningUserViewStatesMediatorLiveData = new MediatorLiveData<>();
    private final MediatorLiveData<Event<Integer>> snackbarMessageEventMediatorLiveData = new MediatorLiveData<>();
    private final String displayedRestaurantId;
    private Restaurant restaurant;
    // Both booleans not to have snackbars at activity starting
    private boolean isLikeClickedOnce = false;
    private boolean isChooseClickedOnce = false;
    // Not to have snackbar when clearing current chosen restaurant prior to setting the displayed one
    private boolean blockNextChosenSnackbar = false;

    public RestaurantDetailViewModel(
            String displayedRestaurantId,
            BitmapUtil bitmapUtil,
            UserRepository userRepository,
            RestaurantRepository restaurantRepository,
            GetCurrentUserChosenRestaurantId getCurrentUserChosenRestaurantId,
            GetIsLikedRestaurant getIsLikedRestaurant,
            SetClearChosenRestaurant setClearChosenRestaurant,
            SetClearLikedRestaurant setClearLikedRestaurant
    ) {
        this.displayedRestaurantId = displayedRestaurantId;
        currentUserLiveData = userRepository.getCurrentUser();
        allLoggedUsersLiveData = userRepository.getAllLoggedUsers();
        allRestaurantsLiveData = restaurantRepository.getAllRestaurants();
        restaurantChosenByUserIdsMapLiveData = restaurantRepository.getChosenRestaurantByUserIdsMap();
        currentUserChosenRestaurantIdLiveData = getCurrentUserChosenRestaurantId.getCurrentUserChosenRestaurantId();
        this.getIsLikedRestaurant = getIsLikedRestaurant;
        isLikedRestaurantLiveData = getIsLikedRestaurant.isLikedRestaurant();
        this.getUserViewStates = new GetUserViewStates(currentUserLiveData.getValue());
        this.setClearChosenRestaurant = setClearChosenRestaurant;
        this.setClearLikedRestaurant = setClearLikedRestaurant;
        this.bitmapUtil = bitmapUtil;

        init();
    }

    /**
     * Valorize all depending on displayed restaurant and add all sources to mediators
     */
    private void init() {
        // Retrieve displayed restaurant
        if (allRestaurantsLiveData.getValue() != null) {
            restaurant = allRestaurantsLiveData.getValue().get(displayedRestaurantId);
        }
        // Listen to current user document in "liked_by" subcollection in displayed restaurant document in Firestore
        getIsLikedRestaurant.addListenerRegistration(displayedRestaurantId);

        // Add all mediators sources to properly trigger updating
        joiningUserViewStatesMediatorLiveData.addSource(currentUserLiveData, currentUser ->
                buildAndSetJoiningUserViewStates());
        joiningUserViewStatesMediatorLiveData.addSource(allLoggedUsersLiveData, allLoggedUsers ->
                buildAndSetJoiningUserViewStates());
        joiningUserViewStatesMediatorLiveData.addSource(restaurantChosenByUserIdsMapLiveData, restaurantChosenByUserIdsMap ->
                buildAndSetJoiningUserViewStates());

        restaurantDetailViewStateMediatorLiveData.addSource(currentUserChosenRestaurantIdLiveData, chosenRestaurantId ->
                buildAndSetRestaurantDetailViewState());
        restaurantDetailViewStateMediatorLiveData.addSource(isLikedRestaurantLiveData, isLiked ->
                buildAndSetRestaurantDetailViewState());
        restaurantDetailViewStateMediatorLiveData.addSource(joiningUserViewStatesMediatorLiveData, joiningUserViewStates ->
                buildAndSetRestaurantDetailViewState());

        snackbarMessageEventMediatorLiveData.addSource(isLikedRestaurantLiveData, isLikedRestaurant -> {
            if (isLikedRestaurant != null && isLikeClickedOnce) {
                buildAndSetSnackbarMessageEvent(LIKE, isLiked());
            }
        });
        snackbarMessageEventMediatorLiveData.addSource(currentUserChosenRestaurantIdLiveData, chosenRestaurantId -> {
            if (displayedRestaurantId != null && chosenRestaurantId != null && isChooseClickedOnce && !blockNextChosenSnackbar) {
                buildAndSetSnackbarMessageEvent(CHOOSE, isChosen());
            }
            blockNextChosenSnackbar = false;
        });
    }

    /**
     * Observed by activity to get the up-to-date restaurant view state to feed the view,
     *
     * @return a restaurant view state livedata
     */
    public LiveData<RestaurantDetailViewSate> getRestaurantViewState() {
        return restaurantDetailViewStateMediatorLiveData;
    }

    /**
     * Build and set the restaurant view state to feed the view,
     * based on the displayed restaurant,
     * including the joining user view states
     */
    private void buildAndSetRestaurantDetailViewState() {
        if (restaurant != null
                && getIsLikedRestaurant != null
                && currentUserChosenRestaurantIdLiveData != null) {
            Bitmap photo = bitmapUtil.decodeBase64(restaurant.getPhotoString());
            String name = restaurant.getName();
            String address = restaurant.getAddress();
            boolean isChosen = isChosen();
            int chosenResource = isChosen ? R.drawable.ic_check_circle : R.drawable.ic_circle;
            boolean isLiked = isLiked();
            int likeVisibility = isLiked ? View.VISIBLE : View.INVISIBLE;
            RestaurantDetailViewSate restaurantDetailViewState =
                    new RestaurantDetailViewSate(
                            displayedRestaurantId,
                            photo,
                            name,
                            address,
                            chosenResource,
                            likeVisibility,
                            joiningUserViewStatesMediatorLiveData.getValue());
            restaurantDetailViewStateMediatorLiveData.setValue(restaurantDetailViewState);
        }
    }

    /**
     * Build and set the list of user view state to feed UserAdapter,
     * based on users lunching at the displayed restaurant
     */
    public void buildAndSetJoiningUserViewStates() {
        List<UserViewState> userViewStates = new ArrayList<>(); //To be produced to valorize livedata
        User currentUser = currentUserLiveData.getValue();
        if (currentUser != null) {
            Map<ChosenRestaurant, List<String>> restaurantChosenByUserIdsMap = restaurantChosenByUserIdsMapLiveData.getValue();
            if (restaurantChosenByUserIdsMap != null) {
                Map<String, Pair<String, String>> userChosenRestaurantMap = new HashMap<>(); //To be valorized to get userviewstates
                for (Map.Entry<ChosenRestaurant, List<String>> entry : restaurantChosenByUserIdsMap.entrySet()) {
                    String placeId = entry.getKey().getPlaceId();
                    if (placeId.equals(displayedRestaurantId)) {
                        String placeName = entry.getKey().getPlaceName();
                        List<String> chosenByUserIds = entry.getValue();
                        for (int i = 0; i < chosenByUserIds.size(); i++) {
                            String uid = chosenByUserIds.get(i);
                            if (!uid.equals(currentUser.getUid())) {
                                userChosenRestaurantMap.put(uid, new Pair<>(placeId, placeName));
                            }
                        }
                        break;
                    }
                }
                Map<String, User> allLoggedUsers = allLoggedUsersLiveData.getValue();
                userViewStates = getUserViewStates.getUserViewStates(displayedRestaurantId,
                        allLoggedUsers, userChosenRestaurantMap);
            }
        }
        joiningUserViewStatesMediatorLiveData.setValue(userViewStates);
        Log.d(TAG, "buildAndSetAllUserViewStates: " + userViewStates.size() + " workmates");
    }

    /**
     * Called by activity on choose fab click
     */
    public void clickOnChooseFab() {
        isChooseClickedOnce = true;
        boolean isChosen = !isChosen();
        if (isChosen) {
            if (currentUserChosenRestaurantIdLiveData.getValue() != null
                    && !currentUserChosenRestaurantIdLiveData.getValue().isEmpty()) {
                blockNextChosenSnackbar = true;
            }
            setClearChosenRestaurant.setChosenRestaurant(displayedRestaurantId);
        } else {
            setClearChosenRestaurant.clearChosenRestaurant();
        }
    }

    /**
     * Called by activity when navigation item is selected
     *
     * @param itemOrder navigation selected item order
     * @return an intent to start an activity or null if action in VM only is needed
     */
    public Intent getIntent(int itemOrder) {
        Intent intent = null;
        switch (itemOrder) {
            case 100:
                String phoneNumber = restaurant.getPhoneNumber();
                Uri tel = Uri.fromParts("tel", phoneNumber, null);
                intent = new Intent(Intent.ACTION_DIAL, tel);
                break;
            case 200:
                isLikeClickedOnce = true;
                boolean isLiked = !isLiked();
                if (isLiked) {
                    setClearLikedRestaurant.setLikedRestaurant(displayedRestaurantId);
                } else {
                    setClearLikedRestaurant.clearLikedRestaurant(displayedRestaurantId);
                }
                break;
            case 300:
                String websiteUrl = restaurant.getWebSiteUrl();
                Uri url = Uri.parse(websiteUrl);
                intent = new Intent(Intent.ACTION_VIEW, url);
                break;
        }
        return intent;
    }

    /**
     * Observed by activity to trigger snackbar popping
     *
     * @return an event to manage handling of contained string resource integer value
     */
    public LiveData<Event<Integer>> getSnackbarMessageEvent() {
        return snackbarMessageEventMediatorLiveData;
    }

    /**
     * Check on demand if displayed restaurant is chosen
     *
     * @return a boolean
     */
    private boolean isChosen() {
        return displayedRestaurantId.equals(currentUserChosenRestaurantIdLiveData.getValue());
    }

    /**
     * Check on demand if displayed restaurant is liked
     *
     * @return a boolean
     */
    private boolean isLiked() {
        Boolean isLiked = isLikedRestaurantLiveData.getValue();
        return isLiked != null ? isLiked : false;
    }

    /**
     * Build and set the snackbar message to be popped in activity
     *
     * @param likeOrChoose  string constant precising the event
     * @param isLikedChosen boolean precising the new state
     */
    private void buildAndSetSnackbarMessageEvent(String likeOrChoose, boolean isLikedChosen) {
        int messageResource;
        switch (likeOrChoose) {
            case LIKE:
                messageResource =
                        isLikedChosen ? R.string.like_restaurant : R.string.unlike_restaurant;
                break;
            case CHOOSE:
                messageResource =
                        isLikedChosen ? R.string.choose_restaurant : R.string.unchoose_restaurant;
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + likeOrChoose);
        }
        snackbarMessageEventMediatorLiveData.setValue(new Event<>(messageResource));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        getIsLikedRestaurant.removeListenerRegistration();
    }
}
