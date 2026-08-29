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
    static final String ACTION_ENABLE = "com.johnbieniek.sonicshielding.ENABLE";
    static final String ACTION_DISABLE = "com.johnbieniek.sonicshielding.DISABLE";
    static final String ACTION_REFRESH = "com.johnbieniek.sonicshielding.REFRESH";
    static final String ACTION_TOGGLE = "com.johnbieniek.sonicshielding.TOGGLE";
    static final String ACTION_OPEN_PROFILE = "com.johnbieniek.sonicshielding.OPEN_PROFILE";

    private static final String CHANNEL_ID = "shield_status";
    private static final int NOTIFICATION_ID = 101;
    private final AudioEffectController audioEffect = new AudioEffectController();

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent == null ? ACTION_ENABLE : intent.getAction();
        if (ACTION_DISABLE.equals(action) || !ShieldPreferences.isShieldEnabled(this)) {
            audioEffect.release();
            stopForeground(STOP_FOREGROUND_REMOVE);
            stopSelf();
            return START_NOT_STICKY;
        }

        startVisible();
        boolean effectAvailable = audioEffect.apply(this);
        updateNotification(effectAvailable);
        return START_STICKY;
    }

    private void startVisible() {
        Notification notification = buildNotification(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
        } else {
            startForeground(NOTIFICATION_ID, notification);
        }
    }

    private void updateNotification(boolean effectAvailable) {
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.notify(NOTIFICATION_ID, buildNotification(effectAvailable));
    }

    private Notification buildNotification(boolean effectAvailable) {
        Intent profileIntent = new Intent(this, ComfortProfileActivity.class);
        PendingIntent profile = PendingIntent.getActivity(this, 1, profileIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent offIntent = new Intent(this, ShieldActionReceiver.class).setAction(ACTION_TOGGLE);
        PendingIntent off = PendingIntent.getBroadcast(this, 2, offIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        String status;
        if (!ShieldPreferences.isEqEnabled(this) && !ShieldPreferences.isBeepBlockerEnabled(this)) {
            status = getString(R.string.notification_profile_off);
        } else if (effectAvailable) {
            status = getString(R.string.notification_active);
        } else {
            status = getString(R.string.notification_unavailable);
        }

        return new Notification.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_shield)
                .setContentTitle(getString(R.string.shield_on))
                .setContentText(status)
                .setContentIntent(profile)
                .setOngoing(true)
                .setCategory(Notification.CATEGORY_SERVICE)
                .addAction(new Notification.Action.Builder(null, getString(R.string.turn_off), off).build())
                .addAction(new Notification.Action.Builder(null, getString(R.string.comfort_profile), profile).build())
                .build();
    }

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID, getString(R.string.status_channel), NotificationManager.IMPORTANCE_LOW);
        channel.setDescription(getString(R.string.status_channel_description));
        getSystemService(NotificationManager.class).createNotificationChannel(channel);
    }

    @Override
    public void onDestroy() {
        audioEffect.release();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
