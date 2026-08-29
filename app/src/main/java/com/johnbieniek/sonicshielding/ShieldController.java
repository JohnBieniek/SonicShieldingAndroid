package com.johnbieniek.sonicshielding;

import android.content.Context;
import android.content.Intent;
import android.service.quicksettings.TileService;
import android.content.ComponentName;

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

}
