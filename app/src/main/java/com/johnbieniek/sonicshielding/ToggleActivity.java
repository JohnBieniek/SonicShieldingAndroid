package com.johnbieniek.sonicshielding;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

public final class ToggleActivity extends Activity {
    private static final int NOTIFICATION_PERMISSION = 10;
    private boolean handled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            boolean turningOn = !ShieldPreferences.isShieldEnabled(this);
            if (turningOn && ShieldPreferences.willRestoreComfortEq(this)
                    && ShieldPreferences.shouldKeepRunning(this)
                    && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                    && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION);
                return;
            }
            completeToggle();
        }
    }

    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION) completeToggle();
    }

    private void completeToggle() {
        if (!handled) {
            handled = true;
            ShieldController.toggle(this);
        }
        finishAndRemoveTask();
    }
}
