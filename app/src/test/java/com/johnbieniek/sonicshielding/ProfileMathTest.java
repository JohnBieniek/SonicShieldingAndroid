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
    @Test public void speechProtectionCapsProtectedSpeechBands() {
        assertEquals(35, ProfileMath.effectiveReduction(2000, 0, false, true, 1000, 99, true, false));
        assertEquals(100, ProfileMath.effectiveReduction(8000, 0, false, true, 1000, 99, true, true));
        assertEquals(0, ProfileMath.effectiveReduction(500, 0, false, true, 1000, 99, true, true));
    }
    @Test public void twentyFiveIsOldMaximumAndOneHundredUsesFourStages() {
        assertEquals(100, ProfileMath.scaledProtectionReduction(8000, 25, false));
        assertEquals(0, ProfileMath.protectionStageReduction(8000, true, 1000, 25, false, 1));
        assertEquals(100, ProfileMath.scaledProtectionReduction(8000, 100, false));
        assertEquals(100, ProfileMath.protectionStageReduction(8000, true, 1000, 50, false, 1));
        assertEquals(0, ProfileMath.protectionStageReduction(8000, true, 1000, 50, false, 2));
        assertEquals(100, ProfileMath.protectionStageReduction(8000, true, 1000, 100, false, 1));
        assertEquals(100, ProfileMath.protectionStageReduction(8000, true, 1000, 100, false, 2));
        assertEquals(100, ProfileMath.protectionStageReduction(8000, true, 1000, 100, false, 3));
        assertEquals(0, ProfileMath.protectionStageReduction(2000, true, 1000, 100, true, 1));
    }
    @Test public void iconIsRedOnlyWhenBothProtectionsAreOff() {
        assertEquals(false, ProfileMath.shouldUseActiveIcon(false, false));
        assertEquals(true, ProfileMath.shouldUseActiveIcon(true, false));
        assertEquals(true, ProfileMath.shouldUseActiveIcon(false, true));
        assertEquals(true, ProfileMath.shouldUseActiveIcon(true, true));
    }
}
