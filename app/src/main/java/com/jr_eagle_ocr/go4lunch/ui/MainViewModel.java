package com.jr_eagle_ocr.go4lunch.ui;

import static com.jr_eagle_ocr.go4lunch.data.repositories.usecases.PlaceAutocompleteSearch.NO_SEARCH_MADE;
import static com.jr_eagle_ocr.go4lunch.data.repositories.usecases.PlaceAutocompleteSearch.OK;
import static com.jr_eagle_ocr.go4lunch.data.repositories.usecases.PlaceAutocompleteSearch.REQUEST_DENIED;
import static com.jr_eagle_ocr.go4lunch.data.repositories.usecases.PlaceAutocompleteSearch.ZERO_RESULTS;

import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseUser;
import com.jr_eagle_ocr.go4lunch.R;
import com.jr_eagle_ocr.go4lunch.data.models.User;
import com.jr_eagle_ocr.go4lunch.data.repositories.LocationRepository;
import com.jr_eagle_ocr.go4lunch.data.repositories.RestaurantRepository;
import com.jr_eagle_ocr.go4lunch.data.repositories.UserRepository;
import com.jr_eagle_ocr.go4lunch.data.repositories.usecases.GetCurrentUserChosenRestaurantId;
import com.jr_eagle_ocr.go4lunch.data.repositories.usecases.PlaceAutocompleteSearch;
import com.jr_eagle_ocr.go4lunch.util.Event;

import java.util.Calendar;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;

/**
 * @author jrigault
 */
public class MainViewModel extends ViewModel {
    private final String TAG = getClass().getSimpleName();
    public static final String MAPVIEW = "MAPVIEW";
    public static final String LISTVIEW = "LISTVIEW";
    public static final String WORKMATES = "WORKMATES";
    public static final String SET_WORKER = "SET_WORKER";
    public static final String CANCEL_WORKER = "UNSET_WORKER";
    public static final String SET_ALARM = "SET_ALARM";
    public static final String CANCEL_ALARM = "CANCEL_ALARM";
    private final boolean jumpToRestaurantDetail;
    private final LocationRepository locationRepository;
    private final LiveData<Boolean> locationPermissionGrantedLiveData;
    private final Observer<Boolean> locationPermissionGrantedObserver;
    private final MutableLiveData<String> currentFragmentMutableLiveData = new MutableLiveData<>();
    private final RestaurantRepository restaurantRepository;
    private final PlaceAutocompleteSearch placeAutocompleteSearch;
    private Disposable disposable;
    private final MutableLiveData<AutocompleteRestaurantViewState[]> autocompleteRestaurantArrayMutableLiveData = new MutableLiveData<>();
    private final MutableLiveData<AutocompleteRestaurantViewState> selectedItemMutableLiveData = new MutableLiveData<>();
    private final LiveData<FirebaseUser> currentFirebaseUserLiveData;
    private final LiveData<User> currentUserLiveData;
    private final LiveData<String> currentUserChosenRestaurantIdLiveData;
    private final MediatorLiveData<MainViewState> currentUserViewStateMediatorLiveData;
    private final MutableLiveData<Event<Integer>> navigateToEventMutableLiveData = new MutableLiveData<>();
    private final MutableLiveData<Event<Integer>> toastMessageEventMutableLiveData = new MutableLiveData<>();
    private long aboutNoonMillis;
    private long initialDelay;

    public MainViewModel(
            boolean jumpToRestaurantDetail,
            LocationRepository locationRepository,
            UserRepository userRepository,
            RestaurantRepository restaurantRepository,
            PlaceAutocompleteSearch placeAutocompleteSearch,
            GetCurrentUserChosenRestaurantId getCurrentUserChosenRestaurantId
    ) {
        this.jumpToRestaurantDetail = jumpToRestaurantDetail;

        this.locationRepository = locationRepository;
        locationPermissionGrantedLiveData = locationRepository.getLocationPermissionGranted();
        locationPermissionGrantedObserver = aBoolean -> {
            if (aBoolean != null && !aBoolean
                    && currentFragmentMutableLiveData.getValue() != null && !currentFragmentMutableLiveData.getValue().equals(MAPVIEW)) {
                this.navigationItemSelected(R.id.nav_map_view);
            }
        };
        locationPermissionGrantedLiveData.observeForever(locationPermissionGrantedObserver);

        this.restaurantRepository = restaurantRepository;
        this.placeAutocompleteSearch = placeAutocompleteSearch;

        currentFirebaseUserLiveData = userRepository.getCurrentFirebaseUser();

        currentUserLiveData = userRepository.getCurrentUser();
        currentUserChosenRestaurantIdLiveData = getCurrentUserChosenRestaurantId.getCurrentUserChosenRestaurantId();

        currentUserViewStateMediatorLiveData = new MediatorLiveData<>();
        currentUserViewStateMediatorLiveData.addSource(currentUserLiveData, currentUser ->
                setMainViewState());
        currentUserViewStateMediatorLiveData.addSource(currentUserChosenRestaurantIdLiveData, chosenRestaurantId ->
                setMainViewState());

        setAutocompleteRestaurantArray();
    }

    // --- CURRENT FRAGMENT ---

    public LiveData<String> getCurrentFragment() {
        return currentFragmentMutableLiveData;
    }

    public void setCurrentFragment(String currentFragment) {
        currentFragmentMutableLiveData.setValue(currentFragment);
    }


    // --- LOCATION PERMISSION ---

    public LiveData<Boolean> getLocationPermissionGranted() {
        return locationPermissionGrantedLiveData;
    }

    public void setLocationPermissionGranted(boolean locationPermissionGranted) {
        locationRepository.setLocationPermissionGranted(locationPermissionGranted);
    }


    // --- REMINDER VARIABLES ---

    public long getAlarmTrigger() {
        return aboutNoonMillis;
    }

    public long getInitialDelay() {
        return initialDelay;
    }

    /**
     *
     */
    private void setReminderVariables() {
        int hour = 11;
        int minute = 59;

        Calendar c = Calendar.getInstance();
        long nowMillis = c.getTimeInMillis();
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, 30);
        aboutNoonMillis = c.getTimeInMillis();
        initialDelay = aboutNoonMillis - nowMillis;
    }


    // --- MAIN VIEW STATE ---

    public LiveData<MainViewState> getMainViewState() {
        return currentUserViewStateMediatorLiveData;
    }

    /**
     *
     */
    private void setMainViewState() {
        MainViewState mainViewState;
        User user = currentUserLiveData.getValue();
        String action;
        if (user != null) {
            String userName = user.getUserName();
            String userUrlPicture = user.getUserUrlPicture();
            String userEmail = user.getUserEmail();
            String userChosenRestaurantId = currentUserChosenRestaurantIdLiveData.getValue();
            boolean isNoonReminderEnabled = user.isNoonReminderEnabled();
            setReminderVariables();
            if (this.getInitialDelay() > 0 && isNoonReminderEnabled
                    && userChosenRestaurantId != null && !userChosenRestaurantId.isEmpty()) {
                action = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) ? SET_WORKER : SET_ALARM;
                Log.d(TAG, "getMainViewState: initialDelay = " + this.getInitialDelay());
            } else {
                action = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) ? CANCEL_WORKER : CANCEL_ALARM;
            }
            mainViewState = new MainViewState(
                    userName,
                    userUrlPicture != null ? userUrlPicture : "",
                    userEmail,
                    userChosenRestaurantId != null ? userChosenRestaurantId : "",
                    action);
        } else {
            action = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) ? CANCEL_WORKER : CANCEL_ALARM;
            mainViewState = new MainViewState(
                    "",
                    null,
                    "",
                    null,
                    action);
        }
        currentUserViewStateMediatorLiveData.setValue(mainViewState);
        checkAndNavigateIfNeeded();
    }


    // --- NAVIGATION ---

    /**
     *
     */
    private void checkAndNavigateIfNeeded() {
        if (currentFirebaseUserLiveData.getValue() == null) {
            this.navigationItemSelected(R.id.authentication);
            restaurantRepository.unsetChosenRestaurantIdsAndCleanCollection();
            restaurantRepository.unsetAllRestaurants();
        } else if (jumpToRestaurantDetail) {
            this.navigationItemSelected(R.id.nav_your_lunch);
        }
    }

    /**
     * @param navigationItemId
     */
    public void navigationItemSelected(int navigationItemId) {
        String currentUserChosenRestaurantId = currentUserChosenRestaurantIdLiveData.getValue();
        boolean doNavigate = !(navigationItemId == R.id.nav_your_lunch &&
                (currentUserChosenRestaurantId == null || currentUserChosenRestaurantId.isEmpty()));
        Integer navigateTo = doNavigate ? navigationItemId : null;
        Event<Integer> navigateToEvent = new Event<>(navigateTo);
        navigateToEventMutableLiveData.setValue(navigateToEvent);
    }

    /**
     * @return
     */
    public LiveData<Event<Integer>> navigateTo() {
        return navigateToEventMutableLiveData;
    }


    // --- PLACE AUTOCOMPLETE SEARCH ---

    /**
     * @return
     */
    public LiveData<AutocompleteRestaurantViewState[]> getAutocompleteRestaurantArray() {
        return autocompleteRestaurantArrayMutableLiveData;
    }

    /**
     * Get the search result selected item
     *
     * @return the selected item
     */
    public LiveData<AutocompleteRestaurantViewState> getSelectedItem() {
        return selectedItemMutableLiveData;
    }

    /**
     * @param text
     */
    public void setPlaceAutocompleteSearchText(@Nullable String text) {
        if (locationPermissionGrantedLiveData.getValue() != null
                && locationPermissionGrantedLiveData.getValue()) {
            if (text != null && text.length() > 2) {
                placeAutocompleteSearch.setSearchInput(text);
            } else {
                placeAutocompleteSearch.setSearchInput("");
                if (text != null && text.length() == 1) {
                    toastMessageEventMutableLiveData.setValue(new Event<>(R.string.type_more_than_three_chars));
                }
            }
        } else {
            this.navigationItemSelected(R.id.nav_map_view);
        }
    }

    /**
     *
     */
    private void setAutocompleteRestaurantArray() {
        disposable = placeAutocompleteSearch.getAutocompleteRestaurantsObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<List<AutocompleteRestaurantViewState>>() {
                    @Override
                    public void onNext(@NonNull List<AutocompleteRestaurantViewState> autocompleteRestaurants) {
                        if (!autocompleteRestaurants.isEmpty()) {
                            AutocompleteRestaurantViewState header = autocompleteRestaurants.get(0);
                            autocompleteRestaurants.remove(0);
                            AutocompleteRestaurantViewState[] autocompleteRestaurantArray = autocompleteRestaurants.toArray(new AutocompleteRestaurantViewState[0]);
                            switch (header.getDescription()) {
                                case OK:
                                case ZERO_RESULTS:
                                    autocompleteRestaurantArrayMutableLiveData.setValue(autocompleteRestaurantArray);
                                    Log.d(TAG, "onNext: " + header.getDescription() + " " + header.getPlaceId() + " filtered restaurant(s)");
                                    break;
                                case NO_SEARCH_MADE:
                                    autocompleteRestaurantArrayMutableLiveData.setValue(null);
                                    Log.d(TAG, "onNext: " + header.getDescription());
                                    break;
                                case REQUEST_DENIED:
                                    autocompleteRestaurantArrayMutableLiveData.setValue(null);
                                    toastMessageEventMutableLiveData.setValue(new Event<>(R.string.error_unknown_error));
                                    Log.d(TAG, "onNext: " + header.getDescription());
                            }
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        autocompleteRestaurantArrayMutableLiveData.setValue(null);
                        toastMessageEventMutableLiveData.setValue(new Event<>(R.string.error_unknown_error));
                        Log.d(TAG, "onError: " + e + ": value set to null");
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onComplete: ");
                    }
                });
    }

    /**
     * @param selectedItem
     */
    public void setSelectedItem(AutocompleteRestaurantViewState selectedItem) {
        selectedItemMutableLiveData.setValue(selectedItem);
    }


    // --- TOAST MESSAGE EVENT ---

    /**
     * Observed by activity to trigger toast
     *
     * @return an event to manage handling of contained string resource integer value
     */
    public LiveData<Event<Integer>> getToastMessageEvent() {
        return toastMessageEventMutableLiveData;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        locationPermissionGrantedLiveData.removeObserver(locationPermissionGrantedObserver);
        if (disposable != null && !disposable.isDisposed()) disposable.dispose();
    }
}