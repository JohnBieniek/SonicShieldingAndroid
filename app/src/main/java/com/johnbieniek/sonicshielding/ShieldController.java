package com.johnbieniek.sonicshielding;

import android.content.Context;
import android.content.Intent;
import android.service.quicksettings.TileService;

final class ShieldController {
    private ShieldController() {}

    static void toggle(Context context) {
        setEnabled(context, !ShieldPreferences.isShieldEnabled(context));
    }

    static void setEnabled(Context context, boolean enabled) {
        ShieldPreferences.setShieldEnabled(context, enabled);
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
}
