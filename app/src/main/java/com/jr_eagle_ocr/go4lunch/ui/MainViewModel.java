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

import com.jr_eagle_ocr.go4lunch.R;
import com.jr_eagle_ocr.go4lunch.model.User;
import com.jr_eagle_ocr.go4lunch.notification.NotificationsWorker;
import com.jr_eagle_ocr.go4lunch.repositories.UserRepository;
import com.jr_eagle_ocr.go4lunch.ui.viewstates.UserViewState;
import com.jr_eagle_ocr.go4lunch.usecases.GetCurrentUserChosenRestaurantId;
import com.jr_eagle_ocr.go4lunch.util.Event;

import java.time.Duration;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

/**
 * @author jrigault
 */
public class MainViewModel extends ViewModel {
    private final LiveData<User> currentUserLiveData;
    private final LiveData<String> currentUserChosenRestaurantIdLiveData;
    private final MediatorLiveData<UserViewState> currentUserViewStateMediatorLiveData;
    private final MutableLiveData<Event<Integer>> navigateToEventMutableLiveData = new MutableLiveData<>();

    public MainViewModel(
            boolean jumpToRestaurantDetail,
            UserRepository userRepository,
            GetCurrentUserChosenRestaurantId getCurrentUserChosenRestaurantId
    ) {
        currentUserLiveData = userRepository.getCurrentUser();
        currentUserChosenRestaurantIdLiveData = getCurrentUserChosenRestaurantId.getCurrentUserChosenRestaurantId();

        currentUserViewStateMediatorLiveData = new MediatorLiveData<>();
        currentUserViewStateMediatorLiveData.addSource(currentUserLiveData, currentUser ->
                getCurrentUserViewState());
        currentUserViewStateMediatorLiveData.addSource(currentUserChosenRestaurantIdLiveData, chosenRestaurantId ->
                getCurrentUserViewState());

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

    public LiveData<UserViewState> getCurrentUserViewState() {
        UserViewState userViewState;
        User user = currentUserLiveData.getValue();
        if (user != null) {
            String name = user.getUserName();
            String urlPicture = user.getUserUrlPicture();
            String email = user.getUserEmail();
            String chosenRestaurantId = currentUserChosenRestaurantIdLiveData.getValue();
            userViewState = new UserViewState(
                    name, 0, urlPicture, email, chosenRestaurantId,
                    null, 0, 0);
        } else {
            userViewState = null;
        }
        currentUserViewStateMediatorLiveData.setValue(userViewState);

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