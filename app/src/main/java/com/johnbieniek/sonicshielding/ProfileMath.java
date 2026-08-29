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
        int protection = scaledProtectionReduction(frequencyHz, maximumTonalReduction, preserveSpeech);
        if (aggressiveAlarmBlocking && frequencyHz >= 5000) {
            protection = scaledProtectionReduction(frequencyHz, maximumTonalReduction, false);
        }
        return Math.max(reduction, protection);
    }
    public static int scaledProtectionReduction(int frequencyHz, int requestedStrength, boolean preserveSpeech) {
        int doubled = Math.max(0, Math.min(200, requestedStrength * 2));
        if (preserveSpeech && frequencyHz <= 4000) doubled = Math.min(doubled, 35);
        return Math.min(100, doubled);
    }
    public static int additionalProtectionReduction(int frequencyHz, boolean beepBlocker,
                                                    int minimumProtectedFrequency, int requestedStrength,
                                                    boolean preserveSpeech) {
        if (!beepBlocker || frequencyHz < minimumProtectedFrequency) return 0;
        int doubled = Math.max(0, Math.min(200, requestedStrength * 2));
        if (preserveSpeech && frequencyHz <= 4000) doubled = Math.min(doubled, 35);
        return Math.max(0, doubled - 100);
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
