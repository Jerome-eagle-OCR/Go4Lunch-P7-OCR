package com.jr_eagle_ocr.go4lunch.viewmodels;

import static com.jr_eagle_ocr.go4lunch.TestUtils.TEST_CHOSEN_RESTAURANT;
import static com.jr_eagle_ocr.go4lunch.TestUtils.TEST_PLACE_ID;
import static com.jr_eagle_ocr.go4lunch.TestUtils.TEST_RESTAURANT;
import static com.jr_eagle_ocr.go4lunch.TestUtils.TEST_RESTAURANT2;
import static com.jr_eagle_ocr.go4lunch.TestUtils.TEST_RESTAURANT_BITMAP;
import static com.jr_eagle_ocr.go4lunch.TestUtils.TEST_RESTAURANT_STRING_BITMAP;
import static com.jr_eagle_ocr.go4lunch.TestUtils.TEST_USER1;
import static com.jr_eagle_ocr.go4lunch.TestUtils.TEST_USER1_ID;
import static com.jr_eagle_ocr.go4lunch.TestUtils.TEST_USER2;
import static com.jr_eagle_ocr.go4lunch.TestUtils.TEST_USER2_ID;
import static com.jr_eagle_ocr.go4lunch.TestUtils.TEST_USER3;
import static com.jr_eagle_ocr.go4lunch.TestUtils.TEST_USER3_ID;
import static com.jr_eagle_ocr.go4lunch.TestUtils.TEST_USER_VIEWSTATE_IS_JOINING;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import android.view.View;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.core.util.Pair;
import androidx.lifecycle.MutableLiveData;

import com.jr_eagle_ocr.go4lunch.LiveDataTestUtil;
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
import com.jr_eagle_ocr.go4lunch.ui.restaurant_detail.RestaurantDetailViewModel;
import com.jr_eagle_ocr.go4lunch.ui.restaurant_detail.RestaurantDetailViewSate;
import com.jr_eagle_ocr.go4lunch.util.BitmapUtil;
import com.jr_eagle_ocr.go4lunch.util.Event;
import com.jr_eagle_ocr.go4lunch.util.IntentUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author jrigault
 */
@RunWith(MockitoJUnitRunner.class)
public class RestaurantDetailViewModelTest {

    public static final int ITEM_ORDER_100 = 100;
    public static final int ITEM_ORDER_200 = 200;
    public static final int ITEM_ORDER_300 = 300;
    private static final String ANOTHER_PLACE_ID = "ANOTHER_PLACE_ID";

    @Mock
    private BitmapUtil mockBitmapUtil;
    @Mock
    private UserRepository mockUserRepository;
    @Mock
    private RestaurantRepository mockRestaurantRepository;
    @Mock
    private GetCurrentUserChosenRestaurantId mockGetCurrentUserChosenRestaurantId;
    @Mock
    private GetIsLikedRestaurant mockGetIsLikedRestaurant;
    @Mock
    private SetClearChosenRestaurant mockSetClearChosenRestaurant;
    @Mock
    private SetClearLikedRestaurant mockSetClearLikedRestaurant;
    @Mock
    private GetUserViewStates mockGetUserViewStates;

    // Mock livedatas
    private final MutableLiveData<User> currentUserMutableLiveData = new MutableLiveData<>();
    private final MutableLiveData<Map<String, User>> allLoggedUsersMutableLiveData = new MutableLiveData<>();
    private final MutableLiveData<Map<String, Restaurant>> allRestaurantsMutableLiveData = new MutableLiveData<>();
    private final MutableLiveData<Map<ChosenRestaurant, List<String>>> restaurantChosenByUserIdsMapMutableLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> currentUserChosenRestaurantIdMutableLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLikedRestaurantMutableLiveData = new MutableLiveData<>();

    private RestaurantDetailViewModel underTestRestaurantDetailViewModel;

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Before
    public void setUp() throws Exception {
        currentUserMutableLiveData.setValue(TEST_USER1);
        when(mockUserRepository.getCurrentUser()).thenReturn(currentUserMutableLiveData);
        // 3 users logged
        Map<String, User> allLoggedUsers = new HashMap<>();
        allLoggedUsers.put(TEST_USER1_ID, TEST_USER1);
        allLoggedUsers.put(TEST_USER2_ID, TEST_USER2);
        allLoggedUsers.put(TEST_USER3_ID, TEST_USER3);
        allLoggedUsersMutableLiveData.setValue(allLoggedUsers);
        when(mockUserRepository.getAllLoggedUsers()).thenReturn(allLoggedUsersMutableLiveData);

        // 3 restaurants
        String TEST_RESTAURANT_ID = TEST_RESTAURANT.getId();
        String TEST_RESTAURANT2_ID = TEST_RESTAURANT2.getId();
        Map<String, Restaurant> allRestaurants = new HashMap<>();
        allRestaurants.put(TEST_RESTAURANT_ID, TEST_RESTAURANT);
        allRestaurants.put(TEST_RESTAURANT2_ID, TEST_RESTAURANT2);
        allRestaurantsMutableLiveData.setValue(allRestaurants);
        when(mockRestaurantRepository.getAllRestaurants()).thenReturn(allRestaurantsMutableLiveData);
        // Only current user and test user 2 have chosen the test restaurant
        Map<ChosenRestaurant, List<String>> chosenRestaurantByUserIdsMap = new HashMap<>();
        List<String> byUserIds = new ArrayList<>();
        byUserIds.add(TEST_USER1_ID);
        byUserIds.add(TEST_USER2_ID);
        chosenRestaurantByUserIdsMap.put(TEST_CHOSEN_RESTAURANT, byUserIds);
        restaurantChosenByUserIdsMapMutableLiveData.setValue(chosenRestaurantByUserIdsMap);
        when(mockRestaurantRepository.getChosenRestaurantByUserIdsMap()).thenReturn(restaurantChosenByUserIdsMapMutableLiveData);

        currentUserChosenRestaurantIdMutableLiveData.setValue("");
        when(mockGetCurrentUserChosenRestaurantId.getCurrentUserChosenRestaurantId()).thenReturn(currentUserChosenRestaurantIdMutableLiveData);

        when(mockGetIsLikedRestaurant.isLikedRestaurant()).thenReturn(isLikedRestaurantMutableLiveData);
    }

    private void valorizeUnderTestRestaurantDetailViewModel(){
        underTestRestaurantDetailViewModel = new RestaurantDetailViewModel(
                TEST_PLACE_ID,
                mockBitmapUtil,
                mockUserRepository,
                mockRestaurantRepository,
                mockGetCurrentUserChosenRestaurantId,
                mockGetIsLikedRestaurant,
                mockSetClearChosenRestaurant,
                mockSetClearLikedRestaurant,
                mockGetUserViewStates);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void startingWithSetUpHypotheses_shouldCallMethodsAsExpectedAndReturnNullSnackbarEvent() throws InterruptedException {
        valorizeUnderTestRestaurantDetailViewModel();

        verifyNoInteractions(mockBitmapUtil);
        verify(mockUserRepository).getCurrentUser();
        verify(mockUserRepository).getAllLoggedUsers();
        verifyNoMoreInteractions(mockUserRepository);
        verify(mockRestaurantRepository).getAllRestaurants();
        verify(mockRestaurantRepository).getChosenRestaurantByUserIdsMap();
        verifyNoMoreInteractions(mockRestaurantRepository);
        verify(mockGetCurrentUserChosenRestaurantId).getCurrentUserChosenRestaurantId();
        verifyNoMoreInteractions(mockGetCurrentUserChosenRestaurantId);
        verify(mockGetIsLikedRestaurant).isLikedRestaurant();
        verify(mockGetIsLikedRestaurant).addListenerRegistration(TEST_PLACE_ID);
        verifyNoMoreInteractions(mockGetIsLikedRestaurant);
        verifyNoInteractions(mockSetClearChosenRestaurant);
        verifyNoInteractions(mockSetClearLikedRestaurant);
        verifyNoInteractions(mockGetUserViewStates);

        Event<Integer> actualSnackbarMessageEvent = LiveDataTestUtil.getValue(underTestRestaurantDetailViewModel.getSnackbarMessageEvent());
        assertNull(actualSnackbarMessageEvent);
    }

    @Test
    public void startingWithSetUpHypotheses_butDisplayedRestaurantIsAlreadyLikedAndChosen_shouldBehaveAsExpected() throws InterruptedException {
        isLikedRestaurantMutableLiveData.setValue(true);
        currentUserChosenRestaurantIdMutableLiveData.setValue(TEST_PLACE_ID);

        // Should not behave differently as previous test as starting-up snackbar events are filtered
        startingWithSetUpHypotheses_shouldCallMethodsAsExpectedAndReturnNullSnackbarEvent();
    }

    @Test
    public void getRestaurantViewState_withCurrentHypotheses_shouldReturnExpectedValue() throws InterruptedException {
        isLikedRestaurantMutableLiveData.setValue(false);
        currentUserChosenRestaurantIdMutableLiveData.setValue(TEST_PLACE_ID);
        when(mockBitmapUtil.decodeBase64(TEST_RESTAURANT_STRING_BITMAP)).thenReturn(TEST_RESTAURANT_BITMAP);

        Map<String, Pair<String, String>> expectedUserChosenRestaurantMap = new HashMap<>();
        expectedUserChosenRestaurantMap.put(TEST_USER2_ID, new Pair<>(TEST_PLACE_ID, TEST_CHOSEN_RESTAURANT.getPlaceName()));
        List<UserViewState> expectedJoiningUsers = new ArrayList<>();
        expectedJoiningUsers.add(TEST_USER_VIEWSTATE_IS_JOINING);
        when(mockGetUserViewStates.getUserViewStates(TEST_PLACE_ID,
                                                     currentUserMutableLiveData.getValue(),
                                                     allLoggedUsersMutableLiveData.getValue(),
                                                     expectedUserChosenRestaurantMap)).thenReturn(expectedJoiningUsers);

        valorizeUnderTestRestaurantDetailViewModel();
        LiveDataTestUtil.getValue(underTestRestaurantDetailViewModel.getRestaurantViewState()); // Used to trigger

        // Construct expected view state
        RestaurantDetailViewSate expectedViewState = new RestaurantDetailViewSate(TEST_PLACE_ID,
                                                                                  TEST_RESTAURANT_BITMAP,
                                                                                  TEST_RESTAURANT.getName(),
                                                                                  TEST_RESTAURANT.getAddress(),
                                                                                  TEST_RESTAURANT.getPhoneNumber(),
                                                                                  TEST_RESTAURANT.getWebsiteUrl(),
                                                                                  R.drawable.ic_check_circle,
                                                                                  View.INVISIBLE,
                                                                                  expectedJoiningUsers);



        // Late valorization to be sure to have last value because of multiple call due to multiple mediatorLD sources
        RestaurantDetailViewSate actualViewState = LiveDataTestUtil.getValue(underTestRestaurantDetailViewModel.getRestaurantViewState());

        assertEquals(expectedViewState, actualViewState);
    }

    @Test
    public void getRestaurantViewState_withCurrentAlternativeHypotheses_shouldReturnExpectedValue() throws InterruptedException {
        isLikedRestaurantMutableLiveData.setValue(true); // Opposite of previous test
        currentUserChosenRestaurantIdMutableLiveData.setValue(""); // Opposite of previous test
        when(mockBitmapUtil.decodeBase64(TEST_RESTAURANT_STRING_BITMAP)).thenReturn(TEST_RESTAURANT_BITMAP);

        Map<String, Pair<String, String>> expectedUserChosenRestaurantMap = new HashMap<>();
        expectedUserChosenRestaurantMap.put(TEST_USER2_ID, new Pair<>(TEST_PLACE_ID, TEST_CHOSEN_RESTAURANT.getPlaceName()));
        List<UserViewState> expectedJoiningUsers = new ArrayList<>();
        expectedJoiningUsers.add(TEST_USER_VIEWSTATE_IS_JOINING);
        // Mock getUserViewStates return only when expected passed arguments validating expectedUserChosenRestaurantMap
        when(mockGetUserViewStates.getUserViewStates(TEST_PLACE_ID,
                                                     currentUserMutableLiveData.getValue(),
                                                     allLoggedUsersMutableLiveData.getValue(),
                                                     expectedUserChosenRestaurantMap)).thenReturn(expectedJoiningUsers);

        valorizeUnderTestRestaurantDetailViewModel();
        LiveDataTestUtil.getValue(underTestRestaurantDetailViewModel.getRestaurantViewState()); // Used to trigger

        // Construct expected view state
        RestaurantDetailViewSate expectedViewState = new RestaurantDetailViewSate(TEST_PLACE_ID,
                                                                                  TEST_RESTAURANT_BITMAP,
                                                                                  TEST_RESTAURANT.getName(),
                                                                                  TEST_RESTAURANT.getAddress(),
                                                                                  TEST_RESTAURANT.getPhoneNumber(),
                                                                                  TEST_RESTAURANT.getWebsiteUrl(),
                                                                                  R.drawable.ic_circle,
                                                                                  View.VISIBLE,
                                                                                  expectedJoiningUsers);



        // Late valorization to be sure to have last value because of multiple call due to multiple mediatorLD sources
        RestaurantDetailViewSate actualViewState = LiveDataTestUtil.getValue(underTestRestaurantDetailViewModel.getRestaurantViewState());

        assertEquals(expectedViewState, actualViewState);
    }

    @Test
    public void clickOnChooseFab_whenDisplayedRestaurantIsNotChosen_shouldBehaveAsExpected() {
        valorizeUnderTestRestaurantDetailViewModel();

        underTestRestaurantDetailViewModel.clickOnChooseFab();

        verify(mockSetClearChosenRestaurant).setChosenRestaurant(TEST_PLACE_ID);
        verifyNoMoreInteractions(mockSetClearChosenRestaurant);
        assertTrue(underTestRestaurantDetailViewModel.isChooseClickedOnce());
        assertFalse(underTestRestaurantDetailViewModel.isBlockNextChosenSnackbar());
    }

    @Test
    public void clickOnChooseFab_whenDisplayedRestaurantIsAlreadyChosen_shouldBehaveAsExpected() {
        currentUserChosenRestaurantIdMutableLiveData.setValue(TEST_PLACE_ID);
        valorizeUnderTestRestaurantDetailViewModel();

        underTestRestaurantDetailViewModel.clickOnChooseFab();

        verify(mockSetClearChosenRestaurant).clearChosenRestaurant();
        verifyNoMoreInteractions(mockSetClearChosenRestaurant);
        assertTrue(underTestRestaurantDetailViewModel.isChooseClickedOnce());
        assertFalse(underTestRestaurantDetailViewModel.isBlockNextChosenSnackbar());
    }

    @Test
    public void clickOnChooseFab_whenAnotherRestaurantThanDisplayedIsAlreadyChosen_shouldBehaveAsExpected() {
        currentUserChosenRestaurantIdMutableLiveData.setValue(ANOTHER_PLACE_ID);
        valorizeUnderTestRestaurantDetailViewModel();

        underTestRestaurantDetailViewModel.clickOnChooseFab();

        verify(mockSetClearChosenRestaurant).setChosenRestaurant(TEST_PLACE_ID);
        verifyNoMoreInteractions(mockSetClearChosenRestaurant);
        assertTrue(underTestRestaurantDetailViewModel.isChooseClickedOnce());
        assertTrue(underTestRestaurantDetailViewModel.isBlockNextChosenSnackbar());
    }

    @Test
    public void getIntentString_withSuccessivelyItemOrder_shouldSuccessivelyReturnExpectedIntents() {
        valorizeUnderTestRestaurantDetailViewModel();

        String actualIntentString = underTestRestaurantDetailViewModel.getIntentString(ITEM_ORDER_100);
        assertEquals(IntentUtil.ACTION_DIAL_INTENT, actualIntentString);

        actualIntentString = underTestRestaurantDetailViewModel.getIntentString(ITEM_ORDER_200);
        assertNull(actualIntentString);

        actualIntentString = underTestRestaurantDetailViewModel.getIntentString(ITEM_ORDER_300);
        assertEquals(IntentUtil.ACTION_VIEW_INTENT, actualIntentString);
    }

    @Test
    public void getSnackMessageEvent_whenSuccessivelyChangingLikedAndChosen_shouldSuccessivelyReturnExpectedEvents() throws InterruptedException {
        valorizeUnderTestRestaurantDetailViewModel();
        // Needed to unblock snackbar events simulating an user action and not starting the activity
        underTestRestaurantDetailViewModel.setLikeAndChooseClickedOnceToTrue();

        // When successively like/unlike and choose/unchoose we should get successively the expected snackbar event
        isLikedRestaurantMutableLiveData.setValue(true);
        Event<Integer> actualSnackbarMessageEvent = LiveDataTestUtil.getValue(underTestRestaurantDetailViewModel.getSnackbarMessageEvent());
        assertEquals(new Event<>(R.string.like_restaurant), actualSnackbarMessageEvent);

        isLikedRestaurantMutableLiveData.setValue(false);
        actualSnackbarMessageEvent = LiveDataTestUtil.getValue(underTestRestaurantDetailViewModel.getSnackbarMessageEvent());
        assertEquals(new Event<>(R.string.unlike_restaurant), actualSnackbarMessageEvent);

        currentUserChosenRestaurantIdMutableLiveData.setValue(TEST_PLACE_ID);
        actualSnackbarMessageEvent = LiveDataTestUtil.getValue(underTestRestaurantDetailViewModel.getSnackbarMessageEvent());
        assertEquals(new Event<>(R.string.choose_restaurant), actualSnackbarMessageEvent);

        currentUserChosenRestaurantIdMutableLiveData.setValue("");
        actualSnackbarMessageEvent = LiveDataTestUtil.getValue(underTestRestaurantDetailViewModel.getSnackbarMessageEvent());
        assertEquals(new Event<>(R.string.unchoose_restaurant), actualSnackbarMessageEvent);
    }

    @Test
    public void onCleared() {
        valorizeUnderTestRestaurantDetailViewModel();
        clearInvocations(mockGetIsLikedRestaurant);
        underTestRestaurantDetailViewModel.onCleared();

        verify(mockGetIsLikedRestaurant).removeListenerRegistration();
        verifyNoMoreInteractions(mockGetIsLikedRestaurant);
    }
}