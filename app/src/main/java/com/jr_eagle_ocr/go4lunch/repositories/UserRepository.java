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
        String userId = this.getCurrentFirebaseUser().getUid();
        String userName = this.getCurrentFirebaseUser().getDisplayName();
        String userEmail;
        if (userName == null || userName.equals("")) {
            userEmail = this.getCurrentFirebaseUser().getEmail();
            if (userEmail != null) {
                userName = this.getNameFromEmail(userEmail);
            } else {
                userName = "Anonymous";
            }
        }
        // Get photo url and manage if null
        Uri photoUrl = getCurrentFirebaseUser().getPhotoUrl();
        String userUrlPicture;
        if (photoUrl != null) {
            userUrlPicture = this.getCurrentFirebaseUser().getPhotoUrl().toString();
        } else {
            switch (userName) {
                case "Tintin":
                    userUrlPicture = "https://static.wikia.nocookie.net/tintinfr/images/a/a0/Unnamed.png/revision/latest?cb=20200817143958&path-prefix=fr";
                    break;
                default:
                    userUrlPicture = null;
            }
        }
        if (currentUser == null || !currentUser.getUid().equals(userId)) {
            currentUser = new User(userId, userName, userUrlPicture);
        }
        return new User(userId, userName, userUrlPicture);
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

    private String getNameFromEmail(String email) {
        String[] words = email.split("@")[0].split("_");
        StringBuilder capitalizedStr = new StringBuilder();

        for (String word : words) {
            // Capitalize first letter
            String firstLetter = word.substring(0, 1);
            // Get remaining letter
            String remainingLetters = word.substring(1);
            capitalizedStr.append(firstLetter.toUpperCase()).append(remainingLetters).append(" ");
        }
        return capitalizedStr.toString().trim();
    }
}
