package com.johnbieniek.sonicshielding;

import android.content.Context;
import android.media.audiofx.Equalizer;
import android.util.Log;
import java.util.Collections;
import java.util.List;

final class AudioEffectController {
    private static final String TAG = "SonicShielding";
    private Equalizer equalizer;

    synchronized boolean apply(Context context) {
        return applyAdaptive(context, Collections.emptyList(), false);
    }

    synchronized boolean applyAdaptive(Context context, List<Integer> detectedHz, boolean alarm) {
        release();

        // Beep/alarm protection must never be translated into a permanent EQ.
        // Without a live detector, doing so treats all speech and music as a beep.
        if (detectedHz.isEmpty() && !alarm && !ProfileMath.shouldApplyPermanentFiltering(
                ShieldPreferences.isBeepBlockerEnabled(context),
                ShieldPreferences.isEqEnabled(context))) return true;

        try {
            equalizer = new Equalizer(0, 0);
            short bands = equalizer.getNumberOfBands();
            short[] levelRange = equalizer.getBandLevelRange();
            int[] bandCenters = new int[bands];
            for (short band = 0; band < bands; band++) {
                bandCenters[band] = equalizer.getCenterFreq(band) / 1000;
            }
            int[] reductions = ShieldPreferences.getReductions(context);
            for (short band = 0; band < bands; band++) {
                int centerHz = bandCenters[band];
                int profileIndex = ProfileMath.closestFrequencyIndex(
                        centerHz, ShieldPreferences.FREQUENCIES);
                int reduction = ShieldPreferences.isEqEnabled(context) ? reductions[profileIndex] : 0;
                for (int detected : detectedHz) {
                    if (ProfileMath.closestFrequencyIndex(detected, bandCenters) == band) {
                        int adaptiveReduction = ShieldPreferences.getTonalReduction(context);
                        if (alarm) adaptiveReduction = Math.max(adaptiveReduction,
                                ShieldPreferences.getSuddenSoundReduction(context));
                        reduction = Math.max(reduction, adaptiveReduction);
                    }
                }
                equalizer.setBandLevel(band, ProfileMath.reductionToBandLevel(
                        reduction, levelRange[0], levelRange[1]));
            }
            equalizer.setEnabled(true);
            return true;
        } catch (RuntimeException error) {
            Log.w(TAG, "This device did not expose the output-mix equalizer", error);
            release();
            return false;
        }
    }

    synchronized void release() {
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
