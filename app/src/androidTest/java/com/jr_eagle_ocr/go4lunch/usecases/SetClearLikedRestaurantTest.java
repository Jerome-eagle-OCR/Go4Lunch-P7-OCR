package com.jr_eagle_ocr.go4lunch.usecases;

import static com.jr_eagle_ocr.go4lunch.AndroidTestUtils.TEST_CHOSENLIKED_RESTAURANT1;
import static com.jr_eagle_ocr.go4lunch.AndroidTestUtils.TEST_LIKED_RESTAURANTS_COLLECTION;
import static com.jr_eagle_ocr.go4lunch.AndroidTestUtils.TEST_USER1;
import static com.jr_eagle_ocr.go4lunch.AndroidTestUtils.TEST_USER1_EMAIL;
import static com.jr_eagle_ocr.go4lunch.AndroidTestUtils.TEST_USER1_PWD;
import static com.jr_eagle_ocr.go4lunch.AndroidTestUtils.TEST_USERS_COLLECTION;
import static com.jr_eagle_ocr.go4lunch.AndroidTestUtils.cleanTestLikedRestaurantsCollection;
import static com.jr_eagle_ocr.go4lunch.AndroidTestUtils.cleanTestUsersCollection;
import static com.jr_eagle_ocr.go4lunch.AndroidTestUtils.createUser;
import static com.jr_eagle_ocr.go4lunch.AndroidTestUtils.signInUser;
import static com.jr_eagle_ocr.go4lunch.AndroidTestUtils.signOut;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.jr_eagle_ocr.go4lunch.LiveDataTestUtil;
import com.jr_eagle_ocr.go4lunch.data.models.User;
import com.jr_eagle_ocr.go4lunch.data.repositories.RestaurantRepository;
import com.jr_eagle_ocr.go4lunch.data.repositories.UserRepository;
import com.jr_eagle_ocr.go4lunch.data.usecases.GetIsLikedRestaurant;
import com.jr_eagle_ocr.go4lunch.data.usecases.SetClearLikedRestaurant;
import com.jr_eagle_ocr.go4lunch.di.Go4LunchApplication;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Objects;

public class SetClearLikedRestaurantTest {

    // Needed to get result (all methods tested in UserRepositoryTest class)
    private final UserRepository userRepository =
            Go4LunchApplication.getDependencyContainer().getUserRepository();
    // Needed to set listener and get result (all methods tested in GetIsLikedRestaurantTest class)
    private final GetIsLikedRestaurant getIsLikedRestaurant =
            Go4LunchApplication.getDependencyContainer().getIsLikedRestaurant();

    private final SetClearLikedRestaurant underTestSetClearLikedRestaurant =
            Go4LunchApplication.getDependencyContainer().setClearLikedRestaurant();

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Before
    public void setUp() throws Exception {
        assert UserRepository.USERS_COLLECTION_NAME.equals(TEST_USERS_COLLECTION.getPath());
        assert RestaurantRepository.LIKED_COLLECTION_NAME.equals(TEST_LIKED_RESTAURANTS_COLLECTION.getPath());
        signInUser(TEST_USER1_EMAIL, TEST_USER1_PWD);
        cleanTestLikedRestaurantsCollection();
        cleanTestUsersCollection();
        User currentUser = createUser(userRepository);
        assert Objects.equals(currentUser, TEST_USER1);
        getIsLikedRestaurant.addListenerRegistration(TEST_CHOSENLIKED_RESTAURANT1.getPlaceId());
    }

    @After
    public void tearDown() throws Exception {
        getIsLikedRestaurant.removeListenerRegistration();
        cleanTestLikedRestaurantsCollection();
        cleanTestUsersCollection();
        signOut();
    }

    @Test
    public void setLikedRestaurant_asRestaurantIsNotAlreadyLiked_withSuccess() throws InterruptedException {
        assert !LiveDataTestUtil.getValue(getIsLikedRestaurant.isLikedRestaurant()); // Useful to observe so valorize livedata once
        underTestSetClearLikedRestaurant.setLikedRestaurant(TEST_CHOSENLIKED_RESTAURANT1.getPlaceId());
        // Assert like effectively taken into account
        // Wait for livedata valorization (triggered by Firestore listener)
        Boolean isLikedRestaurant = null;
        long startTime = System.currentTimeMillis();
        do {
            long lapTime = System.currentTimeMillis();
            if (lapTime - startTime > 2000) break; // To avoid infinite loop
            isLikedRestaurant = LiveDataTestUtil.getValue(getIsLikedRestaurant.isLikedRestaurant());
        } while (!isLikedRestaurant);
        Assert.assertTrue(isLikedRestaurant);
    }

    @Test
    public void clearLikedRestaurant_asRestaurantIsAlreadyLiked_withSuccess() throws InterruptedException {
        // Use setLikedRestaurant test to like test restaurant prior to test clearing method
        setLikedRestaurant_asRestaurantIsNotAlreadyLiked_withSuccess();

        underTestSetClearLikedRestaurant.clearLikedRestaurant(TEST_CHOSENLIKED_RESTAURANT1.getPlaceId());
        // Assert like effectively taken into account
        // Wait for livedata valorization (triggered by Firestore listener)
        Boolean isLikedRestaurant = null;
        long startTime = System.currentTimeMillis();
        do {
            long lapTime = System.currentTimeMillis();
            if (lapTime - startTime > 2000) break; // To avoid infinite loop
            isLikedRestaurant = LiveDataTestUtil.getValue(getIsLikedRestaurant.isLikedRestaurant());
        } while (isLikedRestaurant);
        Assert.assertFalse(isLikedRestaurant);
    }
}