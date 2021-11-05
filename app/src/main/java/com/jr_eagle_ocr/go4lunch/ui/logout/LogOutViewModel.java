package com.jr_eagle_ocr.go4lunch.ui.logout;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.jr_eagle_ocr.go4lunch.di.Go4LunchApplication;
import com.jr_eagle_ocr.go4lunch.repositories.UserRepository;

public class LogOutViewModel extends ViewModel {

    private static final String TAG = LogOutViewModel.class.getSimpleName();
    private final UserRepository userRepository;

    private final MutableLiveData<Boolean> isLoggedOutMutableLiveData;

    public LogOutViewModel() {
        userRepository = Go4LunchApplication.getDependencyContainer().getUserRepository();
        isLoggedOutMutableLiveData = new MutableLiveData<>();
    }

    public void signOut(Context context) {
        FirebaseUser firebaseUser = userRepository.getCurrentFirebaseUser().getValue();
        if (firebaseUser != null) {
            Task<Void> task = userRepository.signOut(context);
            task.addOnSuccessListener(unused -> {
                isLoggedOutMutableLiveData.setValue(true);
            }).addOnFailureListener(e -> {
                isLoggedOutMutableLiveData.setValue(false);
                Log.e(TAG, "signOut: ", e);
            });
        }
    }

    public LiveData<Boolean> isLoggedOut() {
        return isLoggedOutMutableLiveData;
    }
}