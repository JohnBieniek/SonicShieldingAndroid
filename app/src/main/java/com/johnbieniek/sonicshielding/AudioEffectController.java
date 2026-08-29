package com.johnbieniek.sonicshielding;

import android.content.Context;
import android.media.audiofx.Equalizer;
import android.util.Log;

final class AudioEffectController {
    private static final String TAG = "SonicShielding";
    private Equalizer equalizer;

    boolean apply(Context context) {
        release();
        boolean comfortEnabled = ShieldPreferences.isEqEnabled(context);
        boolean beepBlocker = ShieldPreferences.isBeepBlockerEnabled(context);
        if (!comfortEnabled && !beepBlocker) {
            return true;
        }

        try {
            // Audio session 0 is Android's output mix. Device vendors may disable it.
            equalizer = new Equalizer(0, 0);
            short bands = equalizer.getNumberOfBands();
            short[] levelRange = equalizer.getBandLevelRange();
            int[] reductions = ShieldPreferences.getReductions(context);

            for (short band = 0; band < bands; band++) {
                int centerHz = equalizer.getCenterFreq(band) / 1000;
                int profileIndex = ProfileMath.closestFrequencyIndex(centerHz, ShieldPreferences.FREQUENCIES);
                int effectiveReduction = ProfileMath.effectiveReduction(centerHz, reductions[profileIndex],
                        comfortEnabled, beepBlocker, ShieldPreferences.getMinimumFrequency(context),
                        ShieldPreferences.getTonalReduction(context),
                        ShieldPreferences.isSpeechProtectionEnabled(context),
                        ShieldPreferences.isAlarmBlockerEnabled(context));
                short level = ProfileMath.reductionToBandLevel(
                        effectiveReduction, levelRange[0], levelRange[1]);
                equalizer.setBandLevel(band, level);
            }
            equalizer.setEnabled(true);
            return true;
        } catch (RuntimeException error) {
            Log.w(TAG, "This device did not expose the output-mix equalizer", error);
            release();
            return false;
        }
    }

    void release() {
        if (equalizer != null) {
            try {
                equalizer.setEnabled(false);
                equalizer.release();
            } catch (RuntimeException error) {
                Log.w(TAG, "Unable to release equalizer cleanly", error);
            }
            equalizer = null;
        }
    }
}
