package com.jr_eagle_ocr.go4lunch.viewmodels;

import static com.jr_eagle_ocr.go4lunch.TestUtils.TEST_AUTOCOMPLETE_RESTAURANT_VIEWSTATE_ARRAY2;
import static com.jr_eagle_ocr.go4lunch.TestUtils.TEST_CHOSEN_RESTAURANT;
import static com.jr_eagle_ocr.go4lunch.TestUtils.TEST_CHOSEN_RESTAURANT2;
import static com.jr_eagle_ocr.go4lunch.TestUtils.TEST_PLACE_ID;
import static com.jr_eagle_ocr.go4lunch.TestUtils.TEST_RESTAURANT1_VIEWSTATE;
import static com.jr_eagle_ocr.go4lunch.TestUtils.TEST_RESTAURANT2_VIEWSTATE;
import static com.jr_eagle_ocr.go4lunch.TestUtils.TEST_RESTAURANT3_VIEWSTATE;
import static com.jr_eagle_ocr.go4lunch.TestUtils.TEST_USER1;
import static com.jr_eagle_ocr.go4lunch.TestUtils.TEST_USER1_ID;
import static com.jr_eagle_ocr.go4lunch.TestUtils.TEST_USER2_ID;
import static com.jr_eagle_ocr.go4lunch.TestUtils.TEST_USER3_ID;
import static com.jr_eagle_ocr.go4lunch.ui.MainViewModel.LISTVIEW;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.jr_eagle_ocr.go4lunch.BuildConfig;
import com.jr_eagle_ocr.go4lunch.LiveDataTestUtil;
import com.jr_eagle_ocr.go4lunch.data.models.ChosenRestaurant;
import com.jr_eagle_ocr.go4lunch.data.models.User;
import com.jr_eagle_ocr.go4lunch.data.repositories.RestaurantRepository;
import com.jr_eagle_ocr.go4lunch.data.repositories.UserRepository;
import com.jr_eagle_ocr.go4lunch.data.usecases.GetRestaurantViewStates;
import com.jr_eagle_ocr.go4lunch.ui.AutocompleteRestaurantViewState;
import com.jr_eagle_ocr.go4lunch.ui.MainViewModel;
import com.jr_eagle_ocr.go4lunch.ui.adapters.RestaurantViewSate;
import com.jr_eagle_ocr.go4lunch.ui.listview.ListViewViewModel;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author jrigault
 */
@RunWith(MockitoJUnitRunner.class)
public class ListViewViewModelTest {
    static {
        BuildConfig.IS_TESTING.set(true);
    }

    @Mock
    private UserRepository mockUserRepository;
    @Mock
    private RestaurantRepository mockRestaurantRepository;
    @Mock
    private GetRestaurantViewStates mockGetRestaurantViewStates;
    @Captor
    private ArgumentCaptor<Map<String, Integer>> restaurantChosenByUsersCountMapArgumentCaptor;
    @Mock
    private MainViewModel mockMainViewModel;
    @Captor
    private ArgumentCaptor<String> currentFragmentArgumentCaptor;

    // Mock livedatas
    private final MutableLiveData<User> currentUserMutableLiveData = new MutableLiveData<>();
    private final MutableLiveData<Map<ChosenRestaurant, List<String>>> chosenRestaurantByUserIdsMapMutableLiveData = new MutableLiveData<>();
    private MutableLiveData<AutocompleteRestaurantViewState[]> autocompleteRestaurantArrayMutableLiveData;
    private MutableLiveData<AutocompleteRestaurantViewState> selectedItemMutableLiveData;

    private ListViewViewModel underTestListViewViewModel;

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Before
    public void setUp() throws Exception {
        currentUserMutableLiveData.setValue(TEST_USER1);
        when(mockUserRepository.getCurrentUser()).thenReturn(currentUserMutableLiveData);

        Map<ChosenRestaurant, List<String>> chosenRestaurantByUserIdsMap = new HashMap<>();
        List<String> byUserIds = new ArrayList<>();
        byUserIds.add(TEST_USER1_ID);
        byUserIds.add(TEST_USER2_ID);
        byUserIds.add(TEST_USER3_ID);
        chosenRestaurantByUserIdsMap.put(TEST_CHOSEN_RESTAURANT, byUserIds);
        chosenRestaurantByUserIdsMap.put(TEST_CHOSEN_RESTAURANT2, Collections.singletonList("#####"));
        chosenRestaurantByUserIdsMapMutableLiveData.setValue(chosenRestaurantByUserIdsMap);
        when(mockRestaurantRepository.getChosenRestaurantByUserIdsMap()).thenReturn(chosenRestaurantByUserIdsMapMutableLiveData);

        autocompleteRestaurantArrayMutableLiveData = new MutableLiveData<>();
        when(mockMainViewModel.getAutocompleteRestaurantArray()).thenReturn(autocompleteRestaurantArrayMutableLiveData);
        selectedItemMutableLiveData = new MutableLiveData<>();
        when(mockMainViewModel.getSelectedItem()).thenReturn(selectedItemMutableLiveData);

        underTestListViewViewModel = new ListViewViewModel(mockUserRepository, mockRestaurantRepository, mockGetRestaurantViewStates);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void startingWithSetUpHypotheses_shouldCallMethodsAsExpected() {
        // Should not be called
        verify(mockMainViewModel, never()).setCurrentFragment(anyString());
        verify(mockMainViewModel, never()).getAutocompleteRestaurantArray();
        verify(mockMainViewModel, never()).getSelectedItem();
        verify(mockGetRestaurantViewStates, never()).getRestaurantViewStates(anyMap());
    }

    @Test
    public void setMainViewModel_shouldCallMethodsAsExpected() {
        underTestListViewViewModel.setMainViewModel(mockMainViewModel);

        // Should be called once passing expected argument
        verify(mockMainViewModel).setCurrentFragment(currentFragmentArgumentCaptor.capture());
        String actualCurrentFragmentArgument = currentFragmentArgumentCaptor.getValue();
        assertEquals(LISTVIEW, actualCurrentFragmentArgument);
        // Should be called once
        verify(mockMainViewModel).getAutocompleteRestaurantArray();
        verify(mockMainViewModel).getSelectedItem();
        // Should not be called
        verify(mockGetRestaurantViewStates, never()).getRestaurantViewStates(anyMap());
    }

    @Test
    public void getFilteredRestaurantIds_whenNoMainViewModelSet_shouldReturnNullLiveData() {
        // As Transformations.map only done when mainviewmodel is set, livedata should not be initialized
        LiveData<List<String>> actualFilteredRestaurantIdsLiveData = underTestListViewViewModel.getFilteredRestaurantIds();

        assertNull(actualFilteredRestaurantIdsLiveData);
    }

    @Test
    public void getFilteredRestaurantIds_whenMainViewModelSet_shouldReturnExpectedValue() throws InterruptedException {
        autocompleteRestaurantArrayMutableLiveData.setValue(TEST_AUTOCOMPLETE_RESTAURANT_VIEWSTATE_ARRAY2);
        underTestListViewViewModel.setMainViewModel(mockMainViewModel);

        List<String> actualFilteredRestaurantIds = LiveDataTestUtil.getValue(underTestListViewViewModel.getFilteredRestaurantIds());

        List<String> expectedFilteredRestaurantIds = Arrays.asList(TEST_RESTAURANT1_VIEWSTATE.getId(), TEST_RESTAURANT3_VIEWSTATE.getId());

        assertEquals(expectedFilteredRestaurantIds, actualFilteredRestaurantIds);
    }

    @Test
    public void getAllRestaurantViewStates_whenNoAutocompleteSearchYetPerformed_shouldCallMethodsAsExpected() throws InterruptedException {
        underTestListViewViewModel.setMainViewModel(mockMainViewModel);

        LiveDataTestUtil.getValue(underTestListViewViewModel.getRestaurantViewStates());

        // Should be called once
        verify(mockMainViewModel).getAutocompleteRestaurantArray();
        // Should be called 2 times
        verify(mockGetRestaurantViewStates, times(2))
                .getRestaurantViewStates(restaurantChosenByUsersCountMapArgumentCaptor.capture());
        // With expected argument
        Map<String, Integer> actualRestaurantChosenByUsersCountMapArgument = restaurantChosenByUsersCountMapArgumentCaptor.getValue();
        Map<String, Integer> expectedRestaurantChosenByUsersCountMapArgument = new HashMap<>();
        expectedRestaurantChosenByUsersCountMapArgument.put(TEST_CHOSEN_RESTAURANT2.getPlaceId(), 1);
        expectedRestaurantChosenByUsersCountMapArgument.put(TEST_PLACE_ID, 2); // Current user choice should not be taken into account
        assertEquals(expectedRestaurantChosenByUsersCountMapArgument, actualRestaurantChosenByUsersCountMapArgument);
    }

    @Test
    public void getAllRestaurantViewStates_whenAutocompleteSearchPerformedButNoSelectionMade_shouldCallMethodsAndReturnValueAsExpected() throws InterruptedException {
        List<RestaurantViewSate> fakeRestaurantViewStates = Arrays.asList(TEST_RESTAURANT1_VIEWSTATE,
                                                                          TEST_RESTAURANT3_VIEWSTATE,
                                                                          TEST_RESTAURANT2_VIEWSTATE);
        when(mockGetRestaurantViewStates.getRestaurantViewStates(anyMap()))
                .thenReturn(fakeRestaurantViewStates); // Totally fake list
        autocompleteRestaurantArrayMutableLiveData.setValue(TEST_AUTOCOMPLETE_RESTAURANT_VIEWSTATE_ARRAY2);
        underTestListViewViewModel.setMainViewModel(mockMainViewModel);

        // We trigger all computation here but we'll get the value at the end to have final value
        // (because of multiple sources added to mediatorLD, computation is done as multiple times
        // with different values)
        LiveDataTestUtil.getValue(underTestListViewViewModel.getRestaurantViewStates());

        // Should be called once
        verify(mockMainViewModel).getAutocompleteRestaurantArray();
        // Should be called 3 times
        verify(mockGetRestaurantViewStates, times(3))
                .getRestaurantViewStates(restaurantChosenByUsersCountMapArgumentCaptor.capture());
        // With expected argument (no change as when no search yet performed)
        Map<String, Integer> actualRestaurantChosenByUsersCountMapArgument = restaurantChosenByUsersCountMapArgumentCaptor.getValue();
        Map<String, Integer> expectedRestaurantChosenByUsersCountMapArgument = new HashMap<>();
        expectedRestaurantChosenByUsersCountMapArgument.put(TEST_CHOSEN_RESTAURANT2.getPlaceId(), 1);
        expectedRestaurantChosenByUsersCountMapArgument.put(TEST_PLACE_ID, 2); // Current user choice should not be taken into account
        assertEquals(expectedRestaurantChosenByUsersCountMapArgument, actualRestaurantChosenByUsersCountMapArgument);

        List<RestaurantViewSate> actualRestaurantViewStates = LiveDataTestUtil.getValue(underTestListViewViewModel.getRestaurantViewStates());
        List<RestaurantViewSate> expectedRestaurantViewStates = fakeRestaurantViewStates.subList(0, 2);
        assertEquals(expectedRestaurantViewStates, actualRestaurantViewStates);
    }

    @Test
    public void getAllRestaurantViewStates_whenAutocompleteSearchPerformedAndSelectionMade_shouldCallMethodsAndReturnValueAsExpected() throws InterruptedException {
        List<RestaurantViewSate> fakeRestaurantViewStates = Arrays.asList(TEST_RESTAURANT1_VIEWSTATE,
                                                                          TEST_RESTAURANT2_VIEWSTATE,
                                                                          TEST_RESTAURANT3_VIEWSTATE);
        when(mockGetRestaurantViewStates.getRestaurantViewStates(anyMap()))
                .thenReturn(fakeRestaurantViewStates); // Totally fake list
        autocompleteRestaurantArrayMutableLiveData.setValue(TEST_AUTOCOMPLETE_RESTAURANT_VIEWSTATE_ARRAY2);
        selectedItemMutableLiveData.setValue(TEST_AUTOCOMPLETE_RESTAURANT_VIEWSTATE_ARRAY2[1]);
        underTestListViewViewModel.setMainViewModel(mockMainViewModel);

        // We trigger all computation here but we'll get the value at the end to have final value
        // (because of multiple sources added to mediatorLD, computation is done as multiple times
        // with different values)
        LiveDataTestUtil.getValue(underTestListViewViewModel.getRestaurantViewStates());

        // Should be called once
        verify(mockMainViewModel).getAutocompleteRestaurantArray();
        // Should be called 4 times
        verify(mockGetRestaurantViewStates, times(4))
                .getRestaurantViewStates(restaurantChosenByUsersCountMapArgumentCaptor.capture());
        // With expected argument (no change as when no search yet performed)
        Map<String, Integer> actualRestaurantChosenByUsersCountMapArgument = restaurantChosenByUsersCountMapArgumentCaptor.getValue();
        Map<String, Integer> expectedRestaurantChosenByUsersCountMapArgument = new HashMap<>();
        expectedRestaurantChosenByUsersCountMapArgument.put(TEST_CHOSEN_RESTAURANT2.getPlaceId(), 1);
        expectedRestaurantChosenByUsersCountMapArgument.put(TEST_PLACE_ID, 2); // Current user choice should not be taken into account
        assertEquals(expectedRestaurantChosenByUsersCountMapArgument, actualRestaurantChosenByUsersCountMapArgument);

        List<RestaurantViewSate> actualRestaurantViewStates = LiveDataTestUtil.getValue(underTestListViewViewModel.getRestaurantViewStates());
        List<RestaurantViewSate> expectedRestaurantViewStates = fakeRestaurantViewStates.subList(2, 3);
        assertEquals(expectedRestaurantViewStates, actualRestaurantViewStates);
    }
}