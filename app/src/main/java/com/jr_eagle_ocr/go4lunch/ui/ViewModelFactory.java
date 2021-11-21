package com.jr_eagle_ocr.go4lunch.ui;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.jr_eagle_ocr.go4lunch.di.Go4LunchApplication;
import com.jr_eagle_ocr.go4lunch.repositories.LocationRepository;
import com.jr_eagle_ocr.go4lunch.repositories.RestaurantRepository;
import com.jr_eagle_ocr.go4lunch.repositories.UserRepository;
import com.jr_eagle_ocr.go4lunch.ui.listview.ListViewViewModel;
import com.jr_eagle_ocr.go4lunch.ui.logout.LogOutViewModel;
import com.jr_eagle_ocr.go4lunch.ui.mapview.MapsViewViewModel;
import com.jr_eagle_ocr.go4lunch.ui.workmates.WorkmatesViewModel;
import com.jr_eagle_ocr.go4lunch.usecases.GetCurrentUserChosenRestaurantId;
import com.jr_eagle_ocr.go4lunch.usecases.GetRestaurantViewStates;
import com.jr_eagle_ocr.go4lunch.usecases.GetUserViewStates;

import org.jetbrains.annotations.NotNull;

/**
 * @author jrigault
 */
public class ViewModelFactory implements ViewModelProvider.Factory {
    private static ViewModelFactory factory;

    public static ViewModelFactory getInstance() {
        if (factory == null) {
            synchronized (ViewModelFactory.class) {
                if (factory == null) {
                    factory = new ViewModelFactory(
                            Go4LunchApplication.getDependencyContainer().getUserRepository(),
                            Go4LunchApplication.getDependencyContainer().getLocationRepository(),
                            Go4LunchApplication.getDependencyContainer().getRestaurantRepository(),
                            Go4LunchApplication.getDependencyContainer().getCurrentUserChosenRestaurantId(),
                            Go4LunchApplication.getDependencyContainer().getUserViewStates(),
                            Go4LunchApplication.getDependencyContainer().getRestaurantViewStates());
                }
            }
        }
        return factory;
    }

    @NonNull
    private final UserRepository userRepository;
    @NonNull
    private final LocationRepository locationRepository;
    @NonNull
    private final RestaurantRepository restaurantRepository;
    @NonNull
    private final GetCurrentUserChosenRestaurantId getCurrentUserChosenRestaurantId;
    @NonNull
    private final GetUserViewStates getUserViewStates;
    @NonNull
    private final GetRestaurantViewStates getRestaurantViewStates;


    private ViewModelFactory(
            @NonNull UserRepository userRepository,
            @NonNull LocationRepository locationRepository,
            @NonNull RestaurantRepository restaurantRepository,
            @NonNull GetCurrentUserChosenRestaurantId getCurrentUserChosenRestaurantId,
            @NonNull GetUserViewStates getUserViewStates,
            @NonNull GetRestaurantViewStates getRestaurantViewStates
    ) {
        this.userRepository = userRepository;
        this.locationRepository = locationRepository;
        this.restaurantRepository = restaurantRepository;
        this.getCurrentUserChosenRestaurantId = getCurrentUserChosenRestaurantId;
        this.getUserViewStates = getUserViewStates;
        this.getRestaurantViewStates = getRestaurantViewStates;
    }

    @SuppressWarnings("unchecked")
    @NonNull
    @NotNull
    @Override
    public <T extends ViewModel> T create(@NonNull @NotNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(LogOutViewModel.class)) {
            return (T) new LogOutViewModel(
                    userRepository);
        }
        if (modelClass.isAssignableFrom(WorkmatesViewModel.class)) {
            return (T) new WorkmatesViewModel(
                    userRepository, restaurantRepository, getUserViewStates);
        }
        if (modelClass.isAssignableFrom(ListViewViewModel.class)) {
            return (T) new ListViewViewModel(
                    restaurantRepository, getRestaurantViewStates);
        }
        if (modelClass.isAssignableFrom(MapsViewViewModel.class)) {
            return (T) new MapsViewViewModel(
                    locationRepository, restaurantRepository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }


}
