package com.jr_eagle_ocr.go4lunch.ui.workmates;

import static com.jr_eagle_ocr.go4lunch.repositories.RestaurantRepository.PLACENAME_FIELD;

import android.util.Log;

import androidx.core.util.Pair;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.DocumentSnapshot;
import com.jr_eagle_ocr.go4lunch.model.User;
import com.jr_eagle_ocr.go4lunch.repositories.RestaurantRepository;
import com.jr_eagle_ocr.go4lunch.repositories.UserRepository;
import com.jr_eagle_ocr.go4lunch.ui.viewstates.UserViewState;
import com.jr_eagle_ocr.go4lunch.usecases.GetUserViewStates;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author jrigault
 */
public class WorkmatesViewModel extends ViewModel {
    private final String TAG = getClass().getSimpleName();
    private final MediatorLiveData<List<UserViewState>> allUserViewStatesMediatorLiveData = new MediatorLiveData<>();

    public WorkmatesViewModel(
            UserRepository userRepository,
            RestaurantRepository restaurantRepository,
            GetUserViewStates getUserViewStates
    ) {
        LiveData<Map<String, User>> allUsersLiveData = userRepository.getAllUsers();
        allUserViewStatesMediatorLiveData.addSource(allUsersLiveData, allUsers ->
                buildAndSetAllUserViewStates(allUsers, restaurantRepository, getUserViewStates));
    }

    /**
     * Observed by activity to get up-to-date all user view states to display in recyclerview
     */
    public LiveData<List<UserViewState>> getAllUserViewStates() {
        return allUserViewStatesMediatorLiveData;
    }

    /**
     * Build and set all users to display in recyclerview UserAdapter
     */
    private void buildAndSetAllUserViewStates(Map<String, User> allUsers, RestaurantRepository restaurantRepository, GetUserViewStates getUserViewStates) {
        restaurantRepository.getChosenRestaurantsCollection()
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<UserViewState> userViewStates; //To be produced to valorize livedata
                    Map<String, Pair<String, String>> userChosenRestaurantMap = new HashMap<>();
                    List<DocumentSnapshot> documents = queryDocumentSnapshots.getDocuments();
                    for (DocumentSnapshot d : documents) {
                        String placeId = d.getId();
                        String placeName = d.getString(PLACENAME_FIELD);
                        List<String> byUserIds = restaurantRepository.getByUserIds(d);
                        for (int i = 0; i < byUserIds.size(); i++) {
                            String uid = byUserIds.get(i);
                            userChosenRestaurantMap.put(uid, new Pair<>(placeId, placeName));
                        }
                    }
                    userViewStates = getUserViewStates.getUserViewStates(null, allUsers, userChosenRestaurantMap);
                    allUserViewStatesMediatorLiveData.setValue(userViewStates);
                    Log.d(TAG, "buildAndSetAllUserViewStates: " + userViewStates.toString());
                });
    }

}