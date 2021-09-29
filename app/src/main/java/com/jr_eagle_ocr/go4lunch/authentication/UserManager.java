package com.jr_eagle_ocr.go4lunch.authentication;

import android.content.Context;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.jr_eagle_ocr.go4lunch.model.User;
import com.jr_eagle_ocr.go4lunch.repositories.UserRepository;

import java.util.List;

// TODO: to be replaced by ViewModels where User is needed
public class UserManager {
    private static volatile UserManager instance;
    private final UserRepository userRepository;

    private UserManager() {
        userRepository = UserRepository.getInstance();
    }

    public static UserManager getInstance() {
        UserManager result = instance;
        if (result != null) {
            return result;
        }
        synchronized (UserManager.class) {
            if (instance == null) {
                instance = new UserManager();
            }
            return instance;
        }
    }

    public FirebaseUser getCurrentFirebaseUser() {
        return userRepository.getCurrentFirebaseUser();
    }

    public Boolean isCurrentUserLogged() {
        return (this.getCurrentFirebaseUser() != null);
    }

    public User getCurrentUser() {
        return userRepository.getCurrentUser();
    }

    public List<User> getAllUsers() {
        return userRepository.getAllUsers();
    }

    public String getUserChoice() {
        return this.getCurrentUser().getChosenRestaurantId();
    }

    public void setUserChoice(String userChoice) {
        this.getCurrentUser().setChosenRestaurantId(userChoice);
    }

    public Task<Void> signOut(Context context){
        return userRepository.signOut(context);
    }

    public Task<Void> deleteUser(Context context){
        return userRepository.deleteUser(context);
    }
}
