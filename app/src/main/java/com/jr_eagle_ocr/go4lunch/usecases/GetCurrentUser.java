package com.jr_eagle_ocr.go4lunch.usecases;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.jr_eagle_ocr.go4lunch.model.User;
import com.jr_eagle_ocr.go4lunch.repositories.UserRepository;

public class GetCurrentUser {

    private static final String TAG = GetCurrentUser.class.getSimpleName();
    private final Task<DocumentSnapshot> getUserData;
    private final MutableLiveData<User> currentUserMutableLiveData;


    public GetCurrentUser(
            UserRepository userRepository
    ) {
        getUserData = userRepository.getUserData();
        currentUserMutableLiveData = new MutableLiveData<>();
    }


    /**
     * Get User Data from Firestore (livedata valorized by getUserDataFromFirestore())
     *
     * @return the current user in a livedata
     */
    public LiveData<User> getCurrentUser() {
        if (getUserData != null) {
            getUserData.continueWith(task ->
                    task.getResult().getReference()
                            .addSnapshotListener((value, error) -> {
                                if (value != null && value.exists()) {
                                    User currentUser = value.toObject(User.class);
                                    currentUserMutableLiveData.setValue(currentUser);
                                } else if (error != null) {
                                    Log.e(TAG, "getUserDataFromFirestore: ", error);
                                    currentUserMutableLiveData.setValue(null);
                                }
                            }));
        } else {
            currentUserMutableLiveData.setValue(null);
        }
        return currentUserMutableLiveData;
    }
}
