package com.johnbieniek.sonicshielding;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;

public final class ShieldService extends Service {
    private static final String CHANNEL_ID = "persistent_protection";
    private static final int NOTIFICATION_ID = 101;
    private final AudioEffectController audioEffect = new AudioEffectController();

    @Override public void onCreate() {
        super.onCreate();
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                getString(R.string.status_channel), NotificationManager.IMPORTANCE_LOW);
        channel.setDescription(getString(R.string.status_channel_description));
        channel.setShowBadge(false);
        getSystemService(NotificationManager.class).createNotificationChannel(channel);
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        if (!ShieldPreferences.isEqEnabled(this) || !ShieldPreferences.shouldKeepRunning(this)) {
            audioEffect.release();
            stopForeground(STOP_FOREGROUND_REMOVE);
            stopSelf();
            return START_NOT_STICKY;
        }
        Notification notification = buildNotification();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
        } else {
            startForeground(NOTIFICATION_ID, notification);
        }
        audioEffect.apply(this);
        return START_STICKY;
    }

    private Notification buildNotification() {
        PendingIntent profile = PendingIntent.getActivity(this, 1,
                new Intent(this, ComfortProfileActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        return new Notification.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_shield)
                .setContentTitle(getString(R.string.shield_on))
                .setContentText(getString(R.string.persistent_status))
                .setStyle(new Notification.BigTextStyle().bigText(getString(R.string.persistent_status)))
                .setContentIntent(profile)
                .setOngoing(true)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setSubText("ON")
                .setNumber(0)
                .build();
    }

    @Override public void onDestroy() {
        audioEffect.release();
        super.onDestroy();
    }

    @Override public IBinder onBind(Intent intent) { return null; }
}
