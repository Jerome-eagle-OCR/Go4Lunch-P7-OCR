package com.jr_eagle_ocr.go4lunch.ui;

import android.os.Build;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseUser;
import com.jr_eagle_ocr.go4lunch.R;
import com.jr_eagle_ocr.go4lunch.model.User;
import com.jr_eagle_ocr.go4lunch.repositories.RestaurantRepository;
import com.jr_eagle_ocr.go4lunch.repositories.UserRepository;
import com.jr_eagle_ocr.go4lunch.usecases.GetCurrentUserChosenRestaurantId;
import com.jr_eagle_ocr.go4lunch.util.Event;

import java.util.Calendar;

/**
 * @author jrigault
 */
public class MainViewModel extends ViewModel {
    private final String TAG = getClass().getSimpleName();
    public static final String SET_WORKER = "SET_WORKER";
    public static final String CANCEL_WORKER = "UNSET_WORKER";
    public static final String SET_ALARM = "SET_ALARM";
    public static final String CANCEL_ALARM = "CANCEL_ALARM";
    public static final String BLANK_ACTION = "NO_ACTION";
    private final boolean jumpToRestaurantDetail;
    private final RestaurantRepository restaurantRepository;
    private final LiveData<FirebaseUser> currentFirebaseUserLiveData;
    private final LiveData<User> currentUserLiveData;
    private final LiveData<String> currentUserChosenRestaurantIdLiveData;
    private final MediatorLiveData<MainViewState> currentUserViewStateMediatorLiveData;
    private final MutableLiveData<Event<Integer>> navigateToEventMutableLiveData = new MutableLiveData<>();
    private long aboutNoonMillis;
    private long initialDelay;
    private boolean didNavigateToAuthenticationSend = false;

    public MainViewModel(
            boolean jumpToRestaurantDetail,
            UserRepository userRepository,
            RestaurantRepository restaurantRepository,
            GetCurrentUserChosenRestaurantId getCurrentUserChosenRestaurantId
    ) {
        this.jumpToRestaurantDetail = jumpToRestaurantDetail;
        this.restaurantRepository = restaurantRepository;

        currentFirebaseUserLiveData = userRepository.getCurrentFirebaseUser();

        currentUserLiveData = userRepository.getCurrentUser();
        currentUserChosenRestaurantIdLiveData = getCurrentUserChosenRestaurantId.getCurrentUserChosenRestaurantId();

        currentUserViewStateMediatorLiveData = new MediatorLiveData<>();
        currentUserViewStateMediatorLiveData.addSource(currentUserLiveData, currentUser ->
                setMainViewState());
        currentUserViewStateMediatorLiveData.addSource(currentUserChosenRestaurantIdLiveData, chosenRestaurantId ->
                setMainViewState());
    }

    public LiveData<MainViewState> getMainViewState() {
        return currentUserViewStateMediatorLiveData;
    }

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
            if (initialDelay > 0 && isNoonReminderEnabled && userChosenRestaurantId != null) {
                action = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) ? SET_WORKER : SET_ALARM;
                action = SET_ALARM;
                Log.d(TAG, "getMainViewState: initialDelay = " + initialDelay);
            } else {
                action = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) ? CANCEL_WORKER : CANCEL_ALARM;
                action = CANCEL_ALARM;
            }
            mainViewState = new MainViewState(
                    userName,
                    userUrlPicture,
                    userEmail,
                    userChosenRestaurantId,
                    action);
        } else {
            action = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) ? CANCEL_WORKER : CANCEL_ALARM;
            action = CANCEL_ALARM;
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

    private void checkAndNavigateIfNeeded() {
        if (currentFirebaseUserLiveData.getValue() == null) {
            this.navigationItemSelected(R.id.authentication);
            restaurantRepository.unsetChosenRestaurantIdsAndCleanCollection();
            restaurantRepository.unsetFoundRestaurants();
            didNavigateToAuthenticationSend = true;
        } else if (jumpToRestaurantDetail) {
            this.navigationItemSelected(R.id.nav_your_lunch);
        }
    }

    public long getAlarmTrigger() {
        return aboutNoonMillis;
    }

    public long getInitialDelay() {
        return initialDelay;
    }

    private void setReminderVariables() {
        int hour = 16;
        int minute = 10;

        Calendar c = Calendar.getInstance();
        long nowMillis = c.getTimeInMillis();
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, 30);
//        if (c.before(Calendar.getInstance())) {
//            c.add(Calendar.DATE, 1);
//        }
        aboutNoonMillis = c.getTimeInMillis();
        initialDelay = aboutNoonMillis - nowMillis;
    }

    public void navigationItemSelected(int navigationItemId) {
        boolean doNavigate = !(navigationItemId == R.id.nav_your_lunch && currentUserChosenRestaurantIdLiveData.getValue() == null);
        Integer navigateTo = doNavigate ? navigationItemId : null;
        Event<Integer> navigateToEvent = new Event<>(navigateTo);
        navigateToEventMutableLiveData.setValue(navigateToEvent);
    }

    public LiveData<Event<Integer>> navigateTo() {
        return navigateToEventMutableLiveData;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
    }
}