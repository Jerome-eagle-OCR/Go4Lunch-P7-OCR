package com.jr_eagle_ocr.go4lunch.repositories;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.Nullable;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.jr_eagle_ocr.go4lunch.model.User;

import java.util.List;
import java.util.Objects;

public final class UserRepository {

    private static volatile UserRepository instance;
    private User currentUser;

    private UserRepository() {
    }

    public static UserRepository getInstance() {
        UserRepository result = instance;
        if (result != null) {
            return result;
        }
        synchronized (UserRepository.class) {
            if (instance == null) {
                instance = new UserRepository();
            }
            return instance;
        }
    }

    @Nullable
    public FirebaseUser getCurrentFirebaseUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    public User getCurrentUser() {
        // TODO: to be modified when Firestore is implemented
        String userId = Objects.requireNonNull(this.getCurrentFirebaseUser()).getUid();
        String userName = this.getCurrentFirebaseUser().getDisplayName();
        // Get photo url and manage if null
        Uri photoUrl = getCurrentFirebaseUser().getPhotoUrl();
        String userUrlPicture =
                photoUrl != null ? this.getCurrentFirebaseUser().getPhotoUrl().toString() : null;
        if (currentUser == null || !currentUser.getUid().equals(userId)) {
            currentUser = new User(userId, userName, userUrlPicture);
        }
        return currentUser;
    }

    public List<User> getAllUsers() {
        //TODO: get all users from DB when Firestore is implemented
        return null;
    }

    public String getUserChoice() {
        return this.getCurrentUser().getChosenRestaurantId();
    }

    public void setUserChoice(String userChoice) {
        this.getCurrentUser().setChosenRestaurantId(userChoice);
    }

    public Task<Void> signOut(Context context) {
        return AuthUI.getInstance().signOut(context);
    }

    public Task<Void> deleteUser(Context context) {
        return AuthUI.getInstance().delete(context);
    }
}
