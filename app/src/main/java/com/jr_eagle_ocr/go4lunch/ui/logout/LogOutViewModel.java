package com.jr_eagle_ocr.go4lunch.ui.logout;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseUser;
import com.jr_eagle_ocr.go4lunch.R;
import com.jr_eagle_ocr.go4lunch.data.models.User;
import com.jr_eagle_ocr.go4lunch.data.repositories.UserRepository;

import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class LogOutViewModel extends ViewModel {
    private final String TAG = getClass().getSimpleName();
    private final UserRepository userRepository;

    private final MutableLiveData<Integer> signOutResultMutableLiveData = new MutableLiveData<>();

    public LogOutViewModel(
            UserRepository userRepository
    ) {
        this.userRepository = userRepository;
    }

    public void signOut(Context context) {
        FirebaseUser firebaseUser = userRepository.getCurrentFirebaseUser().getValue();
        if (firebaseUser != null) {
            User currentUser = userRepository.getCurrentUser().getValue();
            Objects.requireNonNull(currentUser).setLogged(false);
            userRepository.setUser(currentUser);
            userRepository.signOut(context).addOnSuccessListener(unused -> {
                signOutResultMutableLiveData.setValue(R.string.disconnection_successful);
            }).addOnFailureListener(e -> {
                currentUser.setLogged(true);
                userRepository.setUser(currentUser);
                signOutResultMutableLiveData.setValue(R.string.disconnection_unsuccessful);
                Log.e(TAG, "signOut: ", e);
            });
        }
    }

    public LiveData<Integer> signOutResult() {
        return signOutResultMutableLiveData;
    }
}