package com.jr_eagle_ocr.go4lunch.usecases;

import static com.jr_eagle_ocr.go4lunch.TestUtils.TEST_CHOSENRESTAURANT_BYUSERIDS_MAP;
import static com.jr_eagle_ocr.go4lunch.TestUtils.TEST_CHOSEN_RESTAURANT;
import static com.jr_eagle_ocr.go4lunch.TestUtils.TEST_USER1;
import static org.mockito.Mockito.doReturn;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.MutableLiveData;

import com.jr_eagle_ocr.go4lunch.data.models.ChosenRestaurant;
import com.jr_eagle_ocr.go4lunch.data.models.User;
import com.jr_eagle_ocr.go4lunch.data.repositories.RestaurantRepository;
import com.jr_eagle_ocr.go4lunch.data.repositories.UserRepository;
import com.jr_eagle_ocr.go4lunch.data.usecases.GetCurrentUserChosenRestaurantId;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author jrigault
 */
@RunWith(MockitoJUnitRunner.class)
public class GetCurrentUserChosenRestaurantIdTest {

    @Mock
    private UserRepository mockUserRepository;
    @Mock
    private RestaurantRepository mockRestaurantRepository;

    //LiveDatas from repositories to be mocked
    private final MutableLiveData<User> currentUserMutableLiveData = new MutableLiveData<>();
    private final MutableLiveData<Map<ChosenRestaurant, List<String>>> chosenRestaurantByUserIdsMapMutableLiveData = new MutableLiveData<>();

    private GetCurrentUserChosenRestaurantId underTestGetCurrentUserChosenRestaurantId;

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Test
    public void getCurrentUserChosenRestaurantId_WhenNoUserLogged_WithSuccess() {
        valorizeAndmockLivedatasAndValorizeUnderTestUsecase(null, null);

        String currentUserChosenRestaurantId =
                underTestGetCurrentUserChosenRestaurantId.getCurrentUserChosenRestaurantId().getValue();

        assert currentUserChosenRestaurantId != null;
        Assert.assertTrue(currentUserChosenRestaurantId.isEmpty());
    }

    @Test
    public void getCurrentUserChosenRestaurantId_WhenUserLoggedButNotHavingChosenARestaurant_WithSuccess() {
        valorizeAndmockLivedatasAndValorizeUnderTestUsecase(TEST_USER1, new HashMap<>());

        String currentUserChosenRestaurantId =
                underTestGetCurrentUserChosenRestaurantId.getCurrentUserChosenRestaurantId().getValue();

        assert currentUserChosenRestaurantId != null;
        Assert.assertTrue(currentUserChosenRestaurantId.isEmpty());
    }

    @Test
    public void getCurrentUserChosenRestaurantId_WhenUserLoggedAndHavingChosenARestaurant_WithSuccess() {
        valorizeAndmockLivedatasAndValorizeUnderTestUsecase(TEST_USER1, TEST_CHOSENRESTAURANT_BYUSERIDS_MAP);

        String currentUserChosenRestaurantId =
                underTestGetCurrentUserChosenRestaurantId.getCurrentUserChosenRestaurantId().getValue();

        assert currentUserChosenRestaurantId != null;
        Assert.assertEquals(TEST_CHOSEN_RESTAURANT.getPlaceId(), currentUserChosenRestaurantId);
    }

    private void valorizeAndmockLivedatasAndValorizeUnderTestUsecase(
            User currentUser,
            Map<ChosenRestaurant, List<String>> chosenRestaurantByUserIdsMap
    ) {
        currentUserMutableLiveData.setValue(currentUser);
        doReturn(currentUserMutableLiveData).when(mockUserRepository).getCurrentUser();

        chosenRestaurantByUserIdsMapMutableLiveData.setValue(chosenRestaurantByUserIdsMap);
        doReturn(chosenRestaurantByUserIdsMapMutableLiveData).when(mockRestaurantRepository).getChosenRestaurantByUserIdsMap();

        underTestGetCurrentUserChosenRestaurantId =
                new GetCurrentUserChosenRestaurantId(mockUserRepository, mockRestaurantRepository);
    }
}