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

    private ShieldPreferences() {}
    private static SharedPreferences preferences(Context context) { return context.getSharedPreferences(FILE, Context.MODE_PRIVATE); }
    public static boolean isEnabled(Context context) { return preferences(context).getBoolean(ENABLED, false); }
    public static void setEnabled(Context context, boolean enabled) { preferences(context).edit().putBoolean(ENABLED, enabled).apply(); }
    public static boolean isShieldEnabled(Context context) { return isEnabled(context); }
    public static void setShieldEnabled(Context context, boolean enabled) { setEnabled(context, enabled); }
    public static boolean isComfortEqEnabled(Context context) { return preferences(context).getBoolean(EQ_ENABLED, true); }
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
    public static void resetProfile(Context context) {
        SharedPreferences.Editor editor = preferences(context).edit().putBoolean(EQ_ENABLED, false);
        for (int index = 0; index < DEFAULT_REDUCTIONS.length; index++) {
            editor.remove(REDUCTION_PREFIX + index);
        }
        editor.apply();
    }
}
