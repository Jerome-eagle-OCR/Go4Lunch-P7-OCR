package com.jr_eagle_ocr.go4lunch.ui;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.jr_eagle_ocr.go4lunch.data.repositories.LocationRepository;
import com.jr_eagle_ocr.go4lunch.data.repositories.RestaurantRepository;
import com.jr_eagle_ocr.go4lunch.data.repositories.UserRepository;
import com.jr_eagle_ocr.go4lunch.data.usecases.GetRestaurantViewStates;
import com.jr_eagle_ocr.go4lunch.data.usecases.GetUserViewStates;
import com.jr_eagle_ocr.go4lunch.data.usecases.SetClearChosenRestaurant;
import com.jr_eagle_ocr.go4lunch.di.Go4LunchApplication;
import com.jr_eagle_ocr.go4lunch.ui.authentication.AuthenticationViewModel;
import com.jr_eagle_ocr.go4lunch.ui.listview.ListViewViewModel;
import com.jr_eagle_ocr.go4lunch.ui.logout.LogOutViewModel;
import com.jr_eagle_ocr.go4lunch.ui.mapview.MapViewViewModel;
import com.jr_eagle_ocr.go4lunch.ui.settings.SettingsViewModel;
import com.jr_eagle_ocr.go4lunch.ui.workmates.WorkmatesViewModel;
import com.jr_eagle_ocr.go4lunch.util.BitmapUtil;

import org.jetbrains.annotations.NotNull;

import java.util.Calendar;

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
                            Go4LunchApplication.getDependencyContainer().getBitmapUtil(),
                            Go4LunchApplication.getDependencyContainer().getUserRepository(),
                            Go4LunchApplication.getDependencyContainer().getLocationRepository(),
                            Go4LunchApplication.getDependencyContainer().getRestaurantRepository(),
                            Go4LunchApplication.getDependencyContainer().setClearChosenRestaurant()
                    );
                }
            }
        }
        return factory;
    }

    @NonNull
    private final BitmapUtil bitmapUtil;
    @NonNull
    private final UserRepository userRepository;
    @NonNull
    private final LocationRepository locationRepository;
    @NonNull
    private final RestaurantRepository restaurantRepository;
    @NonNull
    private final SetClearChosenRestaurant setClearChosenRestaurant;

    private ViewModelFactory(
            @NonNull BitmapUtil bitmapUtil,
            @NonNull UserRepository userRepository,
            @NonNull LocationRepository locationRepository,
            @NonNull RestaurantRepository restaurantRepository,
            @NonNull SetClearChosenRestaurant setClearChosenRestaurant
    ) {
        this.bitmapUtil = bitmapUtil;
        this.userRepository = userRepository;
        this.locationRepository = locationRepository;
        this.restaurantRepository = restaurantRepository;
        this.setClearChosenRestaurant = setClearChosenRestaurant;
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
                    userRepository, restaurantRepository,
                    new GetUserViewStates());
        }
        if (modelClass.isAssignableFrom(ListViewViewModel.class)) {
            return (T) new ListViewViewModel(
                    userRepository, restaurantRepository,
                    new GetRestaurantViewStates(bitmapUtil,
                                                locationRepository,
                                                restaurantRepository,
                                                Calendar.getInstance()));
        }
        if (modelClass.isAssignableFrom(MapViewViewModel.class)) {
            return (T) new MapViewViewModel(
                    locationRepository, restaurantRepository);
        }
        if (modelClass.isAssignableFrom(SettingsViewModel.class)) {
            return (T) new SettingsViewModel(
                    userRepository, restaurantRepository,
                    setClearChosenRestaurant);
        }
        if (modelClass.isAssignableFrom(AuthenticationViewModel.class)) {
            return (T) new AuthenticationViewModel(
                    userRepository, restaurantRepository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }


}
