package com.jr_eagle_ocr.go4lunch.notification;

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

import com.jr_eagle_ocr.go4lunch.R;
import com.jr_eagle_ocr.go4lunch.di.Go4LunchApplication;
import com.jr_eagle_ocr.go4lunch.ui.restaurant_detail.RestaurantDetailActivity;
import com.jr_eagle_ocr.go4lunch.usecases.GetNotificationLines;

import java.util.List;

public class NotificationsWorker extends Worker {

    private static final String TAG = NotificationsWorker.class.getSimpleName();
    private final int NOTIFICATION_ID = 007;
    private final String NOTIFICATION_TAG = "GO4LUNCH";
    private final GetNotificationLines mGetNotificationLines;

    public NotificationsWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        mGetNotificationLines = Go4LunchApplication.getDependencyContainer().getNotificationLines();
    }

    @NonNull
    @Override
    public Result doWork() {
        // Get notification lines if user is authenticated
        List<String> notificationLines = mGetNotificationLines.getNotificationLines();
        if (notificationLines != null && !notificationLines.isEmpty()) {
            Log.d(TAG, "Notification text: " + notificationLines);

            // Create an Intent that will be shown when user will click on the Notification
            Intent intent = new Intent(getApplicationContext(), RestaurantDetailActivity.class);
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
    }
}
