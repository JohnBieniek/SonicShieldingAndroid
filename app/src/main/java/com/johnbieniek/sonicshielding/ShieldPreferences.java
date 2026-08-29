package com.johnbieniek.sonicshielding;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.Arrays;

public final class ShieldPreferences {
    public static final int[] FREQUENCIES = {63, 125, 250, 500, 1000, 2000, 4000, 8000, 12000};
    public static final int[] DEFAULT_REDUCTIONS = {0, 0, 0, 0, 97, 98, 99, 100, 100};
    private static final String FILE = "sonic_shielding";
    private static final String ENABLED = "enabled";
    private static final String EQ_ENABLED = "comfort_eq_enabled";
    private static final String REDUCTION_PREFIX = "reduction_";
    private static final String BEEP_BLOCKER = "beep_blocker";
    private static final String STRENGTH = "protection_strength";
    private static final String PRESERVE_SPEECH = "preserve_speech";
    private static final String ALARM_BLOCKER = "alarm_blocker";
    private static final String SUDDEN_REDUCTION = "sudden_reduction";
    private static final String SENSITIVITY = "sensitivity";
    private static final String TONAL_REDUCTION = "tonal_reduction";
    private static final String MINIMUM_FREQUENCY = "minimum_frequency";
    private static final String RELEASE_DURATION = "release_duration";

    private ShieldPreferences() {}
    private static SharedPreferences preferences(Context context) { return context.getSharedPreferences(FILE, Context.MODE_PRIVATE); }
    public static boolean isEnabled(Context context) { return preferences(context).getBoolean(ENABLED, false); }
    public static void setEnabled(Context context, boolean enabled) { preferences(context).edit().putBoolean(ENABLED, enabled).apply(); }
    public static boolean isShieldEnabled(Context context) { return isEnabled(context); }
    public static void setShieldEnabled(Context context, boolean enabled) { setEnabled(context, enabled); }
    public static boolean isComfortEqEnabled(Context context) { return preferences(context).getBoolean(EQ_ENABLED, false); }
    public static void setComfortEqEnabled(Context context, boolean enabled) { preferences(context).edit().putBoolean(EQ_ENABLED, enabled).apply(); }
    public static boolean isEqEnabled(Context context) { return isComfortEqEnabled(context); }
    public static void setEqEnabled(Context context, boolean enabled) { setComfortEqEnabled(context, enabled); }
    public static int[] reductions(Context context) {
        int[] values = Arrays.copyOf(DEFAULT_REDUCTIONS, DEFAULT_REDUCTIONS.length);
        SharedPreferences preferences = preferences(context);
        for (int index = 0; index < values.length; index++) values[index] = preferences.getInt(REDUCTION_PREFIX + index, values[index]);
        return values;
    }
    public static void setReduction(Context context, int index, int reduction) {
        preferences(context).edit().putInt(REDUCTION_PREFIX + index, Math.max(0, Math.min(100, reduction))).apply();
    }
    public static int[] getReductions(Context context) { return reductions(context); }
    public static boolean isBeepBlockerEnabled(Context context) { return preferences(context).getBoolean(BEEP_BLOCKER, true); }
    public static void setBeepBlockerEnabled(Context context, boolean value) { preferences(context).edit().putBoolean(BEEP_BLOCKER, value).apply(); }
    public static String getProtectionStrength(Context context) { return preferences(context).getString(STRENGTH, "strong"); }
    public static void setProtectionStrength(Context context, String value) { preferences(context).edit().putString(STRENGTH, value).apply(); }
    public static boolean isSpeechProtectionEnabled(Context context) { return preferences(context).getBoolean(PRESERVE_SPEECH, true); }
    public static void setSpeechProtectionEnabled(Context context, boolean value) { preferences(context).edit().putBoolean(PRESERVE_SPEECH, value).apply(); }
    public static boolean isAlarmBlockerEnabled(Context context) { return preferences(context).getBoolean(ALARM_BLOCKER, false); }
    public static void setAlarmBlockerEnabled(Context context, boolean value) { preferences(context).edit().putBoolean(ALARM_BLOCKER, value).apply(); }
    public static int getSuddenSoundReduction(Context context) { return preferences(context).getInt(SUDDEN_REDUCTION, 50); }
    public static void setSuddenSoundReduction(Context context, int value) { setInt(context, SUDDEN_REDUCTION, value, 0, 90); }
    public static int getSensitivity(Context context) { return preferences(context).getInt(SENSITIVITY, 95); }
    public static void setSensitivity(Context context, int value) { setInt(context, SENSITIVITY, value, 0, 100); }
    public static int getTonalReduction(Context context) { return preferences(context).getInt(TONAL_REDUCTION, 99); }
    public static void setTonalReduction(Context context, int value) { setInt(context, TONAL_REDUCTION, value, 0, 100); }
    public static int getMinimumFrequency(Context context) { return preferences(context).getInt(MINIMUM_FREQUENCY, 1000); }
    public static void setMinimumFrequency(Context context, int value) { setInt(context, MINIMUM_FREQUENCY, value, 1000, 5000); }
    public static int getReleaseDuration(Context context) { return preferences(context).getInt(RELEASE_DURATION, 110); }
    public static void setReleaseDuration(Context context, int value) { setInt(context, RELEASE_DURATION, value, 40, 250); }
    private static void setInt(Context context, String key, int value, int minimum, int maximum) {
        preferences(context).edit().putInt(key, Math.max(minimum, Math.min(maximum, value))).apply();
    }
    public static void resetProfile(Context context) {
        SharedPreferences.Editor editor = preferences(context).edit().putBoolean(EQ_ENABLED, false);
        for (int index = 0; index < DEFAULT_REDUCTIONS.length; index++) {
            editor.remove(REDUCTION_PREFIX + index);
        }
        editor.remove(BEEP_BLOCKER).remove(STRENGTH).remove(PRESERVE_SPEECH)
                .remove(ALARM_BLOCKER).remove(SUDDEN_REDUCTION).remove(SENSITIVITY)
                .remove(TONAL_REDUCTION).remove(MINIMUM_FREQUENCY).remove(RELEASE_DURATION);
        editor.apply();
    }
}
