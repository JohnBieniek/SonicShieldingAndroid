package com.johnbieniek.sonicshielding;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;

final class TonePlayer {
    private static final int SAMPLE_RATE = 44100;
    private AudioTrack activeTrack;

    void play(int frequencyHz) {
        stop();
        int samples = SAMPLE_RATE;
        short[] audio = new short[samples];
        int ramp = SAMPLE_RATE / 40;
        for (int index = 0; index < samples; index++) {
            double envelope = Math.min(1.0, Math.min(index / (double) ramp, (samples - index - 1) / (double) ramp));
            audio[index] = (short) (Math.sin(2.0 * Math.PI * frequencyHz * index / SAMPLE_RATE) * 5000 * envelope);
        }
        activeTrack = new AudioTrack.Builder()
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build())
                .setAudioFormat(new AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build())
                .setBufferSizeInBytes(audio.length * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build();
        activeTrack.write(audio, 0, audio.length);
        activeTrack.setNotificationMarkerPosition(audio.length - 1);
        activeTrack.setPlaybackPositionUpdateListener(new AudioTrack.OnPlaybackPositionUpdateListener() {
            @Override public void onMarkerReached(AudioTrack track) { stop(); }
            @Override public void onPeriodicNotification(AudioTrack track) {}
        });
        activeTrack.play();
    }

    void stop() {
        if (activeTrack != null) {
            try { activeTrack.stop(); } catch (IllegalStateException ignored) {}
            activeTrack.release();
            activeTrack = null;
        }
    }
}
