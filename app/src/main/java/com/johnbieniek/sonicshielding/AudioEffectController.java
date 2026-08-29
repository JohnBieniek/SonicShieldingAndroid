package com.johnbieniek.sonicshielding;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.audiofx.Equalizer;
import android.media.audiofx.DynamicsProcessing;
import android.os.Build;
import android.util.Log;

final class AudioEffectController {
    private static final String TAG = "SonicShielding";
    private Equalizer equalizer;
    private DynamicsProcessing additionalProtection;

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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                applyAdditionalProtection(context, levelRange[0], beepBlocker);
            }
            return true;
        } catch (RuntimeException error) {
            Log.w(TAG, "This device did not expose the output-mix equalizer", error);
            release();
            return false;
        }
    }

    @TargetApi(Build.VERSION_CODES.P)
    private void applyAdditionalProtection(Context context, short minimumLevel, boolean beepBlocker) {
        try {
            int bandCount = ShieldPreferences.FREQUENCIES.length;
            DynamicsProcessing.Config config = new DynamicsProcessing.Config.Builder(
                    DynamicsProcessing.VARIANT_FAVOR_FREQUENCY_RESOLUTION,
                    2, false, 0, false, 0, true, bandCount, false).build();
            additionalProtection = new DynamicsProcessing(0, 0, config);
            for (int band = 0; band < bandCount; band++) {
                int frequency = ShieldPreferences.FREQUENCIES[band];
                int extra = ProfileMath.additionalProtectionReduction(frequency, beepBlocker,
                        ShieldPreferences.getMinimumFrequency(context),
                        ShieldPreferences.getTonalReduction(context),
                        ShieldPreferences.isSpeechProtectionEnabled(context));
                float gainDb = (minimumLevel / 100f) * (extra / 100f);
                additionalProtection.setPostEqBandAllChannelsTo(band,
                        new DynamicsProcessing.EqBand(true, frequency, gainDb));
            }
            additionalProtection.setEnabled(true);
        } catch (RuntimeException error) {
            Log.w(TAG, "Additional high-strength protection is not available on this device", error);
            releaseAdditionalProtection();
        }
    }

    void release() {
        releaseAdditionalProtection();
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

    private void releaseAdditionalProtection() {
        if (additionalProtection != null) {
            try {
                additionalProtection.setEnabled(false);
                additionalProtection.release();
            } catch (RuntimeException error) {
                Log.w(TAG, "Unable to release additional protection cleanly", error);
            }
            additionalProtection = null;
        }
    }
}
