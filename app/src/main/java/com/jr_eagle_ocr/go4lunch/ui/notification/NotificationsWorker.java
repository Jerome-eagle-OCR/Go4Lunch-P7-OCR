package com.jr_eagle_ocr.go4lunch.ui.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.media.RingtoneManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.util.Pair;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.jr_eagle_ocr.go4lunch.R;
import com.jr_eagle_ocr.go4lunch.di.Go4LunchApplication;
import com.jr_eagle_ocr.go4lunch.data.usecases.GetNotificationPair;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @author jrigault
 */
public final class NotificationsWorker extends Worker {

    private static final String TAG = NotificationsWorker.class.getSimpleName();
    private final int NOTIFICATION_ID = 007;
    private final String NOTIFICATION_TAG = "GO4LUNCH";
    private final Context context;
    private final GetNotificationPair getNotificationPair;

    public NotificationsWorker(
            @NonNull Context context,
            @NonNull WorkerParameters workerParams
    ) {
        super(context, workerParams);
        this.context = context;
        getNotificationPair = Go4LunchApplication.getDependencyContainer().getNotificationKit();
    }

    @NonNull
    @Override
    public Result doWork() {
        // Get the style with lines and pending intent for the notification
        Pair<List<String>, PendingIntent> stylePendingIntentPair = null;
        try {
            stylePendingIntentPair = getNotificationPair.getNotificationPair(context);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        if (stylePendingIntentPair != null
                && stylePendingIntentPair.first != null
                && stylePendingIntentPair.second != null) {
            // Create a Style for the Notification
            NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
            inboxStyle.setBigContentTitle(context.getString(R.string.app_name) + " !");
            for (String line : stylePendingIntentPair.first) {
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
                            .setContentIntent(stylePendingIntentPair.second);
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
