package com.jr_eagle_ocr.go4lunch.data.usecases;

import static com.jr_eagle_ocr.go4lunch.data.repositories.RestaurantRepository.BYUSERS_FIELD;
import static com.jr_eagle_ocr.go4lunch.data.repositories.RestaurantRepository.CHOSENBY_COLLECTION_NAME;
import static com.jr_eagle_ocr.go4lunch.data.repositories.RestaurantRepository.PLACEADDRESS_FIELD;
import static com.jr_eagle_ocr.go4lunch.data.repositories.RestaurantRepository.PLACENAME_FIELD;
import static com.jr_eagle_ocr.go4lunch.data.repositories.RestaurantRepository.USERNAME_FIELD;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.util.Pair;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.jr_eagle_ocr.go4lunch.R;
import com.jr_eagle_ocr.go4lunch.data.repositories.RestaurantRepository;
import com.jr_eagle_ocr.go4lunch.data.repositories.UserRepository;
import com.jr_eagle_ocr.go4lunch.data.usecases.parent.UseCase;
import com.jr_eagle_ocr.go4lunch.ui.MainActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @author jrigault
 */
public final class GetNotificationPair extends UseCase {
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;

    public GetNotificationPair(
            UserRepository userRepository,
            RestaurantRepository restaurantRepository
    ) {
        this.userRepository = userRepository;
        this.restaurantRepository = restaurantRepository;
    }

    /**
     * Get the notification kit if a user is authenticated, null kit if not
     *
     * @param context the application context
     * @return a Pair, first InboxStyle, second PendingIntent
     */
    public Pair<List<String>, PendingIntent> getNotificationPair(Context context) throws ExecutionException, InterruptedException {
        List<String> notificationLines = null;
        PendingIntent pendingIntent = null;
        List<String> tempNotificationLines = getNotificationLines(context);
        if (!tempNotificationLines.isEmpty()) {
            notificationLines = tempNotificationLines;
            Log.d(TAG, "Notification text: " + notificationLines);
            // Create an Intent to go to chosen restaurant detail when user click on the Notification
            Intent intent = MainActivity.navigate(context, true);
            pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        }
        return new Pair<>(notificationLines, pendingIntent);
    }

    /**
     * Build the notification lines
     *
     * @param context the Android context
     * @return a list of strings
     */
    private List<String> getNotificationLines(Context context) throws ExecutionException, InterruptedException {
        List<String> notificationLines = new ArrayList<>();
        String prefix = context.getString(R.string.you_lunch_at);
        String alone = context.getString(R.string.alone);
        String with = context.getString(R.string.with);

        // Do work only if user is authenticated
        FirebaseUser firebaseUser = userRepository.getCurrentFirebaseUser().getValue();
        if (firebaseUser != null) {
            String uid = firebaseUser.getUid();
            QuerySnapshot querySnapshot = Tasks.await(restaurantRepository
                    .getChosenRestaurantsCollection()
                    .whereArrayContains(BYUSERS_FIELD, uid)
                    .get());

            if (querySnapshot.size() == 1) {
                List<String> restaurantLines = new ArrayList<>();
                DocumentSnapshot restaurantDocument = querySnapshot.getDocuments().get(0);
                String restaurantName = restaurantDocument.getString(PLACENAME_FIELD);
                restaurantLines.add(prefix + restaurantName);
                String restaurantAddress = restaurantDocument.getString(PLACEADDRESS_FIELD);
                if (restaurantAddress != null) {
                    restaurantAddress = restaurantAddress.replace(", France", "");
                }
                restaurantLines.add(restaurantAddress);
                notificationLines.addAll(restaurantLines);
                String joiningUsers = this.getJoiningUsers(context, uid, restaurantDocument);
                if (!joiningUsers.isEmpty()) {
                    notificationLines.add(with + joiningUsers);
                } else {
                    notificationLines.add(alone);
                }
            }
        }
        return notificationLines;
    }

    /**
     * Build the joining users string
     *
     * @param context the Android context
     * @param uid                the authenticated user id
     * @param restaurantDocument the authenticated user chosen restaurant Firestore document
     * @return a string
     */
    private String getJoiningUsers(Context context, String uid, DocumentSnapshot restaurantDocument) throws ExecutionException, InterruptedException {
        StringBuilder joiningUsers = new StringBuilder();
        String and = context.getString(R.string.and);
        QuerySnapshot querySnapshot = Tasks.await(restaurantDocument.getReference()
                .collection(CHOSENBY_COLLECTION_NAME)
                .get());
        List<DocumentSnapshot> userDocuments = querySnapshot.getDocuments();
        for (DocumentSnapshot documentSnapshot : userDocuments) {
            if (documentSnapshot.getId().equals(uid)) {
                userDocuments.remove(documentSnapshot);
                break;
            }
        }
        if (!userDocuments.isEmpty()) {
            boolean isFirstAddedUser = true;
            for (int i = 0; i < userDocuments.size(); i++) {
                DocumentSnapshot userDocument = userDocuments.get(i);
                String userDocumentId = userDocument.getId();
                if (!userDocumentId.equals(uid)) {
                    if (!isFirstAddedUser) {
                        joiningUsers.append((i < (userDocuments.size() - 1)) ? ", " : and);
                    }
                    joiningUsers.append(userDocument.getString(USERNAME_FIELD));
                    isFirstAddedUser = false;
                }
            }
        }
        return joiningUsers.toString();
    }
}
