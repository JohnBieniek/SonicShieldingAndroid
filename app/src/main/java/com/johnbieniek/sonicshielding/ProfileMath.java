package com.johnbieniek.sonicshielding;

public final class ProfileMath {
    private ProfileMath() {}
    public static short reductionToBandLevel(int reductionPercent, short minimumLevel, short maximumLevel) {
        int reduction = Math.max(0, Math.min(100, reductionPercent));
        double level = minimumLevel * (reduction / 100.0);
        return (short) Math.max(minimumLevel, Math.min(0, Math.round(level)));
    }
    public static int closestFrequencyIndex(int frequencyHz, int[] centersHz) {
        int closest = 0;
        long distance = Long.MAX_VALUE;
        for (int index = 0; index < centersHz.length; index++) {
            long candidate = Math.abs((long) centersHz[index] - frequencyHz);
            if (candidate < distance) { distance = candidate; closest = index; }
        }
        return closest;
    }
    public static boolean shouldUseActiveIcon(boolean beepBlockerEnabled, boolean comfortEqEnabled) {
        return beepBlockerEnabled || comfortEqEnabled;
    }
    public static boolean shouldApplyPermanentFiltering(boolean beepBlockerEnabled, boolean comfortEqEnabled) {
        return comfortEqEnabled;
    }
}
