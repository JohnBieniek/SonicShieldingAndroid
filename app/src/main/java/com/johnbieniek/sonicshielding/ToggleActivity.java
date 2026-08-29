package com.johnbieniek.sonicshielding;

import android.app.Activity;
import android.os.Bundle;

public final class ToggleActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            ShieldController.toggle(this);
        }
        finishAndRemoveTask();
    }
}
