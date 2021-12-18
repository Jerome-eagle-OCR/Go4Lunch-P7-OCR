package com.jr_eagle_ocr.go4lunch.repositories;

import static com.jr_eagle_ocr.go4lunch.AndroidTestUtils.TEST_USER1;
import static com.jr_eagle_ocr.go4lunch.AndroidTestUtils.TEST_USER1_EMAIL;
import static com.jr_eagle_ocr.go4lunch.AndroidTestUtils.TEST_USER1_ID;
import static com.jr_eagle_ocr.go4lunch.AndroidTestUtils.TEST_USER1_PWD;
import static com.jr_eagle_ocr.go4lunch.AndroidTestUtils.TEST_USER2_EMAIL;
import static com.jr_eagle_ocr.go4lunch.AndroidTestUtils.TEST_USER2_PWD;
import static com.jr_eagle_ocr.go4lunch.AndroidTestUtils.TEST_USERS_COLLECTION;
import static com.jr_eagle_ocr.go4lunch.AndroidTestUtils.createUser;
import static com.jr_eagle_ocr.go4lunch.AndroidTestUtils.cleanTestUsersCollection;
import static com.jr_eagle_ocr.go4lunch.AndroidTestUtils.signInUser;
import static com.jr_eagle_ocr.go4lunch.AndroidTestUtils.signOut;

import android.app.Application;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.jr_eagle_ocr.go4lunch.LiveDataTestUtil;
import com.jr_eagle_ocr.go4lunch.data.models.User;
import com.jr_eagle_ocr.go4lunch.data.repositories.UserRepository;
import com.jr_eagle_ocr.go4lunch.di.Go4LunchApplication;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

/**
 * @author jrigault
 */
@RunWith(AndroidJUnit4.class)
public class UserRepositoryTest {

    private final Application context = Go4LunchApplication.getDependencyContainer().getContext();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final UserRepository userRepository =
            Go4LunchApplication.getDependencyContainer().getUserRepository();

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Before
    public void setUp() throws Exception {
        assert UserRepository.USERS_COLLECTION_NAME.equals("users_test");
        signInUser(TEST_USER1_EMAIL, TEST_USER1_PWD);
        cleanTestUsersCollection();
    }

    @After
    public void tearDown() throws ExecutionException, InterruptedException {
        List<String> signInMethods = Tasks.await(auth.fetchSignInMethodsForEmail(TEST_USER2_EMAIL)).getSignInMethods();
        if (signInMethods != null && !signInMethods.isEmpty()) {
            FirebaseUser firebaseUser = Tasks.await(auth.signInWithEmailAndPassword(TEST_USER2_EMAIL, TEST_USER2_PWD)).getUser();
            if (firebaseUser != null) Tasks.await(firebaseUser.delete());
        }
        if (auth.getCurrentUser() == null) {
            signInUser(TEST_USER1_EMAIL, TEST_USER1_PWD);
        }
        cleanTestUsersCollection();
        signOut();
    }

    // --- FIREBASE ---

    @Test
    public void getCurrentFirebaseUser_WithSuccess() throws InterruptedException {
        FirebaseUser currentFirebaseUser = auth.getCurrentUser();
        FirebaseUser retrieved_firebaseUser = LiveDataTestUtil.getValue(userRepository.getCurrentFirebaseUser()); //Use under test method
        Assert.assertNotNull(retrieved_firebaseUser);
        Assert.assertEquals(currentFirebaseUser, retrieved_firebaseUser);
    }

    @Test
    public void signOut_WithSuccess() throws ExecutionException, InterruptedException {
        Tasks.await(userRepository.signOut(context)); //Use under test method
        Assert.assertNull(auth.getCurrentUser());
    }

    @Test
    public void deleteUser_WithSuccess() throws ExecutionException, InterruptedException {
        Tasks.await(userRepository.signOut(context));
        Tasks.await(auth.createUserWithEmailAndPassword(TEST_USER2_EMAIL, TEST_USER2_PWD));
        Assert.assertNotNull(auth.getCurrentUser());
        Tasks.await(userRepository.deleteUser(context)); // Use under test method
        List<String> signInMethods = Tasks.await(auth.fetchSignInMethodsForEmail(TEST_USER2_EMAIL)).getSignInMethods();
        assert signInMethods != null;
        Assert.assertTrue(signInMethods.isEmpty());
        Assert.assertNull(auth.getCurrentUser());
    }

    // --- FIRESTORE USERS ---

    @Test
    public void getUsersCollection_WithSuccess() {
        CollectionReference retrieved_usersCollection = userRepository.getUsersCollection(); // Use under test method
        Assert.assertNotNull(retrieved_usersCollection);
        Assert.assertEquals(TEST_USERS_COLLECTION, retrieved_usersCollection);
    }

    @Test
    public void getAllLoggedUsers_WhenNoUsersCreated_WithSuccess() throws InterruptedException {
        Map<String, User> retrieved_allLoggedUsers = LiveDataTestUtil.getValue(userRepository.getAllLoggedUsers()); // Use under test method 3
        Assert.assertNotNull(retrieved_allLoggedUsers);
        Assert.assertEquals(new HashMap<String, User>(), retrieved_allLoggedUsers);
    }

    @Test
    public void getCurrentUser_WhenNoUsersCreated_WithSuccess() throws InterruptedException {
        User retrieved_currentUser = LiveDataTestUtil.getValue(userRepository.getCurrentUser()); // Use under test method 4
        Assert.assertNull(retrieved_currentUser);
    }

    @Test
    public void createUser_And_UserCreatedEvent_And_GetAllLoggedUsers_And_GetCurrentUser_WithSuccess() throws InterruptedException, ExecutionException {
        createUser(userRepository); // Use under test method 1 (create test_user1)
        Tasks.await(userRepository.signOut(context)); // log-out test_user1 (repository sign-out does not update document boolean isLogged
                                                      // so allLoggedUsers will contain 2 users)

        Tasks.await(auth.createUserWithEmailAndPassword(TEST_USER2_EMAIL, TEST_USER2_PWD)); // log new test_user2 to create one more user document
        FirebaseUser currentFirebaseUser = auth.getCurrentUser();
        assert currentFirebaseUser != null && currentFirebaseUser.getEmail().equals(TEST_USER2_EMAIL);
        String test_user2_id = currentFirebaseUser.getUid();
        createUser(userRepository);

        Tasks.await(userRepository.signOut(context));
        signInUser(TEST_USER1_EMAIL, TEST_USER1_PWD);

        // Get users directly from Firestore
        User expected_test_user1 = Tasks.await(TEST_USERS_COLLECTION.document(TEST_USER1_ID).get()).toObject(User.class);
        User expected_test_user2 = Tasks.await(TEST_USERS_COLLECTION.document(test_user2_id).get()).toObject(User.class);

        Map<String, User> retrieved_allLoggedUsers = LiveDataTestUtil.getValue(userRepository.getAllLoggedUsers()); // Use under test method 3
        Assert.assertNotNull(retrieved_allLoggedUsers);
        Assert.assertEquals(expected_test_user1, retrieved_allLoggedUsers.get(TEST_USER1_ID));
        Assert.assertEquals(expected_test_user2, retrieved_allLoggedUsers.get(test_user2_id));

        User retrieved_currentUser = LiveDataTestUtil.getValue(userRepository.getCurrentUser()); // Use under test method 4

        Assert.assertNotNull(retrieved_currentUser);
        Assert.assertEquals(TEST_USER1, retrieved_currentUser); // Ensure to have all expected infos in user document
    }

    @Test
    public void getAllLoggedUsers_And_getCurrentUser_WhenNoUserLogged_WithSuccess() throws InterruptedException {
        // Create user first and check valorizations
        User currentUser = createUser(userRepository);
        assert Objects.equals(currentUser, TEST_USER1);
        assert !LiveDataTestUtil.getValue(userRepository.getAllLoggedUsers()).isEmpty();
        // Log-out:  ListenerRegistration removing and  should be done thanks to auth listener
        auth.signOut();

        // Wait for livedatas nulling
        long startTime1 = System.currentTimeMillis();
        while (userRepository.getAllLoggedUsers().getValue() != null) {
            if (System.currentTimeMillis() - startTime1 > 2000)
                break; // Avoid infinite loop in case something went wrong
        }

        Map<String, User> actualAllLoggedUsers = LiveDataTestUtil.getValue(userRepository.getAllLoggedUsers());
        Assert.assertNull(actualAllLoggedUsers);

        User actualCurrentUser = LiveDataTestUtil.getValue(userRepository.getCurrentUser());
        Assert.assertNull(actualCurrentUser);
    }
}