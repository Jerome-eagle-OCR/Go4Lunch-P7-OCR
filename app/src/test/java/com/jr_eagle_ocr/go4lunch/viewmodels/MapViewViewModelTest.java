package com.jr_eagle_ocr.go4lunch.viewmodels;

import static com.jr_eagle_ocr.go4lunch.TestUtils.TEST_AUTOCOMPLETE_RESTAURANT_VIEWSTATE_ARRAY;
import static com.jr_eagle_ocr.go4lunch.TestUtils.TEST_AUTOCOMPLETE_RESTAURANT_VIEWSTATE_ARRAY2;
import static com.jr_eagle_ocr.go4lunch.TestUtils.TEST_PLACE_ID;
import static com.jr_eagle_ocr.go4lunch.TestUtils.TEST_RESTAURANT;
import static com.jr_eagle_ocr.go4lunch.TestUtils.TEST_RESTAURANT1_VIEWSTATE;
import static com.jr_eagle_ocr.go4lunch.TestUtils.TEST_RESTAURANT2;
import static com.jr_eagle_ocr.go4lunch.TestUtils.TEST_RESTAURANT3_VIEWSTATE;
import static com.jr_eagle_ocr.go4lunch.ui.MainViewModel.MAPVIEW;
import static com.jr_eagle_ocr.go4lunch.ui.mapview.MapViewViewModel.DRAWABLE_RESOURCE;
import static com.jr_eagle_ocr.go4lunch.ui.mapview.MapViewViewModel.LATLNG;
import static com.jr_eagle_ocr.go4lunch.ui.mapview.MapViewViewModel.NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.location.Location;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.maps.model.LatLng;
import com.jr_eagle_ocr.go4lunch.BuildConfig;
import com.jr_eagle_ocr.go4lunch.LiveDataTestUtil;
import com.jr_eagle_ocr.go4lunch.R;
import com.jr_eagle_ocr.go4lunch.data.models.FoundRestaurant;
import com.jr_eagle_ocr.go4lunch.data.models.Restaurant;
import com.jr_eagle_ocr.go4lunch.data.repositories.LocationRepository;
import com.jr_eagle_ocr.go4lunch.data.repositories.RestaurantRepository;
import com.jr_eagle_ocr.go4lunch.data.usecases.GetRestaurantViewStates;
import com.jr_eagle_ocr.go4lunch.ui.AutocompleteRestaurantViewState;
import com.jr_eagle_ocr.go4lunch.ui.MainViewModel;
import com.jr_eagle_ocr.go4lunch.ui.mapview.MapViewViewModel;

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
import java.util.HashMap;
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

        autocompleteRestaurantArrayMutableLiveData = new MutableLiveData<>();
        when(mockMainViewModel.getAutocompleteRestaurantArray()).thenReturn(autocompleteRestaurantArrayMutableLiveData);
        selectedItemMutableLiveData = new MutableLiveData<>();
        when(mockMainViewModel.getSelectedItem()).thenReturn(selectedItemMutableLiveData);

        underTestMapViewViewModel = new MapViewViewModel(mockLocationRepository, mockRestaurantRepository);
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
        // Needed to have filteredRestaurantIdsLivedata initialized when setMarkerDetails() is called
        // otherwise the test fails with NPE
        underTestMapViewViewModel.setMainViewModel(mockMainViewModel);
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
    public void getMarkerDetails_whenNoAutocompleteSearchYetPerformed_shouldReturnValueAsExpected() throws InterruptedException {
        setCommonHypothesesForMarkerDetailsTests();
        // Needed to initialize filteredRestaurantIdsLD and selectedItemLD
        underTestMapViewViewModel.setMainViewModel(mockMainViewModel);


        Map<String, Map<String, Object>> actualMarkerDetails = LiveDataTestUtil.getValue(underTestMapViewViewModel.getMarkerDetails());

        // Construct expected 2 elements hashmap
        Map<String, Map<String, Object>> expectedMarkerDetails = new HashMap<>();
        Map<String, Object> detailsHashMap = new HashMap<>();
        Map<String, Object> detailsHashMap2 = new HashMap<>();
        detailsHashMap.put(NAME, TEST_RESTAURANT.getName());
        detailsHashMap2.put(NAME, TEST_RESTAURANT2.getName());
        LatLng latLng = new LatLng(TEST_RESTAURANT.getGeoPoint().getLatitude(), TEST_RESTAURANT.getGeoPoint().getLongitude());
        detailsHashMap.put(LATLNG, latLng);
        detailsHashMap2.put(LATLNG, latLng);
        detailsHashMap.put(DRAWABLE_RESOURCE, R.drawable.green_marker);
        detailsHashMap2.put(DRAWABLE_RESOURCE, R.drawable.orange_marker);
        expectedMarkerDetails.put(TEST_RESTAURANT.getId(), detailsHashMap);
        expectedMarkerDetails.put(TEST_RESTAURANT2.getId(), detailsHashMap2);

        assertEquals(expectedMarkerDetails, actualMarkerDetails);
    }

    @Test
    public void getMarkerDetails_whenAutocompleteSearchPerformed_shouldReturnValueAsExpected() throws InterruptedException {
        setCommonHypothesesForMarkerDetailsTests();
        autocompleteRestaurantArrayMutableLiveData.setValue(TEST_AUTOCOMPLETE_RESTAURANT_VIEWSTATE_ARRAY);
        // Needed to initialize filteredRestaurantIdsLD and selectedItemLD
        underTestMapViewViewModel.setMainViewModel(mockMainViewModel);

        LiveDataTestUtil.getValue(underTestMapViewViewModel.getMarkerDetails());

        // Construct expected 1 element hashmap
        Map<String, Map<String, Object>> expectedMarkerDetails = new HashMap<>();
        Map<String, Object> detailsHashMap = new HashMap<>();
        detailsHashMap.put(NAME, TEST_RESTAURANT.getName());
        LatLng latLng = new LatLng(TEST_RESTAURANT.getGeoPoint().getLatitude(), TEST_RESTAURANT.getGeoPoint().getLongitude());
        detailsHashMap.put(LATLNG, latLng);
        detailsHashMap.put(DRAWABLE_RESOURCE, R.drawable.green_marker);
        expectedMarkerDetails.put(TEST_RESTAURANT.getId(), detailsHashMap);

        // Lately valorization to get last value as setMarkerDetails is called multiple times
        Map<String, Map<String, Object>> actualMarkerDetails = LiveDataTestUtil.getValue(underTestMapViewViewModel.getMarkerDetails());

        assertEquals(expectedMarkerDetails, actualMarkerDetails);
    }

    @Test
    public void getMarkerDetails_whenAutocompleteSearchPerformedAnditemSelected_shouldReturnValueAsExpected() throws InterruptedException {
        selectedItemMutableLiveData.setValue(TEST_AUTOCOMPLETE_RESTAURANT_VIEWSTATE_ARRAY[0]);

        // As it should not have any influence this previous test should succeed
        getMarkerDetails_whenAutocompleteSearchPerformed_shouldReturnValueAsExpected();
    }

    private void setCommonHypothesesForMarkerDetailsTests() {
        // For allRestaurantsLD
        String TEST_RESTAURANT_ID = TEST_RESTAURANT.getId();
        String TEST_RESTAURANT2_ID = TEST_RESTAURANT2.getId();
        Map<String, Restaurant> fakeAllRestaurants = new HashMap<>();
        fakeAllRestaurants.put(TEST_RESTAURANT_ID, TEST_RESTAURANT);
        fakeAllRestaurants.put(TEST_RESTAURANT2_ID, TEST_RESTAURANT2);
        allRestaurantsMutableLiveData.setValue(fakeAllRestaurants);
        // For chosenRestaurantIdsLD
        List<String> fakeChosenRestaurantIds = new ArrayList<>();
        fakeChosenRestaurantIds.add(TEST_PLACE_ID);
        chosenRestaurantIdsMutableLiveData.setValue(fakeChosenRestaurantIds);
        // For foundRestaurantsIds
        List<String> foundRestaurantIds = new ArrayList<>();
        foundRestaurantIds.add(TEST_RESTAURANT_ID);
        foundRestaurantIds.add(TEST_RESTAURANT2_ID);
        when(mockRestaurantRepository.getFoundRestaurantIds()).thenReturn(foundRestaurantIds);
    }
}