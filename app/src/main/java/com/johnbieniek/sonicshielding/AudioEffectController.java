package com.johnbieniek.sonicshielding;

import android.content.Context;
import android.media.audiofx.Equalizer;
import android.util.Log;

final class AudioEffectController {
    private static final String TAG = "SonicShielding";
    private Equalizer equalizer;

    boolean apply(Context context) {
        release();

        // Beep/alarm protection must never be translated into a permanent EQ.
        // Without a live detector, doing so treats all speech and music as a beep.
        if (!ProfileMath.shouldApplyPermanentFiltering(
                ShieldPreferences.isBeepBlockerEnabled(context),
                ShieldPreferences.isEqEnabled(context))) return true;

        try {
            equalizer = new Equalizer(0, 0);
            short bands = equalizer.getNumberOfBands();
            short[] levelRange = equalizer.getBandLevelRange();
            int[] reductions = ShieldPreferences.getReductions(context);
            for (short band = 0; band < bands; band++) {
                int centerHz = equalizer.getCenterFreq(band) / 1000;
                int profileIndex = ProfileMath.closestFrequencyIndex(
                        centerHz, ShieldPreferences.FREQUENCIES);
                equalizer.setBandLevel(band, ProfileMath.reductionToBandLevel(
                        reductions[profileIndex], levelRange[0], levelRange[1]));
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
