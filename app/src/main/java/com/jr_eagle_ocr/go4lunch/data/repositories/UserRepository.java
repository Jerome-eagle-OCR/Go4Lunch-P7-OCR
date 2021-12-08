package com.jr_eagle_ocr.go4lunch.data.repositories;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.jr_eagle_ocr.go4lunch.data.models.User;
import com.jr_eagle_ocr.go4lunch.data.repositories.parent.Repository;
import com.jr_eagle_ocr.go4lunch.util.Event;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author jrigault
 */
public final class UserRepository extends Repository {
    private final MutableLiveData<FirebaseUser> currentFirebaseUserMutableLiveData;
    private final MutableLiveData<Event<Boolean>> isUserCreatedEventMutableLiveData = new MutableLiveData<>();
    private final MutableLiveData<Map<String, User>> allLoggedUsersMutableLiveData = new MutableLiveData<>();
    private final MutableLiveData<User> currentUserMutableLiveData = new MutableLiveData<>();
    private ListenerRegistration listenerRegistration;

    public UserRepository() {
        auth.addAuthStateListener(UserRepository.this::onAuthStateChanged);
        FirebaseUser currentFirebaseUser = auth.getCurrentUser();
        currentFirebaseUserMutableLiveData = new MutableLiveData<>(currentFirebaseUser);
    }

    // ----------------
    // --- FIREBASE ---
    // ----------------

    /**
     * AuthStateListener triggered on authentication change
     *
     * @param firebaseAuth Firebase Authentication SDK entry point
     */
    private void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        currentFirebaseUserMutableLiveData.setValue(firebaseUser);
        setAllLoggedUsers(firebaseUser);
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
     * Signs the current user out, if one is signed in.
     *
     * @param context the context requesting the user be signed out
     * @return A task which, upon completion, signals that the user has been signed out ({@link
     * Task#isSuccessful()}, or that the sign-out attempt failed unexpectedly !{@link
     * Task#isSuccessful()}).
     */
    public Task<Void> signOut(Context context) {
        return authUI.signOut(context);
    }

    /**
     * Delete the use from FirebaseAuth and delete any associated credentials from the Credentials
     * API. Returns a {@link Task} that succeeds if the Firebase Auth user deletion succeeds and
     * fails if the Firebase Auth deletion fails. Credentials deletion failures are handled
     * silently.
     *
     * @param context the calling {@link Context}.
     */
    public Task<Void> deleteUser(Context context) {
        return authUI.delete(context);
    }


    // -----------------
    // --- FIRESTORE ---
    // -----------------

    private static final String USERS_COLLECTION_NAME = "users";
    private static final String USERID_FIELD = "uid";
    private static final String USERNAME_FIELD = "userName";
    private static final String USERURLPICTURE_FIELD = "userUrlPicture";


    /**
     * Set user in Firestore from FirebaseAuth eventually modified/completed
     * with Firestore db infos if user already exists
     */
    public void createUser() {
        FirebaseUser firebaseUser = currentFirebaseUserMutableLiveData.getValue();
        if (firebaseUser != null) {
            String uid = firebaseUser.getUid();
            String name = firebaseUser.getDisplayName();
            String userEmail = firebaseUser.getEmail();
            String urlPicture;
            String NOPHOTOURL = "https://ia801503.us.archive.org/3/items/default_avatar_202110/no_photo.png";
            urlPicture = firebaseUser.getPhotoUrl() != null ? firebaseUser.getPhotoUrl().toString() : NOPHOTOURL;

            User userToCreate = new User(uid, name, userEmail, urlPicture, true, true);

            // If the user already exists in Firestore, we get his data (userUrlPicture, userName, isNoonReminderEnabled)
            this.getUsersCollection().document(uid).get()
                    .addOnSuccessListener(document -> {
                        User firestoreUser = null;
                        if (document.exists()) firestoreUser = document.toObject(User.class);
                        if (firestoreUser != null) {
                            if (firestoreUser.getUserUrlPicture() != null) {
                                userToCreate.setUserUrlPicture(firestoreUser.getUserUrlPicture());
                            }
                            if (firestoreUser.getUserName() != null) {
                                userToCreate.setUserName(firestoreUser.getUserName());
                            }
                            userToCreate.setNoonReminderEnabled(firestoreUser.isNoonReminderEnabled());
                        }
                        String userName = userToCreate.getUserName();
                        if (userName == null || userName.equals("")) {
                            // Compose alternative name from email
                            String nameFromEmail = userEmail == null || userEmail.equals("") ?
                                    "ANONYMOUS" : this.getNameFromEmail(userEmail);
                            userToCreate.setUserName(nameFromEmail);
                        }
                        this.setUser(userToCreate).addOnSuccessListener(unused ->
                                isUserCreatedEventMutableLiveData.setValue(new Event<>(Boolean.TRUE)));
                    });
        }
    }

    /**
     * Get a name from an email address
     * (assuming name@server.domain or name_surname@server.domain)
     *
     * @param email the email address (e.g.: tryphon.tournesol@herge.be)
     * @return a name only or with surname (e.g.: Tryphon Tournesol)
     */
    private String getNameFromEmail(@NonNull String email) {
        String[] words = email.split("[@]")[0].split("[_.]");
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

    /**
     * Get an event as soon as user is created
     *
     * @return a livedata holding a boolean event
     */
    public LiveData<Event<Boolean>> isUserCreatedEvent() {
        return isUserCreatedEventMutableLiveData;
    }

    /**
     * Set a user in Firestore "users" collection
     *
     * @param userToSet the user to set
     * @return a {@link Task} to be listened to
     */
    public Task<Void> setUser(User userToSet) {
        String uid = userToSet.getUid();
        return this.getUsersCollection().document(uid).set(userToSet);
    }

    /**
     * Get User Data from Firestore "users" collection
     *
     * @return a {@link Task} to get the documentSnapshot
     */
    public LiveData<User> getCurrentUser() {
        return currentUserMutableLiveData;
    }

    /**
     * Set current user getting it from allUsers (see getAllUsers() ) if user is authenticated,
     * or null if not
     *
     * @param firebaseUser the currently authenticated user
     * @param allUsers     all users from Firestore db
     */
    private void setCurrentUser(FirebaseUser firebaseUser, Map<String, User> allUsers) {
        User currentUser = null;
        if (firebaseUser != null && allUsers != null) {
            String uid = firebaseUser.getUid();
            if (allUsers.containsKey(uid)) currentUser = allUsers.get(uid);
        }
        currentUserMutableLiveData.setValue(currentUser);
    }

    /**
     * Get all users from Firestore "users" collection
     *
     * @return a user map with key = uid and value = user, in a livedata
     */
    public LiveData<Map<String, User>> getAllLoggedUsers() {
        return allLoggedUsersMutableLiveData;
    }

    /**
     * Set Firestore "users" collection listener which valorize allUsers (see getAllUsers() )
     *
     * @param firebaseUser the current authenticated Firebase user
     */
    private void setAllLoggedUsers(FirebaseUser firebaseUser) {
        if (firebaseUser != null) {
            listenerRegistration = this.getUsersCollection()
                    .addSnapshotListener((value, error) -> {
                        Map<String, User> currentAllLoggedUsers = allLoggedUsersMutableLiveData.getValue();
                        Map<String, User> newAllLoggedUsers = new LinkedHashMap<>();
                        if (value != null && !value.isEmpty()) {
                            List<DocumentSnapshot> documents = value.getDocuments();
                            for (DocumentSnapshot d : documents) {
                                User user = d.toObject(User.class);
                                if (user != null && user.isLogged()) {
                                    newAllLoggedUsers.put(user.getUid(), user);
                                }
                            }
                        }
                        if (!Objects.equals(currentAllLoggedUsers, newAllLoggedUsers)) {
                            allLoggedUsersMutableLiveData.setValue(newAllLoggedUsers);
                            setCurrentUser(firebaseUser, newAllLoggedUsers);
                        }
                        if (error != null) {
                            allLoggedUsersMutableLiveData.setValue(null);
                            setCurrentUser(firebaseUser, null);
                            Log.e(TAG, "getAllUsers: ", error);
                        }
                    });
            Log.d(TAG, "setAllLoggedUsers: users collection listener set");

        } else if (listenerRegistration != null) {
            setCurrentUser(null, null);
            allLoggedUsersMutableLiveData.setValue(null);
            listenerRegistration.remove();
            Log.d(TAG, "setAllLoggedUsers: users collection listener removed");
        }
    }

    /**
     * Get the "users" Collection Reference
     *
     * @return the "users" Collection Reference
     */
    public CollectionReference getUsersCollection() {
        return db.collection(USERS_COLLECTION_NAME);
    }
}
