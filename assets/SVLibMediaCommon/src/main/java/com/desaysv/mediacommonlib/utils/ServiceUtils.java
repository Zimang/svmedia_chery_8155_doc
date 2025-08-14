package com.desaysv.mediacommonlib.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.os.Build;

public class ServiceUtils {

    private ServiceUtils() {
    }


    public static void startForegroundNotification(Service service, String channelName, String notificationTitle) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final String channelId = service.getPackageName();
            NotificationManager notificationManager = (NotificationManager) service.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW);
                channel.enableLights(false);
                channel.enableVibration(false);
                channel.setVibrationPattern(new long[]{0});
                channel.setSound(null, null);

                notificationManager.createNotificationChannel(channel);
                Notification notification = new Notification.Builder(service, channelId)
                        .setContentTitle(notificationTitle)
                        .build();
                service.startForeground(1, notification);
            }
        }
    }
}
