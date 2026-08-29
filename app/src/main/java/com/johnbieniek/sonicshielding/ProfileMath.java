package com.johnbieniek.sonicshielding;

public final class ProfileMath {
    private ProfileMath() {}
    public static short reductionToBandLevel(int reductionPercent, short minimumLevel, short maximumLevel) {
        int reduction = Math.max(0, Math.min(100, reductionPercent));
        double level = minimumLevel * (reduction / 100.0);
        return (short) Math.max(minimumLevel, Math.min(0, Math.round(level)));
    }
    public static int effectiveReduction(int frequencyHz, int comfortReduction, boolean comfortEnabled,
                                         boolean beepBlocker, int minimumProtectedFrequency,
                                         int maximumTonalReduction, boolean preserveSpeech,
                                         boolean aggressiveAlarmBlocking) {
        int reduction = comfortEnabled ? comfortReduction : 0;
        if (!beepBlocker || frequencyHz < minimumProtectedFrequency) return reduction;
        int protection = maximumTonalReduction;
        if (preserveSpeech && frequencyHz <= 4000) protection = Math.min(protection, 35);
        if (aggressiveAlarmBlocking && frequencyHz >= 5000) protection = maximumTonalReduction;
        return Math.max(reduction, protection);
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
}
