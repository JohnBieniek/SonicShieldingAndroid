package com.johnbieniek.sonicshielding;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;

public final class CapturePermissionActivity extends Activity {
    private static final int AUDIO_PERMISSION = 21;
    private static final int PROJECTION_PERMISSION = 22;

    static void launch(Context context) {
        context.startActivity(new Intent(context, CapturePermissionActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        if (state == null) requestAudioOrProjection();
    }
    private void requestAudioOrProjection() {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q) {
            ShieldPreferences.setCaptureActive(this, false);
            finishAndRemoveTask();
            return;
        }
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, AUDIO_PERMISSION);
            return;
        }
        startActivityForResult(getSystemService(MediaProjectionManager.class)
                .createScreenCaptureIntent(), PROJECTION_PERMISSION);
    }
    @Override public void onRequestPermissionsResult(int code, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(code, permissions, results);
        if (code == AUDIO_PERMISSION && results.length > 0
                && results[0] == PackageManager.PERMISSION_GRANTED) requestAudioOrProjection();
        else finishAndRemoveTask();
    }
    @Override protected void onActivityResult(int code, int resultCode, Intent data) {
        super.onActivityResult(code, resultCode, data);
        if (code == PROJECTION_PERMISSION && resultCode == RESULT_OK && data != null) {
            ShieldService.startCapture(this, resultCode, data);
        } else {
            ShieldPreferences.setCaptureActive(this, false);
            ShieldController.refreshProfile(this);
        }
        finishAndRemoveTask();
    }
}
