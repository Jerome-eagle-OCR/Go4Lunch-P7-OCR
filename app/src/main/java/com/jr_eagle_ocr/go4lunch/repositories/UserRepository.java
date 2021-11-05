package com.jr_eagle_ocr.go4lunch.repositories;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jr_eagle_ocr.go4lunch.model.User;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author jrigault
 */
public final class UserRepository {
    private static final String TAG = "UserRepository";
    private static volatile UserRepository instance;

    private final FirebaseFirestore db;
    private final MutableLiveData<FirebaseUser> currentFirebaseUserMutableLiveData;
    private final MutableLiveData<User> currentUser;
    private final MutableLiveData<Map<String, User>> allUsers;


    private UserRepository() {
        db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.addAuthStateListener(this::onAuthStateChanged);
        currentFirebaseUserMutableLiveData = new MutableLiveData<>();
        currentUser = new MutableLiveData<>(null);
        allUsers = new MutableLiveData<>(null);
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

    // --- FIREBASE ---

    /**
     *
     * @param firebaseAuth
     */
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        currentFirebaseUserMutableLiveData.setValue(firebaseAuth.getCurrentUser());
    }

    /**
     * Get the current Firebase user
     *
     * @return the current Firebase user
     */
    public LiveData<FirebaseUser> getCurrentFirebaseUser() {
        return currentFirebaseUserMutableLiveData;
    }

    /**
     *
     * @param context
     * @return
     */
    public Task<Void> signOut(Context context) {
        return AuthUI.getInstance().signOut(context);
    }

    // TODO: to be used in settings
    public Task<Void> deleteUser(Context context) {
        return AuthUI.getInstance().delete(context);
    }


    // --- FIRESTORE ---

    private static final String COLLECTION_NAME = "users";
    private static final String USERID_FIELD = "uid";
    private static final String USERNAME_FIELD = "userName";
    private static final String USERURLPICTURE_FIELD = "userUrlPicture";


    /**
     * Create User in Firestore from FirebaseAuth eventually modified/completed with Firestore db infos
     */
    public LiveData<Boolean> createUser() {
        MutableLiveData<Boolean> isCreatedMutableLiveData = new MutableLiveData<>();
        isCreatedMutableLiveData.setValue(false);
        FirebaseUser user = getCurrentFirebaseUser().getValue();
        if (user != null) {
            String uid = user.getUid();
            String name = user.getDisplayName();
            String urlPicture;
            String NOPHOTOURL = "https://ia801503.us.archive.org/3/items/default_avatar_202110/no_photo.png";
            urlPicture = user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : NOPHOTOURL;

            User userToCreate = new User(uid, name, urlPicture);

            // If the user already exists in Firestore, we get his data (url_picture, user_name)
            Task<DocumentSnapshot> userData = getUserData();
            if (userData != null) {
                userData.addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.contains(USERURLPICTURE_FIELD)) {
                        userToCreate.setUserUrlPicture(
                                (String) documentSnapshot.get(USERURLPICTURE_FIELD));
                    }
                    if (documentSnapshot.contains(USERNAME_FIELD)) {
                        userToCreate.setUserName(
                                (String) documentSnapshot.get(USERNAME_FIELD));
                    }
                    String userName = userToCreate.getUserName();
                    if (userName == null || userName.equals("")) {
                        // Compose alternative name from email
                        String nameFromEmail;
                        String email = (Objects.equals(user.getEmail(), "")) ? null : user.getEmail();
                        String unknown = "unknown"; // TODO: to set null in case email is null and VM will set R.string.unknown
                        nameFromEmail = (email != null) ? this.getNameFromEmail(email) : unknown;
                        userToCreate.setUserName(nameFromEmail);
                    }
                    currentUser.setValue(userToCreate);
                    this.getUsersCollection().document(uid).set(userToCreate)
                            .addOnSuccessListener(unused -> isCreatedMutableLiveData.setValue(true));
                });
            }
        }
        return isCreatedMutableLiveData;
    }

    /**
     * Get User Data from Firestore
     *
     * @return a task to get the documentSnapshot
     */
    @Nullable
    public Task<DocumentSnapshot> getUserData() {
        FirebaseUser firebaseUser = getCurrentFirebaseUser().getValue();
        if (firebaseUser != null) {
            String uid = firebaseUser.getUid();
            return this.getUsersCollection().document(uid).get();
        } else {
            return null;
        }
    }

    /**
     * Get all users in Firestore db
     *
     * @return a user map with key = uid and value = user, in a livedata
     */
    public LiveData<Map<String, User>> getAllUsers() {
        this.getUsersCollection().addSnapshotListener((value, error) -> {
            Map<String, User> users = new LinkedHashMap<>();
            if (value != null && !value.isEmpty()) {
                List<DocumentSnapshot> documents = value.getDocuments();
                for (DocumentSnapshot d : documents) {
                    User user = d.toObject(User.class);
                    if (user != null) users.put(user.getUid(), user);
                }
            }
            allUsers.setValue(users);
            if (error != null) {
                allUsers.setValue(null);
                Log.e(TAG, "getAllUsers: ", error);
            }
        });
        return allUsers;
    }

    /**
     * Get the "users" Collection Reference
     *
     * @return the "users" Collection Reference
     */
    private CollectionReference getUsersCollection() {
        return db.collection(COLLECTION_NAME);
    }


    // --- UTIL ---

    /**
     * Get a name from an email address
     * (assuming name@server.domain or name_surname@server.domain)
     *
     * @param email the email address (e.g.: tryphon.tournesol@herge.be)
     * @return a name only or with surname (e.g.: Tryphon Tournesol)
     */
    private String getNameFromEmail(@NonNull String email) {
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
