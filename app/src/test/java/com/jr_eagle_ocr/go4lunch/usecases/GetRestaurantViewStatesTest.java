package com.jr_eagle_ocr.go4lunch.usecases;

import static com.jr_eagle_ocr.go4lunch.TestUtils.CLOSED_CALENDAR;
import static com.jr_eagle_ocr.go4lunch.TestUtils.CLOSING_AT_CALENDAR;
import static com.jr_eagle_ocr.go4lunch.TestUtils.CLOSING_SOON_CALENDAR;
import static com.jr_eagle_ocr.go4lunch.TestUtils.TEST_FOUND_RESTAURANT_IDS;
import static com.jr_eagle_ocr.go4lunch.TestUtils.TEST_LATITUDE;
import static com.jr_eagle_ocr.go4lunch.TestUtils.TEST_LONGITUDE;
import static com.jr_eagle_ocr.go4lunch.TestUtils.TEST_RESTAURANT;
import static com.jr_eagle_ocr.go4lunch.TestUtils.TEST_RESTAURANT_BITMAP;
import static com.jr_eagle_ocr.go4lunch.TestUtils.TEST_RESTAURANT_STRING_BITMAP;
import static com.jr_eagle_ocr.go4lunch.TestUtils.TEST_RESTAURANT_VIEWSTATE_1_JOINER_CLOSED;
import static com.jr_eagle_ocr.go4lunch.TestUtils.TEST_RESTAURANT_VIEWSTATE_1_JOINER_CLOSING_SOON;
import static com.jr_eagle_ocr.go4lunch.TestUtils.TEST_RESTAURANT_VIEWSTATE_NO_JOINER_CLOSING_AT;
import static org.mockito.Mockito.doReturn;

import android.location.Location;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.MutableLiveData;

import com.jr_eagle_ocr.go4lunch.BuildConfig;
import com.jr_eagle_ocr.go4lunch.data.models.Restaurant;
import com.jr_eagle_ocr.go4lunch.data.repositories.LocationRepository;
import com.jr_eagle_ocr.go4lunch.data.repositories.RestaurantRepository;
import com.jr_eagle_ocr.go4lunch.data.usecases.GetRestaurantViewStates;
import com.jr_eagle_ocr.go4lunch.ui.adapters.RestaurantViewSate;
import com.jr_eagle_ocr.go4lunch.util.BitmapUtil;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author jrigault
 */
@RunWith(MockitoJUnitRunner.class)
public class GetRestaurantViewStatesTest {
    static {
        BuildConfig.IS_TESTING.set(true);
    }

    @Mock
    private BitmapUtil mockBitmapUtil;
    @Mock
    private Location mockLocation;
    @Mock
    private LocationRepository mockLocationRepository;
    @Mock
    private RestaurantRepository mockRestaurantRepository;

    //LiveData from repositories to be mocked
    private final MutableLiveData<Map<String, Restaurant>> allRestaurantsMutableLiveData = new MutableLiveData<>();

    private GetRestaurantViewStates underTestGetRestaurantViewStates;

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Before
    public void setUp() throws Exception {
        valorizeLivedataAndSetMocking();
    }

    @Test
    public void getRestaurantViewStates_WithNoJoiner_ClosingAt_WithSuccess() {
        Map<String, Integer> restaurantChosenByUsersCountMap = new HashMap<>();
        restaurantChosenByUsersCountMap.put(TEST_RESTAURANT_VIEWSTATE_NO_JOINER_CLOSING_AT.getId(), 0);
        valorizeUnderTestGetRestaurantViewStatesWithCalendar(CLOSING_AT_CALENDAR);

        List<RestaurantViewSate> actual_restaurantViewState =
                underTestGetRestaurantViewStates.getRestaurantViewStates(restaurantChosenByUsersCountMap);

        Assert.assertEquals(1, actual_restaurantViewState.size());
        Assert.assertEquals(TEST_RESTAURANT_VIEWSTATE_NO_JOINER_CLOSING_AT, actual_restaurantViewState.get(0));
    }

    @Test
    public void getRestaurantViewStates_WithOneJoiner_ClosingSoon_WithSuccess() {
        Map<String, Integer> restaurantChosenByUsersCountMap = new HashMap<>();
        restaurantChosenByUsersCountMap.put(TEST_RESTAURANT_VIEWSTATE_NO_JOINER_CLOSING_AT.getId(), 1);
        valorizeUnderTestGetRestaurantViewStatesWithCalendar(CLOSING_SOON_CALENDAR);

        List<RestaurantViewSate> actual_restaurantViewState =
                underTestGetRestaurantViewStates.getRestaurantViewStates(restaurantChosenByUsersCountMap);

        Assert.assertEquals(1, actual_restaurantViewState.size());
        Assert.assertEquals(TEST_RESTAURANT_VIEWSTATE_1_JOINER_CLOSING_SOON, actual_restaurantViewState.get(0));
    }

    @Test
    public void getRestaurantViewStates_WithTwoJoiners_Closed_WithSuccess() {
        Map<String, Integer> restaurantChosenByUsersCountMap = new HashMap<>();
        restaurantChosenByUsersCountMap.put(TEST_RESTAURANT_VIEWSTATE_NO_JOINER_CLOSING_AT.getId(), 1);
        valorizeUnderTestGetRestaurantViewStatesWithCalendar(CLOSED_CALENDAR);

        List<RestaurantViewSate> actual_restaurantViewState =
                underTestGetRestaurantViewStates.getRestaurantViewStates(restaurantChosenByUsersCountMap);

        Assert.assertEquals(1, actual_restaurantViewState.size());
        Assert.assertEquals(TEST_RESTAURANT_VIEWSTATE_1_JOINER_CLOSED, actual_restaurantViewState.get(0));
    }

    private void valorizeLivedataAndSetMocking() {
        Map<String, Restaurant> allRestaurants = new HashMap<>();
        allRestaurants.put(TEST_RESTAURANT.getId(), TEST_RESTAURANT);
        allRestaurantsMutableLiveData.setValue(allRestaurants);
        doReturn(allRestaurantsMutableLiveData).when(mockRestaurantRepository).getAllRestaurants();

        doReturn(TEST_LATITUDE).when(mockLocation).getLatitude();
        doReturn(TEST_LONGITUDE).when(mockLocation).getLongitude();
        doReturn(mockLocation).when(mockLocationRepository).getMapLocation();
        doReturn(TEST_FOUND_RESTAURANT_IDS).when(mockRestaurantRepository).getFoundRestaurantIds();
        doReturn(TEST_RESTAURANT_BITMAP).when(mockBitmapUtil).decodeBase64(TEST_RESTAURANT_STRING_BITMAP);
    }

    private void valorizeUnderTestGetRestaurantViewStatesWithCalendar(Calendar calendar) {
        underTestGetRestaurantViewStates =
                new GetRestaurantViewStates(mockBitmapUtil, mockLocationRepository, mockRestaurantRepository, calendar);
    }
}