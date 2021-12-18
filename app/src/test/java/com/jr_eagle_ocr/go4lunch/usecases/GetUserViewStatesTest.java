package com.jr_eagle_ocr.go4lunch.usecases;

import static com.jr_eagle_ocr.go4lunch.TestUtils.TEST_CHOSEN_RESTAURANT;
import static com.jr_eagle_ocr.go4lunch.TestUtils.TEST_USER1;
import static com.jr_eagle_ocr.go4lunch.TestUtils.TEST_USER1_ID;
import static com.jr_eagle_ocr.go4lunch.TestUtils.TEST_USER2;
import static com.jr_eagle_ocr.go4lunch.TestUtils.TEST_USER2_ID;
import static com.jr_eagle_ocr.go4lunch.TestUtils.TEST_USER3;
import static com.jr_eagle_ocr.go4lunch.TestUtils.TEST_USER3_ID;
import static com.jr_eagle_ocr.go4lunch.TestUtils.TEST_USER_VIEWSTATE_IS_EATING_AT;
import static com.jr_eagle_ocr.go4lunch.TestUtils.TEST_USER_VIEWSTATE_IS_JOINING;
import static com.jr_eagle_ocr.go4lunch.TestUtils.TEST_USER_VIEWSTATE_NOT_DECIDED_YET;

import androidx.core.util.Pair;

import com.jr_eagle_ocr.go4lunch.data.models.User;
import com.jr_eagle_ocr.go4lunch.data.usecases.GetUserViewStates;
import com.jr_eagle_ocr.go4lunch.ui.adapters.UserViewState;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetUserViewStatesTest {

    private GetUserViewStates underTestGetUserViewStates;
    private final Map<String, User> allLoggedUsers = new HashMap<>();
    private final Map<String, Pair<String, String>> userChosenRestaurantMap = new HashMap<>();

    @Test
    public void getUserViewStates_NoLoggedUser_NoRestaurantChosenByAnyUser_WithSuccess() {
        underTestGetUserViewStates = new GetUserViewStates();

        // Workmate list case: no logged user -> 0 workmate
        Assert.assertTrue(underTestGetUserViewStates.getUserViewStates(null,
                                                                       null,
                                                                       allLoggedUsers,
                                                                       userChosenRestaurantMap)
                                                                       .isEmpty());

        // Joining workmate list case: no logged user -> 0 workmate
        Assert.assertTrue(underTestGetUserViewStates.getUserViewStates(TEST_CHOSEN_RESTAURANT.getPlaceId(),
                                                                       null,
                                                                       allLoggedUsers,
                                                                       userChosenRestaurantMap)
                                                                       .isEmpty());
    }

    @Test
    public void getUserViewStates_OnlyCurrentUserLogged_NoRestaurantChosenByAnyUser_WithSuccess() {
        underTestGetUserViewStates = new GetUserViewStates();
        allLoggedUsers.put(TEST_USER1_ID, TEST_USER1);

        // Workmate list case: only current user logged -> 0 workmate
        Assert.assertTrue(underTestGetUserViewStates.getUserViewStates(null,
                                                                       TEST_USER1,
                                                                       allLoggedUsers,
                                                                       userChosenRestaurantMap)
                                                                       .isEmpty());

        // Joining workmate list case: only current user logged -> 0 workmate
        Assert.assertTrue(underTestGetUserViewStates.getUserViewStates(TEST_CHOSEN_RESTAURANT.getPlaceId(),
                                                                       TEST_USER1,
                                                                       allLoggedUsers,
                                                                       userChosenRestaurantMap)
                                                                       .isEmpty());
    }

    @Test
    public void getUserViewStates_OnlyCurrentUserLogged_RestaurantChosenByCurrentUser_WithSuccess() {
        underTestGetUserViewStates = new GetUserViewStates();
        allLoggedUsers.put(TEST_USER1_ID, TEST_USER1);
        userChosenRestaurantMap.put(TEST_USER1_ID,
                new Pair<>(TEST_CHOSEN_RESTAURANT.getPlaceId(), TEST_CHOSEN_RESTAURANT.getPlaceName()));

        // Workmate list case: only current user logged -> 0 workmate
        Assert.assertTrue(underTestGetUserViewStates.getUserViewStates(null,
                                                                       TEST_USER1,
                                                                       allLoggedUsers,
                                                                       userChosenRestaurantMap)
                                                                       .isEmpty());

        // Joining workmate list case: only current user logged -> 0 workmate
        Assert.assertTrue(underTestGetUserViewStates.getUserViewStates(TEST_CHOSEN_RESTAURANT.getPlaceId(),
                                                                       TEST_USER1,
                                                                       allLoggedUsers,
                                                                       userChosenRestaurantMap)
                                                                       .isEmpty());
    }

    @Test
    public void getUserViewStates_CurrentUserAndOneOtherLogged_RestaurantChosenByCurrentUser_WithSuccess() {
        underTestGetUserViewStates = new GetUserViewStates();
        allLoggedUsers.put(TEST_USER1_ID, TEST_USER1);
        allLoggedUsers.put(TEST_USER3_ID, TEST_USER3);
        userChosenRestaurantMap.put(TEST_USER1_ID,
                new Pair<>(TEST_CHOSEN_RESTAURANT.getPlaceId(), TEST_CHOSEN_RESTAURANT.getPlaceName()));

        // Workmate list case: one other user logged -> 1 workmate
        List<UserViewState> actual_userViewstates = underTestGetUserViewStates.getUserViewStates(null,
                                                                                                 TEST_USER1,
                                                                                                 allLoggedUsers,
                                                                                                 userChosenRestaurantMap);

        Assert.assertEquals(1, actual_userViewstates.size());
        Assert.assertEquals(TEST_USER_VIEWSTATE_NOT_DECIDED_YET, actual_userViewstates.get(0));

        // Joining workmate list case: one other user logged -> 0 workmate as displayed restaurant not chosen by other user
        Assert.assertTrue(underTestGetUserViewStates.getUserViewStates(TEST_CHOSEN_RESTAURANT.getPlaceId(),
                                                                       TEST_USER1,
                                                                       allLoggedUsers,
                                                                       userChosenRestaurantMap)
                                                                       .isEmpty());
    }

    @Test
    public void getUserViewStates_CurrentUserAndOneOtherLogged_RestaurantChosenByOtherUser_WithSuccess() {
        underTestGetUserViewStates = new GetUserViewStates();
        allLoggedUsers.put(TEST_USER1_ID, TEST_USER1);
        allLoggedUsers.put(TEST_USER2_ID, TEST_USER2);
        userChosenRestaurantMap.put(TEST_USER2_ID,
                new Pair<>(TEST_CHOSEN_RESTAURANT.getPlaceId(), TEST_CHOSEN_RESTAURANT.getPlaceName()));

        // Workmate list case: one other user logged -> 1 workmate
        List<UserViewState> actual_userViewstates = underTestGetUserViewStates.getUserViewStates(null,
                                                                                                 TEST_USER1,
                                                                                                 allLoggedUsers,
                                                                                                 userChosenRestaurantMap);

        Assert.assertEquals(1, actual_userViewstates.size());
        Assert.assertEquals(TEST_USER_VIEWSTATE_IS_EATING_AT, actual_userViewstates.get(0));

        // Joining workmate list case: one other user logged -> 1 workmate as displayed restaurant chosen by other user
        actual_userViewstates = underTestGetUserViewStates.getUserViewStates(TEST_CHOSEN_RESTAURANT.getPlaceId(),
                                                                             TEST_USER1,
                                                                             allLoggedUsers,
                                                                             userChosenRestaurantMap);

        Assert.assertEquals(1, actual_userViewstates.size());
        Assert.assertEquals(TEST_USER_VIEWSTATE_IS_JOINING, actual_userViewstates.get(0));
    }

    @Test
    public void getUserViewStates_CurrentUserAndOneOtherLogged_RestaurantChosenByBothUsers_WithSuccess() {
        underTestGetUserViewStates = new GetUserViewStates();
        allLoggedUsers.put(TEST_USER1_ID, TEST_USER1);
        allLoggedUsers.put(TEST_USER2_ID, TEST_USER2);
        userChosenRestaurantMap.put(TEST_USER1_ID,
                new Pair<>(TEST_CHOSEN_RESTAURANT.getPlaceId(), TEST_CHOSEN_RESTAURANT.getPlaceName()));
        userChosenRestaurantMap.put(TEST_USER2_ID,
                new Pair<>(TEST_CHOSEN_RESTAURANT.getPlaceId(), TEST_CHOSEN_RESTAURANT.getPlaceName()));

        // Workmate list case: one other user logged -> 1 workmate
        List<UserViewState> actual_userViewstates = underTestGetUserViewStates.getUserViewStates(null,
                                                                                                 TEST_USER1,
                                                                                                 allLoggedUsers,
                                                                                                 userChosenRestaurantMap);

        Assert.assertEquals(1, actual_userViewstates.size());
        Assert.assertEquals(TEST_USER_VIEWSTATE_IS_EATING_AT, actual_userViewstates.get(0));

        // Joining workmate list case: one other user logged -> 1 workmate as displayed restaurant chosen by other user
        actual_userViewstates = underTestGetUserViewStates.getUserViewStates(TEST_CHOSEN_RESTAURANT.getPlaceId(),
                                                                             TEST_USER1,
                                                                             allLoggedUsers,
                                                                             userChosenRestaurantMap);

        Assert.assertEquals(1, actual_userViewstates.size());
        Assert.assertEquals(TEST_USER_VIEWSTATE_IS_JOINING, actual_userViewstates.get(0));
    }
}