package com.jr_eagle_ocr.go4lunch.viewmodels;

import static com.jr_eagle_ocr.go4lunch.TestUtils.TEST_CHOSEN_RESTAURANT;
import static com.jr_eagle_ocr.go4lunch.TestUtils.TEST_PLACE_ID;
import static com.jr_eagle_ocr.go4lunch.TestUtils.TEST_USER1;
import static com.jr_eagle_ocr.go4lunch.TestUtils.TEST_USER1_ID;
import static com.jr_eagle_ocr.go4lunch.TestUtils.TEST_USER2;
import static com.jr_eagle_ocr.go4lunch.TestUtils.TEST_USER2_ID;
import static com.jr_eagle_ocr.go4lunch.TestUtils.TEST_USER3;
import static com.jr_eagle_ocr.go4lunch.TestUtils.TEST_USER3_ID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.core.util.Pair;
import androidx.lifecycle.MutableLiveData;

import com.jr_eagle_ocr.go4lunch.BuildConfig;
import com.jr_eagle_ocr.go4lunch.LiveDataTestUtil;
import com.jr_eagle_ocr.go4lunch.data.models.ChosenRestaurant;
import com.jr_eagle_ocr.go4lunch.data.models.User;
import com.jr_eagle_ocr.go4lunch.data.repositories.RestaurantRepository;
import com.jr_eagle_ocr.go4lunch.data.repositories.UserRepository;
import com.jr_eagle_ocr.go4lunch.data.usecases.GetUserViewStates;
import com.jr_eagle_ocr.go4lunch.ui.workmates.WorkmatesViewModel;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
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
public class WorkmatesViewModelTest {
    static {
        BuildConfig.IS_TESTING.set(true);
    }

    @Mock
    private UserRepository mockUserRepository;
    @Mock
    private RestaurantRepository mockRestaurantRepository;
    @Mock
    private GetUserViewStates mockGetUserViewStates;
    @Captor
    private ArgumentCaptor<String> displayedRestaurantIdCaptor;
    @Captor
    private ArgumentCaptor<User> currentUserArgumentCaptor;
    @Captor
    private ArgumentCaptor<Map<String, User>> allLoggedUsersArgumentCaptor;
    @Captor
    private ArgumentCaptor<Map<String, Pair<String, String>>> userChosenRestaurantMapArgumentCaptor;

    private WorkmatesViewModel underTestWorkmatesViewModel;

    // Mock Livedatas
    private final MutableLiveData<User> currentUserMutableLiveData = new MutableLiveData<>();
    private final MutableLiveData<Map<String, User>> allLoggedUsersMutableLiveData = new MutableLiveData<>();
    private final MutableLiveData<Map<ChosenRestaurant, List<String>>> restaurantChosenByUserIdsMapMutableLiveData = new MutableLiveData<>();

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

        // Only current user and test user 2 have chosen the test restaurant
        Map<ChosenRestaurant, List<String>> chosenRestaurantByUserIdsMap = new HashMap<>();
        List<String> byUserIds = new ArrayList<>();
        byUserIds.add(TEST_USER1_ID);
        byUserIds.add(TEST_USER2_ID);
        chosenRestaurantByUserIdsMap.put(TEST_CHOSEN_RESTAURANT, byUserIds);
        restaurantChosenByUserIdsMapMutableLiveData.setValue(chosenRestaurantByUserIdsMap);
        when(mockRestaurantRepository.getChosenRestaurantByUserIdsMap()).thenReturn(restaurantChosenByUserIdsMapMutableLiveData);

        underTestWorkmatesViewModel = new WorkmatesViewModel(mockUserRepository, mockRestaurantRepository, mockGetUserViewStates);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void getUserViewStates_accordingSetUpHypotheses_shouldCallUseCaseMethodWithExpectedArguments() throws InterruptedException {
        LiveDataTestUtil.getValue(underTestWorkmatesViewModel.getUserViewStates());

        Map<String, Pair<String, String>> expectedUserChosenRestaurantMapArgument = new HashMap<>();
        expectedUserChosenRestaurantMapArgument.put(TEST_USER2_ID, new Pair<>(TEST_PLACE_ID, TEST_CHOSEN_RESTAURANT.getPlaceName()));

        assertAllActualArgumentsAreAsExpected(expectedUserChosenRestaurantMapArgument);
    }

    @Test
    public void getUserViewStates_accordingSetUpHypothesesButOnlyCurrentUserHavingChosenARestaurant_shouldCallUseCaseMethodWithExpectedArguments() throws InterruptedException {
        Map<ChosenRestaurant, List<String>> chosenRestaurantByUserIdsMap = new HashMap<>();
        List<String> byUserIds = new ArrayList<>();
        byUserIds.add(TEST_USER1_ID);
        chosenRestaurantByUserIdsMap.put(TEST_CHOSEN_RESTAURANT, byUserIds);
        restaurantChosenByUserIdsMapMutableLiveData.setValue(chosenRestaurantByUserIdsMap);

        LiveDataTestUtil.getValue(underTestWorkmatesViewModel.getUserViewStates());

        Map<String, Pair<String, String>> expectedUserChosenRestaurantMapArgument = new HashMap<>();

        assertAllActualArgumentsAreAsExpected(expectedUserChosenRestaurantMapArgument);
    }

    @Test
    public void getUserViewStates_accordingSetUpHypothesesButOnlyCurrentUserLogged_shouldCallUseCaseMethodWithExpectedArguments() throws InterruptedException {
        Map<String, User> allLoggedUsers = new HashMap<>();
        allLoggedUsers.put(TEST_USER1_ID, TEST_USER1);
        allLoggedUsersMutableLiveData.setValue(allLoggedUsers);

        LiveDataTestUtil.getValue(underTestWorkmatesViewModel.getUserViewStates());

        Map<String, Pair<String, String>> expectedUserChosenRestaurantMapArgument = new HashMap<>();
        expectedUserChosenRestaurantMapArgument.put(TEST_USER2_ID, new Pair<>(TEST_PLACE_ID, TEST_CHOSEN_RESTAURANT.getPlaceName()));

        assertAllActualArgumentsAreAsExpected(expectedUserChosenRestaurantMapArgument);
    }

    private void assertAllActualArgumentsAreAsExpected(Map<String, Pair<String, String>> expectedUserChosenRestaurantMapArgument) {
        verify(mockGetUserViewStates, atLeastOnce()).getUserViewStates(displayedRestaurantIdCaptor.capture(),
                                                                       currentUserArgumentCaptor.capture(),
                                                                       allLoggedUsersArgumentCaptor.capture(),
                                                                       userChosenRestaurantMapArgumentCaptor.capture());

        String actualDisplayedRestaurantIdArgument = displayedRestaurantIdCaptor.getValue();
        User actualCurrentUserArgument = currentUserArgumentCaptor.getValue();
        Map<String, User> actualAllLoggedUsersArgument = allLoggedUsersArgumentCaptor.getValue();
        Map<String, Pair<String, String>> actualUserChosenRestaurantMapArgument = userChosenRestaurantMapArgumentCaptor.getValue();

        Assert.assertNull(actualDisplayedRestaurantIdArgument);
        Assert.assertEquals(currentUserMutableLiveData.getValue(), actualCurrentUserArgument);
        Assert.assertEquals(allLoggedUsersMutableLiveData.getValue(), actualAllLoggedUsersArgument);
        Assert.assertEquals(expectedUserChosenRestaurantMapArgument, actualUserChosenRestaurantMapArgument);
    }
}