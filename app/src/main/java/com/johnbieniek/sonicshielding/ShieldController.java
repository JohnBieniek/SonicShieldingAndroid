package com.johnbieniek.sonicshielding;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
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
        updateLauncherIcon(context, enabled);
        if (enabled) {
            AUDIO_EFFECT.apply(context.getApplicationContext());
        } else {
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
