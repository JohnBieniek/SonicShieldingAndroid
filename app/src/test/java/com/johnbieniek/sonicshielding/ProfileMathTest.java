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
}

