package com.jr_eagle_ocr.go4lunch.ui;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.jr_eagle_ocr.go4lunch.data.repositories.RestaurantRepository;
import com.jr_eagle_ocr.go4lunch.data.repositories.UserRepository;
import com.jr_eagle_ocr.go4lunch.data.repositories.usecases.GetCurrentUserChosenRestaurantId;
import com.jr_eagle_ocr.go4lunch.data.repositories.usecases.IsLikedRestaurant;
import com.jr_eagle_ocr.go4lunch.data.repositories.usecases.SetClearChosenRestaurant;
import com.jr_eagle_ocr.go4lunch.data.repositories.usecases.SetClearLikedRestaurant;
import com.jr_eagle_ocr.go4lunch.di.Go4LunchApplication;
import com.jr_eagle_ocr.go4lunch.ui.restaurant_detail.RestaurantDetailViewModel;

import org.jetbrains.annotations.NotNull;

/**
 * @author jrigault
 */
public class PassingParameterViewModelFactory extends ViewModelProvider.NewInstanceFactory implements ViewModelProvider.Factory {
    @NonNull
    private final Object parameter;
    @NonNull
    private final UserRepository userRepository;
    @NonNull
    private final RestaurantRepository restaurantRepository;
    @NonNull
    private final GetCurrentUserChosenRestaurantId getCurrentUserChosenRestaurantId;
    @NonNull
    private final IsLikedRestaurant isLikedRestaurant;
    @NonNull
    private final SetClearChosenRestaurant setClearChosenRestaurant;
    @NonNull
    private final SetClearLikedRestaurant setClearLikedRestaurant;


    public PassingParameterViewModelFactory(
            @NonNull Object parameter
    ) {
        this.parameter = parameter;
        this.userRepository = Go4LunchApplication.getDependencyContainer().getUserRepository();
        this.restaurantRepository = Go4LunchApplication.getDependencyContainer().getRestaurantRepository();
        this.getCurrentUserChosenRestaurantId = Go4LunchApplication.getDependencyContainer().getCurrentUserChosenRestaurantId();
        this.isLikedRestaurant = Go4LunchApplication.getDependencyContainer().getIsLikedRestaurant();
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
                    (boolean) parameter, userRepository, restaurantRepository,
                    getCurrentUserChosenRestaurantId);
        }
        if (modelClass.isAssignableFrom(RestaurantDetailViewModel.class)) {
            return (T) new RestaurantDetailViewModel(
                    (String) parameter, userRepository, restaurantRepository,
                    getCurrentUserChosenRestaurantId, isLikedRestaurant,
                    setClearChosenRestaurant, setClearLikedRestaurant);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
