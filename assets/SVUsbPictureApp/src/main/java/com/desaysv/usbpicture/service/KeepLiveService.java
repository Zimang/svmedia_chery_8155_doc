package com.desaysv.usbpicture.service;
/**
 * 这个是用来断电重启之后，保活进程，
 * 否则会被 AMS kill掉进程
 */

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

public class KeepLiveService extends Service {
    public KeepLiveService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(new NotificationChannel("com.desaysv.usbpicture.service", "消息通知", NotificationManager.IMPORTANCE_LOW));
                Notification notification = new Notification.Builder(this, "com.desaysv.usbpicture.service")
                        .setContentTitle("开启前台服务")
                        .build();
                startForeground(1, notification);
            }
        }

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}