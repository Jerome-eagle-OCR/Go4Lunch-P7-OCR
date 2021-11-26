package com.jr_eagle_ocr.go4lunch.ui.restaurant_detail;


import static com.jr_eagle_ocr.go4lunch.repositories.RestaurantRepository.PLACENAME_FIELD;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.View;

import androidx.core.util.Pair;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.jr_eagle_ocr.go4lunch.R;
import com.jr_eagle_ocr.go4lunch.model.User;
import com.jr_eagle_ocr.go4lunch.model.pojo.RestaurantPojo;
import com.jr_eagle_ocr.go4lunch.repositories.RestaurantRepository;
import com.jr_eagle_ocr.go4lunch.repositories.UserRepository;
import com.jr_eagle_ocr.go4lunch.ui.adapters.UserViewState;
import com.jr_eagle_ocr.go4lunch.usecases.GetCurrentUserChosenRestaurantId;
import com.jr_eagle_ocr.go4lunch.usecases.GetUserViewStates;
import com.jr_eagle_ocr.go4lunch.usecases.IsLikedRestaurant;
import com.jr_eagle_ocr.go4lunch.usecases.SetClearChosenRestaurant;
import com.jr_eagle_ocr.go4lunch.usecases.SetClearLikedRestaurant;
import com.jr_eagle_ocr.go4lunch.util.BitmapUtil;
import com.jr_eagle_ocr.go4lunch.util.Event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author jrigault
 */
public class RestaurantDetailViewModel extends ViewModel {
    private final String LIKE = "LIKE";
    private final String CHOOSE = "CHOOSE";
    private final LiveData<Map<String, User>> allUsersLiveData;
    private final RestaurantRepository restaurantRepository;
    private final LiveData<String> currentUserChosenRestaurantIdLiveData;
    private final IsLikedRestaurant isLikedRestaurant;
    private final LiveData<Boolean> isLikedRestaurantLiveData;
    private final GetUserViewStates getUserViewStates;
    private final SetClearChosenRestaurant setClearChosenRestaurant;
    private final SetClearLikedRestaurant setClearLikedRestaurant;
    private final MediatorLiveData<RestaurantDetailViewSate> restaurantDetailViewStateMediatorLiveData = new MediatorLiveData<>();
    private final MediatorLiveData<List<UserViewState>> joiningUserViewStatesMediatorLiveData = new MediatorLiveData<>();
    private final MediatorLiveData<Event<Integer>> snackbarMessageMediatorLiveData = new MediatorLiveData<>();
    private final String displayedRestaurantId;
    private RestaurantPojo restaurant;
    private int count = 0;

    public RestaurantDetailViewModel(
            String displayedRestaurantId,
            UserRepository userRepository,
            RestaurantRepository restaurantRepository,
            GetCurrentUserChosenRestaurantId getCurrentUserChosenRestaurantId,
            IsLikedRestaurant isLikedRestaurant,
            GetUserViewStates getUserViewStates,
            SetClearChosenRestaurant setClearChosenRestaurant,
            SetClearLikedRestaurant setClearLikedRestaurant
    ) {
        this.displayedRestaurantId = displayedRestaurantId;
        allUsersLiveData = userRepository.getAllUsers();
        this.restaurantRepository = restaurantRepository;
        currentUserChosenRestaurantIdLiveData = getCurrentUserChosenRestaurantId.getCurrentUserChosenRestaurantId();
        this.isLikedRestaurant = isLikedRestaurant;
        isLikedRestaurantLiveData = isLikedRestaurant.isLikedRestaurant();
        this.getUserViewStates = getUserViewStates;
        this.setClearChosenRestaurant = setClearChosenRestaurant;
        this.setClearLikedRestaurant = setClearLikedRestaurant;

        init();
    }

    /**
     * Valorize all depending on displayed restaurant and add all sources to mediators
     */
    private void init() {
        // Retrieve displayed restaurant
        if (restaurantRepository.getAllRestaurants().getValue() != null) {
            restaurant = restaurantRepository.getAllRestaurants().getValue().get(displayedRestaurantId);
        }
        // Listen to current user document in "liked_by" subcollection in displayed restaurant document in Firestore
        isLikedRestaurant.addListenerRegistration(displayedRestaurantId);

        // Add all mediators sources to properly trigger updating
        joiningUserViewStatesMediatorLiveData.addSource(allUsersLiveData, uidUserMap ->
                buildAndSetJoiningUserViewStates());
        LiveData<List<String>> chosenRestaurantIdsLiveData = restaurantRepository.getChosenRestaurantIds();
        joiningUserViewStatesMediatorLiveData.addSource(chosenRestaurantIdsLiveData, chosenRestaurantIds ->
                buildAndSetJoiningUserViewStates());

        restaurantDetailViewStateMediatorLiveData.addSource(currentUserChosenRestaurantIdLiveData, chosenRestaurantId ->
                buildAndSetRestaurantViewState());
        restaurantDetailViewStateMediatorLiveData.addSource(isLikedRestaurantLiveData, isLiked ->
                buildAndSetRestaurantViewState());
        restaurantDetailViewStateMediatorLiveData.addSource(joiningUserViewStatesMediatorLiveData, joiningUserViewStates ->
                buildAndSetRestaurantViewState());

        snackbarMessageMediatorLiveData.addSource(isLikedRestaurantLiveData, isLikedRestaurant -> {
            if (isLikedRestaurant != null) buildAndSetSnackbarMessage(LIKE, isLiked());
        });
        snackbarMessageMediatorLiveData.addSource(currentUserChosenRestaurantIdLiveData, chosenRestaurantId -> {
            if (displayedRestaurantId != null) buildAndSetSnackbarMessage(CHOOSE, isChosen());
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
    private void buildAndSetRestaurantViewState() {
        if (restaurant != null) {
            Bitmap photo = BitmapUtil.decodeBase64(restaurant.getPhotoString());
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
        Map<String, User> allUsers = allUsersLiveData.getValue();
        restaurantRepository.getChosenRestaurantsCollection()
                .whereEqualTo(FieldPath.documentId(), displayedRestaurantId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<UserViewState> userViewStates; //To be produced to valorize livedata
                    Map<String, Pair<String, String>> userChosenRestaurantMap = new HashMap<>();
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                        String placeId = document.getId();
                        String placeName = document.getString(PLACENAME_FIELD);
                        if (placeId.equals(displayedRestaurantId)) {
                            List<String> byUserIds = restaurantRepository.getByUserIds(document);
                            for (String uid : byUserIds) {
                                userChosenRestaurantMap.put(uid, new Pair<>(placeId, placeName));
                            }
                        }
                    }
                    userViewStates =
                            getUserViewStates.getUserViewStates(
                                    displayedRestaurantId, allUsers, userChosenRestaurantMap);
                    joiningUserViewStatesMediatorLiveData.setValue(userViewStates);
                });
    }

    /**
     * Called by activity on choose fab click
     */
    public void clickOnChooseFab() {
        boolean isChosen = !isChosen();
        if (isChosen) {
            setClearChosenRestaurant.setChosenRestaurant(displayedRestaurantId);
        } else {
            setClearChosenRestaurant.clearChosenRestaurant(displayedRestaurantId);
        }
    }

    /**
     * Called by activity when navigation item is selected
     *
     * @param itemOrder navigation selected item order
     * @return an intent to start an activity or null if action in VM only is needed
     */
    public Intent getIntentFromItemOrder(int itemOrder) {
        Intent intent = null;
        switch (itemOrder) {
            case 100:
                String phoneNumber = restaurant.getPhoneNumber();
                Uri tel = Uri.fromParts("tel", phoneNumber, null);
                intent = new Intent(Intent.ACTION_DIAL, tel);
                break;
            case 200:
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
    public LiveData<Event<Integer>> getSnackbarMessage() {
        return snackbarMessageMediatorLiveData;
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
    private void buildAndSetSnackbarMessage(String likeOrChoose, boolean isLikedChosen) {
        if (count++ > 2) {
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
            snackbarMessageMediatorLiveData.setValue(new Event<>(messageResource));
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        isLikedRestaurant.removeListenerRegistration();
    }
}
