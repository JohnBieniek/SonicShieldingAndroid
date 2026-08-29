package com.johnbieniek.sonicshielding;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Small, allocation-conscious spectral detector modeled after the browser extension. */
final class ToneDetector {
    static final int FFT_SIZE = 1024;
    private static final double MIN_RMS = 0.006;
    private final int sampleRate;
    private final double[] real = new double[FFT_SIZE];
    private final double[] imaginary = new double[FFT_SIZE];
    private int[] priorBins = new int[0];

    ToneDetector(int sampleRate) { this.sampleRate = sampleRate; }

    Detection analyze(short[] pcm, int minimumHz, int sensitivity, boolean speechSafe,
                      boolean aggressiveAlarm) {
        double squareSum = 0;
        for (int i = 0; i < FFT_SIZE; i++) {
            double sample = pcm[i] / 32768.0;
            squareSum += sample * sample;
            real[i] = sample * (0.5 - 0.5 * Math.cos(2 * Math.PI * i / (FFT_SIZE - 1)));
            imaginary[i] = 0;
        }
        if (Math.sqrt(squareSum / FFT_SIZE) < MIN_RMS) return remember(new int[0], false);
        fft(real, imaginary);
        int first = Math.max(2, minimumHz * FFT_SIZE / sampleRate);
        int last = Math.min(FFT_SIZE / 2 - 2, 16000 * FFT_SIZE / sampleRate);
        double thresholdDb = 12.0 + (50 - sensitivity) * 0.08 + (speechSafe ? 2.0 : 0.0);
        List<Peak> peaks = new ArrayList<>();
        for (int bin = first; bin <= last; bin++) {
            double magnitude = magnitude(bin);
            if (magnitude <= magnitude(bin - 1) || magnitude < magnitude(bin + 1)) continue;
            double neighborhood = 0;
            int count = 0;
            for (int offset = -8; offset <= 8; offset++) {
                if (Math.abs(offset) <= 1 || bin + offset < 1 || bin + offset >= FFT_SIZE / 2) continue;
                neighborhood += magnitude(bin + offset);
                count++;
            }
            double prominence = 20 * Math.log10((magnitude + 1e-9) / (neighborhood / count + 1e-9));
            if (prominence >= thresholdDb) peaks.add(new Peak(bin, prominence));
        }
        peaks.sort(Comparator.comparingDouble((Peak peak) -> peak.prominence).reversed());
        if (peaks.size() > 6) peaks = peaks.subList(0, 6);
        List<Integer> stableHz = new ArrayList<>();
        for (Peak peak : peaks) {
            boolean stable = peak.prominence >= 18;
            for (int old : priorBins) if (Math.abs(old - peak.bin) <= 2) stable = true;
            if (stable) stableHz.add(Math.round(peak.bin * sampleRate / (float) FFT_SIZE));
        }
        int[] current = new int[peaks.size()];
        for (int i = 0; i < peaks.size(); i++) current[i] = peaks.get(i).bin;
        boolean alarm = aggressiveAlarm && stableHz.size() >= (speechSafe ? 3 : 2)
                && hasHighPeak(stableHz, 5000);
        priorBins = current;
        return new Detection(stableHz, alarm);
    }

    private Detection remember(int[] bins, boolean alarm) {
        priorBins = bins;
        return new Detection(new ArrayList<>(), alarm);
    }
    private boolean hasHighPeak(List<Integer> frequencies, int minimum) {
        for (int value : frequencies) if (value >= minimum) return true;
        return false;
    }
    private double magnitude(int bin) { return Math.hypot(real[bin], imaginary[bin]); }

    private static void fft(double[] real, double[] imaginary) {
        int n = real.length;
        for (int i = 1, j = 0; i < n; i++) {
            int bit = n >> 1;
            for (; (j & bit) != 0; bit >>= 1) j ^= bit;
            j ^= bit;
            if (i < j) {
                double swap = real[i]; real[i] = real[j]; real[j] = swap;
                swap = imaginary[i]; imaginary[i] = imaginary[j]; imaginary[j] = swap;
            }
        }
        for (int length = 2; length <= n; length <<= 1) {
            double angle = -2 * Math.PI / length;
            for (int start = 0; start < n; start += length) {
                for (int offset = 0; offset < length / 2; offset++) {
                    double cos = Math.cos(angle * offset), sin = Math.sin(angle * offset);
                    int even = start + offset, odd = even + length / 2;
                    double tr = real[odd] * cos - imaginary[odd] * sin;
                    double ti = real[odd] * sin + imaginary[odd] * cos;
                    real[odd] = real[even] - tr; imaginary[odd] = imaginary[even] - ti;
                    real[even] += tr; imaginary[even] += ti;
                }
            }
        }
    }

    static final class Detection {
        final List<Integer> frequenciesHz;
        final boolean alarm;
        Detection(List<Integer> frequenciesHz, boolean alarm) {
            this.frequenciesHz = frequenciesHz;
            this.alarm = alarm;
        }
        boolean active() { return alarm || !frequenciesHz.isEmpty(); }
    }
    private static final class Peak {
        final int bin;
        final double prominence;
        Peak(int bin, double prominence) { this.bin = bin; this.prominence = prominence; }
    }
}
