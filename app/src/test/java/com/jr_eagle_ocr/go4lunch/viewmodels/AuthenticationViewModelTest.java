package com.jr_eagle_ocr.go4lunch.viewmodels;

import static com.jr_eagle_ocr.go4lunch.ui.authentication.AuthenticationViewModel.AUTHENTICATE;
import static com.jr_eagle_ocr.go4lunch.ui.authentication.AuthenticationViewModel.NAVIGATE_TO_MAIN;
import static com.jr_eagle_ocr.go4lunch.ui.authentication.AuthenticationViewModel.TOAST_AUTH_SUCCESS;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.google.firebase.auth.FirebaseUser;
import com.jr_eagle_ocr.go4lunch.BuildConfig;
import com.jr_eagle_ocr.go4lunch.LiveDataTestUtil;
import com.jr_eagle_ocr.go4lunch.data.repositories.RestaurantRepository;
import com.jr_eagle_ocr.go4lunch.data.repositories.UserRepository;
import com.jr_eagle_ocr.go4lunch.ui.authentication.AuthenticationViewModel;
import com.jr_eagle_ocr.go4lunch.util.Event;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author jrigault
 */
@RunWith(MockitoJUnitRunner.class)
public class AuthenticationViewModelTest {
    static {
        BuildConfig.IS_TESTING.set(true);
    }

    @Mock
    private FirebaseUser mockFirebaseUser;
    @Mock
    private UserRepository mockUserRepository;
    @Mock
    private RestaurantRepository mockRestaurantRepository;

    private AuthenticationViewModel underTestAuthenticationViewModel;

    // Mock livedatas
    private final MutableLiveData<FirebaseUser> currentFirebaseUserMutableLiveData = new MutableLiveData<>();
    private final MutableLiveData<Event<Boolean>> isUserCreatedEventMutableLiveData = new MutableLiveData<>();

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Before
    public void setUp() throws Exception {
        when(mockUserRepository.getCurrentFirebaseUser()).thenReturn(currentFirebaseUserMutableLiveData);

        when(mockUserRepository.getUserCreatedEvent()).thenReturn(isUserCreatedEventMutableLiveData);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void doActionEvent_whenStartingWithNoUserLogged_shouldBeAsExpected() throws InterruptedException {
        currentFirebaseUserMutableLiveData.setValue(null);
        valorizeUnderTestViewModel();

        Event<String> actualDoActionEvent = LiveDataTestUtil.getValue(underTestAuthenticationViewModel.doActionEvent());
        Event<String> expectedDoActionEvent = new Event<>(AUTHENTICATE);

        assertEquals(expectedDoActionEvent, actualDoActionEvent);
    }

    @Test
    public void doActionEvent_whenStartingWithUserLogged_shouldBeAsExpected() throws InterruptedException {
        currentFirebaseUserMutableLiveData.setValue(mockFirebaseUser);
        valorizeUnderTestViewModel();

        Event<String> actualDoActionEvent = LiveDataTestUtil.getValue(underTestAuthenticationViewModel.doActionEvent());
        Event<String> expectedDoActionEvent = new Event<>(NAVIGATE_TO_MAIN);

        assertEquals(expectedDoActionEvent, actualDoActionEvent);
    }

    @Test
    public void doActionEvent_onIsUserCreatedEvent_shouldDynamicallyReactAsExpected() throws InterruptedException {
        // No matter current firebase user
        valorizeUnderTestViewModel();

        LiveData<Event<String>> doActionEventLiveData = underTestAuthenticationViewModel.doActionEvent();

        List<Event<String>> actualDoActionEventList = new ArrayList<>();
        List<Event<String>> expectedDoActionEventList = new ArrayList<>();
        expectedDoActionEventList.add(new Event<>(TOAST_AUTH_SUCCESS));
        expectedDoActionEventList.add(new Event<>(NAVIGATE_TO_MAIN));

        // Observe the livedata and add events in list
        // Stop when list size is reached, after 2 seconds if not
        final CountDownLatch latch = new CountDownLatch(1);
        Observer<Event<String>> observer = e -> {
            actualDoActionEventList.add(e);
            if (actualDoActionEventList.size() == 3) latch.countDown();
        };
        doActionEventLiveData.observeForever(observer);
        isUserCreatedEventMutableLiveData.setValue(new Event<>(true));
        latch.await(2, TimeUnit.SECONDS);

        // Ignore first element which is the added event at starting depending on user logged or not
        assertEquals(expectedDoActionEventList, actualDoActionEventList.subList(1, actualDoActionEventList.size()));

        // Be sure no event added in between prior to removing observer
        assertEquals(3, actualDoActionEventList.size());
        doActionEventLiveData.removeObserver(observer);
    }

    @Test
    public void whenStarting_ifNoUserLogged_shouldCallMethodsAsExpected() {
        currentFirebaseUserMutableLiveData.setValue(null);
        valorizeUnderTestViewModel();

        verify(mockUserRepository).getCurrentFirebaseUser();
        verify(mockUserRepository).getUserCreatedEvent();
        verifyNoMoreInteractions(mockUserRepository);
        verifyNoInteractions(mockRestaurantRepository);
    }

    @Test
    public void whenStarting_ifUserLogged_shouldCallMethodsAsExpected() {
        currentFirebaseUserMutableLiveData.setValue(mockFirebaseUser);
        valorizeUnderTestViewModel();

        verify(mockUserRepository).getCurrentFirebaseUser();
        verify(mockUserRepository).getUserCreatedEvent();
        verify(mockRestaurantRepository).setAllRestaurants();
        verify(mockRestaurantRepository).setChosenRestaurantIdsAndCleanCollection();
        verifyNoMoreInteractions(mockUserRepository);
        verifyNoMoreInteractions(mockRestaurantRepository);
    }

    @Test
    public void setAuthenticationSuccessful_shouldCallMethodsAsExpected() {
        currentFirebaseUserMutableLiveData.setValue(null);
        valorizeUnderTestViewModel();
        // Clear previously validated starting invocations
        clearInvocations(mockUserRepository);
        clearInvocations(mockRestaurantRepository);

        underTestAuthenticationViewModel.setAuthenticationSuccessful();

        verify(mockRestaurantRepository).setAllRestaurants();
        verify(mockRestaurantRepository).setChosenRestaurantIdsAndCleanCollection();
        verify(mockUserRepository).createUser();
        verifyNoMoreInteractions(mockUserRepository);
        verifyNoMoreInteractions(mockRestaurantRepository);
    }

    @Test
    public void onCleared_shouldRemoveObserver() {
        // No matter current firebase user
        valorizeUnderTestViewModel();

        underTestAuthenticationViewModel.onCleared();

        assert !isUserCreatedEventMutableLiveData.hasObservers();
    }

    private void valorizeUnderTestViewModel() {
        underTestAuthenticationViewModel = new AuthenticationViewModel(mockUserRepository, mockRestaurantRepository);
    }
}