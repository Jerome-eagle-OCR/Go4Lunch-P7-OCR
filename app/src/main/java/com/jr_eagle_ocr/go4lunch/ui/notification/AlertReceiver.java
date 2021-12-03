package com.jr_eagle_ocr.go4lunch.ui.notification;


import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.util.Pair;

import com.jr_eagle_ocr.go4lunch.di.Go4LunchApplication;
import com.jr_eagle_ocr.go4lunch.data.repositories.usecases.GetNotificationKit;

import java.util.concurrent.ExecutionException;

/**
 * @author CodingInFlow
 */
public class AlertReceiver extends BroadcastReceiver {
    private final String TAG = getClass().getSimpleName();
    private final GetNotificationKit getNotificationKit;

    public AlertReceiver() {
        getNotificationKit = Go4LunchApplication.getDependencyContainer().getNotificationKit();
    }

    @Override
    public void onReceive(
            Context context,
            Intent intent
    ) {
        NotificationHelper notificationHelper = new NotificationHelper(context);
        NotificationCompat.Builder nb = notificationHelper.getChannelNotification();

        final PendingResult result = goAsync();
        Thread thread = new Thread() {
            public void run() {
                int i = 111;
                if (nb != null) {
                    // Get the style with lines and pending intent for the notification
                    Pair<NotificationCompat.InboxStyle, PendingIntent> stylePendingIntentPair = null;
                    try {
                        stylePendingIntentPair = getNotificationKit.getNotificationKit(context);
                    } catch (ExecutionException | InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (stylePendingIntentPair != null
                            && stylePendingIntentPair.first != null
                            && stylePendingIntentPair.second != null) {
                        // Set style and intent in notification builder
                        nb.setStyle(stylePendingIntentPair.first);
                        nb.setContentIntent(stylePendingIntentPair.second);
                        // Send notification
                        notificationHelper.getManager().notify(1, nb.build());
                    }
                }
                result.setResultCode(i);
                result.finish();
            }
        };
        thread.start();
    }
}