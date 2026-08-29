package com.johnbieniek.sonicshielding;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
public final class ProfileMathTest {
    @Test public void reductionMapsAcrossSupportedRange() {
        assertEquals(0, ProfileMath.reductionToBandLevel(0, (short) -1500, (short) 0));
        assertEquals(-750, ProfileMath.reductionToBandLevel(50, (short) -1500, (short) 0));
        assertEquals(-1500, ProfileMath.reductionToBandLevel(100, (short) -1500, (short) 0));
    }
    @Test public void reductionIsClamped() {
        assertEquals(0, ProfileMath.reductionToBandLevel(-20, (short) -1200, (short) 0));
        assertEquals(-1200, ProfileMath.reductionToBandLevel(140, (short) -1200, (short) 0));
    }
    @Test public void findsClosestFrequency() {
        assertEquals(0, ProfileMath.closestFrequencyIndex(60, new int[]{63, 125, 250}));
        assertEquals(2, ProfileMath.closestFrequencyIndex(220, new int[]{63, 125, 250}));
    }
    @Test public void iconIsRedOnlyWhenBothProtectionsAreOff() {
        assertEquals(false, ProfileMath.shouldUseActiveIcon(false, false));
        assertEquals(true, ProfileMath.shouldUseActiveIcon(true, false));
        assertEquals(true, ProfileMath.shouldUseActiveIcon(false, true));
        assertEquals(true, ProfileMath.shouldUseActiveIcon(true, true));
    }
    @Test public void beepAndAlarmProfilesNeverCausePermanentFiltering() {
        assertEquals(false, ProfileMath.shouldApplyPermanentFiltering(false, false));
        assertEquals(false, ProfileMath.shouldApplyPermanentFiltering(true, false));
        assertEquals(true, ProfileMath.shouldApplyPermanentFiltering(false, true));
        assertEquals(true, ProfileMath.shouldApplyPermanentFiltering(true, true));
    }
    @Test public void beepBlockerFallsBackOnlyWithoutCapture() {
        assertEquals(false, ProfileMath.shouldApplyFallbackFiltering(false, false));
        assertEquals(true, ProfileMath.shouldApplyFallbackFiltering(true, false));
        assertEquals(false, ProfileMath.shouldApplyFallbackFiltering(true, true));
    }
}
