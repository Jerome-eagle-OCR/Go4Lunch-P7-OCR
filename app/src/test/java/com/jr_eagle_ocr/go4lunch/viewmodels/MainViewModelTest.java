package com.jr_eagle_ocr.go4lunch.viewmodels;

import static com.jr_eagle_ocr.go4lunch.TestUtils.BEFORE_NOON_CALENDAR;
import static com.jr_eagle_ocr.go4lunch.TestUtils.CLOSING_AT_CALENDAR;
import static com.jr_eagle_ocr.go4lunch.TestUtils.TEST_AUTOCOMPLETE_RESTAURANT_VIEW_STATE;
import static com.jr_eagle_ocr.go4lunch.TestUtils.TEST_CHOSEN_RESTAURANT_ID;
import static com.jr_eagle_ocr.go4lunch.TestUtils.TEST_INITIAL_DELAY;
import static com.jr_eagle_ocr.go4lunch.TestUtils.TEST_RESTAURANT;
import static com.jr_eagle_ocr.go4lunch.TestUtils.TEST_USER1;
import static com.jr_eagle_ocr.go4lunch.TestUtils.TEST_USER1_EMAIL;
import static com.jr_eagle_ocr.go4lunch.data.usecases.PlaceAutocompleteSearch.NO_SEARCH_MADE;
import static com.jr_eagle_ocr.go4lunch.data.usecases.PlaceAutocompleteSearch.OK;
import static com.jr_eagle_ocr.go4lunch.ui.MainViewModel.LISTVIEW;
import static com.jr_eagle_ocr.go4lunch.ui.MainViewModel.MAPVIEW;
import static com.jr_eagle_ocr.go4lunch.ui.MainViewModel.SET_WORKER;
import static com.jr_eagle_ocr.go4lunch.ui.MainViewModel.UNSET_WORKER;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.google.firebase.auth.FirebaseUser;
import com.jr_eagle_ocr.go4lunch.BuildConfig;
import com.jr_eagle_ocr.go4lunch.LiveDataTestUtil;
import com.jr_eagle_ocr.go4lunch.R;
import com.jr_eagle_ocr.go4lunch.RxSchedulersOverrideRule;
import com.jr_eagle_ocr.go4lunch.data.models.User;
import com.jr_eagle_ocr.go4lunch.data.repositories.LocationRepository;
import com.jr_eagle_ocr.go4lunch.data.repositories.RestaurantRepository;
import com.jr_eagle_ocr.go4lunch.data.repositories.UserRepository;
import com.jr_eagle_ocr.go4lunch.data.usecases.GetCurrentUserChosenRestaurantId;
import com.jr_eagle_ocr.go4lunch.data.usecases.PlaceAutocompleteSearch;
import com.jr_eagle_ocr.go4lunch.ui.AutocompleteRestaurantViewState;
import com.jr_eagle_ocr.go4lunch.ui.MainViewModel;
import com.jr_eagle_ocr.go4lunch.ui.MainViewState;
import com.jr_eagle_ocr.go4lunch.util.Event;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import io.reactivex.Observable;

/**
 * @author jrigault
 */
@RunWith(MockitoJUnitRunner.class)
public class MainViewModelTest {
    static {
        BuildConfig.IS_TESTING.set(true);
    }

    @Mock
    private FirebaseUser mockFirebaseUser;
    @Mock
    private LocationRepository mockLocationRepository;
    @Mock
    private UserRepository mockUserRepository;
    @Mock
    private RestaurantRepository mockRestaurantRepository;
    @Mock
    private PlaceAutocompleteSearch mockPlaceAutocompleteSearch;
    @Mock
    private GetCurrentUserChosenRestaurantId mockGetCurrentUserChosenRestaurantId;

    private MainViewModel underTestMainViewModel;

    // Mock Livedatas
    private final MutableLiveData<Boolean> locationPermissionGrantedMutableLiveData = new MutableLiveData<>();
    private final MutableLiveData<FirebaseUser> currentFirebaseUserMutableLiveData = new MutableLiveData<>();
    private final MutableLiveData<User> currentUserMutableLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> currentUserChosenRestaurantIdMutableLiveData = new MutableLiveData<>();

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();
    @Rule
    public RxSchedulersOverrideRule schedulersOverrideRule = new RxSchedulersOverrideRule();

    @Before
    public void setUp() throws Exception {
        // Livedata values must not be changed
        locationPermissionGrantedMutableLiveData.setValue(false);
        when(mockLocationRepository.getLocationPermissionGranted()).thenReturn(locationPermissionGrantedMutableLiveData);
        currentFirebaseUserMutableLiveData.setValue(mockFirebaseUser);
        when(mockUserRepository.getCurrentFirebaseUser()).thenReturn(currentFirebaseUserMutableLiveData);
        currentUserMutableLiveData.setValue(TEST_USER1);
        when(mockUserRepository.getCurrentUser()).thenReturn(currentUserMutableLiveData);
        currentUserChosenRestaurantIdMutableLiveData.setValue("");
        when(mockGetCurrentUserChosenRestaurantId.getCurrentUserChosenRestaurantId()).thenReturn(currentUserChosenRestaurantIdMutableLiveData);
        List<AutocompleteRestaurantViewState> observableData = new ArrayList<>();
        observableData.add(new AutocompleteRestaurantViewState(NO_SEARCH_MADE, NO_SEARCH_MADE));
        when(mockPlaceAutocompleteSearch.getAutocompleteRestaurantsObservable())
                .thenReturn(Observable.just(observableData));
        underTestMainViewModel = new MainViewModel(false,
                mockLocationRepository,
                mockUserRepository,
                mockRestaurantRepository,
                mockPlaceAutocompleteSearch,
                mockGetCurrentUserChosenRestaurantId,
                BEFORE_NOON_CALENDAR);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void getCalendar_whenTestRunning_shouldBeExpectedOne(){
        Calendar actualCalendar = underTestMainViewModel.getCalendar();

        Assert.assertEquals(BEFORE_NOON_CALENDAR, actualCalendar);
    }

    @Test
    public void getCalendar_whenTestNotRunning_shouldBeExpectedOne(){
        BuildConfig.IS_TESTING.set(false);
        Calendar actualCalendar = underTestMainViewModel.getCalendar();

        Assert.assertEquals(Calendar.getInstance(), actualCalendar);
        BuildConfig.IS_TESTING.set(true);
    }

    @Test
    public void getCurrentFragment_whenStarting_shouldBeNull() throws InterruptedException {
        Assert.assertNull(LiveDataTestUtil.getValue(underTestMainViewModel.getCurrentFragment()));
    }

    @Test
    public void setCurrentFragment_withSuccess() throws InterruptedException {
        underTestMainViewModel.setCurrentFragment(MAPVIEW);
        String actualCurrentFragment = LiveDataTestUtil.getValue(underTestMainViewModel.getCurrentFragment());

        Assert.assertEquals(MAPVIEW, actualCurrentFragment);
    }

    @Test
    public void getLocationPermissionGranted_whenStarting_shouldBeLocationRepositoryValue() throws InterruptedException {
        // LocationPermissionGranted assumed to be set to false in setUp() (mock return value)
        Boolean actualLocationPermissionGranted = LiveDataTestUtil.getValue(underTestMainViewModel.getLocationPermissionGranted());

        Assert.assertFalse(actualLocationPermissionGranted);
    }

    @Test
    public void setLocationPermissionGranted_withSuccess() throws InterruptedException {
        locationPermissionGrantedMutableLiveData.setValue(true);
        Boolean actualLocationPermissionGranted = LiveDataTestUtil.getValue(underTestMainViewModel.getLocationPermissionGranted());

        Assert.assertTrue(actualLocationPermissionGranted);
    }

    @Test
    public void getMainViewState_whenNoChosenRestaurant_withSuccess() throws InterruptedException {
        MainViewState expectedMainViewState = new MainViewState(TEST_USER1.getUserName(),
                TEST_USER1.getUserUrlPicture(),
                TEST_USER1_EMAIL,
                "",
                UNSET_WORKER);

        MainViewState actualMainViewState = LiveDataTestUtil.getValue(underTestMainViewModel.getMainViewState());

        Assert.assertEquals(expectedMainViewState, actualMainViewState);
    }

    @Test
    public void getMainViewState_whenAChosenRestaurant_withSuccess() throws InterruptedException {
        currentUserChosenRestaurantIdMutableLiveData.setValue(TEST_CHOSEN_RESTAURANT_ID);
        MainViewState expectedMainViewState = new MainViewState(TEST_USER1.getUserName(),
                TEST_USER1.getUserUrlPicture(),
                TEST_USER1_EMAIL,
                TEST_CHOSEN_RESTAURANT_ID,
                SET_WORKER);

        MainViewState actualMainViewState = LiveDataTestUtil.getValue(underTestMainViewModel.getMainViewState());

        Assert.assertEquals(expectedMainViewState, actualMainViewState);
    }

    @Test
    public void getMainViewState_whenAChosenRestaurant_butNoonReminderDisabled_withSuccess() throws InterruptedException {
        currentUserChosenRestaurantIdMutableLiveData.setValue(TEST_CHOSEN_RESTAURANT_ID);
        TEST_USER1.setNoonReminderEnabled(false);
        MainViewState expectedMainViewState = new MainViewState(TEST_USER1.getUserName(),
                TEST_USER1.getUserUrlPicture(),
                TEST_USER1_EMAIL,
                TEST_CHOSEN_RESTAURANT_ID,
                UNSET_WORKER);

        MainViewState actualMainViewState = LiveDataTestUtil.getValue(underTestMainViewModel.getMainViewState());

        Assert.assertEquals(expectedMainViewState, actualMainViewState);
    }

    @Test
    public void getMainViewState_whenCurrentUserNull_withSuccess() throws InterruptedException {
        currentUserMutableLiveData.setValue(null);
        MainViewState expectedMainViewState = new MainViewState("",
                null,
                "",
                null,
                UNSET_WORKER);

        MainViewState actualMainViewState = LiveDataTestUtil.getValue(underTestMainViewModel.getMainViewState());

        Assert.assertEquals(expectedMainViewState, actualMainViewState);
    }

    @Test
    public void onCurrentUserChange_getMainViewStateShouldDynamicallyChange() throws InterruptedException {
        currentUserMutableLiveData.setValue(TEST_USER1); // To be sure of initial state
        // Here check that source is properly added to MediatorLiveData
        final boolean[] hasChanged = {false};
        final MainViewState actualMainViewState = LiveDataTestUtil.getValue(underTestMainViewModel.getMainViewState());

        // Create and add observer to livedata
        Observer<MainViewState> observer = mainViewState -> {
            if (!mainViewState.equals(actualMainViewState)) hasChanged[0] = true;
        };
        underTestMainViewModel.getMainViewState().observeForever(observer);

        // Change current user and wait for livedata change
        currentUserMutableLiveData.setValue(null);
        long startTime = System.currentTimeMillis();
        do {
            long lapTime = System.currentTimeMillis();
            if (lapTime - startTime > 2000) break; // To avoid infinite loop
        } while (!hasChanged[0]);

        Assert.assertTrue(hasChanged[0]);

        underTestMainViewModel.getMainViewState().removeObserver(observer);
    }

    @Test
    public void onCurrentUserChosenRestaurantIdChange_getMainViewStateShouldDynamicallyChange() throws InterruptedException {
        currentUserChosenRestaurantIdMutableLiveData.setValue(""); // To be sure of initial state
        // Here check that source is properly added to MediatorLiveData
        final boolean[] hasChanged = {false};
        final MainViewState actualMainViewState = LiveDataTestUtil.getValue(underTestMainViewModel.getMainViewState());

        // Create and add observer to livedata
        Observer<MainViewState> observer = mainViewState -> {
            if (!mainViewState.equals(actualMainViewState)) hasChanged[0] = true;
        };
        underTestMainViewModel.getMainViewState().observeForever(observer);

        // Change current user and wait for livedata change
        currentUserChosenRestaurantIdMutableLiveData.setValue(TEST_CHOSEN_RESTAURANT_ID);
        long startTime = System.currentTimeMillis();
        do {
            long lapTime = System.currentTimeMillis();
            if (lapTime - startTime > 2000) break; // To avoid infinite loop
        } while (!hasChanged[0]);

        Assert.assertTrue(hasChanged[0]);

        underTestMainViewModel.getMainViewState().removeObserver(observer);
    }

    @Test
    public void getInitialDelay_withSuccess() throws InterruptedException {
        MainViewState actualMainViewState = LiveDataTestUtil.getValue(underTestMainViewModel.getMainViewState()); // Used to trigger valorization
        assert actualMainViewState != null;
        long actualInitialDelay;
        do {
            actualInitialDelay = underTestMainViewModel.getInitialDelay();
        } while (actualInitialDelay == 0);

        Assert.assertEquals(TEST_INITIAL_DELAY, actualInitialDelay);
    }

    @Test
    public void navigateTo_whenLocationPermissionNotGranted_UserLoggedButNoChoiceMade_CurrentFragmentIsMapView_shouldBeNull() throws InterruptedException {
        // Default case when user log in
        underTestMainViewModel.setCurrentFragment(MAPVIEW);
        MainViewState actualMainViewState = LiveDataTestUtil.getValue(underTestMainViewModel.getMainViewState()); // Used to trigger valorization
        assert actualMainViewState != null;


        Event<Integer> actualNavigateTo = LiveDataTestUtil.getValue(underTestMainViewModel.navigateTo());

        Assert.assertNull(actualNavigateTo);
    }

    @Test
    public void navigateTo_whenLocationPermissionNotGrantedAndCurrentFragmentOtherThanMapView_whenLocationPermissionChangeToNotGranted_shouldTriggerNavigateToMapViewEvent() throws InterruptedException {
        locationPermissionGrantedMutableLiveData.setValue(true);
        underTestMainViewModel.setCurrentFragment(LISTVIEW);
        locationPermissionGrantedMutableLiveData.setValue(false);

        Event<Integer> actualNavigateTo = LiveDataTestUtil.getValue(underTestMainViewModel.navigateTo());

        Event<Integer> expectedNavigateTo = new Event<>(R.id.nav_map_view);

        Assert.assertEquals(expectedNavigateTo, actualNavigateTo);
    }

    @Test
    public void navigateTo_whenLocationPermissionGranted_andCurrentUserNull_shouldBeNavigateToAuthenticationEvent() throws InterruptedException {
        locationPermissionGrantedMutableLiveData.setValue(true);
        underTestMainViewModel.setCurrentFragment(MAPVIEW);
        currentFirebaseUserMutableLiveData.setValue(null);
        MainViewState actualMainViewState = LiveDataTestUtil.getValue(underTestMainViewModel.getMainViewState()); // Used to trigger valorization
        assert actualMainViewState != null;

        Event<Integer> actualNavigateTo = LiveDataTestUtil.getValue(underTestMainViewModel.navigateTo());

        Event<Integer> expectedNavigateTo = new Event<>(R.id.authentication);

        Assert.assertEquals(expectedNavigateTo, actualNavigateTo);
        // Also listeners removing should be called
        verify(mockRestaurantRepository, atLeastOnce()).unsetChosenRestaurantIdsAndCleanCollection();
        verify(mockRestaurantRepository, atLeastOnce()).unsetAllRestaurants();
    }

    @Test
    public void navigateTo_whenJumpToRestaurantDetailTrue_andCurrentUserChosenRestaurantEmpty_shouldBeNull() throws InterruptedException {
        // This test also validate that navigation to restaurant detail is unhallowed if current user didn't choose a restaurant
        locationPermissionGrantedMutableLiveData.setValue(true);
        underTestMainViewModel = new MainViewModel(true,
                mockLocationRepository,
                mockUserRepository,
                mockRestaurantRepository,
                mockPlaceAutocompleteSearch,
                mockGetCurrentUserChosenRestaurantId,
                BEFORE_NOON_CALENDAR);
        underTestMainViewModel.setCurrentFragment(MAPVIEW);
        MainViewState actualMainViewState = LiveDataTestUtil.getValue(underTestMainViewModel.getMainViewState()); // Used to trigger valorization
        assert actualMainViewState != null;

        Event<Integer> actualNavigateTo = LiveDataTestUtil.getValue(underTestMainViewModel.navigateTo());

        Assert.assertNull(actualNavigateTo.peekContent()); // Event is not null but content is
    }

    @Test
    public void navigateTo_whenJumpToRestaurantDetailTrue_andCurrentUserChosenRestaurantNotEmpty_shouldBeNavigateToYourLunchEvent() throws InterruptedException {
        locationPermissionGrantedMutableLiveData.setValue(true);
        currentUserChosenRestaurantIdMutableLiveData.setValue(TEST_CHOSEN_RESTAURANT_ID);
        underTestMainViewModel = new MainViewModel(true,
                mockLocationRepository,
                mockUserRepository,
                mockRestaurantRepository,
                mockPlaceAutocompleteSearch,
                mockGetCurrentUserChosenRestaurantId,
                BEFORE_NOON_CALENDAR);
        underTestMainViewModel.setCurrentFragment(MAPVIEW);

        MainViewState actualMainViewState = LiveDataTestUtil.getValue(underTestMainViewModel.getMainViewState()); // Used to trigger valorization
        assert actualMainViewState != null;

        Event<Integer> actualNavigateTo = LiveDataTestUtil.getValue(underTestMainViewModel.navigateTo());

        Event<Integer> expectedNavigateTo = new Event<>(R.id.nav_your_lunch);

        Assert.assertEquals(expectedNavigateTo, actualNavigateTo);
    }

    @Test
    public void navigationItemSelected_shouldTriggerCorrespondingNavigateToEvent() throws InterruptedException {
        // Doesn't depend on current user, firebase user,location permission granted or not, or even current fragment
        // Null navigateTo only if current user hasn't chosen a restaurant and navigate to restaurant detail (your lunch) is asked

        // Navigate to list view selected
        assertNavigationItemTriggerCorrespondingNavigateToEvent(R.id.nav_list_view);

        // Navigate to wormates selected
        assertNavigationItemTriggerCorrespondingNavigateToEvent(R.id.nav_workmates);

        // Navigate to settings selected
        assertNavigationItemTriggerCorrespondingNavigateToEvent(R.id.nav_settings);

        // Navigate to logout selected
        assertNavigationItemTriggerCorrespondingNavigateToEvent(R.id.nav_logout);

        // Navigate to map view selected
        assertNavigationItemTriggerCorrespondingNavigateToEvent(R.id.nav_map_view);

        // Navigate to your lunch selected (when no choice made)
        underTestMainViewModel.navigationItemSelected(R.id.nav_your_lunch);
        Event<Integer> triggeredNavigateToEvent = LiveDataTestUtil.getValue(underTestMainViewModel.navigateTo());
        Assert.assertNull(triggeredNavigateToEvent.peekContent()); // Event is not null but content is

        // Navigate to your lunch selected (when choice made)
        currentUserChosenRestaurantIdMutableLiveData.setValue(TEST_CHOSEN_RESTAURANT_ID);
        assertNavigationItemTriggerCorrespondingNavigateToEvent(R.id.nav_your_lunch);
    }

    private void assertNavigationItemTriggerCorrespondingNavigateToEvent(int navigationItemId) throws InterruptedException {
        underTestMainViewModel.navigationItemSelected(navigationItemId);
        Event<Integer> triggeredNavigateToEvent = LiveDataTestUtil.getValue(underTestMainViewModel.navigateTo());
        Event<Integer> expectedNavigateToEvent = new Event<>(navigationItemId);
        Assert.assertEquals(expectedNavigateToEvent, triggeredNavigateToEvent);
    }

    @Test
    public void getAutocompleteRestaurantArray_whenStartingSayingNoExplicitSearchMade_shouldReturnNull() throws InterruptedException {
        locationPermissionGrantedMutableLiveData.setValue(true);

        AutocompleteRestaurantViewState[] actualAutocompleteRestaurantArray =
                LiveDataTestUtil.getValue(underTestMainViewModel.getAutocompleteRestaurantArray());

        verify(mockPlaceAutocompleteSearch).getAutocompleteRestaurantsObservable(); // Usecase getter should be called once
        Assert.assertNull(actualAutocompleteRestaurantArray);
    }

    @Test
    public void setPlaceAutocompleteSearchText_whenLocationPermissionNotGranted_shouldTriggerNavigateToMapViewEvent() throws InterruptedException {
        Event<Integer> actualNavigateTo = LiveDataTestUtil.getValue(underTestMainViewModel.navigateTo());
        assert actualNavigateTo == null;

        underTestMainViewModel.setPlaceAutocompleteSearchText(TEST_RESTAURANT.getName());
        verify(mockPlaceAutocompleteSearch, times(0)).setSearchInput(anyString()); // Usecase setter should not be called

        actualNavigateTo = LiveDataTestUtil.getValue(underTestMainViewModel.navigateTo());

        Event<Integer> expectedNavigateTo = new Event<>(R.id.nav_map_view);

        Assert.assertEquals(expectedNavigateTo, actualNavigateTo);
    }

    @Test
    public void setLessThan3CharsPlaceAutocompleteSearchText_whenLocationPermissionGranted_shouldLeadToNullArray() throws InterruptedException {
        locationPermissionGrantedMutableLiveData.setValue(true);

        underTestMainViewModel.setPlaceAutocompleteSearchText("bi");
        verify(mockPlaceAutocompleteSearch).setSearchInput(anyString()); // Usecase setter should be called once

        AutocompleteRestaurantViewState[] actualAutocompleteRestaurantArray =
                LiveDataTestUtil.getValue(underTestMainViewModel.getAutocompleteRestaurantArray());

        verify(mockPlaceAutocompleteSearch).getAutocompleteRestaurantsObservable(); // Usecase getter should be called once

        Assert.assertNull(actualAutocompleteRestaurantArray);
    }

    @Test
    public void setAtLeast3CharsPlaceAutocompleteSearchText_whenLocationPermissionGranted_shouldLeadToExpectedArray() throws InterruptedException {
        locationPermissionGrantedMutableLiveData.setValue(true);
        List<AutocompleteRestaurantViewState> dobservableData = new ArrayList<>();
        dobservableData.add(0, new AutocompleteRestaurantViewState(OK, "1"));
        dobservableData.add(TEST_AUTOCOMPLETE_RESTAURANT_VIEW_STATE[0]);
        when(mockPlaceAutocompleteSearch.getAutocompleteRestaurantsObservable())
                .thenReturn(Observable.just(dobservableData));
        underTestMainViewModel = new MainViewModel(false,
                mockLocationRepository,
                mockUserRepository,
                mockRestaurantRepository,
                mockPlaceAutocompleteSearch,
                mockGetCurrentUserChosenRestaurantId,
                BEFORE_NOON_CALENDAR);

        underTestMainViewModel.setPlaceAutocompleteSearchText("big");
        verify(mockPlaceAutocompleteSearch).setSearchInput(anyString()); // Usecase setter should be called once

        AutocompleteRestaurantViewState[] actualAutocompleteRestaurantArray =
                LiveDataTestUtil.getValue(underTestMainViewModel.getAutocompleteRestaurantArray());

        verify(mockPlaceAutocompleteSearch, times(2)).getAutocompleteRestaurantsObservable(); // Usecase getter should be called 2 times
        // (mock usecase records since first underTestMainViewModel valorization)

        Assert.assertArrayEquals(actualAutocompleteRestaurantArray, TEST_AUTOCOMPLETE_RESTAURANT_VIEW_STATE);
    }

    @Test
    public void getSelectedItem_whenNoItemSelected_noMatterLocationPermissionGrantedOrNot_shouldReturnNull() throws InterruptedException {
        AutocompleteRestaurantViewState actualSelectedItem = LiveDataTestUtil.getValue(underTestMainViewModel.getSelectedItem());

        Assert.assertNull(actualSelectedItem);

        locationPermissionGrantedMutableLiveData.setValue(true);
        actualSelectedItem = LiveDataTestUtil.getValue(underTestMainViewModel.getSelectedItem());

        Assert.assertNull(actualSelectedItem);
    }

    @Test
    public void setSelectedItem_noMatterLocationPermissionGrantedOrNot_withSuccess() throws InterruptedException {
        underTestMainViewModel.setSelectedItem(TEST_AUTOCOMPLETE_RESTAURANT_VIEW_STATE[0]);

        AutocompleteRestaurantViewState actualSelectedItem = LiveDataTestUtil.getValue(underTestMainViewModel.getSelectedItem());

        Assert.assertEquals(TEST_AUTOCOMPLETE_RESTAURANT_VIEW_STATE[0], actualSelectedItem);

        locationPermissionGrantedMutableLiveData.setValue(true);
        underTestMainViewModel.setSelectedItem(TEST_AUTOCOMPLETE_RESTAURANT_VIEW_STATE[0]);

        actualSelectedItem = LiveDataTestUtil.getValue(underTestMainViewModel.getSelectedItem());

        Assert.assertEquals(TEST_AUTOCOMPLETE_RESTAURANT_VIEW_STATE[0], actualSelectedItem);
    }

    @Test
    public void getToastMessageEvent_whenStarting_shouldBeNull() throws InterruptedException {
        Event<Integer> actualToastMessageEvent = LiveDataTestUtil.getValue(underTestMainViewModel.getToastMessageEvent());

        Assert.assertNull(actualToastMessageEvent);
    }

    @Test
    public void getToastMessageEvent_setting1CharPlaceAutocompleteSearchText_shouldBeAsExpected() throws InterruptedException {
        locationPermissionGrantedMutableLiveData.setValue(true);
        underTestMainViewModel.setPlaceAutocompleteSearchText("b");

        Event<Integer> actualToastMessageEvent = LiveDataTestUtil.getValue(underTestMainViewModel.getToastMessageEvent());

        Event<Integer> expectedToastMessageEvent = new Event<>(R.string.type_more_than_three_chars);

        Assert.assertEquals(expectedToastMessageEvent, actualToastMessageEvent);
    }

    @Test
    public void getToastMessageEvent_settingNavigationItemSelectedToNavYourLunch_whenNoChoiceMade_noMatterLocationPermissionGrantedOrNot_shouldBeAsExpected() throws InterruptedException {
        underTestMainViewModel.navigationItemSelected(R.id.nav_your_lunch);

        Event<Integer> actualToastMessageEvent = LiveDataTestUtil.getValue(underTestMainViewModel.getToastMessageEvent());

        Event<Integer> expectedToastMessageEvent = new Event<>(R.string.you_have_not_decided_yet);

        Assert.assertEquals(expectedToastMessageEvent, actualToastMessageEvent);

        locationPermissionGrantedMutableLiveData.setValue(true);
        underTestMainViewModel.navigationItemSelected(R.id.nav_your_lunch);

        actualToastMessageEvent = LiveDataTestUtil.getValue(underTestMainViewModel.getToastMessageEvent());

        expectedToastMessageEvent = new Event<>(R.string.you_have_not_decided_yet);

        Assert.assertEquals(expectedToastMessageEvent, actualToastMessageEvent);
    }
}