package com.johnbieniek.sonicshielding;

import android.content.Context;
import android.content.Intent;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.service.quicksettings.TileService;

final class ShieldController {
    private ShieldController() {}

    static void toggle(Context context) {
        setEnabled(context, !ShieldPreferences.isShieldEnabled(context));
    }

    static void setEnabled(Context context, boolean enabled) {
        ShieldPreferences.setShieldEnabled(context, enabled);
        updateLauncherIcon(context, enabled);
        Intent serviceIntent = new Intent(context, ShieldService.class)
                .setAction(enabled ? ShieldService.ACTION_ENABLE : ShieldService.ACTION_DISABLE);

        if (enabled) {
            context.startForegroundService(serviceIntent);
        } else {
            context.stopService(serviceIntent);
        }
        refreshTile(context);
    }

    static void refreshProfile(Context context) {
        if (ShieldPreferences.isShieldEnabled(context)) {
            Intent intent = new Intent(context, ShieldService.class).setAction(ShieldService.ACTION_REFRESH);
            context.startForegroundService(intent);
        }
    }

    static void refreshTile(Context context) {
        TileService.requestListeningState(context, new android.content.ComponentName(context, ShieldTileService.class));
    }

    private static void updateLauncherIcon(Context context, boolean enabled) {
        PackageManager manager = context.getPackageManager();
        ComponentName active = new ComponentName(context, enabled ? "com.johnbieniek.sonicshielding.ShieldOn" : "com.johnbieniek.sonicshielding.ShieldOff");
        ComponentName inactive = new ComponentName(context, enabled ? "com.johnbieniek.sonicshielding.ShieldOff" : "com.johnbieniek.sonicshielding.ShieldOn");
        manager.setComponentEnabledSetting(active, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        manager.setComponentEnabledSetting(inactive, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
    }
}
