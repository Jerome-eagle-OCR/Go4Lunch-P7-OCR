package com.jr_eagle_ocr.go4lunch.data.repositories.usecases;

import android.graphics.Bitmap;
import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.google.android.libraries.places.api.model.DayOfWeek;
import com.jr_eagle_ocr.go4lunch.R;
import com.jr_eagle_ocr.go4lunch.data.models.Restaurant;
import com.jr_eagle_ocr.go4lunch.data.repositories.LocationRepository;
import com.jr_eagle_ocr.go4lunch.data.repositories.RestaurantRepository;
import com.jr_eagle_ocr.go4lunch.ui.adapters.RestaurantViewSate;
import com.jr_eagle_ocr.go4lunch.data.repositories.usecases.parent.UseCase;
import com.jr_eagle_ocr.go4lunch.util.BitmapUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * @author jrigault
 */
public final class GetRestaurantViewStates extends UseCase {
    private final LocationRepository locationRepository;
    private final RestaurantRepository restaurantRepository;
    private final LiveData<Map<String, Restaurant>> allRestaurantsLiveData;

    public GetRestaurantViewStates(
            LocationRepository locationRepository,
            RestaurantRepository restaurantRepository
    ) {
        this.locationRepository = locationRepository;
        this.restaurantRepository = restaurantRepository;
        allRestaurantsLiveData = restaurantRepository.getAllRestaurants();
    }

    /**
     * Generate a list of RestaurantViewState
     *
     * @param restaurantChosenByUsersCountMap a map<restaurant id, by users count> to get
     *                                        the count of users having chosen a specific restaurant
     * @return a list of RestaurantViewState
     */
    public List<RestaurantViewSate> getRestaurantViewStates(Map<String, Integer> restaurantChosenByUsersCountMap) {
        List<RestaurantViewSate> restaurantViewSates = new ArrayList<>();
        List<String> foundRestaurantIds = restaurantRepository.getFoundRestaurantIds();
        Map<String, Restaurant> allRestaurants = allRestaurantsLiveData.getValue();
        if (foundRestaurantIds != null && allRestaurants != null) {
            for (String id : foundRestaurantIds) {
                Restaurant restaurant = allRestaurants.get(id);
                if (restaurant != null) {

                    // Restaurant photo
                    Bitmap photo = BitmapUtil.decodeBase64(restaurant.getPhotoString());
                    // Restaurant name
                    String name = restaurant.getName();
                    // Restaurant distance from current maps location
                    String distance = this.getDistanceText(restaurant);
                    // Restaurant address
                    String address = restaurant.getAddress();
                    if (address.endsWith(", France"))
                        address = address.substring(0, address.length() - 8);
                    // Restaurant number of joining users and text visibility
                    Object[] joinersArray = this.getJoinersArray(restaurantChosenByUsersCountMap, id);
                    String joiners = (String) joinersArray[0];
                    boolean isJoinersVisible = (boolean) joinersArray[1];
                    // Restaurant opening
                    Object[] openingArray = this.getOpeningArray(restaurant);
                    int openingPrefix = (int) openingArray[0];
                    String closingTime = (String) openingArray[1];
                    boolean isWarningStyle = (boolean) openingArray[2];
                    // Restaurant rating range reduced to 3 stars
                    float rating;
                    rating = restaurant.getRating();

                    // Restaurant view state creation
                    RestaurantViewSate restaurantViewSate = new RestaurantViewSate(
                            id, photo, name, distance, address,
                            joiners, isJoinersVisible, openingPrefix, closingTime, isWarningStyle, rating);

                    // Add view state in the list
                    restaurantViewSates.add(restaurantViewSate);
                    Log.d(TAG, "getRestaurantViewStates: added " + restaurantViewSate.toString());
                }
            }
        }

        return restaurantViewSates;
    }

    /**
     * Build a distance view state based on current user location and a given restaurant location
     *
     * @param restaurant a given Restaurant
     * @return a formatted text for the view
     */
    @NonNull
    private String getDistanceText(Restaurant restaurant) {
        String distance;
        double startLat = restaurant.getGeoPoint().getLatitude();
        double startLng = restaurant.getGeoPoint().getLongitude();
        double endLat = locationRepository.getLocation().getLatitude();
        double endLng = locationRepository.getLocation().getLongitude();
        float[] results = new float[1];
        Location.distanceBetween(startLat, startLng, endLat, endLng, results);
        double result = Math.rint(results[0]);
        distance = String.valueOf(result).split("\\.")[0] + "m";
        return distance;
    }

    /**
     * Build an open/close view state based on a given restaurant OpeningHours and current time
     *
     * @param restaurant a given Restaurant
     * @return an object array containing infos for the view
     */
    @NonNull
    private Object[] getOpeningArray(Restaurant restaurant) {
        Object[] openingArray = new Object[3];
        int openingPrefix;
        String closingTime = "";
        boolean isWarningStyle = true;

        Calendar calendar = Calendar.getInstance();
        long nowTimeMillis = calendar.getTimeInMillis();
        int todayDayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
        int todayDayInt = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        String todayDay = DayOfWeek.values()[todayDayInt].name();
        String closeTime = restaurant.getCloseTimes().get(todayDay);

        if (closeTime != null) {
            String[] closeTimeArray = closeTime.split("[:]");
            int closeTimeHour = Integer.parseInt(closeTimeArray[0]);
            int closeTimeMinute = Integer.parseInt(closeTimeArray[1]);
            int closeTimeDayOfYear;
            if (closeTimeHour < 12) {
                closeTimeDayOfYear = todayDayOfYear + 1;
            } else {
                closeTimeDayOfYear = todayDayOfYear;
            }
            calendar.set(Calendar.DAY_OF_YEAR, closeTimeDayOfYear);
            calendar.set(Calendar.HOUR_OF_DAY, closeTimeHour);
            calendar.set(Calendar.MINUTE, closeTimeMinute);
            long closeTimeMillis = calendar.getTimeInMillis();
            long compareMinutes = (closeTimeMillis - nowTimeMillis) / 60000;
            if (compareMinutes <= 0) {
                openingPrefix = R.string.closed;
            } else if (compareMinutes < 60) {
                openingPrefix = R.string.closing_soon;
            } else {
                openingPrefix = R.string.open_until;
                closingTime = closeTime;
                isWarningStyle = false;
            }
        } else {
            openingPrefix = R.string.always_open;
            isWarningStyle = false;
        }
        openingArray[0] = openingPrefix;
        openingArray[1] = closingTime;
        openingArray[2] = isWarningStyle;

        return openingArray;
    }

    /**
     * Build a joining user count view state based on the number of users having chosen the given restaurant
     *
     * @param restaurantByUsersCountMap a map<restaurant id, by users count> to get
     *                                  the count of users having chosen a specific restaurant
     * @param placeId                   the id of a specific restaurant
     * @return an object array containing infos for the view
     */
    @NonNull
    private Object[] getJoinersArray(Map<String, Integer> restaurantByUsersCountMap, String placeId) {
        Object[] joinersArray = new Object[2];
        Integer byUsersCount = restaurantByUsersCountMap.get(placeId);
        int byUsersNumber = byUsersCount != null ? byUsersCount : 0;
        String joiners = (byUsersNumber != 0) ? ("(" + byUsersNumber + ")") : "";
        boolean isJoinersVisible = byUsersNumber != 0;
        joinersArray[0] = joiners;
        joinersArray[1] = isJoinersVisible;

        return joinersArray;
    }
}
