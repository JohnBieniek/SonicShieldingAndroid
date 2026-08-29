package com.johnbieniek.sonicshielding;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public final class ToneDetectorTest {
    private static final int RATE = 48000;

    @Test public void stableElectronicToneIsDetectedAfterConsecutiveFrames() {
        ToneDetector detector = new ToneDetector(RATE);
        short[] tone = sine(4000, 0.35);
        ToneDetector.Detection result = null;
        for (int frame = 0; frame < 6; frame++) {
            result = detector.analyze(tone, 1000, 95, true, false);
        }
        assertTrue(result.active());
        assertTrue(Math.abs(result.frequenciesHz.get(0) - 4000) < 80);
    }

    @Test public void silenceNeverActivatesProtection() {
        ToneDetector detector = new ToneDetector(RATE);
        assertFalse(detector.analyze(new short[ToneDetector.FFT_SIZE],
                1000, 100, false, true).active());
    }

    @Test public void alarmRequiresSeveralStablePeaksWhenSpeechSafe() {
        ToneDetector detector = new ToneDetector(RATE);
        short[] alarm = mixed(2200, 5200, 8200);
        ToneDetector.Detection result = null;
        for (int frame = 0; frame < 14; frame++) {
            result = detector.analyze(alarm, 1000, 95, true, true);
        }
        assertTrue(result.alarm);
    }

    @Test public void shortSpeechLikeHarmonicsDoNotOpenProtection() {
        ToneDetector detector = new ToneDetector(RATE);
        short[] voiced = mixed(1200, 2400, 3600, 4800);
        for (int frame = 0; frame < 5; frame++) {
            assertFalse(detector.analyze(voiced, 1000, 100, true, true).active());
        }
    }

    private static short[] sine(int frequency, double amplitude) {
        short[] output = new short[ToneDetector.FFT_SIZE];
        for (int i = 0; i < output.length; i++) output[i] = (short) (Short.MAX_VALUE
                * amplitude * Math.sin(2 * Math.PI * frequency * i / RATE));
        return output;
    }

    private static short[] mixed(int... frequencies) {
        short[] output = new short[ToneDetector.FFT_SIZE];
        for (int i = 0; i < output.length; i++) {
            double sample = 0;
            for (int frequency : frequencies) sample += Math.sin(2 * Math.PI * frequency * i / RATE);
            output[i] = (short) (Short.MAX_VALUE * 0.18 * sample);
        }
        return output;
    }
}
