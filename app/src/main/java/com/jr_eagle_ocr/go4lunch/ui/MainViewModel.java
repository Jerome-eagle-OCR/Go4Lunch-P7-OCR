package com.jr_eagle_ocr.go4lunch.ui;

import static com.jr_eagle_ocr.go4lunch.data.usecases.PlaceAutocompleteSearch.NO_SEARCH_MADE;
import static com.jr_eagle_ocr.go4lunch.data.usecases.PlaceAutocompleteSearch.OK;
import static com.jr_eagle_ocr.go4lunch.data.usecases.PlaceAutocompleteSearch.REQUEST_DENIED;
import static com.jr_eagle_ocr.go4lunch.data.usecases.PlaceAutocompleteSearch.ZERO_RESULTS;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseUser;
import com.jr_eagle_ocr.go4lunch.BuildConfig;
import com.jr_eagle_ocr.go4lunch.R;
import com.jr_eagle_ocr.go4lunch.data.models.User;
import com.jr_eagle_ocr.go4lunch.data.repositories.LocationRepository;
import com.jr_eagle_ocr.go4lunch.data.repositories.RestaurantRepository;
import com.jr_eagle_ocr.go4lunch.data.repositories.UserRepository;
import com.jr_eagle_ocr.go4lunch.data.usecases.GetCurrentUserChosenRestaurantId;
import com.jr_eagle_ocr.go4lunch.data.usecases.PlaceAutocompleteSearch;
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
    public static final String UNSET_WORKER = "UNSET_WORKER";
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
    private long initialDelay;
    private Calendar calendar;

    public MainViewModel(
            boolean jumpToRestaurantDetail,
            @NonNull LocationRepository locationRepository,
            @NonNull UserRepository userRepository,
            @NonNull RestaurantRepository restaurantRepository,
            @NonNull PlaceAutocompleteSearch placeAutocompleteSearch,
            @NonNull GetCurrentUserChosenRestaurantId getCurrentUserChosenRestaurantId,
            @Nullable Calendar calendar
    ) {
        this.calendar = calendar;
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


    // --- REMINDER INITIAL DELAY ---

    public long getInitialDelay() {
        return initialDelay;
    }

    /**
     * Calculate and set initial delay for noon reminder
     */
    private void setInitialDelay() {
        int hour = 11;
        int minute = 59;

        calendar = getCalendar();
        long nowMillis = calendar.getTimeInMillis();
        Calendar aboutNoonCalendar = (Calendar) calendar.clone();
        aboutNoonCalendar.set(Calendar.HOUR_OF_DAY, hour);
        aboutNoonCalendar.set(Calendar.MINUTE, minute);
        aboutNoonCalendar.set(Calendar.SECOND, 30);
        long aboutNoonMillis = aboutNoonCalendar.getTimeInMillis();
        initialDelay = aboutNoonMillis - nowMillis;
    }

    /**
     * Get Calendar depending on test running or not
     *
     * @return constructor calendar if test is running or new instance
     */
    @VisibleForTesting
    public Calendar getCalendar() {
        return BuildConfig.IS_TESTING.get() ? calendar : Calendar.getInstance();
    }

    // --- MAIN VIEW STATE ---

    public LiveData<MainViewState> getMainViewState() {
        return currentUserViewStateMediatorLiveData;
    }

    /**
     * Set MainViewState depending on current user infos and current time
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
            setInitialDelay();
            if (this.getInitialDelay() > 0 && isNoonReminderEnabled
                    && userChosenRestaurantId != null && !userChosenRestaurantId.isEmpty()) {
                action = SET_WORKER;
                Log.d(TAG, "getMainViewState: initialDelay = " + this.getInitialDelay());
            } else {
                action = UNSET_WORKER;
            }
            mainViewState = new MainViewState(
                    userName,
                    userUrlPicture != null ? userUrlPicture : "",
                    userEmail,
                    userChosenRestaurantId != null ? userChosenRestaurantId : "",
                    action);
        } else {
            action = UNSET_WORKER;
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
     * After MainViewState setting, check if some navigation should occur using
     * navigationItemSelected() method
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
     * Manage navigation item selected and valorize navigateToEvent livedata consequently
     *
     * @param navigationItemId the navigation item resource id
     */
    public void navigationItemSelected(int navigationItemId) {
        String currentUserChosenRestaurantId = currentUserChosenRestaurantIdLiveData.getValue();
        boolean doNavigate = !(navigationItemId == R.id.nav_your_lunch &&
                (currentUserChosenRestaurantId == null || currentUserChosenRestaurantId.isEmpty()));
        Integer navigateTo = doNavigate ? navigationItemId : null;
        if (!doNavigate) toastMessageEventMutableLiveData.setValue(new Event<>(R.string.you_have_not_decided_yet));
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
     * Get autocomplete restaurant array based place autocomplete search result
     *
     * @return an array of AutocompleteRestaurantViewState
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
     * Set text from search view
     *
     * @param text the text to search with google place autocomplete
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
     * Observe Google place autocomplete search result and set array accordingly
     */
    private void setAutocompleteRestaurantArray() {
        disposable = placeAutocompleteSearch.getAutocompleteRestaurantsObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<List<AutocompleteRestaurantViewState>>() {
                    @Override
                    public void onNext(@NonNull List<AutocompleteRestaurantViewState> autocompleteRestaurants) {
                        if (!autocompleteRestaurants.isEmpty()) {
                            AutocompleteRestaurantViewState header = autocompleteRestaurants.remove(0);
//                            autocompleteRestaurants.remove(0);
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