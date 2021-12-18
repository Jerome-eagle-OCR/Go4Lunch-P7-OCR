package com.jr_eagle_ocr.go4lunch.usecases;

import static com.jr_eagle_ocr.go4lunch.AndroidTestUtils.TEST_CHOSENLIKED_RESTAURANT1;
import static com.jr_eagle_ocr.go4lunch.AndroidTestUtils.TEST_CHOSEN_RESTAURANT2;
import static com.jr_eagle_ocr.go4lunch.AndroidTestUtils.TEST_FOUND_RESTAURANT;
import static com.jr_eagle_ocr.go4lunch.AndroidTestUtils.TEST_FOUND_RESTAURANT2;
import static com.jr_eagle_ocr.go4lunch.AndroidTestUtils.TEST_USER1;
import static com.jr_eagle_ocr.go4lunch.AndroidTestUtils.TEST_USER1_EMAIL;
import static com.jr_eagle_ocr.go4lunch.AndroidTestUtils.TEST_USER1_PWD;
import static com.jr_eagle_ocr.go4lunch.AndroidTestUtils.TEST_USER2;
import static com.jr_eagle_ocr.go4lunch.AndroidTestUtils.cleanTestChosenRestaurantsCollection;
import static com.jr_eagle_ocr.go4lunch.AndroidTestUtils.cleanTestRestaurantsCollection;
import static com.jr_eagle_ocr.go4lunch.AndroidTestUtils.cleanTestUsersCollection;
import static com.jr_eagle_ocr.go4lunch.AndroidTestUtils.createUser;
import static com.jr_eagle_ocr.go4lunch.AndroidTestUtils.setChosenRestaurantDocument;
import static com.jr_eagle_ocr.go4lunch.AndroidTestUtils.setChosenRestaurantForUser;
import static com.jr_eagle_ocr.go4lunch.AndroidTestUtils.signInUser;
import static com.jr_eagle_ocr.go4lunch.AndroidTestUtils.signOut;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.gms.tasks.Tasks;
import com.jr_eagle_ocr.go4lunch.LiveDataTestUtil;
import com.jr_eagle_ocr.go4lunch.data.models.ChosenRestaurant;
import com.jr_eagle_ocr.go4lunch.data.models.User;
import com.jr_eagle_ocr.go4lunch.data.repositories.RestaurantRepository;
import com.jr_eagle_ocr.go4lunch.data.repositories.UserRepository;
import com.jr_eagle_ocr.go4lunch.data.usecases.GetCurrentUserChosenRestaurantId;
import com.jr_eagle_ocr.go4lunch.data.usecases.SetClearChosenRestaurant;
import com.jr_eagle_ocr.go4lunch.di.Go4LunchApplication;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

/**
 * @author jrigault
 */
@RunWith(AndroidJUnit4.class)
public class SetClearChosenRestaurantTest {

    // Needed to get result (all methods tested in UserRepositoryTest class)
    private final UserRepository userRepository =
            Go4LunchApplication.getDependencyContainer().getUserRepository();
    // Needed to get result (all methods tested in RestaurantRepositoryTest class)
    private final RestaurantRepository restaurantRepository =
            Go4LunchApplication.getDependencyContainer().getRestaurantRepository();
    // Needed to get result (all methods tested in GetCurrentUserChosenRestaurantIdTest class)
    private final GetCurrentUserChosenRestaurantId getCurrentUserChosenRestaurantId =
            Go4LunchApplication.getDependencyContainer().getCurrentUserChosenRestaurantId();

    private final SetClearChosenRestaurant underTestSetClearChosenRestaurant =
            Go4LunchApplication.getDependencyContainer().setClearChosenRestaurant();

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Before
    public void setUp() throws Exception {
        assert UserRepository.USERS_COLLECTION_NAME.equals("users_test");
        assert RestaurantRepository.RESTAURANTS_COLLECTION_NAME.equals("restaurants_test");
        assert RestaurantRepository.CHOSEN_COLLECTION_NAME.equals("chosen_restaurants_test");
        signInUser(TEST_USER1_EMAIL, TEST_USER1_PWD);
        cleanTestRestaurantsCollection();
        cleanTestChosenRestaurantsCollection();
        cleanTestUsersCollection();
        User currentUser = createUser(userRepository);
        assert Objects.equals(currentUser, TEST_USER1);
        restaurantRepository.setAllRestaurants();
        restaurantRepository.setChosenRestaurantIdsAndCleanCollection();
        restaurantRepository.addFoundRestaurant(TEST_FOUND_RESTAURANT);
        restaurantRepository.addFoundRestaurant(TEST_FOUND_RESTAURANT2);
        assert !LiveDataTestUtil.getValue(restaurantRepository.getAllRestaurants()).isEmpty();
    }

    @After
    public void tearDown() throws Exception {
        restaurantRepository.unsetAllRestaurants();
        restaurantRepository.unsetChosenRestaurantIdsAndCleanCollection();
        cleanTestRestaurantsCollection();
        cleanTestChosenRestaurantsCollection();
        cleanTestUsersCollection();
        signOut();
    }

    @Test
    public void clearChosenRestaurant_withSuccess() throws ExecutionException, InterruptedException {
        // Set chosen restaurant for current user without using any under test method
        setChosenRestaurantDocument(TEST_CHOSENLIKED_RESTAURANT1);
        setChosenRestaurantForUser(TEST_CHOSENLIKED_RESTAURANT1, TEST_USER1);

        // Assert choice effectively made
        String currentUserChosenRestaurantId = LiveDataTestUtil.getValue(getCurrentUserChosenRestaurantId.getCurrentUserChosenRestaurantId());
        Assert.assertEquals(TEST_CHOSENLIKED_RESTAURANT1.getPlaceId(), currentUserChosenRestaurantId);

        // Clear current user choice using under test method
        boolean isClearingDone = Tasks.await(underTestSetClearChosenRestaurant.clearChosenRestaurant()); // Should not be null but if yes the test must surely fail
        Assert.assertTrue(isClearingDone);

        // Wait for livedata update
        long startTime = System.currentTimeMillis();
        do {
            long lapTime = System.currentTimeMillis();
            if (lapTime - startTime > 2000) break; // To avoid infinite loop
            currentUserChosenRestaurantId = LiveDataTestUtil.getValue(getCurrentUserChosenRestaurantId.getCurrentUserChosenRestaurantId());
        } while (!currentUserChosenRestaurantId.isEmpty());

        // Assert clearing effectively taken into account
        Assert.assertTrue(currentUserChosenRestaurantId.isEmpty());
    }

    @Test
    public void setANullChosenRestaurant_whenNoChosenRestaurants_shouldLeaveListAndMapEmpty() throws InterruptedException {
        underTestSetClearChosenRestaurant.setChosenRestaurant(null);

        List<String> actualChosenRestaurantIds =
                LiveDataTestUtil.getValue(restaurantRepository.getChosenRestaurantIds());
        Map<ChosenRestaurant, List<String>> actualChosenRestaurantByUserIdsMap =
                LiveDataTestUtil.getValue(restaurantRepository.getChosenRestaurantByUserIdsMap());


        Assert.assertTrue(actualChosenRestaurantIds.isEmpty());

        Assert.assertTrue(actualChosenRestaurantByUserIdsMap.isEmpty());
    }

    @Test
    public void setAChosenRestaurant_whenNoChosenRestaurant_withSuccess() throws InterruptedException {
        underTestSetClearChosenRestaurant.setChosenRestaurant(TEST_CHOSENLIKED_RESTAURANT1.getPlaceId());

        // Assert choice effectively taken into account
        String currentUserChosenRestaurantId = null;
        // Wait for livedata valorization (triggered by Firestore listener)
        long startTime1 = System.currentTimeMillis();
        do {
            long lapTime = System.currentTimeMillis();
            if (lapTime - startTime1 > 2000) break; // To avoid infinite loop
            currentUserChosenRestaurantId = LiveDataTestUtil.getValue(getCurrentUserChosenRestaurantId.getCurrentUserChosenRestaurantId());
        } while (currentUserChosenRestaurantId.isEmpty());
        Assert.assertEquals(TEST_CHOSENLIKED_RESTAURANT1.getPlaceId(), currentUserChosenRestaurantId);
    }

    @Test
    public void setAChosenRestaurant_whenRestaurantAlreadyChosenByAnotherUser_withSuccess() throws InterruptedException, ExecutionException {
        // Create test chosen restaurant1 document and set it chosen for test user2
        setChosenRestaurantDocument(TEST_CHOSENLIKED_RESTAURANT1);
        setChosenRestaurantForUser(TEST_CHOSENLIKED_RESTAURANT1, TEST_USER2);

        // Assert there is no impact on current user choice
        String currentUserChosenRestaurantId = LiveDataTestUtil.getValue(getCurrentUserChosenRestaurantId.getCurrentUserChosenRestaurantId());
        Assert.assertTrue(currentUserChosenRestaurantId.isEmpty());

        // Set the same choice for current user using under test method
        underTestSetClearChosenRestaurant.setChosenRestaurant(TEST_CHOSENLIKED_RESTAURANT1.getPlaceId());

        // Assert choice effectively taken into account
        // Wait for livedata valorization (triggered by Firestore listener)
        long startTime = System.currentTimeMillis();
        do {
            long lapTime = System.currentTimeMillis();
            if (lapTime - startTime > 2000) break; // To avoid infinite loop
            currentUserChosenRestaurantId = LiveDataTestUtil.getValue(getCurrentUserChosenRestaurantId.getCurrentUserChosenRestaurantId());
        } while (currentUserChosenRestaurantId.isEmpty());
        Assert.assertEquals(TEST_CHOSENLIKED_RESTAURANT1.getPlaceId(), currentUserChosenRestaurantId);
    }

    @Test
    public void setAChosenRestaurant_whenAnotherRestaurantAlreadyChosenByCurrentUser_withSuccess() throws InterruptedException {
        // Set the first restaurant choice
        underTestSetClearChosenRestaurant.setChosenRestaurant(TEST_CHOSENLIKED_RESTAURANT1.getPlaceId());

        // Assert choice effectively taken into account
        String currentUserChosenRestaurantId = null;
        // Wait for livedata valorization (triggered by Firestore listener)
        long startTime = System.currentTimeMillis();
        do {
            long lapTime = System.currentTimeMillis();
            if (lapTime - startTime > 2000) break; // To avoid infinite loop
            currentUserChosenRestaurantId = LiveDataTestUtil.getValue(getCurrentUserChosenRestaurantId.getCurrentUserChosenRestaurantId());
        } while (currentUserChosenRestaurantId.isEmpty());
        Assert.assertEquals(TEST_CHOSENLIKED_RESTAURANT1.getPlaceId(), currentUserChosenRestaurantId);

        // Set the new restaurant choice
        underTestSetClearChosenRestaurant.setChosenRestaurant(TEST_CHOSEN_RESTAURANT2.getPlaceId());

        // Assert new choice effectively taken into account
        // Wait for livedata update (triggered by Firestore listener)
        long startTime1 = System.currentTimeMillis();
        do {
            long lapTime = System.currentTimeMillis();
            if (lapTime - startTime1 > 2000) break; // To avoid infinite loop
            currentUserChosenRestaurantId = LiveDataTestUtil.getValue(getCurrentUserChosenRestaurantId.getCurrentUserChosenRestaurantId());
        } while (!currentUserChosenRestaurantId.equals(TEST_CHOSEN_RESTAURANT2.getPlaceId()));
        Assert.assertEquals(TEST_CHOSEN_RESTAURANT2.getPlaceId(), currentUserChosenRestaurantId);
    }
}