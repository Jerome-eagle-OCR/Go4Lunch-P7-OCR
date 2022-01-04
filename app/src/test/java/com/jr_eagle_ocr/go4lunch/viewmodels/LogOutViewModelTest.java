package com.jr_eagle_ocr.go4lunch.viewmodels;

import static com.jr_eagle_ocr.go4lunch.TestUtils.TEST_USER1;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.jr_eagle_ocr.go4lunch.BuildConfig;
import com.jr_eagle_ocr.go4lunch.LiveDataTestUtil;
import com.jr_eagle_ocr.go4lunch.TestUtils;
import com.jr_eagle_ocr.go4lunch.data.models.User;
import com.jr_eagle_ocr.go4lunch.data.repositories.UserRepository;
import com.jr_eagle_ocr.go4lunch.ui.logout.LogOutViewModel;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author jrigault
 */
@RunWith(MockitoJUnitRunner.class)
public class LogOutViewModelTest {
    static {
        BuildConfig.IS_TESTING.set(true);
    }

    @Mock
    private Task<Void> mockTask;
    @Mock
    private UserRepository mockUserRepository;

    private LogOutViewModel underTestLogOutViewModel;

    @Before
    public void setUp() throws Exception {
        when(mockUserRepository.getCurrentFirebaseUser()).thenReturn(new MutableLiveData<>(mock(FirebaseUser.class)));
        when(mockUserRepository.getCurrentUser()).thenReturn(new MutableLiveData<>(TEST_USER1));
        when(mockUserRepository.signOut(any())).thenReturn(mockTask);
        underTestLogOutViewModel = new LogOutViewModel(mockUserRepository);
    }

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void signOutResult_whenStarting_shouldReturnNull() throws InterruptedException {
        Integer actualSignOutResult = LiveDataTestUtil.getValue(underTestLogOutViewModel.signOutResult());

        assertNull(actualSignOutResult);
    }

    @Test
    public void signOut_shouldCallMethod() {
        when(mockTask.addOnSuccessListener((OnSuccessListener<Void>) any(OnSuccessListener.class))).thenReturn(mockTask);
        when(mockTask.addOnFailureListener(any(OnFailureListener.class))).thenReturn(mockTask);

        underTestLogOutViewModel.signOut(null);

        TEST_USER1.setLogged(false); // As user sign out isLogged must be false
        verify(mockUserRepository).setUser(TEST_USER1);
        verify(mockUserRepository).signOut(any());
        TEST_USER1.setLogged(true); // Set it back to true for other test class to perform normally
    }
}