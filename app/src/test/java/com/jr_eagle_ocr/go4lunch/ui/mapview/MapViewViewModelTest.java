package com.jr_eagle_ocr.go4lunch.ui.mapview;

import static com.jr_eagle_ocr.go4lunch.TestUtils.TEST_AUTOCOMPLETE_RESTAURANT_VIEWSTATE_ARRAY;
import static com.jr_eagle_ocr.go4lunch.TestUtils.TEST_AUTOCOMPLETE_RESTAURANT_VIEWSTATE_ARRAY2;
import static com.jr_eagle_ocr.go4lunch.TestUtils.TEST_CHOSEN_RESTAURANT2;
import static com.jr_eagle_ocr.go4lunch.TestUtils.TEST_CHOSEN_RESTAURANT_ID;
import static com.jr_eagle_ocr.go4lunch.TestUtils.TEST_RESTAURANT;
import static com.jr_eagle_ocr.go4lunch.TestUtils.TEST_RESTAURANT1_VIEWSTATE;
import static com.jr_eagle_ocr.go4lunch.TestUtils.TEST_RESTAURANT3_VIEWSTATE;
import static com.jr_eagle_ocr.go4lunch.ui.MainViewModel.LISTVIEW;
import static com.jr_eagle_ocr.go4lunch.ui.MainViewModel.MAPVIEW;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.location.Location;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.jr_eagle_ocr.go4lunch.BuildConfig;
import com.jr_eagle_ocr.go4lunch.LiveDataTestUtil;
import com.jr_eagle_ocr.go4lunch.data.models.FoundRestaurant;
import com.jr_eagle_ocr.go4lunch.data.models.Restaurant;
import com.jr_eagle_ocr.go4lunch.data.repositories.LocationRepository;
import com.jr_eagle_ocr.go4lunch.data.repositories.RestaurantRepository;
import com.jr_eagle_ocr.go4lunch.data.usecases.GetRestaurantViewStates;
import com.jr_eagle_ocr.go4lunch.ui.AutocompleteRestaurantViewState;
import com.jr_eagle_ocr.go4lunch.ui.MainViewModel;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author jrigault
 */
@RunWith(MockitoJUnitRunner.class)
public class MapViewViewModelTest {
    static {
        BuildConfig.IS_TESTING.set(true);
    }

    @Mock
    private LocationRepository mockLocationRepository;
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

    //Mock livedatas
    private final MutableLiveData<Map<String, Restaurant>> allRestaurantsMutableLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<String>> chosenRestaurantIdsMutableLiveData = new MutableLiveData<>();
    private MutableLiveData<AutocompleteRestaurantViewState[]> autocompleteRestaurantArrayMutableLiveData;
    private MutableLiveData<AutocompleteRestaurantViewState> selectedItemMutableLiveData;

    private MapViewViewModel underTestMapViewViewModel;

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Before
    public void setUp() throws Exception {
        allRestaurantsMutableLiveData.setValue(null);
        when(mockRestaurantRepository.getAllRestaurants()).thenReturn(allRestaurantsMutableLiveData);
        chosenRestaurantIdsMutableLiveData.setValue(null);
        when(mockRestaurantRepository.getChosenRestaurantIds()).thenReturn(chosenRestaurantIdsMutableLiveData);

        underTestMapViewViewModel = new MapViewViewModel(mockLocationRepository, mockRestaurantRepository);
        autocompleteRestaurantArrayMutableLiveData = new MutableLiveData<>();
        when(mockMainViewModel.getAutocompleteRestaurantArray()).thenReturn(autocompleteRestaurantArrayMutableLiveData);
        selectedItemMutableLiveData = new MutableLiveData<>();
        when(mockMainViewModel.getSelectedItem()).thenReturn(selectedItemMutableLiveData);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void setLocation_shouldCallMethodAsExpected() {
        Location mockLocation = mock(Location.class);
        underTestMapViewViewModel.setLocation(mockLocation);

        verify(mockLocationRepository).setMapLocation(mockLocation);
    }

    @Test
    public void setLocationPermissionGranted_shouldCallMethodAsExpected() {
        underTestMapViewViewModel.setLocationPermissionGranted(true);
        verify(mockLocationRepository).setLocationPermissionGranted(true);

        underTestMapViewViewModel.setLocationPermissionGranted(false);
        verify(mockLocationRepository).setLocationPermissionGranted(false);
    }

    @Test
    public void setMainViewModel_shouldCallMethodsAsExpected() {
        underTestMapViewViewModel.setMainViewModel(mockMainViewModel);

        // Should be called once passing expected argument
        verify(mockMainViewModel).setCurrentFragment(currentFragmentArgumentCaptor.capture());
        String actualCurrentFragmentArgument = currentFragmentArgumentCaptor.getValue();
        assertEquals(MAPVIEW, actualCurrentFragmentArgument);
        // Should be called once
        verify(mockMainViewModel).getAutocompleteRestaurantArray();
        verify(mockMainViewModel).getSelectedItem();
        // Should not be called
        verify(mockGetRestaurantViewStates, never()).getRestaurantViewStates(anyMap());
    }

    @Test
    public void getFilteredRestaurantIds_whenNoMainViewModelSet_shouldReturnNullLiveData() {
        // As Transformations.map only done when mainviewmodel is set, livedata should not be initialized
        LiveData<List<String>> actualFilteredRestaurantIdsLiveData = underTestMapViewViewModel.getFilteredRestaurantIds();

        assertNull(actualFilteredRestaurantIdsLiveData);
    }

    @Test
    public void getFilteredRestaurantIds_whenMainViewModelSet_shouldReturnExpectedValue() throws InterruptedException {
        autocompleteRestaurantArrayMutableLiveData.setValue(TEST_AUTOCOMPLETE_RESTAURANT_VIEWSTATE_ARRAY2);
        underTestMapViewViewModel.setMainViewModel(mockMainViewModel);

        List<String> actualFilteredRestaurantIds = LiveDataTestUtil.getValue(underTestMapViewViewModel.getFilteredRestaurantIds());

        List<String> expectedFilteredRestaurantIds = Arrays.asList(TEST_RESTAURANT1_VIEWSTATE.getId(), TEST_RESTAURANT3_VIEWSTATE.getId());

        assertEquals(expectedFilteredRestaurantIds, actualFilteredRestaurantIds);
    }

    @Test
    public void setFoundRestaurantIds_shouldCallMethodAsExpected() {
        underTestMapViewViewModel.setMainViewModel(mockMainViewModel); // Needed to have filteredRestaurantIdsLivedata initialized
        List<String> mockList = (List<String>) mock(List.class);

        underTestMapViewViewModel.setFoundRestaurantIds(mockList);

        verify(mockRestaurantRepository).setFoundRestaurantIds(mockList);
    }

    @Test
    public void getAllRestaurants_shouldReturnExpectedObject() {
        LiveData<Map<String, Restaurant>> actualAllRestaurants = underTestMapViewViewModel.getAllRestaurants();

        assertEquals(allRestaurantsMutableLiveData, actualAllRestaurants);
    }

    @Test
    public void addFoundRestaurant_shouldCallMethodAsExpected() {
        FoundRestaurant mockFoundRestaurant = mock(FoundRestaurant.class);
        underTestMapViewViewModel.addFoundRestaurant(mockFoundRestaurant);

        verify(mockRestaurantRepository).addFoundRestaurant(mockFoundRestaurant);
    }

    @Test
    public void getSelectedItem_shouldReturnExpectedObject() {
        underTestMapViewViewModel.setMainViewModel(mockMainViewModel); // Needed to initialize selectedItem
        LiveData<AutocompleteRestaurantViewState> actualSelectedItem = underTestMapViewViewModel.getSelectedItem();

        assertEquals(selectedItemMutableLiveData, actualSelectedItem);
    }

    @Test
    public void getMarkerDetails() {

    }
}