package com.jr_eagle_ocr.go4lunch.repositories;

import static org.mockito.Mockito.mock;

import android.location.Location;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.jr_eagle_ocr.go4lunch.data.repositories.LocationRepository;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * @author jrigault
 */
@RunWith(JUnit4.class)
public class LocationRepositoryTest {
    public static final boolean TRUE = Boolean.TRUE;
    public static final boolean FALSE = Boolean.FALSE;
    private final Location TEST_LOCATION = mock(Location.class);

    private final LocationRepository underTestLocationRepository = new LocationRepository();

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Test
    public void getLocationPermissionGranted_shouldReturnFalseByDefault() {
        Boolean isActualLocationPermissionGranted = underTestLocationRepository.getLocationPermissionGranted().getValue();
        Assert.assertEquals(false, isActualLocationPermissionGranted);
    }

    @Test
    public void afterSettingLocationGrantedToTrue_getLocationPermissionGranted_shouldReturnTrue() {
        underTestLocationRepository.setLocationPermissionGranted(TRUE);
        Boolean actualLocationPermissionGranted = underTestLocationRepository.getLocationPermissionGranted().getValue();
        Assert.assertEquals(true, actualLocationPermissionGranted);
    }

    @Test
    public void afterSettingLocationGrantedToFalse_getLocationPermissionGranted_shouldReturnFalse() {
        underTestLocationRepository.setLocationPermissionGranted(FALSE);
        Boolean actualLocationPermissionGranted = underTestLocationRepository.getLocationPermissionGranted().getValue();
        Assert.assertEquals(false, actualLocationPermissionGranted);
    }

    @Test
    public void getLocation_shouldReturnNullByDefault() {
        Location actualLocation = underTestLocationRepository.getMapLocation();
        Assert.assertNull(actualLocation);
    }

    @Test
    public void afterSettingLocation_getLocation_shouldReturnExpectedLocation() {
        underTestLocationRepository.setMapLocation(TEST_LOCATION);
        Location actualLocation = underTestLocationRepository.getMapLocation();
        Assert.assertEquals(TEST_LOCATION, actualLocation);
    }
}