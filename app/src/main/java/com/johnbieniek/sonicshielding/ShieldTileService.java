package com.johnbieniek.sonicshielding;

import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

public final class ShieldTileService extends TileService {
    @Override
    public void onStartListening() {
        super.onStartListening();
        updateTile();
    }

    @Override
    public void onClick() {
        super.onClick();
        ShieldController.toggle(this);
        updateTile();
    }

    private void updateTile() {
        Tile tile = getQsTile();
        if (tile == null) {
            return;
        }
        boolean enabled = ShieldPreferences.isShieldEnabled(this);
        tile.setState(enabled ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            tile.setSubtitle(getString(enabled ? R.string.on : R.string.off));
        }
        tile.updateTile();
    }
}
