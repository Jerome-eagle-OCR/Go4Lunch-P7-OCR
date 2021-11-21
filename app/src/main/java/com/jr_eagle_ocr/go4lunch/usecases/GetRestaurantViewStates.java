package com.jr_eagle_ocr.go4lunch.usecases;

import android.graphics.Bitmap;
import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.google.android.libraries.places.api.model.LocalTime;
import com.google.android.libraries.places.api.model.Period;
import com.jr_eagle_ocr.go4lunch.R;
import com.jr_eagle_ocr.go4lunch.model.Restaurant;
import com.jr_eagle_ocr.go4lunch.repositories.LocationRepository;
import com.jr_eagle_ocr.go4lunch.repositories.RestaurantRepository;
import com.jr_eagle_ocr.go4lunch.ui.viewstates.RestaurantViewSate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * @author jrigault
 */
public final class GetRestaurantViewStates extends UseCase {
    private final LocationRepository locationRepository;
    private final LiveData<Map<String, Restaurant>> foundRestaurantsLiveData;

    public GetRestaurantViewStates(
            LocationRepository locationRepository,
            RestaurantRepository restaurantRepository
    ) {
        this.locationRepository = locationRepository;
        foundRestaurantsLiveData = restaurantRepository.getFoundRestaurants();
    }

    /**
     * Generate a list of RestaurantViewState
     *
     * @param restaurantByUsersCountMap a map<restaurant id, by users count> to get
     *                                  the count of users having chosen a specific restaurant
     * @return a list of RestaurantViewState
     */
    public List<RestaurantViewSate> getRestaurantViewStates(Map<String, Integer> restaurantByUsersCountMap) {
        List<RestaurantViewSate> restaurantViewSates = new ArrayList<>();
        Map<String, Restaurant> foundRestaurants = foundRestaurantsLiveData.getValue();
        if (foundRestaurants != null) {
            for (Map.Entry<String, Restaurant> restaurantEntry : foundRestaurants.entrySet()) {
                String placeId = restaurantEntry.getKey();
                Restaurant restaurant = restaurantEntry.getValue();

                // Restaurant photo
                Bitmap photo = restaurant.getPhoto();
                // Restaurant name
                String name = restaurant.getName();
                // Restaurant distance from current maps location
                String distance = this.getDistanceText(restaurant);
                // Restaurant address
                String address = restaurant.getAddress();
                if (address.endsWith(", France"))
                    address = address.substring(0, address.length() - 8);
                // Restaurant number of joining users and text visibility
                Object[] joinersArray = this.getJoinersArray(restaurantByUsersCountMap, placeId);
                String joiners = (String) joinersArray[0];
                boolean isJoinersVisible = (boolean) joinersArray[1];
                // Restaurant opening
                Object[] openingArray = this.getOpeningArray(restaurant);
                int openingPrefix = (int) openingArray[0];
                String closingTime = (String) openingArray[1];
                boolean isWarningStyle = (boolean) openingArray[2];
                // Restaurant rating range reduced to 3 stars
                int rating;
                rating = (int) (restaurant.getRating() * 3 / 5);

                // Restaurant view state creation
                RestaurantViewSate restaurantViewSate = new RestaurantViewSate(
                        placeId, photo, name, distance, address,
                        joiners, isJoinersVisible, openingPrefix, closingTime, isWarningStyle, rating);

                // Add view state in the list
                restaurantViewSates.add(restaurantViewSate);
                Log.d(TAG, "getRestaurantViewStates: added " + restaurantViewSate.toString());
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
        double startLat = restaurant.getLatLng().latitude;
        double startLng = restaurant.getLatLng().longitude;
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

        Calendar nowCalendar = Calendar.getInstance();
        int todayDay = nowCalendar.get(Calendar.DAY_OF_WEEK) - 1;

        Period todayOpenings = restaurant.getOpeningHours().getPeriods().get(todayDay);

        if (todayOpenings.getClose() != null) {
            LocalTime closeTime = todayOpenings.getClose().getTime();
            int closeMn = closeTime.getHours() * 60 + closeTime.getMinutes();
            int nowMn = nowCalendar.get(Calendar.HOUR_OF_DAY) * 60 + nowCalendar.get(Calendar.MINUTE);
            int compareMn = closeMn - nowMn;
            if (compareMn <= 0) {
                openingPrefix = R.string.closed;
            } else if (compareMn < 60) {
                openingPrefix = R.string.closing_soon;
            } else {
                openingPrefix = R.string.open_until;
                closingTime = closeTime.getHours() + ":" + closeTime.getMinutes();
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
     * @param restaurantByUsersNumberMap a map<restaurant id, by users count> to get
     *                                   the count of users having chosen a specific restaurant
     * @param placeId                    the id of a specific restaurant
     * @return an object array containing infos for the view
     */
    @NonNull
    private Object[] getJoinersArray(Map<String, Integer> restaurantByUsersNumberMap, String placeId) {
        Object[] joinersArray = new Object[2];
        String joiners;
        boolean isJoinersVisible;
        if (restaurantByUsersNumberMap.isEmpty()) {
            joiners = "";
            isJoinersVisible = false;
        } else {
            //noinspection ConstantConditions: placeId is a Key got from entrySet in generateRestaurantViewStates()
            int byUsersNumber = restaurantByUsersNumberMap.get(placeId);
            joiners = "(" + byUsersNumber + ")";
            isJoinersVisible = true;
        }
        joinersArray[0] = joiners;
        joinersArray[1] = isJoinersVisible;

        return joinersArray;
    }

}
