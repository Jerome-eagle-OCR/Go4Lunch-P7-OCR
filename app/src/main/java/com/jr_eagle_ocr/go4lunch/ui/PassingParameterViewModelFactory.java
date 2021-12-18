package com.jr_eagle_ocr.go4lunch.ui;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.jr_eagle_ocr.go4lunch.data.repositories.LocationRepository;
import com.jr_eagle_ocr.go4lunch.data.repositories.RestaurantRepository;
import com.jr_eagle_ocr.go4lunch.data.repositories.UserRepository;
import com.jr_eagle_ocr.go4lunch.data.usecases.GetCurrentUserChosenRestaurantId;
import com.jr_eagle_ocr.go4lunch.data.usecases.GetIsLikedRestaurant;
import com.jr_eagle_ocr.go4lunch.data.usecases.PlaceAutocompleteSearch;
import com.jr_eagle_ocr.go4lunch.data.usecases.SetClearChosenRestaurant;
import com.jr_eagle_ocr.go4lunch.data.usecases.SetClearLikedRestaurant;
import com.jr_eagle_ocr.go4lunch.di.Go4LunchApplication;
import com.jr_eagle_ocr.go4lunch.ui.restaurant_detail.RestaurantDetailViewModel;
import com.jr_eagle_ocr.go4lunch.util.BitmapUtil;

import org.jetbrains.annotations.NotNull;

import java.util.Calendar;

/**
 * @author jrigault
 */
public class PassingParameterViewModelFactory extends ViewModelProvider.NewInstanceFactory implements ViewModelProvider.Factory {
    @NonNull
    private final Object parameter;
    @NonNull
    private final LocationRepository locationRepository;
    @NonNull
    private final UserRepository userRepository;
    @NonNull
    private final RestaurantRepository restaurantRepository;
    @NonNull
    private final PlaceAutocompleteSearch placeAutocompleteSearch;
    @NonNull
    private final GetCurrentUserChosenRestaurantId getCurrentUserChosenRestaurantId;
    @NonNull
    private final GetIsLikedRestaurant getIsLikedRestaurant;
    @NonNull
    private final SetClearChosenRestaurant setClearChosenRestaurant;
    @NonNull
    private final SetClearLikedRestaurant setClearLikedRestaurant;
    @NonNull
    private final BitmapUtil bitmapUtil;

    public PassingParameterViewModelFactory(
            @NonNull Object parameter
    ) {
        this.parameter = parameter;
        this.bitmapUtil = Go4LunchApplication.getDependencyContainer().getBitmapUtil();
        this.locationRepository = Go4LunchApplication.getDependencyContainer().getLocationRepository();
        this.userRepository = Go4LunchApplication.getDependencyContainer().getUserRepository();
        this.restaurantRepository = Go4LunchApplication.getDependencyContainer().getRestaurantRepository();
        this.placeAutocompleteSearch = Go4LunchApplication.getDependencyContainer().getPlaceAutocompleteSearch();
        this.getCurrentUserChosenRestaurantId = Go4LunchApplication.getDependencyContainer().getCurrentUserChosenRestaurantId();
        this.getIsLikedRestaurant = Go4LunchApplication.getDependencyContainer().getIsLikedRestaurant();
        this.setClearChosenRestaurant = Go4LunchApplication.getDependencyContainer().setClearChosenRestaurant();
        this.setClearLikedRestaurant = Go4LunchApplication.getDependencyContainer().setClearLikedRestaurant();
    }

    @SuppressWarnings("unchecked")
    @NonNull
    @NotNull
    @Override
    public <T extends ViewModel> T create(@NonNull @NotNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(MainViewModel.class)) {
            return (T) new MainViewModel(
                    (boolean) parameter, locationRepository, userRepository, restaurantRepository,
                    placeAutocompleteSearch, getCurrentUserChosenRestaurantId, null);
        }
        if (modelClass.isAssignableFrom(RestaurantDetailViewModel.class)) {
            return (T) new RestaurantDetailViewModel(
                    (String) parameter, bitmapUtil,
                    userRepository, restaurantRepository,
                    getCurrentUserChosenRestaurantId, getIsLikedRestaurant,
                    setClearChosenRestaurant, setClearLikedRestaurant);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
