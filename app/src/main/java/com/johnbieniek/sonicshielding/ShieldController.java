package com.johnbieniek.sonicshielding;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.Intent;
import android.service.quicksettings.TileService;

final class ShieldController {
    private static final AudioEffectController AUDIO_EFFECT = new AudioEffectController();

    private ShieldController() {}

    static void toggle(Context context) {
        if (ShieldPreferences.isShieldEnabled(context)) {
            ShieldPreferences.rememberAndDisableBlockers(context);
        } else {
            ShieldPreferences.restoreRememberedBlockers(context);
        }
        refreshProfile(context);
    }

    static void refreshProfile(Context context) {
        boolean enabled = ShieldPreferences.isShieldEnabled(context);
        boolean processingEnabled = ProfileMath.shouldApplyPermanentFiltering(
                ShieldPreferences.isBeepBlockerEnabled(context),
                ShieldPreferences.isEqEnabled(context))
                || ProfileMath.shouldApplyFallbackFiltering(
                        ShieldPreferences.isBeepBlockerEnabled(context),
                        ShieldPreferences.isCaptureActive(context));
        boolean captureEnabled = ShieldPreferences.isBeepBlockerEnabled(context)
                && ShieldPreferences.isCaptureActive(context);
        updateLauncherIcon(context, enabled);
        Intent service = new Intent(context, ShieldService.class);
        if (captureEnabled || (processingEnabled && ShieldPreferences.shouldKeepRunning(context))) {
            AUDIO_EFFECT.release();
            service.setAction(ShieldService.ACTION_REFRESH);
            context.startForegroundService(service);
        } else if (processingEnabled) {
            context.stopService(service);
            AUDIO_EFFECT.apply(context.getApplicationContext());
        } else {
            context.stopService(service);
            ShieldPreferences.setCaptureActive(context, false);
            AUDIO_EFFECT.release();
        }
        refreshTile(context);
    }

    static void refreshTile(Context context) {
        TileService.requestListeningState(context,
                new ComponentName(context, ShieldTileService.class));
    }

    private static void updateLauncherIcon(Context context, boolean enabled) {
        PackageManager manager = context.getPackageManager();
        ComponentName active = new ComponentName(context, enabled
                ? "com.johnbieniek.sonicshielding.ShieldOn"
                : "com.johnbieniek.sonicshielding.ShieldOff");
        ComponentName inactive = new ComponentName(context, enabled
                ? "com.johnbieniek.sonicshielding.ShieldOff"
                : "com.johnbieniek.sonicshielding.ShieldOn");
        manager.setComponentEnabledSetting(active,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
        manager.setComponentEnabledSetting(inactive,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }
}
