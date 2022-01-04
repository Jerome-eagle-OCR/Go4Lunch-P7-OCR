package com.jr_eagle_ocr.go4lunch.viewmodels;

import static com.jr_eagle_ocr.go4lunch.TestUtils.TEST_USER1;
import static com.jr_eagle_ocr.go4lunch.TestUtils.TEST_USER1_ID;
import static com.jr_eagle_ocr.go4lunch.TestUtils.TEST_USER2;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.jr_eagle_ocr.go4lunch.LiveDataTestUtil;
import com.jr_eagle_ocr.go4lunch.R;
import com.jr_eagle_ocr.go4lunch.data.models.User;
import com.jr_eagle_ocr.go4lunch.data.repositories.RestaurantRepository;
import com.jr_eagle_ocr.go4lunch.data.repositories.UserRepository;
import com.jr_eagle_ocr.go4lunch.data.usecases.SetClearChosenRestaurant;
import com.jr_eagle_ocr.go4lunch.ui.settings.SettingsDialogViewState;
import com.jr_eagle_ocr.go4lunch.ui.settings.SettingsViewModel;

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
public class SettingsViewModelTest {

    private static final String TEST_URL_PICTURE = "https://ia801507.us.archive.org/30/items/olivia_202112/Oggy.jpg";

    @Mock
    private UserRepository mockUserRepository;
    @Mock
    private RestaurantRepository mockRestaurantRepository;
    @Mock
    private SetClearChosenRestaurant mockSetClearChosenRestaurant;


    //Mock livedatas
    private final MutableLiveData<User> currentUserMutableLiveData = new MutableLiveData<>();


    private SettingsViewModel underTestSettingsViewModel;

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Before
    public void setUp() throws Exception {
        currentUserMutableLiveData.setValue(TEST_USER1);
        when(mockUserRepository.getCurrentUser()).thenReturn(currentUserMutableLiveData);
        final Task mockTask = mock(Task.class);
        when(mockTask.isSuccessful()).thenReturn(true);
        when(mockTask.addOnCompleteListener(any(OnCompleteListener.class))).thenAnswer(invocation -> {
            ((OnCompleteListener) invocation.getArgument(0)).onComplete(mockTask);
            return mockTask;
        });
        when(mockUserRepository.setUser(any(User.class))).thenReturn(mockTask);

        underTestSettingsViewModel = new SettingsViewModel(mockUserRepository,
                mockRestaurantRepository,
                mockSetClearChosenRestaurant);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void getSettingsDialogViewState() {
        SettingsDialogViewState actual = underTestSettingsViewModel.getSettingsDialogViewState();

        SettingsDialogViewState expected = new SettingsDialogViewState(TEST_USER1.isNoonReminderEnabled(),
                                                                       TEST_USER1.getUserName(),
                                                                       TEST_USER1.getUserUrlPicture());

        assertEquals(expected, actual);
    }

//    @Test
//    public void deleteUser() {
//    }
//
//    @Test
//    public void deleteUserResult() {
//    }

    @Test
    public void setNoonReminderEnabled_withSuccess() {
        underTestSettingsViewModel.setNoonReminderEnabled(true);

        boolean actual = underTestSettingsViewModel.isNoonReminderEnabled();

        assertTrue(actual);
    }

    @Test
    public void setUserName_withSuccess() {
        String expected = TEST_USER2.getUserName();
        underTestSettingsViewModel.setUserName(expected);

        String actual = underTestSettingsViewModel.getUserName();

        assertEquals(expected, actual);
    }

    @Test
    public void setUserUrlPicture_withSuccess() {
        underTestSettingsViewModel.setUserUrlPicture(TEST_URL_PICTURE);

        String actual = underTestSettingsViewModel.getUserUrlPicture();

        assertEquals(TEST_URL_PICTURE, actual);
    }

    @Test
    public void validateResult() throws InterruptedException {
        Integer actual = LiveDataTestUtil.getValue(underTestSettingsViewModel.validateResult());
        assertNull(actual);

        underTestSettingsViewModel.getValidateResultMutableLiveData().setValue(R.string.error_unknown_error);

        actual = LiveDataTestUtil.getValue(underTestSettingsViewModel.validateResult());

        assertEquals(R.string.error_unknown_error, (int) actual);
    }

    @Test
    public void clickOnButtonValidate_whenSettingsActuallyChanged_shouldCallMethodAsExpected() throws InterruptedException {
        User expected = new User(TEST_USER1_ID,
                                 TEST_USER2.getUserName(),
                                 TEST_USER1.getUserEmail(),
                                 TEST_URL_PICTURE,
                                 false,
                                 TEST_USER1.isLogged());

        underTestSettingsViewModel.setUserName(expected.getUserName());
        underTestSettingsViewModel.setUserUrlPicture(expected.getUserUrlPicture());
        underTestSettingsViewModel.setNoonReminderEnabled(expected.isNoonReminderEnabled());

        underTestSettingsViewModel.clickOnButtonValidate(); // Under test method

        verify(mockUserRepository).setUser(expected);

        Integer actualValidateResult = LiveDataTestUtil.getValue(underTestSettingsViewModel.validateResult());

        assertEquals(R.string.changes_made, (int) actualValidateResult);
    }

    @Test
    public void clickOnButtonValidate_whenSettingsNotChanged_shouldCallMethodAsExpected() throws InterruptedException {
        underTestSettingsViewModel.clickOnButtonValidate(); // Under test method

        verify(mockUserRepository).setUser(TEST_USER1);

        Integer actualValidateResult = LiveDataTestUtil.getValue(underTestSettingsViewModel.validateResult());

        assertEquals(R.string.no_changes_made, (int) actualValidateResult);
    }
}