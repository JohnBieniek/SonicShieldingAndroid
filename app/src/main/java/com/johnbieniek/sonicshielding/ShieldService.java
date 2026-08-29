package com.johnbieniek.sonicshielding;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.*;
import android.content.*;
import android.content.pm.ServiceInfo;
import android.content.pm.PackageManager;
import android.media.*;
import android.media.projection.*;
import android.os.*;
import android.util.Log;

public final class ShieldService extends Service {
    static final String ACTION_REFRESH = "com.johnbieniek.sonicshielding.REFRESH";
    private static final String ACTION_CAPTURE = "com.johnbieniek.sonicshielding.CAPTURE";
    private static final String EXTRA_RESULT_CODE = "result_code";
    private static final String EXTRA_RESULT_DATA = "result_data";
    private static final String CHANNEL_ID = "persistent_protection";
    private static final int NOTIFICATION_ID = 101;
    private static final int SAMPLE_RATE = 48000;
    private static final String TAG = "SonicShielding";
    private final AudioEffectController audioEffect = new AudioEffectController();
    private MediaProjection projection;
    private AudioRecord recorder;
    private Thread analysisThread;
    private volatile boolean analyzing;

    static void startCapture(Context context, int resultCode, Intent resultData) {
        context.startForegroundService(new Intent(context, ShieldService.class)
                .setAction(ACTION_CAPTURE).putExtra(EXTRA_RESULT_CODE, resultCode)
                .putExtra(EXTRA_RESULT_DATA, resultData));
    }
    @Override public void onCreate() {
        super.onCreate();
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                getString(R.string.status_channel), NotificationManager.IMPORTANCE_LOW);
        channel.setDescription(getString(R.string.status_channel_description));
        channel.setShowBadge(false);
        getSystemService(NotificationManager.class).createNotificationChannel(channel);
    }
    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        boolean captureRequest = intent != null && ACTION_CAPTURE.equals(intent.getAction());
        if (!captureRequest && projection != null && !ShieldPreferences.isBeepBlockerEnabled(this)) {
            stopCapture();
        }
        if (captureRequest) {
            startAsForeground(true);
            startPlaybackCapture(intent.getIntExtra(EXTRA_RESULT_CODE, 0),
                    intent.getParcelableExtra(EXTRA_RESULT_DATA));
        } else if (projection != null && ShieldPreferences.isBeepBlockerEnabled(this)) {
            startAsForeground(true);
        } else if (hasContinuousEffect() && ShieldPreferences.shouldKeepRunning(this)) {
            startAsForeground(false);
            audioEffect.apply(this);
        } else {
            stopEverything(); stopSelf();
        }
        return projection != null ? START_NOT_STICKY : START_STICKY;
    }
    private void startAsForeground(boolean capturing) {
        Notification notification = buildNotification(capturing);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFICATION_ID, notification, capturing
                    ? ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
                    : ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
        } else startForeground(NOTIFICATION_ID, notification);
    }
    private void startPlaybackCapture(int resultCode, Intent resultData) {
        stopCapture();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || resultData == null
                || !ShieldPreferences.isBeepBlockerEnabled(this)
                || checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            finishCaptureSession();
            return;
        }
        startPlaybackCaptureApi29(resultCode, resultData);
    }

    @TargetApi(Build.VERSION_CODES.Q)
    @SuppressLint("MissingPermission") // Caller checks RECORD_AUDIO immediately before entry.
    private void startPlaybackCaptureApi29(int resultCode, Intent resultData) {
        try {
            projection = getSystemService(MediaProjectionManager.class)
                    .getMediaProjection(resultCode, resultData);
            if (projection == null) {
                finishCaptureSession();
                return;
            }
            projection.registerCallback(new MediaProjection.Callback() {
                @Override public void onStop() {
                    clearCaptureResources();
                    finishCaptureSession();
                    ShieldController.refreshTile(ShieldService.this);
                }
            }, null);
            AudioPlaybackCaptureConfiguration configuration =
                    new AudioPlaybackCaptureConfiguration.Builder(projection)
                            .addMatchingUsage(AudioAttributes.USAGE_MEDIA)
                            .addMatchingUsage(AudioAttributes.USAGE_GAME)
                            .addMatchingUsage(AudioAttributes.USAGE_UNKNOWN).build();
            int minimum = AudioRecord.getMinBufferSize(SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
            recorder = new AudioRecord.Builder().setAudioFormat(new AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT).setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_IN_MONO).build())
                    .setBufferSizeInBytes(Math.max(minimum, ToneDetector.FFT_SIZE * 4))
                    .setAudioPlaybackCaptureConfig(configuration).build();
            recorder.startRecording();
            ShieldPreferences.setCaptureActive(this, true);
            analyzing = true;
            analysisThread = new Thread(this::analyzePlayback, "sonic-playback-analysis");
            analysisThread.start();
            ShieldController.refreshTile(this);
        } catch (RuntimeException error) {
            Log.e(TAG, "Playback capture could not start", error);
            stopCapture(); finishCaptureSession();
        }
    }
    private void analyzePlayback() {
        ToneDetector detector = new ToneDetector(SAMPLE_RATE);
        short[] window = new short[ToneDetector.FFT_SIZE];
        short[] hop = new short[256];
        int filled = 0;
        long releaseAt = 0;
        boolean effectActive = false;
        while (analyzing && recorder != null) {
            int count = recorder.read(hop, 0, hop.length, AudioRecord.READ_BLOCKING);
            if (count <= 0) continue;
            if (filled < window.length) {
                int copy = Math.min(count, window.length - filled);
                System.arraycopy(hop, 0, window, filled, copy);
                filled += copy;
                if (filled < window.length) continue;
            } else {
                System.arraycopy(window, count, window, 0, window.length - count);
                System.arraycopy(hop, 0, window, window.length - count, count);
            }
            ToneDetector.Detection detection = detector.analyze(window,
                    ShieldPreferences.getMinimumFrequency(this),
                    ShieldPreferences.getSensitivity(this),
                    ShieldPreferences.isSpeechProtectionEnabled(this),
                    ShieldPreferences.isAlarmBlockerEnabled(this));
            long now = SystemClock.elapsedRealtime();
            if (detection.active()) {
                if (!effectActive) {
                    audioEffect.applyAdaptive(this, detection.frequenciesHz, detection.alarm);
                }
                releaseAt = now + ShieldPreferences.getReleaseDuration(this);
                effectActive = true;
            } else if (effectActive && now >= releaseAt) {
                audioEffect.apply(this); effectActive = false;
            }
        }
    }
    private Notification buildNotification(boolean capturing) {
        PendingIntent profile = PendingIntent.getActivity(this, 1,
                new Intent(this, ComfortProfileActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        String status = getString(capturing ? R.string.capture_status : R.string.persistent_status);
        return new Notification.Builder(this, CHANNEL_ID).setSmallIcon(R.drawable.ic_shield)
                .setContentTitle(getString(R.string.shield_on)).setContentText(status)
                .setStyle(new Notification.BigTextStyle().bigText(status)).setContentIntent(profile)
                .setOngoing(true).setCategory(Notification.CATEGORY_SERVICE).setSubText("ON")
                .setNumber(0).build();
    }
    private void clearCaptureResources() {
        analyzing = false;
        AudioRecord oldRecorder = recorder; recorder = null;
        if (oldRecorder != null) {
            try { oldRecorder.stop(); } catch (RuntimeException ignored) { }
            oldRecorder.release();
        }
        if (analysisThread != null && analysisThread != Thread.currentThread()) analysisThread.interrupt();
        analysisThread = null; projection = null;
        ShieldPreferences.setCaptureActive(this, false);
    }
    private void stopCapture() {
        MediaProjection oldProjection = projection;
        clearCaptureResources();
        if (oldProjection != null) oldProjection.stop();
    }
    private void finishCaptureSession() {
        if (hasContinuousEffect() && ShieldPreferences.shouldKeepRunning(this)) {
            startAsForeground(false);
            audioEffect.apply(this);
        } else {
            audioEffect.release();
            stopForeground(STOP_FOREGROUND_REMOVE);
            stopSelf();
        }
    }
    private boolean hasContinuousEffect() {
        return ShieldPreferences.isEqEnabled(this)
                || ProfileMath.shouldApplyFallbackFiltering(
                        ShieldPreferences.isBeepBlockerEnabled(this),
                        ShieldPreferences.isCaptureActive(this));
    }
    private void stopEverything() {
        stopCapture(); audioEffect.release(); stopForeground(STOP_FOREGROUND_REMOVE);
    }
    @Override public void onDestroy() { stopEverything(); super.onDestroy(); }
    @Override public IBinder onBind(Intent intent) { return null; }
}
