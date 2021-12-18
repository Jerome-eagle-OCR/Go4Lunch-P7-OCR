package com.jr_eagle_ocr.go4lunch;

import static org.mockito.Mockito.mock;

import android.content.res.Resources;
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

public abstract class TestUtils {

    public static final double TEST_LATITUDE = 48.8066;
    public static final double TEST_LONGITUDE = 2.1320;

    //    public static final CollectionReference TEST_USERS_COLLECTION = FirebaseFirestore.getInstance().collection("users_test");
    public static final String TEST_USER1_EMAIL = "oggy@raimbaud.fr";
    public static final String TEST_USER1_PWD = "raimbaud";
    public static final String TEST_USER1_ID = "SsYqEu2GZUbc0PAk6xke0zjd0kk2";
    public static final String TEST_USER2_ID = "###";
    public static final String TEST_USER2_EMAIL = "jack@raimbaud.fr";
//    public static final String TEST_USER2_PWD = "raimbaud";

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

    public static final String TEST_CHOSEN_RESTAURANT_ID = "ChIJI57TRbF95kcR4sWnC5UBTQQ";
    public static final ChosenRestaurant TEST_CHOSEN_RESTAURANT = new ChosenRestaurant();

    static {
        TEST_CHOSEN_RESTAURANT.setPlaceId(TEST_CHOSEN_RESTAURANT_ID);
        TEST_CHOSEN_RESTAURANT.setPlaceName("Big Fernand");
        TEST_CHOSEN_RESTAURANT.setPlaceAddress("20 Rue au Pain, 78000 Versailles, France");
        TEST_CHOSEN_RESTAURANT.setTimestamp(String.valueOf(System.currentTimeMillis()));
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

    static {
        TEST_RESTAURANT.setId(TEST_CHOSEN_RESTAURANT.getPlaceId());
        TEST_RESTAURANT.setPhotoString(TEST_RESTAURANT_STRING_BITMAP);
        TEST_RESTAURANT.setName(TEST_CHOSEN_RESTAURANT.getPlaceName());
        LatLng latLng = new LatLng(48.80582450000001, 2.1322428);
        TEST_RESTAURANT.setGeoPoint(new GeoPoint(latLng.latitude, latLng.longitude));
        TEST_RESTAURANT.setAddress(TEST_CHOSEN_RESTAURANT.getPlaceAddress());
        TEST_RESTAURANT.setPhoneNumber("+33 1 85 15 23 84");
        TEST_RESTAURANT.setWebSiteUrl("https://bigfernand.com/restaurants/restaurant-big-fernand-versailles/");
        HashMap<String, String> closeTimes = new LinkedHashMap<>();
        for (DayOfWeek day : DayOfWeek.values()) {
            closeTimes.put(day.name(), "22:30");
        }
        TEST_RESTAURANT.setCloseTimes(closeTimes);
        TEST_RESTAURANT.setRating((float) (2.4));
        TEST_RESTAURANT.setTimestamp(String.valueOf(System.currentTimeMillis()));
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
        TEST_RESTAURANT_VIEWSTATE_NO_JOINER_CLOSING_AT = new RestaurantViewSate(
                TEST_RESTAURANT.getId(),
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
        TEST_RESTAURANT_VIEWSTATE_1_JOINER_CLOSING_SOON = new RestaurantViewSate(
                TEST_RESTAURANT.getId(),
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
        TEST_RESTAURANT_VIEWSTATE_1_JOINER_CLOSED = new RestaurantViewSate(
                TEST_RESTAURANT.getId(),
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

    public static final UserViewState TEST_USER_VIEWSTATE_NOT_DECIDED_YET;

    static {
        TEST_USER_VIEWSTATE_NOT_DECIDED_YET = new UserViewState(TEST_USER2.getUserName(),
                                                                R.string.not_decided_yet,
                                                                TEST_USER2.getUserUrlPicture(),
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

    public static final AutocompleteRestaurantViewState[] TEST_AUTOCOMPLETE_RESTAURANT_VIEW_STATE = new AutocompleteRestaurantViewState[1];

    static {
        TEST_AUTOCOMPLETE_RESTAURANT_VIEW_STATE[0] = new AutocompleteRestaurantViewState(
                TEST_RESTAURANT.getName() + TEST_RESTAURANT.getAddress().replace(", France", ""),
                TEST_RESTAURANT.getId());
    }

    public static PlaceAutocompleteApiResponse getBigSearchMockResponse() {
        InputStream is = Objects.requireNonNull(TestUtils.class.getClassLoader()).getResourceAsStream("big_search_mock.json");
        String jsonString = new Scanner(is).useDelimiter("\\A").next();
        Gson gson = new GsonBuilder().create();
        return gson.fromJson(jsonString, PlaceAutocompleteApiResponse.class);
    }

}
