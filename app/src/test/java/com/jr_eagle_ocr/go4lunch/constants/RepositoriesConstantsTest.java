package com.jr_eagle_ocr.go4lunch.constants;

import static org.junit.Assert.assertEquals;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.jr_eagle_ocr.go4lunch.data.place_autocomplete.PlaceAutocompleteRepository;
import com.jr_eagle_ocr.go4lunch.data.repositories.RestaurantRepository;
import com.jr_eagle_ocr.go4lunch.data.repositories.UserRepository;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * @author jrigault
 */
@RunWith(JUnit4.class)
public class RepositoriesConstantsTest {

    public static final String USERS = "users";
    public static final String RESTAURANTS = "restaurants";
    public static final String CHOSEN_RESTAURANTS = "chosen_restaurants";
    public static final String LIKED_RESTAURANTS = "liked_restaurants";
    public static final String HTTPS_MAPS_GOOGLEAPIS_COM_MAPS_API_PLACE = "https://maps.googleapis.com/maps/api/place/";

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Test
    public void placeAutocompleteRepositoryConstants_NoTestRunning_AreOk() {
        assertEquals(HTTPS_MAPS_GOOGLEAPIS_COM_MAPS_API_PLACE, PlaceAutocompleteRepository.BASE_URL);
    }

    @Test
    public void userRepositoryConstants_NoTestRunning_AreOk() {
        assertEquals(USERS, UserRepository.USERS_COLLECTION_NAME);
    }

    @Test
    public void restaurantRepositoryConstants_NoTestRunning_AreOk() {
        assertEquals(RESTAURANTS, RestaurantRepository.RESTAURANTS_COLLECTION_NAME);
        assertEquals(CHOSEN_RESTAURANTS, RestaurantRepository.CHOSEN_COLLECTION_NAME);
        assertEquals(LIKED_RESTAURANTS, RestaurantRepository.LIKED_COLLECTION_NAME);
    }
}
