package com.jr_eagle_ocr.go4lunch.ui;

import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;

import com.google.firebase.auth.FirebaseUser;
import com.jr_eagle_ocr.go4lunch.R;
import com.jr_eagle_ocr.go4lunch.model.User;
import com.jr_eagle_ocr.go4lunch.notification.NotificationsWorker;
import com.jr_eagle_ocr.go4lunch.repositories.RestaurantRepository;
import com.jr_eagle_ocr.go4lunch.repositories.UserRepository;
import com.jr_eagle_ocr.go4lunch.usecases.GetCurrentUserChosenRestaurantId;
import com.jr_eagle_ocr.go4lunch.util.Event;

import java.time.Duration;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

/**
 * @author jrigault
 */
public class MainViewModel extends ViewModel {
    private final RestaurantRepository restaurantRepository;
    private final LiveData<FirebaseUser> currentFirebaseUserLiveData;
    private final LiveData<User> currentUserLiveData;
    private final LiveData<String> currentUserChosenRestaurantIdLiveData;
    private final MediatorLiveData<MainViewState> currentUserViewStateMediatorLiveData;
    private final MutableLiveData<Event<Integer>> navigateToEventMutableLiveData = new MutableLiveData<>();

    public MainViewModel(
            boolean jumpToRestaurantDetail,
            UserRepository userRepository,
            RestaurantRepository restaurantRepository,
            GetCurrentUserChosenRestaurantId getCurrentUserChosenRestaurantId
    ) {
        this.restaurantRepository = restaurantRepository;
        currentFirebaseUserLiveData = userRepository.getCurrentFirebaseUser();
        currentUserLiveData = userRepository.getCurrentUser();
        currentUserChosenRestaurantIdLiveData = getCurrentUserChosenRestaurantId.getCurrentUserChosenRestaurantId();

        currentUserViewStateMediatorLiveData = new MediatorLiveData<>();
        currentUserViewStateMediatorLiveData.addSource(currentUserLiveData, currentUser ->
                getMainViewState());
        currentUserViewStateMediatorLiveData.addSource(currentUserChosenRestaurantIdLiveData, chosenRestaurantId ->
                getMainViewState());

        if (jumpToRestaurantDetail) {
            Event<Integer> navigateToEvent = new Event<>(R.id.nav_your_lunch);
            navigateToEventMutableLiveData.setValue(navigateToEvent);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public PeriodicWorkRequest getPeriodicWorkRequest() {
        int hour = 11;
        int minute = 59;

        Calendar calendar = Calendar.getInstance();
        long nowMillis = calendar.getTimeInMillis();

        if (calendar.get(Calendar.HOUR_OF_DAY) > hour ||
                (calendar.get(Calendar.HOUR_OF_DAY) == hour && calendar.get(Calendar.MINUTE) + 1 >= minute)) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);

        calendar.set(Calendar.SECOND, 45);
        calendar.set(Calendar.MILLISECOND, 0);
        long initialDelay = calendar.getTimeInMillis() - nowMillis;

        Constraints constraints = new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
        PeriodicWorkRequest periodicWorkRequest =
                new PeriodicWorkRequest.Builder(NotificationsWorker.class, Duration.ofMinutes(15))
                        .setConstraints(constraints)
                        .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                        .build();

        return periodicWorkRequest;
    }

    public LiveData<MainViewState> getMainViewState() {
        MainViewState mainViewState;
        User user = currentUserLiveData.getValue();
        if (user != null) {
            String userName = user.getUserName();
            String userUrlPicture = user.getUserUrlPicture();
            String userEmail = user.getUserEmail();
            String userChosenRestaurantId = currentUserChosenRestaurantIdLiveData.getValue();
            mainViewState = new MainViewState(
                    userName, userUrlPicture, userEmail, userChosenRestaurantId);
        } else {
            mainViewState = null;
            if (currentFirebaseUserLiveData.getValue() == null) {
                restaurantRepository.unsetChosenRestaurantIdsAndCleanCollection();
                restaurantRepository.unsetFoundRestaurants();
                this.navigationItemSelected(R.id.authentication);
            }
        }
        currentUserViewStateMediatorLiveData.setValue(mainViewState);

        return currentUserViewStateMediatorLiveData;
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
}