package com.jr_eagle_ocr.go4lunch.repositories;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.jr_eagle_ocr.go4lunch.model.User;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author jrigault
 */
public final class UserRepository extends Repository {
    private final MutableLiveData<FirebaseUser> currentFirebaseUserMutableLiveData;
    private LiveData<User> currentUserLiveData;
    private final MutableLiveData<Map<String, User>> allUsersMutableLiveData = new MutableLiveData<>();

    public UserRepository() {
        auth.addAuthStateListener(this::onAuthStateChanged);
        currentFirebaseUserMutableLiveData = new MutableLiveData<>(auth.getCurrentUser());

        setAllUsers();
        setCurrentUser(auth.getCurrentUser());
    }

    // --- FIREBASE ---

    /**
     * AuthStateListener triggered on authentication change
     *
     * @param firebaseAuth Firebase Authentication SDK entry point
     */
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        currentFirebaseUserMutableLiveData.setValue(firebaseUser);
        setCurrentUser(firebaseUser);
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
        FirebaseUser firebaseUser = getCurrentFirebaseUser().getValue();
        if (firebaseUser != null) {
            String uid = firebaseUser.getUid();
            String name = firebaseUser.getDisplayName();
            String userEmail = firebaseUser.getEmail();
            String urlPicture;
            String NOPHOTOURL = "https://ia801503.us.archive.org/3/items/default_avatar_202110/no_photo.png";
            urlPicture = firebaseUser.getPhotoUrl() != null ? firebaseUser.getPhotoUrl().toString() : NOPHOTOURL;

            User userToCreate = new User(uid, name, userEmail, urlPicture);

            // If the user already exists in Firestore, we get his data (url_picture, user_name)
            if (getCurrentUser() != null) {
                User firestoreUser = getCurrentUser().getValue();
                if (firestoreUser != null) {
                    if (firestoreUser.getUserUrlPicture() != null) {
                        userToCreate.setUserUrlPicture(firestoreUser.getUserUrlPicture());
                    }
                    if (firestoreUser.getUserName() != null) {
                        userToCreate.setUserName(firestoreUser.getUserName());
                    }
                }
            }
            String userName = userToCreate.getUserName();
            if (userName == null || userName.equals("")) {
                // Compose alternative name from email
                String nameFromEmail = userEmail == null || userEmail.equals("") ?
                        "ANONYMOUS" : this.getNameFromEmail(userEmail);
                userToCreate.setUserName(nameFromEmail);
            }
            this.getUsersCollection().document(uid).set(userToCreate)
                    .addOnSuccessListener(unused -> isCreatedMutableLiveData.setValue(true));
        }
        return isCreatedMutableLiveData;
    }

    /**
     * Get User Data from Firestore
     *
     * @return a task to get the documentSnapshot
     */
    public LiveData<User> getCurrentUser() {
        return currentUserLiveData;
    }

    /**
     * Set current user getting it from all users livedata if user authenticated, set null if not
     *
     * @param firebaseUser the currently authenticated user
     */
    private void setCurrentUser(FirebaseUser firebaseUser) {
        if (firebaseUser != null) {
            String uid = firebaseUser.getUid();
            currentUserLiveData = Transformations.map(allUsersMutableLiveData, allUsers ->
                    allUsers != null ? allUsers.get(uid) : null);
        } else {
            currentUserLiveData = new MutableLiveData<>(null);
        }
    }

    /**
     * Get all users from Firestore db
     *
     * @return a user map with key = uid and value = user, in a livedata
     */
    public LiveData<Map<String, User>> getAllUsers() {
        return allUsersMutableLiveData;
    }

    /**
     * Set Firestore "users" collection listener and valorize allUsers livedata
     */
    private void setAllUsers() {
        this.getUsersCollection().addSnapshotListener((value, error) -> {
            Map<String, User> users = new LinkedHashMap<>();
            if (value != null && !value.isEmpty()) {
                List<DocumentSnapshot> documents = value.getDocuments();
                for (DocumentSnapshot d : documents) {
                    User user = d.toObject(User.class);
                    if (user != null) users.put(user.getUid(), user);
                }
            }
            allUsersMutableLiveData.setValue(users);
            if (error != null) {
                allUsersMutableLiveData.setValue(null);
                Log.e(TAG, "getAllUsers: ", error);
            }
        });

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
