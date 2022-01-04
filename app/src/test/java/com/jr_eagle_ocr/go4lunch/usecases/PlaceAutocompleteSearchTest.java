package com.jr_eagle_ocr.go4lunch.usecases;

import static com.jr_eagle_ocr.go4lunch.TestUtils.getBigSearchMockResponse;
import static com.jr_eagle_ocr.go4lunch.data.usecases.PlaceAutocompleteSearch.NO_SEARCH_MADE;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import android.location.Location;
import android.location.LocationManager;

import com.jr_eagle_ocr.go4lunch.BuildConfig;
import com.jr_eagle_ocr.go4lunch.RxSchedulersOverrideRule;
import com.jr_eagle_ocr.go4lunch.data.place_autocomplete.PlaceAutocompleteRepository;
import com.jr_eagle_ocr.go4lunch.data.place_autocomplete.models.PlaceAutocompleteApiResponse;
import com.jr_eagle_ocr.go4lunch.data.place_autocomplete.models.PredictionsItem;
import com.jr_eagle_ocr.go4lunch.data.repositories.LocationRepository;
import com.jr_eagle_ocr.go4lunch.data.usecases.PlaceAutocompleteSearch;
import com.jr_eagle_ocr.go4lunch.ui.AutocompleteRestaurantViewState;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

/**
 * @author jrigault
 */
@RunWith(MockitoJUnitRunner.class)
public class PlaceAutocompleteSearchTest {
    static {
        BuildConfig.IS_TESTING.set(true);
    }

    private final PlaceAutocompleteApiResponse bigSearchMockResponse = getBigSearchMockResponse();
    private final Location location = new Location(LocationManager.PASSIVE_PROVIDER);
    private final LocationRepository locationRepository = new LocationRepository();
    @Spy
    private final PlaceAutocompleteRepository placeAutocompleteRepository = new PlaceAutocompleteRepository(locationRepository);

    @InjectMocks
    private PlaceAutocompleteSearch underTestPlaceAutocompleteSearch;

    @Rule
    public RxSchedulersOverrideRule schedulersOverrideRule = new RxSchedulersOverrideRule();

    @Before
    public void setUp() throws Exception {
        locationRepository.setMapLocation(location);
        when(placeAutocompleteRepository.getPlaceAutocompleteSearchResponse(anyString())).thenReturn(Observable.just(bigSearchMockResponse));
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void getAutocompleteRestaurantsObservable_whenStarting_providesNoSearchMadeOnNext() {
        List<AutocompleteRestaurantViewState> expected = new ArrayList<>();
        expected.add(new AutocompleteRestaurantViewState(NO_SEARCH_MADE, NO_SEARCH_MADE));

        List<List<AutocompleteRestaurantViewState>> actuals = new ArrayList<>();

        Disposable disposable = getDisposable(actuals);

        waitForUpdating(actuals, 0);

        Assert.assertEquals(1, actuals.size());
        Assert.assertEquals(expected, actuals.get(0));
        disposable.dispose();
    }

    @Test
    public void getAutocompleteRestaurantsObservable_whenSearchingEmptyString_doesntProvidesOnNext() {
        List<AutocompleteRestaurantViewState> expected = new ArrayList<>();
        expected.add(new AutocompleteRestaurantViewState(NO_SEARCH_MADE, NO_SEARCH_MADE));

        List<List<AutocompleteRestaurantViewState>> actuals = new ArrayList<>();

        // Same as for whenStarting test -> actuals size will be 1
        Disposable disposable = getDisposable(actuals);
        waitForUpdating(actuals, 0);

        underTestPlaceAutocompleteSearch.setSearchInput("");

        // Waiting until lapTime limit as size must not increase
        waitForUpdating(actuals, 1);

        Assert.assertEquals(1, actuals.size());
        disposable.dispose();
    }

    @Test
    public void getAutocompleteRestaurantsObservable_whenSearchingBigString_providesExpectedOnNext() {
        List<AutocompleteRestaurantViewState> expected = new ArrayList<>();
        // Add expected header
        String expectedStatus = bigSearchMockResponse.getStatus();
        expected.add(new AutocompleteRestaurantViewState(expectedStatus, "1"));
        // Add expected only line
        PredictionsItem expectedPrediction = bigSearchMockResponse.getPredictions().get(0);
        String expectedDescription = expectedPrediction.getDescription().replace(", France", "");
        String expectedPlaceId = expectedPrediction.getPlaceId();
        expected.add(new AutocompleteRestaurantViewState(expectedDescription, expectedPlaceId));

        List<List<AutocompleteRestaurantViewState>> actuals = new ArrayList<>();

        // Same as for whenStarting test -> actuals size will be 1
        Disposable disposable = getDisposable(actuals);
        waitForUpdating(actuals, 0);

        underTestPlaceAutocompleteSearch.setSearchInput("big");

        waitForUpdating(actuals, 1);

        Assert.assertEquals(2, actuals.size());
        Assert.assertEquals(expected, actuals.get(1));
        disposable.dispose();
    }

    private Disposable getDisposable(List<List<AutocompleteRestaurantViewState>> actuals) {
        return underTestPlaceAutocompleteSearch.getAutocompleteRestaurantsObservable()
                .doOnNext(actuals::add)
                .subscribe();
    }

    private void waitForUpdating(List<List<AutocompleteRestaurantViewState>> actuals, int initialSize) {
        int actualSize = actuals.size();
        long startTime = System.currentTimeMillis();
        do {
            long lapTime = System.currentTimeMillis();
            if (actualSize > initialSize || lapTime - startTime > 2000) break;
        } while (actuals.size() == actualSize);
    }
}