package com.jr_eagle_ocr.go4lunch;

import static com.jr_eagle_ocr.go4lunch.data.repositories.RestaurantRepository.BYUSERS_FIELD;
import static com.jr_eagle_ocr.go4lunch.data.repositories.RestaurantRepository.CHOSENBY_COLLECTION_NAME;
import static com.jr_eagle_ocr.go4lunch.data.repositories.RestaurantRepository.LIKEDBY_COLLECTION_NAME;
import static com.jr_eagle_ocr.go4lunch.data.repositories.RestaurantRepository.PLACEID_FIELD;
import static com.jr_eagle_ocr.go4lunch.data.repositories.RestaurantRepository.USERID_FIELD;
import static com.jr_eagle_ocr.go4lunch.data.repositories.RestaurantRepository.USERNAME_FIELD;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Tasks;
import com.google.android.libraries.places.api.model.DayOfWeek;
import com.google.android.libraries.places.api.model.LocalTime;
import com.google.android.libraries.places.api.model.OpeningHours;
import com.google.android.libraries.places.api.model.Period;
import com.google.android.libraries.places.api.model.TimeOfWeek;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.jr_eagle_ocr.go4lunch.data.models.ChosenRestaurant;
import com.jr_eagle_ocr.go4lunch.data.models.FoundRestaurant;
import com.jr_eagle_ocr.go4lunch.data.models.Restaurant;
import com.jr_eagle_ocr.go4lunch.data.models.User;
import com.jr_eagle_ocr.go4lunch.data.repositories.RestaurantRepository;
import com.jr_eagle_ocr.go4lunch.data.repositories.UserRepository;
import com.jr_eagle_ocr.go4lunch.di.Go4LunchApplication;
import com.jr_eagle_ocr.go4lunch.ui.restaurant_detail.RestaurantDetailViewSate;

import org.junit.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public abstract class AndroidTestUtils {

    private static final Context context = Go4LunchApplication.getDependencyContainer().getContext();
    private static final FirebaseAuth auth = FirebaseAuth.getInstance();
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();
    public static final Location TEST_LOCATION = new Location(LocationManager.PASSIVE_PROVIDER);

    static {
        TEST_LOCATION.setLatitude(48.8057);
        TEST_LOCATION.setLongitude(2.1323);
    }

    public static final CollectionReference TEST_USERS_COLLECTION = db.collection("users_test");
    public static final String TEST_USER1_EMAIL = "oggy@raimbaud.fr";
    public static final String TEST_USER1_PWD = "raimbaud";
    public static final String TEST_USER1_ID = "SsYqEu2GZUbc0PAk6xke0zjd0kk2";
    public static final String TEST_USER2_EMAIL = "jack@raimbaud.fr";
    public static final String TEST_USER2_PWD = "raimbaud";

    public static final User TEST_USER1 = new User();
    public static final User TEST_USER2 = new User();
    public static final User TEST_USER3 = new User();
    public static final User TEST_USER4 = new User();

    static {
        TEST_USER1.setUid(TEST_USER1_ID);
        TEST_USER1.setUserName("Oggy");
        TEST_USER1.setUserEmail(TEST_USER1_EMAIL);
        TEST_USER1.setUserUrlPicture("https://ia801503.us.archive.org/3/items/default_avatar_202110/no_photo.png");
        TEST_USER1.setNoonReminderEnabled(true);
        TEST_USER1.setLogged(true);

        TEST_USER2.setUid("###");
        TEST_USER2.setUserName("Jack");
        TEST_USER2.setUserEmail(TEST_USER2_EMAIL);
        TEST_USER2.setUserUrlPicture("https://ia801503.us.archive.org/3/items/default_avatar_202110/no_photo.png");
        TEST_USER2.setNoonReminderEnabled(true);
        TEST_USER2.setLogged(true);

        TEST_USER3.setUid("####");
        TEST_USER3.setUserName("Bob");
        TEST_USER3.setUserEmail("bob@raimbaud.fr");
        TEST_USER3.setUserUrlPicture("https://ia801503.us.archive.org/3/items/default_avatar_202110/no_photo.png");
        TEST_USER3.setNoonReminderEnabled(true);
        TEST_USER3.setLogged(true);

        TEST_USER4.setUid("#####");
        TEST_USER4.setUserName("Olivia");
        TEST_USER4.setUserEmail("olivia@raimbaud.fr");
        TEST_USER4.setUserUrlPicture("https://ia801503.us.archive.org/3/items/default_avatar_202110/no_photo.png");
        TEST_USER4.setNoonReminderEnabled(true);
        TEST_USER4.setLogged(true);
    }

    public static final CollectionReference TEST_RESTAURANTS_COLLECTION = db.collection(RestaurantRepository.RESTAURANTS_COLLECTION_NAME);
    public static final CollectionReference TEST_CHOSEN_RESTAURANTS_COLLECTION = db.collection(RestaurantRepository.CHOSEN_COLLECTION_NAME);
    public static final CollectionReference TEST_LIKED_RESTAURANTS_COLLECTION = db.collection(RestaurantRepository.LIKED_COLLECTION_NAME);

    public static final FoundRestaurant TEST_FOUND_RESTAURANT = new FoundRestaurant("ChIJI57TRbF95kcR4sWnC5UBTQQ");
    public static final FoundRestaurant TEST_FOUND_RESTAURANT2 = new FoundRestaurant("***");

    private static final Bitmap TEST_FOUND_RESTAURANT_BITMAP = BitmapFactory.decodeResource(context.getResources(), R.drawable.blank);
    public static final String TEST_FOUND_RESTAURANT_STRING_BITMAP = "/9j/4AAQSkZJRgABAQAAAQABAAD/2wBDAAoHBwgHBgoICAgLCgoLDhgQDg0NDh0VFhEYIx8lJCIf\n" +
            "IiEmKzcvJik0KSEiMEExNDk7Pj4+JS5ESUM8SDc9Pjv/2wBDAQoLCw4NDhwQEBw7KCIoOzs7Ozs7\n" +
            "Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozv/wAARCABsAE8DASIA\n" +
            "AhEBAxEB/8QAFQABAQAAAAAAAAAAAAAAAAAAAAb/xAAUEAEAAAAAAAAAAAAAAAAAAAAA/8QAFAEB\n" +
            "AAAAAAAAAAAAAAAAAAAAAP/EABQRAQAAAAAAAAAAAAAAAAAAAAD/2gAMAwEAAhEDEQA/ALYAAAAA\n" +
            "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAH//Z\n";

    static {
        TEST_FOUND_RESTAURANT.setName("Big Fernand");
        TEST_FOUND_RESTAURANT.setAddress("20 Rue au Pain, 78000 Versailles, France");
        TEST_FOUND_RESTAURANT.setLatLng(new LatLng(48.80582450000001, 2.1322428));
        TEST_FOUND_RESTAURANT.setRating(4d);
        List<Period> periods = new ArrayList<>();
        LocalTime opening = LocalTime.newInstance(11, 30);
        LocalTime closing = LocalTime.newInstance(22, 30);
        for (DayOfWeek day : DayOfWeek.values()) {
            Period period = Period.builder()
                    .setOpen(TimeOfWeek.newInstance(day, opening))
                    .setClose(TimeOfWeek.newInstance(day, closing))
                    .build();
            periods.add(period);
        }
        OpeningHours openingHours = OpeningHours.builder()
                .setPeriods(periods)
                .build();
        TEST_FOUND_RESTAURANT.setOpeningHours(openingHours);
        TEST_FOUND_RESTAURANT.setPhoneNumber("+33 1 85 15 23 84");
        TEST_FOUND_RESTAURANT.setPhoto(TEST_FOUND_RESTAURANT_BITMAP);
        TEST_FOUND_RESTAURANT.setWebsiteUrl("https://bigfernand.com/restaurants/restaurant-big-fernand-versailles/");

        TEST_FOUND_RESTAURANT2.setName(TEST_FOUND_RESTAURANT.getName());
        TEST_FOUND_RESTAURANT2.setAddress(TEST_FOUND_RESTAURANT.getAddress());
        TEST_FOUND_RESTAURANT2.setLatLng(TEST_FOUND_RESTAURANT.getLatLng());
        TEST_FOUND_RESTAURANT2.setRating(TEST_FOUND_RESTAURANT.getRating());
        TEST_FOUND_RESTAURANT2.setOpeningHours(TEST_FOUND_RESTAURANT.getOpeningHours());
        TEST_FOUND_RESTAURANT2.setPhoneNumber(TEST_FOUND_RESTAURANT.getPhoneNumber());
        TEST_FOUND_RESTAURANT2.setPhoto(TEST_FOUND_RESTAURANT.getPhoto());
        TEST_FOUND_RESTAURANT2.setWebsiteUrl(TEST_FOUND_RESTAURANT.getWebsiteUrl());
    }

    public static final Restaurant TEST_RESTAURANT = new Restaurant();
    public static final Restaurant TEST_RESTAURANT2 = new Restaurant();

    static {
        TEST_RESTAURANT.setId(TEST_FOUND_RESTAURANT.getId());
        TEST_RESTAURANT.setPhotoString(TEST_FOUND_RESTAURANT_STRING_BITMAP);
        TEST_RESTAURANT.setName(TEST_FOUND_RESTAURANT.getName());
        LatLng latLng = TEST_FOUND_RESTAURANT.getLatLng();
        TEST_RESTAURANT.setGeoPoint(new GeoPoint(latLng.latitude, latLng.longitude));
        TEST_RESTAURANT.setAddress(TEST_FOUND_RESTAURANT.getAddress());
        TEST_RESTAURANT.setPhoneNumber(TEST_FOUND_RESTAURANT.getPhoneNumber());
        TEST_RESTAURANT.setWebsiteUrl(TEST_FOUND_RESTAURANT.getWebsiteUrl());
        HashMap<String, String> closeTimes = new LinkedHashMap<>();
        for (DayOfWeek day : DayOfWeek.values()) {
            closeTimes.put(day.name(), "22:30");
        }

        TEST_RESTAURANT.setCloseTimes(closeTimes);
        TEST_RESTAURANT.setRating((float) (TEST_FOUND_RESTAURANT.getRating() * 3 / 5)); // Not null as set to 4
        TEST_RESTAURANT.setTimestamp(String.valueOf(System.currentTimeMillis()));

        TEST_RESTAURANT2.setId(TEST_FOUND_RESTAURANT2.getId());
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

    public static final ChosenRestaurant TEST_CHOSENLIKED_RESTAURANT1 = new ChosenRestaurant();

    static {
        TEST_CHOSENLIKED_RESTAURANT1.setPlaceId(TEST_RESTAURANT.getId());
        TEST_CHOSENLIKED_RESTAURANT1.setPlaceName(TEST_RESTAURANT.getName());
        TEST_CHOSENLIKED_RESTAURANT1.setPlaceAddress(TEST_RESTAURANT.getAddress());
        TEST_CHOSENLIKED_RESTAURANT1.setTimestamp(TEST_RESTAURANT.getTimestamp());
    }

    public static final ChosenRestaurant TEST_CHOSEN_RESTAURANT2 = new ChosenRestaurant();

    static {
        TEST_CHOSEN_RESTAURANT2.setPlaceId(TEST_FOUND_RESTAURANT2.getId());
        TEST_CHOSEN_RESTAURANT2.setPlaceName("name");
        TEST_CHOSEN_RESTAURANT2.setPlaceAddress("address");
        TEST_CHOSEN_RESTAURANT2.setTimestamp(TEST_RESTAURANT.getTimestamp());
    }

    public static final Map<String, String> TEST_USER_DATA = new HashMap<>();

    static {
        TEST_USER_DATA.put(USERID_FIELD, TEST_USER1_ID);
        TEST_USER_DATA.put(USERNAME_FIELD, TEST_USER1.getUserName());
    }

    public static final Map<String, String> TEST_RESTAURANT_DATA = new HashMap<>();

    static {
        TEST_RESTAURANT_DATA.put(PLACEID_FIELD, TEST_RESTAURANT.getId());
    }

    public static final RestaurantDetailViewSate TEST_RESTAURANT_DETAIL_VIEW_SATE;

    static {
        TEST_RESTAURANT_DETAIL_VIEW_SATE = new RestaurantDetailViewSate(TEST_RESTAURANT.getId(),
                null,
                TEST_RESTAURANT.getName(),
                TEST_RESTAURANT.getAddress(),
                TEST_RESTAURANT.getPhoneNumber(),
                TEST_RESTAURANT.getWebsiteUrl(),
                0,
                0,
                null);
    }

    public static void signInUser(String userEmail, String userPwd) throws ExecutionException, InterruptedException {
        FirebaseUser currentFirebaseUser = Tasks.await(auth.signInWithEmailAndPassword(userEmail, userPwd)).getUser();
        Assert.assertNotNull(currentFirebaseUser);
    }

    @Nullable
    public static User createUser(@NonNull UserRepository userRepository) {
        userRepository.createUser();
        long startTime = System.currentTimeMillis();
        while (userRepository.getCurrentUser().getValue() == null) {
            if (System.currentTimeMillis() - startTime > 2000)
                break; // Avoid infinite loop in case something went wrong
        }
        return userRepository.getCurrentUser().getValue();
    }

    public static void signOut() {
        if (auth.getCurrentUser() != null) {
            auth.signOut();
        }
    }

    public static void cleanTestUsersCollection() throws ExecutionException, InterruptedException {
        List<DocumentSnapshot> userDocuments = Tasks.await(TEST_USERS_COLLECTION.get()).getDocuments();
        for (DocumentSnapshot document : userDocuments) {
            Tasks.await(document.getReference().delete());
        }
        db.waitForPendingWrites();
    }

    public static void setChosenRestaurantDocument(@NonNull ChosenRestaurant chosenRestaurant) throws ExecutionException, InterruptedException {
        Tasks.await(TEST_CHOSEN_RESTAURANTS_COLLECTION.document(chosenRestaurant.getPlaceId())
                .set(chosenRestaurant));
        db.waitForPendingWrites();
    }

    public static void setChosenRestaurantForUser(@NonNull ChosenRestaurant chosenRestaurant, @NonNull User user) throws ExecutionException, InterruptedException {
        String ID = chosenRestaurant.getPlaceId();
        String uid = user.getUid();
        Map<String, String> user_data = new HashMap<>();
        user_data.put(USERID_FIELD, uid);
        user_data.put(USERNAME_FIELD, user.getUserName());
        Tasks.await(TEST_CHOSEN_RESTAURANTS_COLLECTION.document(ID)
                .collection(RestaurantRepository.CHOSENBY_COLLECTION_NAME)
                .document(uid)
                .set(user_data));
        Tasks.await(TEST_CHOSEN_RESTAURANTS_COLLECTION.document(ID)
                .update(BYUSERS_FIELD, FieldValue.arrayUnion(uid)));
        db.waitForPendingWrites();
    }

    public static void cleanTestRestaurantsCollection() throws ExecutionException, InterruptedException {
        if (auth.getCurrentUser() != null) {
            Tasks.await(TEST_RESTAURANTS_COLLECTION.document(TEST_FOUND_RESTAURANT.getId()).delete());
            Tasks.await(TEST_RESTAURANTS_COLLECTION.document(TEST_FOUND_RESTAURANT2.getId()).delete());
            db.waitForPendingWrites();
        }
    }

    public static void cleanTestChosenRestaurantsCollection() throws ExecutionException, InterruptedException {
        if (auth.getCurrentUser() != null) {
            List<String> placeIds = Arrays.asList(TEST_CHOSENLIKED_RESTAURANT1.getPlaceId(), TEST_CHOSEN_RESTAURANT2.getPlaceId());
            for (String ID : placeIds) {
                List<DocumentSnapshot> userDocuments = Tasks.await(TEST_CHOSEN_RESTAURANTS_COLLECTION.document(ID)
                        .collection(CHOSENBY_COLLECTION_NAME)
                        .get()).getDocuments();
                for (DocumentSnapshot userDocument : userDocuments) {
                    Tasks.await(userDocument.getReference().delete());
                }
                Tasks.await(TEST_CHOSEN_RESTAURANTS_COLLECTION.document(ID).delete());
            }
            db.waitForPendingWrites();
        }
    }

    public static void cleanTestLikedRestaurantsCollection() throws ExecutionException, InterruptedException {
        if (auth.getCurrentUser() != null) {
            Tasks.await(TEST_LIKED_RESTAURANTS_COLLECTION.document(TEST_CHOSENLIKED_RESTAURANT1.getPlaceId())
                    .collection(LIKEDBY_COLLECTION_NAME).document(TEST_USER1_ID)
                    .delete());
            Tasks.await(TEST_LIKED_RESTAURANTS_COLLECTION.document(TEST_CHOSENLIKED_RESTAURANT1.getPlaceId())
                    .delete());
            db.waitForPendingWrites();
        }
    }
}
