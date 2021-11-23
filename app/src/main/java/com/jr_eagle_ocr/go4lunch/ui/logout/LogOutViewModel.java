package com.jr_eagle_ocr.go4lunch.ui.logout;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.jr_eagle_ocr.go4lunch.R;
import com.jr_eagle_ocr.go4lunch.model.User;
import com.jr_eagle_ocr.go4lunch.repositories.RestaurantRepository;
import com.jr_eagle_ocr.go4lunch.repositories.UserRepository;

public class LogOutViewModel extends ViewModel {
    private final String TAG = getClass().getSimpleName();
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;

    private final MutableLiveData<Integer> signOutResultMutableLiveData;

    public LogOutViewModel(
            UserRepository userRepository,
            RestaurantRepository restaurantRepository
    ) {
        this.userRepository = userRepository;
        this.restaurantRepository = restaurantRepository;
        signOutResultMutableLiveData = new MutableLiveData<>();
    }

    public void signOut(Context context) {
        FirebaseUser firebaseUser = userRepository.getCurrentFirebaseUser().getValue();
        if (firebaseUser != null) {
            Task<Void> task = userRepository.signOut(context);
            task.addOnSuccessListener(unused -> {
                restaurantRepository.unsetChosenRestaurantIdsAndCleanCollection();
                signOutResultMutableLiveData.setValue(R.string.disconnection_successful);
            }).addOnFailureListener(e -> {
                signOutResultMutableLiveData.setValue(R.string.disconnection_unsuccessful);
                Log.e(TAG, "signOut: ", e);
            });
        }
    }

    public LiveData<Integer> signOutResult() {
        return signOutResultMutableLiveData;
    }
}