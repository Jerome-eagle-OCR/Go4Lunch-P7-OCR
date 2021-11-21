package com.jr_eagle_ocr.go4lunch.notification;

import static com.jr_eagle_ocr.go4lunch.repositories.RestaurantRepository.BYUSERS_FIELD;
import static com.jr_eagle_ocr.go4lunch.repositories.RestaurantRepository.CHOSENBY_COLLECTION_NAME;
import static com.jr_eagle_ocr.go4lunch.repositories.RestaurantRepository.PLACEADDRESS_FIELD;
import static com.jr_eagle_ocr.go4lunch.repositories.RestaurantRepository.PLACENAME_FIELD;
import static com.jr_eagle_ocr.go4lunch.repositories.RestaurantRepository.USERNAME_FIELD;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.jr_eagle_ocr.go4lunch.R;
import com.jr_eagle_ocr.go4lunch.di.Go4LunchApplication;
import com.jr_eagle_ocr.go4lunch.repositories.RestaurantRepository;
import com.jr_eagle_ocr.go4lunch.repositories.UserRepository;
import com.jr_eagle_ocr.go4lunch.ui.MainActivity;
import com.jr_eagle_ocr.go4lunch.ui.restaurant_detail.RestaurantDetailActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jrigault
 */
public final class NotificationsWorker extends Worker {

    private static final String TAG = NotificationsWorker.class.getSimpleName();
    private final int NOTIFICATION_ID = 007;
    private final String NOTIFICATION_TAG = "GO4LUNCH";
    private final Context context;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;

    public NotificationsWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
        userRepository = Go4LunchApplication.getDependencyContainer().getUserRepository();
        restaurantRepository = Go4LunchApplication.getDependencyContainer().getRestaurantRepository();
    }

    @NonNull
    @Override
    public Result doWork() {
//        String uid = "LEoFNorNs2X0ycQCxdGb0pvixau1";
        // Do work only if user is authenticated
        FirebaseUser firebaseUser = userRepository.getCurrentFirebaseUser().getValue();
        if (firebaseUser != null) {
            String uid = firebaseUser.getUid();
            QuerySnapshot querySnapshot = restaurantRepository.getChosenRestaurantsCollection()
                    .whereArrayContains(BYUSERS_FIELD, uid)
                    .get()
                    .getResult();

            // Get notification lines and if not empty, build and send notification
            List<String> notificationLines = this.getNotificationLines(uid, querySnapshot);
            if (!notificationLines.isEmpty()) {
                Log.d(TAG, "Notification text: " + notificationLines);

                // Create an Intent that will be shown when user will click on the Notification
                Intent intent = new Intent(context, MainActivity.class);
                String chosenRestaurantId = querySnapshot.getDocuments().get(0).getId();
                intent.putExtra(RestaurantDetailActivity.PLACE_ID, chosenRestaurantId);
                PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT);

                // Create a Style for the Notification
                NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
                inboxStyle.setBigContentTitle(getApplicationContext().getString(R.string.app_name) + " !");
                for (String line : notificationLines) {
                    inboxStyle.addLine(line);
                }

                // Create a Channel (Android 8)
                String channelId = getApplicationContext().getString(R.string.default_notification_channel_id);

                // Build a Notification object
                NotificationCompat.Builder notificationBuilder =
                        new NotificationCompat.Builder(getApplicationContext(), channelId)
                                .setSmallIcon(R.drawable.notification)
                                .setAutoCancel(true)
                                .setTimeoutAfter(600000)
                                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                                .setStyle(inboxStyle)
                                .setContentIntent(pendingIntent);

                // Add the Notification to the Notification Manager and show it.
                NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

                // Support Version >= Android 8
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    CharSequence channelName = "Message provenant de Go4Lunch";
                    int importance = NotificationManager.IMPORTANCE_HIGH;
                    NotificationChannel mChannel = new NotificationChannel(channelId, channelName, importance);
                    notificationManager.createNotificationChannel(mChannel);
                }

                // Show notification
                notificationManager.notify(NOTIFICATION_TAG, NOTIFICATION_ID, notificationBuilder.build());

                return Result.success();
            } else {
                return Result.failure();
            }
        } else {
            return Result.failure();
        }
    }

    /**
     * Build the notification lines
     *
     * @param uid           the authenticated user id
     * @param querySnapshot Firestore QuerySnapshot containing chosen restaurant document(s)
     * @return a list of string
     */
    private List<String> getNotificationLines(String uid, QuerySnapshot querySnapshot) {
        List<String> notificationLines = new ArrayList<>();
        String prefix = context.getString(R.string.you_lunch_at);
        String with = context.getString(R.string.with);

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
            String joiningUsers = this.getJoiningUsers(uid, restaurantDocument);
            notificationLines.add(with + joiningUsers);
        }
        return notificationLines;
    }

    /**
     * Build the joining users string
     *
     * @param uid                the authenticated user id
     * @param restaurantDocument the authenticated user chosen restaurant Firestore document
     * @return a string
     */
    private String getJoiningUsers(String uid, DocumentSnapshot restaurantDocument) {
        StringBuilder joiningUsers = new StringBuilder();
        QuerySnapshot querySnapshot = restaurantDocument.getReference().collection(CHOSENBY_COLLECTION_NAME)
                .get()
                .getResult();
        List<DocumentSnapshot> userDocuments = querySnapshot.getDocuments();
        if (userDocuments.size() > 1) {
            for (int i = 0; i < userDocuments.size(); i++) {
                DocumentSnapshot userDocument = userDocuments.get(i);
                String userDocumentId = userDocument.getId();
                if (!userDocumentId.equals(uid)) {
                    if (i != 0) {
                        String and = context.getString(R.string.and);
                        joiningUsers.append((i < (userDocuments.size() - 1)) ? ", " : and);
                    }
                    joiningUsers.append(userDocument.getString(USERNAME_FIELD));
                }
            }
        }
        return joiningUsers.toString();
    }
}
