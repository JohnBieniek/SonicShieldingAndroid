package com.johnbieniek.sonicshielding;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
public final class ShieldActionReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
        if (ShieldService.ACTION_TOGGLE.equals(intent.getAction())) {
            ShieldController.toggle(context);
        }
    }
}
