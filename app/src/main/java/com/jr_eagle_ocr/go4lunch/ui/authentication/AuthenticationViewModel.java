package com.jr_eagle_ocr.go4lunch.ui.authentication;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseUser;
import com.jr_eagle_ocr.go4lunch.repositories.RestaurantRepository;
import com.jr_eagle_ocr.go4lunch.repositories.UserRepository;
import com.jr_eagle_ocr.go4lunch.util.Event;

/**
 * @author jrigault
 */
public class AuthenticationViewModel extends ViewModel {
    public static final String AUTHENTICATE = "AUTHENTICATE";
    public static final String NAVIGATE_TO_MAIN = "NAVIGATE_TO_MAIN";
    public static final String TOAST_AUTH_SUCCESS = "TOAST_MESSAGE";
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final LiveData<Boolean> isUserCreatedLiveData;
    private final Observer<Boolean> isUserCreatedObserver;
    private final MutableLiveData<Event<String>> actionEventMutableLiveData = new MutableLiveData<>();

    public AuthenticationViewModel(
            UserRepository userRepository,
            RestaurantRepository restaurantRepository
    ) {
        this.userRepository = userRepository;
        this.restaurantRepository = restaurantRepository;

        // Test if user is already authenticated,
        // if yes send event to navigate to MainActivity,
        // if not send event to start authentication
        FirebaseUser firebaseUser = userRepository.getCurrentFirebaseUser().getValue();
        if (firebaseUser != null) {
            setRestaurantListeners();
            actionEventMutableLiveData.setValue(new Event<>(NAVIGATE_TO_MAIN));
        } else {
            actionEventMutableLiveData.setValue(new Event<>(AUTHENTICATE));
        }

        // Listen user creation with observer that will be removed in onCleared()
        isUserCreatedLiveData = userRepository.isUserCreated();
        isUserCreatedObserver = isUserCreated -> {
            actionEventMutableLiveData.setValue(new Event<>(TOAST_AUTH_SUCCESS));
            actionEventMutableLiveData.setValue(new Event<>(NAVIGATE_TO_MAIN));
        };
        isUserCreatedLiveData.observeForever(isUserCreatedObserver);
    }

    /**
     * Set restaurants and chosen_restaurants Firestore collections listeners
     * Not needed for users collection as it is triggered by Firebase authStateChanged
     */
    private void setRestaurantListeners() {
        restaurantRepository.setAllRestaurants();
        restaurantRepository.setChosenRestaurantIdsAndCleanCollection();
    }

    /**
     * Set restaurant listeners and create user as authentication is successful
     */
    public void setAuthenticationSuccessful() {
        setRestaurantListeners();
        userRepository.createUser();
    }

    /**
     * Get the action to do as an event (allows single live event)
     *
     * @return an event in a livedata
     */
    public LiveData<Event<String>> doActionEvent() {
        return actionEventMutableLiveData;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        isUserCreatedLiveData.removeObserver(isUserCreatedObserver);
    }
}
