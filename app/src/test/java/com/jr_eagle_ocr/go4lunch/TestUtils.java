package com.jr_eagle_ocr.go4lunch;

import static org.mockito.Mockito.mock;

import android.graphics.Bitmap;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.DayOfWeek;
import com.google.firebase.firestore.GeoPoint;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jr_eagle_ocr.go4lunch.data.models.ChosenRestaurant;
import com.jr_eagle_ocr.go4lunch.data.models.Restaurant;
import com.jr_eagle_ocr.go4lunch.data.models.User;
import com.jr_eagle_ocr.go4lunch.data.place_autocomplete.models.PlaceAutocompleteApiResponse;
import com.jr_eagle_ocr.go4lunch.ui.AutocompleteRestaurantViewState;
import com.jr_eagle_ocr.go4lunch.ui.adapters.RestaurantViewSate;
import com.jr_eagle_ocr.go4lunch.ui.adapters.UserViewState;
import com.jr_eagle_ocr.go4lunch.ui.restaurant_detail.RestaurantDetailViewSate;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

/**
 * @author jrigault
 */
public abstract class TestUtils {

    public static final double TEST_LATITUDE = 48.8066;
    public static final double TEST_LONGITUDE = 2.1320;

    public static final String TEST_USER1_EMAIL = "oggy@raimbaud.fr";
    public static final String TEST_USER1_ID = "SsYqEu2GZUbc0PAk6xke0zjd0kk2";
    public static final String TEST_USER2_ID = "###";
    public static final String TEST_USER2_EMAIL = "jack@raimbaud.fr";
    public static final String TEST_USER3_ID = "####";
    public static final String TEST_USER3_EMAIL = "bob@raimbaud.fr";

    public static final User TEST_USER1;

    static {
        TEST_USER1 = new User(TEST_USER1_ID,
                              "Oggy",
                              TEST_USER1_EMAIL,
                              "https://ia801503.us.archive.org/3/items/default_avatar_202110/no_photo.png",
                              true,
                              true);
    }

    public static final User TEST_USER2;

    static {
        TEST_USER2 = new User(TEST_USER2_ID,
                              "Jack",
                              TEST_USER2_EMAIL,
                              "https://ia801503.us.archive.org/3/items/default_avatar_202110/no_photo.png",
                              false,
                              true);
    }

    public static final User TEST_USER3;

    static {
        TEST_USER3 = new User(TEST_USER3_ID,
                              "Bob",
                              TEST_USER3_EMAIL,
                              "https://ia801503.us.archive.org/3/items/default_avatar_202110/no_photo.png",
                              true,
                              true);
    }

    public static final String TEST_PLACE_ID = "ChIJI57TRbF95kcR4sWnC5UBTQQ";
    public static final ChosenRestaurant TEST_CHOSEN_RESTAURANT = new ChosenRestaurant();
    public static final ChosenRestaurant TEST_CHOSEN_RESTAURANT2 = new ChosenRestaurant();

    static {
        TEST_CHOSEN_RESTAURANT.setPlaceId(TEST_PLACE_ID);
        TEST_CHOSEN_RESTAURANT.setPlaceName("Big Fernand");
        TEST_CHOSEN_RESTAURANT.setPlaceAddress("20 Rue au Pain, 78000 Versailles, France");
        TEST_CHOSEN_RESTAURANT.setTimestamp(String.valueOf(System.currentTimeMillis()));

        TEST_CHOSEN_RESTAURANT2.setPlaceId("***");
        TEST_CHOSEN_RESTAURANT2.setPlaceName("Big Brother");
        TEST_CHOSEN_RESTAURANT2.setPlaceAddress("1 Rue IsWatchingYou, 78000 Versailles, France");
        TEST_CHOSEN_RESTAURANT2.setTimestamp(TEST_CHOSEN_RESTAURANT.getTimestamp());

    }

    public static final Map<ChosenRestaurant, List<String>> TEST_CHOSENRESTAURANT_BYUSERIDS_MAP = new HashMap<>();

    static {
        TEST_CHOSENRESTAURANT_BYUSERIDS_MAP.put(TEST_CHOSEN_RESTAURANT, Arrays.asList(TEST_USER2_ID, TEST_USER1_ID));
    }

    public static final List<String> TEST_FOUND_RESTAURANT_IDS = Collections.singletonList(TEST_CHOSEN_RESTAURANT.getPlaceId());

    public static String TEST_RESTAURANT_STRING_BITMAP;

    static {
        TEST_RESTAURANT_STRING_BITMAP = "/9j/4AAQSkZJRgABAQAAAQABAAD/2wBDAAoHBwgHBgoICAgLCgoLDhgQDg0" +
                "NDh0VFhEYIx8lJCIfIiEmKzcvJik0KSEiMEExNDk7Pj4+JS5ESUM8SDc9Pjv/2wBDAQoLCw4NDhwQEBw7KC" +
                "IoOzs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozv/wAARCABsAE8DA" +
                "SIAAhEBAxEB/8QAFQABAQAAAAAAAAAAAAAAAAAAAAb/xAAUEAEAAAAAAAAAAAAAAAAAAAAA/8QAFAEBAAAA" +
                "AAAAAAAAAAAAAAAAAP/EABQRAQAAAAAAAAAAAAAAAAAAAAD/2gAMAwEAAhEDEQA/ALYAAAAAAAAAAAAAAAA" +
                "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAH//Z";
    }

    public static final Bitmap TEST_RESTAURANT_BITMAP;

    static {
        TEST_RESTAURANT_BITMAP = mock(Bitmap.class);
    }

    public static final Restaurant TEST_RESTAURANT = new Restaurant();
    public static final Restaurant TEST_RESTAURANT2 = new Restaurant();

    static {
        TEST_RESTAURANT.setId(TEST_PLACE_ID);
        TEST_RESTAURANT.setPhotoString(TEST_RESTAURANT_STRING_BITMAP);
        TEST_RESTAURANT.setName(TEST_CHOSEN_RESTAURANT.getPlaceName());
        LatLng latLng = new LatLng(48.80582450000001, 2.1322428);
        TEST_RESTAURANT.setGeoPoint(new GeoPoint(latLng.latitude, latLng.longitude));
        TEST_RESTAURANT.setAddress(TEST_CHOSEN_RESTAURANT.getPlaceAddress());
        TEST_RESTAURANT.setPhoneNumber("+33 1 85 15 23 84");
        TEST_RESTAURANT.setWebsiteUrl("https://bigfernand.com/restaurants/restaurant-big-fernand-versailles/");
        HashMap<String, String> closeTimes = new LinkedHashMap<>();
        for (DayOfWeek day : DayOfWeek.values()) {
            closeTimes.put(day.name(), "22:30");
        }
        TEST_RESTAURANT.setCloseTimes(closeTimes);
        TEST_RESTAURANT.setRating((float) (2.4));
        TEST_RESTAURANT.setTimestamp(String.valueOf(System.currentTimeMillis()));

        TEST_RESTAURANT2.setId("***");
        TEST_RESTAURANT2.setPhotoString(TEST_RESTAURANT.getPhotoString());
        TEST_RESTAURANT2.setName(TEST_RESTAURANT.getName());
        TEST_RESTAURANT2.setGeoPoint(TEST_RESTAURANT.getGeoPoint());
        TEST_RESTAURANT2.setAddress(TEST_RESTAURANT.getAddress());
        TEST_RESTAURANT2.setPhoneNumber(TEST_RESTAURANT.getPhoneNumber());
        TEST_RESTAURANT2.setWebsiteUrl(TEST_RESTAURANT.getWebsiteUrl());
        TEST_RESTAURANT2.setCloseTimes(TEST_RESTAURANT.getCloseTimes());
        TEST_RESTAURANT2.setCloseTimes(TEST_RESTAURANT.getCloseTimes());
        TEST_RESTAURANT2.setRating(TEST_RESTAURANT.getRating());
        TEST_RESTAURANT2.setTimestamp(TEST_RESTAURANT.getTimestamp());

    }

    public static final Calendar CLOSING_AT_CALENDAR = Calendar.getInstance();

    static {
        CLOSING_AT_CALENDAR.set(Calendar.HOUR_OF_DAY, 10);
        CLOSING_AT_CALENDAR.set(Calendar.MINUTE, 0);
        CLOSING_AT_CALENDAR.set(Calendar.SECOND, 0);
    }

    public static final Calendar CLOSING_SOON_CALENDAR = Calendar.getInstance();

    static {
        CLOSING_SOON_CALENDAR.set(Calendar.HOUR_OF_DAY, 21);
        CLOSING_SOON_CALENDAR.set(Calendar.MINUTE, 30);
        CLOSING_SOON_CALENDAR.set(Calendar.SECOND, 0);
    }

    public static final Calendar CLOSED_CALENDAR = Calendar.getInstance();

    static {
        CLOSED_CALENDAR.set(Calendar.HOUR_OF_DAY, 23);
        CLOSED_CALENDAR.set(Calendar.MINUTE, 0);
        CLOSED_CALENDAR.set(Calendar.SECOND, 0);
    }

    public static RestaurantViewSate TEST_RESTAURANT_VIEWSTATE_NO_JOINER_CLOSING_AT;

    static {
        TEST_RESTAURANT_VIEWSTATE_NO_JOINER_CLOSING_AT = new RestaurantViewSate(TEST_RESTAURANT.getId(),
                                                                                TEST_RESTAURANT_BITMAP,
                                                                                TEST_RESTAURANT.getName(),
                                                                                "88m",
                                                                                TEST_RESTAURANT.getAddress().replace(", France", ""),
                                                                                "",
                                                                                false,
                                                                                R.string.open_until,
                                                                                "22:30",
                                                                                false,
                                                                                TEST_RESTAURANT.getRating());
    }

    public static RestaurantViewSate TEST_RESTAURANT_VIEWSTATE_1_JOINER_CLOSING_SOON;

    static {
        TEST_RESTAURANT_VIEWSTATE_1_JOINER_CLOSING_SOON = new RestaurantViewSate(TEST_RESTAURANT.getId(),
                                                                                 TEST_RESTAURANT_BITMAP,
                                                                                 TEST_RESTAURANT.getName(),
                                                                                 "88m",
                                                                                 TEST_RESTAURANT.getAddress().replace(", France", ""),
                                                                                 "(1)",
                                                                                 true,
                                                                                 R.string.closing_soon,
                                                                                 "",
                                                                                 true,
                                                                                 TEST_RESTAURANT.getRating());
    }

    public static RestaurantViewSate TEST_RESTAURANT_VIEWSTATE_1_JOINER_CLOSED;

    static {
        TEST_RESTAURANT_VIEWSTATE_1_JOINER_CLOSED = new RestaurantViewSate(TEST_RESTAURANT.getId(),
                                                                           TEST_RESTAURANT_BITMAP,
                                                                           TEST_RESTAURANT.getName(),
                                                                           "88m",
                                                                           TEST_RESTAURANT.getAddress().replace(", France", ""),
                                                                           "(1)",
                                                                           true,
                                                                           R.string.closed,
                                                                           "",
                                                                           true,
                                                                           TEST_RESTAURANT.getRating());
    }

    public static RestaurantViewSate TEST_RESTAURANT1_VIEWSTATE;

    static {
        TEST_RESTAURANT1_VIEWSTATE = new RestaurantViewSate(TEST_RESTAURANT.getId(),
                                                            TEST_RESTAURANT_BITMAP,
                                                            TEST_RESTAURANT.getName(),
                                                            "88m",
                                                            TEST_RESTAURANT.getAddress().replace(", France", ""),
                                                            "(1)",
                                                            true,
                                                            R.string.closed,
                                                            "",
                                                            true,
                                                            TEST_RESTAURANT.getRating());
    }

    public static RestaurantViewSate TEST_RESTAURANT2_VIEWSTATE;

    static {
        TEST_RESTAURANT2_VIEWSTATE = new RestaurantViewSate(TEST_CHOSEN_RESTAURANT2.getPlaceId(),
                                                            null,
                                                            TEST_CHOSEN_RESTAURANT2.getPlaceName(),
                                                            "100m",
                                                            TEST_CHOSEN_RESTAURANT2.getPlaceAddress().replace(", France", ""),
                                                            "(2)",
                                                            true,
                                                            R.string.closed,
                                                            "",
                                                            true,
                                                            TEST_RESTAURANT.getRating());
    }

    public static RestaurantViewSate TEST_RESTAURANT3_VIEWSTATE;

    static {
        TEST_RESTAURANT3_VIEWSTATE = new RestaurantViewSate("****",
                                                            null,
                                                            "NoName",
                                                            "88m",
                                                            "NoAddress",
                                                            "",
                                                            false,
                                                            R.string.closed,
                                                            "",
                                                            true,
                                                            TEST_RESTAURANT.getRating());
    }

    public static final UserViewState TEST_USER_VIEWSTATE_NOT_DECIDED_YET;

    static {
        TEST_USER_VIEWSTATE_NOT_DECIDED_YET = new UserViewState(TEST_USER3.getUserName(),
                                                                R.string.not_decided_yet,
                                                                TEST_USER3.getUserUrlPicture(),
                                                                null,
                                                                null,
                                                                0.3f,
                                                                0.6f);
    }

    public static final UserViewState TEST_USER_VIEWSTATE_IS_EATING_AT;

    static {
        TEST_USER_VIEWSTATE_IS_EATING_AT = new UserViewState(TEST_USER2.getUserName(),
                                                                R.string.is_eating_at,
                                                                TEST_USER2.getUserUrlPicture(),
                                                                TEST_CHOSEN_RESTAURANT.getPlaceId(),
                                                                TEST_CHOSEN_RESTAURANT.getPlaceName(),
                                                                1,
                                                                1);
    }

    public static final UserViewState TEST_USER_VIEWSTATE_IS_JOINING;

    static {
        TEST_USER_VIEWSTATE_IS_JOINING = new UserViewState(TEST_USER2.getUserName(),
                                                           R.string.is_joining,
                                                           TEST_USER2.getUserUrlPicture(),
                                                           null,
                                                           null,
                                                           1,
                                                           1);
    }

    public static final Calendar BEFORE_NOON_CALENDAR = Calendar.getInstance();

    static {
        BEFORE_NOON_CALENDAR.set(Calendar.HOUR_OF_DAY, 11);
        BEFORE_NOON_CALENDAR.set(Calendar.MINUTE, 50);
        BEFORE_NOON_CALENDAR.set(Calendar.SECOND, 30);
    }

    public static final long TEST_INITIAL_DELAY = 540000;

    public static final AutocompleteRestaurantViewState[] TEST_AUTOCOMPLETE_RESTAURANT_VIEWSTATE_ARRAY = new AutocompleteRestaurantViewState[1];
    public static final AutocompleteRestaurantViewState[] TEST_AUTOCOMPLETE_RESTAURANT_VIEWSTATE_ARRAY2 = new AutocompleteRestaurantViewState[2];

    static {
        TEST_AUTOCOMPLETE_RESTAURANT_VIEWSTATE_ARRAY[0] = new AutocompleteRestaurantViewState(
                TEST_RESTAURANT.getName() + TEST_RESTAURANT.getAddress().replace(", France", ""),
                TEST_RESTAURANT.getId());

        TEST_AUTOCOMPLETE_RESTAURANT_VIEWSTATE_ARRAY2[0] = new AutocompleteRestaurantViewState(
                TEST_RESTAURANT1_VIEWSTATE.getName() + TEST_RESTAURANT1_VIEWSTATE.getAddress().replace(", France", ""),
                TEST_RESTAURANT1_VIEWSTATE.getId());
        TEST_AUTOCOMPLETE_RESTAURANT_VIEWSTATE_ARRAY2[1] = new AutocompleteRestaurantViewState(
                TEST_RESTAURANT3_VIEWSTATE.getName() + TEST_RESTAURANT3_VIEWSTATE.getAddress().replace(", France", ""),
                TEST_RESTAURANT3_VIEWSTATE.getId());
    }

    public static PlaceAutocompleteApiResponse getBigSearchMockResponse() {
        InputStream is = Objects.requireNonNull(TestUtils.class.getClassLoader()).getResourceAsStream("big_search_mock.json");
        String jsonString = new Scanner(is).useDelimiter("\\A").next();
        Gson gson = new GsonBuilder().create();
        return gson.fromJson(jsonString, PlaceAutocompleteApiResponse.class);
    }
}
