package com.jr_eagle_ocr.go4lunch.ui.settings;

import static com.jr_eagle_ocr.go4lunch.data.repositories.RestaurantRepository.BYUSERS_FIELD;
import static com.jr_eagle_ocr.go4lunch.data.repositories.RestaurantRepository.LIKEDBY_COLLECTION_NAME;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import androidx.annotation.VisibleForTesting;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.QuerySnapshot;
import com.jr_eagle_ocr.go4lunch.R;
import com.jr_eagle_ocr.go4lunch.data.models.User;
import com.jr_eagle_ocr.go4lunch.data.repositories.RestaurantRepository;
import com.jr_eagle_ocr.go4lunch.data.repositories.UserRepository;
import com.jr_eagle_ocr.go4lunch.data.usecases.SetClearChosenRestaurant;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jrigault
 */
public class SettingsViewModel extends ViewModel {
    private final String TAG = getClass().getSimpleName();
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final SetClearChosenRestaurant setClearChosenRestaurant;
    private final User currentUser;
    private String userName;
    private String userUrlPicture;
    private boolean isNoonReminderEnabled;
    private final MutableLiveData<Integer> validateResultMutableLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> deleteUserResultMutableLiveData = new MutableLiveData<>();

    public SettingsViewModel(
            @NonNull UserRepository userRepository,
            @NonNull RestaurantRepository restaurantRepository,
            @NonNull SetClearChosenRestaurant setClearChosenRestaurant
    ) {
        this.userRepository = userRepository;
        this.restaurantRepository = restaurantRepository;
        this.setClearChosenRestaurant = setClearChosenRestaurant;
        currentUser = userRepository.getCurrentUser().getValue();
        if (currentUser != null) {
            userName = currentUser.getUserName();
            userUrlPicture = currentUser.getUserUrlPicture();
            isNoonReminderEnabled = currentUser.isNoonReminderEnabled();
        }
    }

    public SettingsDialogViewState getSettingsDialogViewState() {
        return new SettingsDialogViewState(isNoonReminderEnabled,
                                           userName,
                                           userUrlPicture);
    }

    public void deleteUser(Context context) {
        String uid = currentUser.getUid();
        setClearChosenRestaurant.clearChosenRestaurant();
        userRepository.getUsersCollection().document(uid).delete();
        getLikedRestaurants(uid).continueWith(task -> {
            List<DocumentSnapshot> documents = task.getResult().getDocuments();
            List<Task<Void>> tasks = new ArrayList<>();
            if (!documents.isEmpty()) {
                for (DocumentSnapshot document : documents) {
                    tasks.add(document.getReference().collection(LIKEDBY_COLLECTION_NAME).document(uid).delete());
                    tasks.add(document.getReference().update(BYUSERS_FIELD, FieldValue.arrayRemove(uid)));
                }
            }
            return Tasks.whenAllComplete(tasks);
        }).continueWith(task -> {
            if (task.isComplete()) {
                return Tasks.await(userRepository.deleteUser(context));
            } else {
                return false;
            }
        }).addOnCompleteListener(task -> {
            int message;
            if (task.isComplete()) {
                message = R.string.account_deleted;
            } else {
                message = R.string.error_unknown_error;
            }
            deleteUserResultMutableLiveData.setValue(message);
        });
    }

    private Task<QuerySnapshot> getLikedRestaurants(String uid) {
        return restaurantRepository.getLikedRestaurantsCollection()
                .whereArrayContains(BYUSERS_FIELD, uid)
                .get();
    }

    public MutableLiveData<Integer> deleteUserResult() {
        return deleteUserResultMutableLiveData;
    }

    public void clickOnButtonValidate() {
        User userToSet = new User(currentUser.getUid(),
                                  userName,
                                  currentUser.getUserEmail(),
                                  userUrlPicture,
                                  isNoonReminderEnabled,
                                  currentUser.isLogged());

        userRepository.setUser(userToSet).addOnCompleteListener(task -> {
            int validateResult;
            if (task.isSuccessful()) {
                boolean areChanges = !userToSet.equals(currentUser);
                validateResult = areChanges ? R.string.changes_made : R.string.no_changes_made;
            } else {
                validateResult = R.string.error_unknown_error;
            }
            validateResultMutableLiveData.setValue(validateResult);
        });
    }

    public LiveData<Integer> validateResult() {
        return validateResultMutableLiveData;
    }

    public void setNoonReminderEnabled(boolean isNoonReminderEnabled) {
        this.isNoonReminderEnabled = isNoonReminderEnabled;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setUserUrlPicture(String userUrlPicture) {
        this.userUrlPicture = userUrlPicture;
    }

    @VisibleForTesting
    public final boolean isNoonReminderEnabled() {
        return isNoonReminderEnabled;
    }

    @VisibleForTesting
    public final String getUserName() {
        return userName;
    }

    @VisibleForTesting
    public final String getUserUrlPicture() {
        return userUrlPicture;
    }

    @RestrictTo(RestrictTo.Scope.TESTS)
    public MutableLiveData<Integer> getValidateResultMutableLiveData() {
        return validateResultMutableLiveData;
    }
}