package com.jr_eagle_ocr.go4lunch.repositories;

import static com.jr_eagle_ocr.go4lunch.TestUtils.TEST_LATITUDE;
import static com.jr_eagle_ocr.go4lunch.TestUtils.TEST_LONGITUDE;
import static com.jr_eagle_ocr.go4lunch.TestUtils.getBigSearchMockResponse;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.location.Location;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.jr_eagle_ocr.go4lunch.BuildConfig;
import com.jr_eagle_ocr.go4lunch.data.place_autocomplete.PlaceAutocompleteRepository;
import com.jr_eagle_ocr.go4lunch.data.place_autocomplete.models.PlaceAutocompleteApiResponse;
import com.jr_eagle_ocr.go4lunch.data.repositories.LocationRepository;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import io.reactivex.observers.TestObserver;

/**
 * @author jrigault
 */
@RunWith(MockitoJUnitRunner.class)
public class PlaceAutocompleteRepositoryTest {
    static {
        BuildConfig.IS_TESTING.set(true);
    }

    @Mock
    private Location mockMapLocation;
    @Mock
    private LocationRepository mockLocationRepository;

    @InjectMocks
    private PlaceAutocompleteRepository placeAutocompleteRepository;

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Before
    public void setUp() throws Exception {
        assert PlaceAutocompleteRepository.BASE_URL.equals("https://d3ba77ff-2477-4ff5-b127-81c2f865529c.mock.pstmn.io/");
        when(mockMapLocation.getLatitude()).thenReturn(TEST_LATITUDE);
        when(mockMapLocation.getLongitude()).thenReturn(TEST_LONGITUDE);
        when(mockLocationRepository.getMapLocation()).thenReturn(mockMapLocation);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void getPlaceAutocompleteSearchResponse_WithSuccess() {
        TestObserver<PlaceAutocompleteApiResponse> testObserver =
                placeAutocompleteRepository.getPlaceAutocompleteSearchResponse("big")
                        .test();

        PlaceAutocompleteApiResponse mockResponse = getBigSearchMockResponse();
        testObserver.assertValue(mockResponse);
    }
}