package com.jr_eagle_ocr.go4lunch.repositories;

import static com.jr_eagle_ocr.go4lunch.AndroidTestUtils.TEST_CHOSENLIKED_RESTAURANT1;
import static com.jr_eagle_ocr.go4lunch.AndroidTestUtils.TEST_CHOSEN_RESTAURANT2;
import static com.jr_eagle_ocr.go4lunch.AndroidTestUtils.TEST_CHOSEN_RESTAURANTS_COLLECTION;
import static com.jr_eagle_ocr.go4lunch.AndroidTestUtils.TEST_FOUND_RESTAURANT;
import static com.jr_eagle_ocr.go4lunch.AndroidTestUtils.TEST_FOUND_RESTAURANT2;
import static com.jr_eagle_ocr.go4lunch.AndroidTestUtils.TEST_LIKED_RESTAURANTS_COLLECTION;
import static com.jr_eagle_ocr.go4lunch.AndroidTestUtils.TEST_RESTAURANT;
import static com.jr_eagle_ocr.go4lunch.AndroidTestUtils.TEST_RESTAURANT2;
import static com.jr_eagle_ocr.go4lunch.AndroidTestUtils.TEST_USER1;
import static com.jr_eagle_ocr.go4lunch.AndroidTestUtils.TEST_USER1_EMAIL;
import static com.jr_eagle_ocr.go4lunch.AndroidTestUtils.TEST_USER1_PWD;
import static com.jr_eagle_ocr.go4lunch.AndroidTestUtils.TEST_USER2;
import static com.jr_eagle_ocr.go4lunch.AndroidTestUtils.cleanTestChosenRestaurantsCollection;
import static com.jr_eagle_ocr.go4lunch.AndroidTestUtils.cleanTestRestaurantsCollection;
import static com.jr_eagle_ocr.go4lunch.AndroidTestUtils.setChosenRestaurantDocument;
import static com.jr_eagle_ocr.go4lunch.AndroidTestUtils.setChosenRestaurantForUser;
import static com.jr_eagle_ocr.go4lunch.AndroidTestUtils.signInUser;
import static com.jr_eagle_ocr.go4lunch.AndroidTestUtils.signOut;
import static com.jr_eagle_ocr.go4lunch.data.repositories.RestaurantRepository.TIMESTAMP_FIELD;
import static org.hamcrest.Matchers.containsInAnyOrder;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.jr_eagle_ocr.go4lunch.LiveDataTestUtil;
import com.jr_eagle_ocr.go4lunch.data.models.ChosenRestaurant;
import com.jr_eagle_ocr.go4lunch.data.models.Restaurant;
import com.jr_eagle_ocr.go4lunch.data.repositories.RestaurantRepository;
import com.jr_eagle_ocr.go4lunch.di.Go4LunchApplication;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

/**
 * @author jrigault
 */
@RunWith(AndroidJUnit4.class)
public class RestaurantRepositoryTest {

    private final RestaurantRepository underTestRestaurantRepository =
            Go4LunchApplication.getDependencyContainer().getRestaurantRepository();

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Before
    public void setUp() throws Exception {
        assert RestaurantRepository.RESTAURANTS_COLLECTION_NAME.equals("restaurants_test");
        assert RestaurantRepository.CHOSEN_COLLECTION_NAME.equals("chosen_restaurants_test");
        assert RestaurantRepository.LIKED_COLLECTION_NAME.equals("liked_restaurants_test");
        signInUser(TEST_USER1_EMAIL, TEST_USER1_PWD);
        cleanTestRestaurantsCollection();
        cleanTestChosenRestaurantsCollection();
        underTestRestaurantRepository.setAllRestaurants(); // Needed and at the same time assert that method works properly
        underTestRestaurantRepository.setChosenRestaurantIdsAndCleanCollection(); // Needed and at the same time assert that method works properly
    }

    @After
    public void tearDown() throws Exception {
        underTestRestaurantRepository.unsetAllRestaurants();
        underTestRestaurantRepository.unsetChosenRestaurantIdsAndCleanCollection();
        cleanTestRestaurantsCollection();
        cleanTestChosenRestaurantsCollection();
        signOut();
    }

    @Test
    public void getFoundRestaurantIds_whenNoRestaurantIdAdded_shouldReturnNull() {
        List<String> actualFoundRestaurantIds = underTestRestaurantRepository.getFoundRestaurantIds();

        Assert.assertNull(actualFoundRestaurantIds);
    }

    @Test
    public void setFoundRestaurantIds_withSuccess() {
        List<String> expected = Arrays.asList(TEST_FOUND_RESTAURANT.getId(), TEST_FOUND_RESTAURANT2.getId());

        underTestRestaurantRepository.setFoundRestaurantIds(expected); // Use under test  method

        List<String> actualFoundRestaurantIds = underTestRestaurantRepository.getFoundRestaurantIds();

        Assert.assertEquals(expected, actualFoundRestaurantIds);
    }

    // --- FIRESTORE RESTAURANTS  ---

    @Test
    public void getAllRestaurants_whenNoFoundRestaurantAdded_shouldReturnEmptyMap() throws InterruptedException {
        Map<String, Restaurant> actualAllRestaurants =
                LiveDataTestUtil.getValue(underTestRestaurantRepository.getAllRestaurants()); // Use under test  method

        Assert.assertTrue(actualAllRestaurants.isEmpty());
    }

    @Test
    public void getAllRestaurants_whenTwoFoundRestaurantsAdded_withSuccess() throws InterruptedException {
        Map<String, Restaurant> actualAllRestaurants =
                LiveDataTestUtil.getValue(underTestRestaurantRepository.getAllRestaurants());

        // Add first found restaurant and wait for livedata update
        underTestRestaurantRepository.addFoundRestaurant(TEST_FOUND_RESTAURANT); // Use under test  method
        actualAllRestaurants = waitForAllRestaurantsLiveDataUpdating(actualAllRestaurants);
        assert actualAllRestaurants.size() == 1;

        // Get actual timestamp for first restaurant (otherwise equality assertion will always fail)
        String TEST_FOUND_RESTAURANT_ID = TEST_FOUND_RESTAURANT.getId();
        Assert.assertTrue(actualAllRestaurants.containsKey(TEST_FOUND_RESTAURANT_ID));
        Restaurant retrievedRestaurant = actualAllRestaurants.get(TEST_FOUND_RESTAURANT_ID);
        assert retrievedRestaurant != null; // To get rid of warnings
        TEST_RESTAURANT.setTimestamp(retrievedRestaurant.getTimestamp());

        // Add second found restaurant and wait for livedata update
        underTestRestaurantRepository.addFoundRestaurant(TEST_FOUND_RESTAURANT2); // Use under test  method
        actualAllRestaurants = waitForAllRestaurantsLiveDataUpdating(actualAllRestaurants);
        assert actualAllRestaurants.size() == 2;

        // Get actual timestamp for second restaurant (otherwise equality assertion will always fail)
        String TEST_FOUND_RESTAURANT2_ID = TEST_FOUND_RESTAURANT2.getId();
        Assert.assertTrue(actualAllRestaurants.containsKey(TEST_FOUND_RESTAURANT2_ID));
        Restaurant retrievedRestaurant2 = actualAllRestaurants.get(TEST_FOUND_RESTAURANT2_ID);
        assert retrievedRestaurant2 != null; // To get rid of warnings
        TEST_RESTAURANT2.setTimestamp(retrievedRestaurant2.getTimestamp());

        Assert.assertEquals(2, actualAllRestaurants.size());
        Assert.assertEquals(TEST_RESTAURANT, actualAllRestaurants.get(TEST_FOUND_RESTAURANT_ID));
        Assert.assertEquals(TEST_RESTAURANT2, actualAllRestaurants.get(TEST_FOUND_RESTAURANT2_ID));
    }

    @Test
    public void unsetAllRestaurants_withSuccess() throws InterruptedException {
        underTestRestaurantRepository.unsetAllRestaurants(); // Use under test  method

        Map<String, Restaurant> actualAllRestaurants
                = LiveDataTestUtil.getValue(underTestRestaurantRepository.getAllRestaurants());

        Assert.assertNull(actualAllRestaurants); // allRestaurants is set to null when ListenerRegistration is removed
    }

    // --- FIRESTORE RESTAURANT CHOOSING ---

    @Test
    public void getChosenRestaurantsCollection_withSuccess() {
        CollectionReference actualChosenRestaurantsCollection =
                underTestRestaurantRepository.getChosenRestaurantsCollection(); // Use under test  method

        Assert.assertEquals(TEST_CHOSEN_RESTAURANTS_COLLECTION, actualChosenRestaurantsCollection);
    }

    @Test
    public void getChosenRestaurantIds_whenNoChosenRestaurants_shouldReturnEmptyList() throws InterruptedException {
        List<String> actualChosenRestaurantIds =
                LiveDataTestUtil.getValue(underTestRestaurantRepository.getChosenRestaurantIds()); // Use under test  method

        Assert.assertTrue(actualChosenRestaurantIds.isEmpty());
    }

    @Test
    public void getChosenRestaurantIds_whenTwoChosenRestaurants_shouldReturnExpectedList() throws InterruptedException, ExecutionException {
        setChosenRestaurantDocument(TEST_CHOSENLIKED_RESTAURANT1);
        setChosenRestaurantForUser(TEST_CHOSENLIKED_RESTAURANT1, TEST_USER1);
        setChosenRestaurantDocument(TEST_CHOSEN_RESTAURANT2);
        setChosenRestaurantForUser(TEST_CHOSEN_RESTAURANT2, TEST_USER2);

        List<String> actualChosenRestaurantIds =
                LiveDataTestUtil.getValue(underTestRestaurantRepository.getChosenRestaurantIds()); // Use under test  method

        List<String> expectedChosenRestaurantIds = Arrays.asList(TEST_CHOSENLIKED_RESTAURANT1.getPlaceId(), TEST_CHOSEN_RESTAURANT2.getPlaceId());

        Assert.assertEquals(expectedChosenRestaurantIds.size(), actualChosenRestaurantIds.size());
        Assert.assertThat(actualChosenRestaurantIds, containsInAnyOrder(expectedChosenRestaurantIds.toArray()));
    }

    @Test
    public void getChosenRestaurantIds_whenTwoChosenRestaurantsButOneOutdated_shouldReturnExpectedList() throws InterruptedException, ExecutionException {
        setChosenRestaurantDocument(TEST_CHOSENLIKED_RESTAURANT1);
        setChosenRestaurantForUser(TEST_CHOSENLIKED_RESTAURANT1, TEST_USER1);
        setChosenRestaurantDocument(TEST_CHOSEN_RESTAURANT2);
        // Change chosen restaurant2 document timestamp to be outdated
        Calendar c = Calendar.getInstance();
        int yesterday = c.get(Calendar.DAY_OF_YEAR) - 1;
        c.set(Calendar.DAY_OF_YEAR, yesterday);
        Tasks.await(TEST_CHOSEN_RESTAURANTS_COLLECTION.document(TEST_CHOSEN_RESTAURANT2.getPlaceId())
                .update(TIMESTAMP_FIELD, String.valueOf(c.getTimeInMillis())));

        List<String> actualChosenRestaurantIds =
                LiveDataTestUtil.getValue(underTestRestaurantRepository.getChosenRestaurantIds()); // Use under test  method

        List<String> expectedChosenRestaurantIds = Collections.singletonList(TEST_CHOSENLIKED_RESTAURANT1.getPlaceId());

        Assert.assertEquals(expectedChosenRestaurantIds.size(), actualChosenRestaurantIds.size());
        Assert.assertThat(actualChosenRestaurantIds, containsInAnyOrder(expectedChosenRestaurantIds.toArray()));
    }

    @Test
    public void getChosenRestaurantByUserIdsMap_whenNoChosenRestaurants_shouldReturnEmptyMap() throws InterruptedException {
        Map<ChosenRestaurant, List<String>> actualChosenRestaurantByUserIdsMap =
                LiveDataTestUtil.getValue(underTestRestaurantRepository.getChosenRestaurantByUserIdsMap()); // Use under test  method

        Assert.assertTrue(actualChosenRestaurantByUserIdsMap.isEmpty());
    }

    @Test
    public void getChosenRestaurantByUserIdsMap_whenTwoChosenRestaurants_withSuccess() throws InterruptedException, ExecutionException {
        setChosenRestaurantDocument(TEST_CHOSENLIKED_RESTAURANT1);
        setChosenRestaurantForUser(TEST_CHOSENLIKED_RESTAURANT1, TEST_USER1);
        setChosenRestaurantDocument(TEST_CHOSEN_RESTAURANT2);
        setChosenRestaurantForUser(TEST_CHOSEN_RESTAURANT2, TEST_USER2);
        Map<ChosenRestaurant, List<String>> actualChosenRestaurantByUserIdsMap =
                LiveDataTestUtil.getValue(underTestRestaurantRepository.getChosenRestaurantByUserIdsMap()); // Use under test  method

        Map<ChosenRestaurant, List<String>> expectedChosenRestaurantByUserIdsMap = new HashMap<>();
        expectedChosenRestaurantByUserIdsMap.put(TEST_CHOSENLIKED_RESTAURANT1, Collections.singletonList(TEST_USER1.getUid()));
        expectedChosenRestaurantByUserIdsMap.put(TEST_CHOSEN_RESTAURANT2, Collections.singletonList(TEST_USER2.getUid()));

        Assert.assertEquals(expectedChosenRestaurantByUserIdsMap.size(), actualChosenRestaurantByUserIdsMap.size());
        Assert.assertTrue(expectedChosenRestaurantByUserIdsMap.entrySet().stream().allMatch(e ->
                Objects.equals(e.getValue(), actualChosenRestaurantByUserIdsMap.get(e.getKey()))));
    }

    @Test
    public void getChosenRestaurantByUserIdsMap_whenTwoChosenRestaurantsButOneOutdated_withSuccess() throws InterruptedException, ExecutionException {
        setChosenRestaurantDocument(TEST_CHOSENLIKED_RESTAURANT1);
        setChosenRestaurantForUser(TEST_CHOSENLIKED_RESTAURANT1, TEST_USER1);
        setChosenRestaurantDocument(TEST_CHOSEN_RESTAURANT2);
        setChosenRestaurantForUser(TEST_CHOSEN_RESTAURANT2, TEST_USER2);
        // Change chosen restaurant2 document timestamp to be outdated
        Calendar c = Calendar.getInstance();
        int yesterday = c.get(Calendar.DAY_OF_YEAR) - 1;
        c.set(Calendar.DAY_OF_YEAR, yesterday);
        Tasks.await(TEST_CHOSEN_RESTAURANTS_COLLECTION.document(TEST_CHOSEN_RESTAURANT2.getPlaceId())
                .update(TIMESTAMP_FIELD, String.valueOf(c.getTimeInMillis())));

        Map<ChosenRestaurant, List<String>> actualChosenRestaurantByUserIdsMap =
                LiveDataTestUtil.getValue(underTestRestaurantRepository.getChosenRestaurantByUserIdsMap()); // Use under test  method

        Map<ChosenRestaurant, List<String>> expectedChosenRestaurantByUserIdsMap = new HashMap<>();
        expectedChosenRestaurantByUserIdsMap.put(TEST_CHOSENLIKED_RESTAURANT1, Collections.singletonList(TEST_USER1.getUid()));

        Assert.assertEquals(expectedChosenRestaurantByUserIdsMap.size(), actualChosenRestaurantByUserIdsMap.size());
        Assert.assertTrue(expectedChosenRestaurantByUserIdsMap.entrySet().stream().allMatch(e ->
                Objects.equals(e.getValue(), actualChosenRestaurantByUserIdsMap.get(e.getKey()))));
    }

    @Test
    public void unsetChosenRestaurantIdsAndCleanCollection_And_getChosenRestaurantIds_And_getChosenRestaurantByUserIdsMap_withSuccess() throws InterruptedException {
        underTestRestaurantRepository.unsetChosenRestaurantIdsAndCleanCollection();  // Use under test  method

        List<String> actualChosenRestaurantIds =
                LiveDataTestUtil.getValue(underTestRestaurantRepository.getChosenRestaurantIds());
        Assert.assertNull(actualChosenRestaurantIds);

        Map<ChosenRestaurant, List<String>> actualChosenRestaurantByUserIdsMap =
                LiveDataTestUtil.getValue(underTestRestaurantRepository.getChosenRestaurantByUserIdsMap());
        Assert.assertNull(actualChosenRestaurantByUserIdsMap);
    }

    // --- FIRESTORE RESTAURANT LIKING ---

    @Test
    public void getLikedRestaurantsCollection_withSuccess() {
        CollectionReference actualLikedRestaurantsCollection =
                underTestRestaurantRepository.getLikedRestaurantsCollection();

        Assert.assertEquals(TEST_LIKED_RESTAURANTS_COLLECTION, actualLikedRestaurantsCollection);
    }

    private Map<String, Restaurant> waitForAllRestaurantsLiveDataUpdating(Map<String, Restaurant> actualAllRestaurants) throws InterruptedException {
        Map<String, Restaurant> possiblyUpdatedActualAllRestaurants = actualAllRestaurants;
        long startTime = System.currentTimeMillis();
        do {
            long lapTime = System.currentTimeMillis();
            if (lapTime - startTime > 2000) break;
            possiblyUpdatedActualAllRestaurants = LiveDataTestUtil.getValue(underTestRestaurantRepository.getAllRestaurants());
        } while (Objects.equals(possiblyUpdatedActualAllRestaurants, actualAllRestaurants));

        return possiblyUpdatedActualAllRestaurants;
    }
}