package com.jr_eagle_ocr.go4lunch.usecases;

import static com.jr_eagle_ocr.go4lunch.repositories.RestaurantRepository.BYUSERS_FIELD;
import static com.jr_eagle_ocr.go4lunch.repositories.RestaurantRepository.CHOSENBY_COLLECTION_NAME;
import static com.jr_eagle_ocr.go4lunch.repositories.RestaurantRepository.PLACEADDRESS_FIELD;
import static com.jr_eagle_ocr.go4lunch.repositories.RestaurantRepository.PLACENAME_FIELD;
import static com.jr_eagle_ocr.go4lunch.repositories.RestaurantRepository.USERNAME_FIELD;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.google.firebase.firestore.DocumentSnapshot;
import com.jr_eagle_ocr.go4lunch.R;
import com.jr_eagle_ocr.go4lunch.model.User;

import java.util.ArrayList;
import java.util.List;

public class GetNotificationLines extends UseCase {

    private final Application context;
    private final LiveData<User> currentUserLiveData;
    private final LiveData<String> currentUserChosenRestaurantIdLiveData;

    private final List<String> notificationLines = new ArrayList<>();


    public GetNotificationLines(
            Application context,
            GetCurrentUser getCurrentUser,
            GetCurrentUserChosenRestaurantId getCurrentUserChosenRestaurantId
    ) {
        this.context = context;
        currentUserLiveData = getCurrentUser.getCurrentUser();
        currentUserChosenRestaurantIdLiveData = getCurrentUserChosenRestaurantId.getCurrentUserChosenRestaurantId();

        currentUserChosenRestaurantIdLiveData.observeForever(restaurantId -> setNotificationLines());
    }


    /**
     * @return
     */
    public List<String> getNotificationLines() {
        return notificationLines;
    }

    /**
     *
     */
    private void setNotificationLines() {
        String prefix = context.getString(R.string.you_lunch_at);
        String with = context.getString(R.string.with);
        String and = context.getString(R.string.and);

        notificationLines.clear();
        if (currentUserChosenRestaurantIdLiveData.getValue() != null) {
            User user = currentUserLiveData.getValue();
            if (user != null) {
                String uid = user.getUid();
                chosenRestaurantsCollection
                        .whereArrayContains(BYUSERS_FIELD, uid)
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            List<String> restaurantLines = new ArrayList<>();
                            if (queryDocumentSnapshots.size() == 1) {
                                DocumentSnapshot restaurantDocument = queryDocumentSnapshots.getDocuments().get(0);
                                String restaurantName = restaurantDocument.getString(PLACENAME_FIELD);
                                restaurantLines.add(prefix + restaurantName);
                                String restaurantAddress = restaurantDocument.getString(PLACEADDRESS_FIELD);
                                if (restaurantAddress != null) {
                                    restaurantAddress = restaurantAddress.replace(", France", "");
                                }
                                restaurantLines.add(restaurantAddress);
                                notificationLines.addAll(restaurantLines);
                                restaurantDocument.getReference().collection(CHOSENBY_COLLECTION_NAME)
                                        .get()
                                        .addOnSuccessListener(queryDocumentSnapshots1 -> {
                                            StringBuilder joiningUsers = new StringBuilder();
                                            List<DocumentSnapshot> documents = queryDocumentSnapshots1.getDocuments();
                                            if (documents.size() > 1) {
                                                for (int i = 0; i < documents.size(); i++) {
                                                    DocumentSnapshot document = documents.get(i);
                                                    String documentUid = document.getId();
                                                    if (!documentUid.equals(uid)) {
                                                        if (i != 0) {
                                                            joiningUsers.append((i < (documents.size() - 1)) ? ", " : and);
                                                        }
                                                        joiningUsers.append(document.getString(USERNAME_FIELD));
                                                    }
                                                }
                                                notificationLines.add(with + joiningUsers.toString());
                                            }
                                        });
                            }
                        });
            }
        }
    }
}
