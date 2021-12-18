package com.jr_eagle_ocr.go4lunch.usecases;

import static com.jr_eagle_ocr.go4lunch.AndroidTestUtils.TEST_CHOSENLIKED_RESTAURANT1;
import static com.jr_eagle_ocr.go4lunch.AndroidTestUtils.TEST_CHOSEN_RESTAURANTS_COLLECTION;
import static com.jr_eagle_ocr.go4lunch.AndroidTestUtils.TEST_USER1;
import static com.jr_eagle_ocr.go4lunch.AndroidTestUtils.TEST_USER1_EMAIL;
import static com.jr_eagle_ocr.go4lunch.AndroidTestUtils.TEST_USER1_PWD;
import static com.jr_eagle_ocr.go4lunch.AndroidTestUtils.TEST_USER2;
import static com.jr_eagle_ocr.go4lunch.AndroidTestUtils.TEST_USER3;
import static com.jr_eagle_ocr.go4lunch.AndroidTestUtils.TEST_USER4;
import static com.jr_eagle_ocr.go4lunch.AndroidTestUtils.cleanTestChosenRestaurantsCollection;
import static com.jr_eagle_ocr.go4lunch.AndroidTestUtils.setChosenRestaurantDocument;
import static com.jr_eagle_ocr.go4lunch.AndroidTestUtils.setChosenRestaurantForUser;
import static com.jr_eagle_ocr.go4lunch.AndroidTestUtils.signInUser;
import static com.jr_eagle_ocr.go4lunch.AndroidTestUtils.signOut;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.core.util.Pair;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.auth.FirebaseUser;
import com.jr_eagle_ocr.go4lunch.R;
import com.jr_eagle_ocr.go4lunch.data.repositories.RestaurantRepository;
import com.jr_eagle_ocr.go4lunch.data.repositories.UserRepository;
import com.jr_eagle_ocr.go4lunch.data.usecases.GetNotificationPair;
import com.jr_eagle_ocr.go4lunch.di.Go4LunchApplication;
import com.jr_eagle_ocr.go4lunch.ui.MainActivity;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @author jrigault
 */
@RunWith(AndroidJUnit4.class)
public class GetNotificationPairTest {

    private final Context context =
            Go4LunchApplication.getDependencyContainer().getContext();
    // Needed to check auth state change when sign-out (all methods tested in UserRepositoryTest class)
    private final UserRepository userRepository =
            Go4LunchApplication.getDependencyContainer().getUserRepository();


    private final GetNotificationPair underTestGetNotificationPair =
            Go4LunchApplication.getDependencyContainer().getNotificationKit();

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Before
    public void setUp() throws Exception {
        assert RestaurantRepository.CHOSEN_COLLECTION_NAME.equals(TEST_CHOSEN_RESTAURANTS_COLLECTION.getPath());
        signInUser(TEST_USER1_EMAIL, TEST_USER1_PWD);
    }

    @After
    public void tearDown() throws Exception {
        cleanTestChosenRestaurantsCollection();
        signOut();
    }

    @Test
    public void getNotificationKit_NoUserLogged_WithSuccess() throws ExecutionException, InterruptedException {
        signOut();
        FirebaseUser firebaseUser;
        do {
            firebaseUser = userRepository.getCurrentFirebaseUser().getValue();
        } while (firebaseUser != null);

        Assert.assertEquals(new Pair<>(null, null), underTestGetNotificationPair.getNotificationPair(context));
    }

    @Test
    public void getNotificationKit_UserLoggedButNoChoiceMade_WithSuccess() throws ExecutionException, InterruptedException {
        Assert.assertEquals(new Pair<>(null, null), underTestGetNotificationPair.getNotificationPair(context));
    }

    @Test
    public void getNotificationKit_UserLoggedAndChoiceMadeBut_0_JoiningWorkmate_WithSuccess() throws ExecutionException, InterruptedException {
        setChosenRestaurantDocument(TEST_CHOSENLIKED_RESTAURANT1);
        setChosenRestaurantForUser(TEST_CHOSENLIKED_RESTAURANT1, TEST_USER1);

        assertSuccess(0);
    }

    @Test
    public void getNotificationKit_UserLoggedAndChoiceMadeAnd_1_JoiningWorkmate_WithSuccess() throws ExecutionException, InterruptedException {
        setChosenRestaurantDocument(TEST_CHOSENLIKED_RESTAURANT1);
        setChosenRestaurantForUser(TEST_CHOSENLIKED_RESTAURANT1, TEST_USER1);
        setChosenRestaurantForUser(TEST_CHOSENLIKED_RESTAURANT1, TEST_USER2);

        assertSuccess(1);
    }

    @Test
    public void getNotificationKit_UserLoggedAndChoiceMadeAnd_2_JoiningWorkmates_WithSuccess() throws ExecutionException, InterruptedException {
        setChosenRestaurantDocument(TEST_CHOSENLIKED_RESTAURANT1);
        setChosenRestaurantForUser(TEST_CHOSENLIKED_RESTAURANT1, TEST_USER1);
        setChosenRestaurantForUser(TEST_CHOSENLIKED_RESTAURANT1, TEST_USER2);
        setChosenRestaurantForUser(TEST_CHOSENLIKED_RESTAURANT1, TEST_USER3);

        assertSuccess(2);
    }

    @Test
    public void getNotificationKit_UserLoggedAndChoiceMadeAnd_3_JoiningWorkmates_WithSuccess() throws ExecutionException, InterruptedException {
        setChosenRestaurantDocument(TEST_CHOSENLIKED_RESTAURANT1);
        setChosenRestaurantForUser(TEST_CHOSENLIKED_RESTAURANT1, TEST_USER1);
        setChosenRestaurantForUser(TEST_CHOSENLIKED_RESTAURANT1, TEST_USER2);
        setChosenRestaurantForUser(TEST_CHOSENLIKED_RESTAURANT1, TEST_USER3);
        setChosenRestaurantForUser(TEST_CHOSENLIKED_RESTAURANT1, TEST_USER4);

        assertSuccess(3);
    }

    private Pair<List<String>, PendingIntent> getTestNotificationKit(int joiningCount) {
        List<String> notificationLines;
        PendingIntent test_pendingIntent;

        String prefix = context.getString(R.string.you_lunch_at);
        String alone = context.getString(R.string.alone);
        String with = context.getString(R.string.with);
        String and = context.getString(R.string.and);

        // Create the test notification lines
        notificationLines = new ArrayList<>();
        // Restaurant lines
        notificationLines.add(prefix + TEST_CHOSENLIKED_RESTAURANT1.getPlaceName());
        notificationLines.add(TEST_CHOSENLIKED_RESTAURANT1.getPlaceAddress().replace(", France", ""));

        String withTestUser2 = with + TEST_USER2.getUserName();

        switch (joiningCount) {
            case 0:
                notificationLines.add(alone);
                break;
            case 1:
                notificationLines.add(withTestUser2);
                break;
            case 2:
                notificationLines.add(withTestUser2 + and + TEST_USER3.getUserName());
                break;
            case 3:
                notificationLines.add(withTestUser2 + ", " + TEST_USER3.getUserName() + and + TEST_USER4.getUserName());
                break;
        }

        // Create the test Intent
        Intent intent = MainActivity.navigate(context, true);
        test_pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        return new Pair<>(notificationLines, test_pendingIntent);
    }

    private void assertSuccess(int joiningCount) throws ExecutionException, InterruptedException {
        Pair<List<String>, PendingIntent> expected = getTestNotificationKit(joiningCount);
        Pair<List<String>, PendingIntent> actual = underTestGetNotificationPair.getNotificationPair(context);

        Assert.assertEquals(expected, actual);
    }
}