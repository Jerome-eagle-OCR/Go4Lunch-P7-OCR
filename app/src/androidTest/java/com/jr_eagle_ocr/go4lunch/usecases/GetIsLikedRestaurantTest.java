package com.jr_eagle_ocr.go4lunch.usecases;

import static com.jr_eagle_ocr.go4lunch.AndroidTestUtils.TEST_CHOSENLIKED_RESTAURANT1;
import static com.jr_eagle_ocr.go4lunch.AndroidTestUtils.TEST_LIKED_RESTAURANTS_COLLECTION;
import static com.jr_eagle_ocr.go4lunch.AndroidTestUtils.TEST_RESTAURANT_DATA;
import static com.jr_eagle_ocr.go4lunch.AndroidTestUtils.TEST_USER1;
import static com.jr_eagle_ocr.go4lunch.AndroidTestUtils.TEST_USER1_EMAIL;
import static com.jr_eagle_ocr.go4lunch.AndroidTestUtils.TEST_USER1_ID;
import static com.jr_eagle_ocr.go4lunch.AndroidTestUtils.TEST_USER1_PWD;
import static com.jr_eagle_ocr.go4lunch.AndroidTestUtils.TEST_USERS_COLLECTION;
import static com.jr_eagle_ocr.go4lunch.AndroidTestUtils.TEST_USER_DATA;
import static com.jr_eagle_ocr.go4lunch.AndroidTestUtils.cleanTestLikedRestaurantsCollection;
import static com.jr_eagle_ocr.go4lunch.AndroidTestUtils.cleanTestUsersCollection;
import static com.jr_eagle_ocr.go4lunch.AndroidTestUtils.signInUser;
import static com.jr_eagle_ocr.go4lunch.AndroidTestUtils.signOut;
import static com.jr_eagle_ocr.go4lunch.data.repositories.RestaurantRepository.LIKEDBY_COLLECTION_NAME;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.gms.tasks.Tasks;
import com.jr_eagle_ocr.go4lunch.LiveDataTestUtil;
import com.jr_eagle_ocr.go4lunch.data.repositories.RestaurantRepository;
import com.jr_eagle_ocr.go4lunch.data.repositories.UserRepository;
import com.jr_eagle_ocr.go4lunch.data.usecases.GetIsLikedRestaurant;
import com.jr_eagle_ocr.go4lunch.di.Go4LunchApplication;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.ExecutionException;

/**
 * @author jrigault
 */
@RunWith(AndroidJUnit4.class)
public class GetIsLikedRestaurantTest {

    private final GetIsLikedRestaurant underTestGetIsLikedRestaurant =
            Go4LunchApplication.getDependencyContainer().getIsLikedRestaurant();

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Before
    public void setUp() throws Exception {
        assert UserRepository.USERS_COLLECTION_NAME.equals(TEST_USERS_COLLECTION.getPath());
        assert RestaurantRepository.LIKED_COLLECTION_NAME.equals(TEST_LIKED_RESTAURANTS_COLLECTION.getPath());
        signInUser(TEST_USER1_EMAIL, TEST_USER1_PWD);
        cleanTestLikedRestaurantsCollection();
        cleanTestUsersCollection();
        Tasks.await(TEST_USERS_COLLECTION.document(TEST_USER1_ID).set(TEST_USER1));
    }

    @After
    public void tearDown() throws Exception {
        underTestGetIsLikedRestaurant.removeListenerRegistration();
        cleanTestLikedRestaurantsCollection();
        cleanTestUsersCollection();
        signOut();
    }

    @Test
    public void givenUserDidNotLikeRestaurant_WhenAddingListener_ThenIsLikedRestaurantShouldBeFalse() throws InterruptedException, ExecutionException {
        setUserDoNotLikeRestaurant();
        addListener();
        Boolean isLikedRestaurant = LiveDataTestUtil.getValue(underTestGetIsLikedRestaurant.isLikedRestaurant());
        Assert.assertFalse(isLikedRestaurant);
    }

    @Test
    public void givenUserLikedRestaurant_WhenAddingListener_ThenIsLikedRestaurantShouldBeTrue() throws InterruptedException, ExecutionException {
        setUserLikeRestaurant();
        addListener();
        Boolean isLikedRestaurant = LiveDataTestUtil.getValue(underTestGetIsLikedRestaurant.isLikedRestaurant());
        Assert.assertTrue(isLikedRestaurant);
    }

    @Test
    public void givenUserDidNotLikeRestaurant_whenAddingListenerAndAfterwardsUserLikeRestaurant_ThenIsLikedRestaurantShouldBeTrue() throws ExecutionException, InterruptedException {
        setUserDoNotLikeRestaurant();
        addListener();
        setUserLikeRestaurant();
        Boolean isLikedRestaurant = LiveDataTestUtil.getValue(underTestGetIsLikedRestaurant.isLikedRestaurant());
        Assert.assertTrue(isLikedRestaurant);
    }

    @Test
    public void givenUserLikedRestaurant_whenAddingListenerAndAfterwardsUserUnlikeRestaurant_ThenIsLikedRestaurantShouldBeFalse() throws ExecutionException, InterruptedException {
        setUserLikeRestaurant();
        addListener();
        setUserDoNotLikeRestaurant();
        Boolean isLikedRestaurant = LiveDataTestUtil.getValue(underTestGetIsLikedRestaurant.isLikedRestaurant());
        Assert.assertFalse(isLikedRestaurant);
    }

    private void setUserDoNotLikeRestaurant() throws ExecutionException, InterruptedException {
        String placeId = TEST_CHOSENLIKED_RESTAURANT1.getPlaceId();
        Tasks.await(TEST_LIKED_RESTAURANTS_COLLECTION.document(placeId).set(TEST_RESTAURANT_DATA));
        Tasks.await(TEST_LIKED_RESTAURANTS_COLLECTION.document(placeId)
                .collection(LIKEDBY_COLLECTION_NAME).document(TEST_USER1_ID).delete());
    }

    private void setUserLikeRestaurant() throws ExecutionException, InterruptedException {
        String placeId = TEST_CHOSENLIKED_RESTAURANT1.getPlaceId();
        setUserDoNotLikeRestaurant();
        Tasks.await(TEST_LIKED_RESTAURANTS_COLLECTION.document(placeId)
                .collection(LIKEDBY_COLLECTION_NAME).document(TEST_USER1_ID).set(TEST_USER_DATA));
    }

    private void addListener() {
        String placeId = TEST_CHOSENLIKED_RESTAURANT1.getPlaceId();
        underTestGetIsLikedRestaurant.addListenerRegistration(placeId); // Needed and at the same time asserts that method works properly
    }
}