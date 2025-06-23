// AudioFormat.java
package com.eakins.zello.api.model;

/**
 * Represents the audio format used for capture and playback.
 * This corresponds to javax.sound.sampled.AudioFormat but simplified for API configuration.
 */
public class AudioFormat {
    private final Encoding encoding;
    private final float sampleRate;
    private final int sampleSizeInBits;
    private final int channels;
    private final boolean bigEndian; // Typically false for PCM_SIGNED in Java, but kept for completeness
    private final int frameSize; // Number of bytes per frame
    private final float frameRate; // Number of frames per second

    // Zello's packet duration can range from 2.5ms to 60ms.
    // For Opus, common frame sizes are 2.5, 5, 10, 20, 40, 60 ms.
    // We'll use a default if not specified, or it can be derived from the
    // audio data received (e.g., from codec_header).
    private int packetDurationMs = 20; // Default Opus packet duration

    /**
     * Represents the encoding of the audio data.
     */
    public enum Encoding {
        PCM_SIGNED,
        PCM_UNSIGNED,
        ULAW,
        ALAW
        // Add other relevant encodings if necessary
    }

    public AudioFormat(Encoding encoding, float sampleRate, int sampleSizeInBits, int channels) {
        this(encoding, sampleRate, sampleSizeInBits, channels, false); // Default to little-endian
    }

    public AudioFormat(Encoding encoding, float sampleRate, int sampleSizeInBits, int channels, boolean bigEndian) {
        if (encoding == null) {
            throw new IllegalArgumentException("Encoding cannot be null.");
        }
        if (sampleRate <= 0 || sampleSizeInBits <= 0 || channels <= 0) {
            throw new IllegalArgumentException("Sample rate, sample size, and channels must be positive.");
        }

        this.encoding = encoding;
        this.sampleRate = sampleRate;
        this.sampleSizeInBits = sampleSizeInBits;
        this.channels = channels;
        this.bigEndian = bigEndian;
        this.frameSize = (sampleSizeInBits / 8) * channels;
        this.frameRate = sampleRate / frameSize; // This might be more complex for variable frame sizes

        // Set a reasonable default packet duration if not explicitly configured
        // This is primarily for the *sending* side's internal buffering logic
        // The *receiving* side will interpret packet duration from the Opus codec header
    }

    public Encoding getEncoding() {
        return encoding;
    }

    public float getSampleRate() {
        return sampleRate;
    }

    public int getSampleSizeInBits() {
        return sampleSizeInBits;
    }

    public int getChannels() {
        return channels;
    }

    public boolean isBigEndian() {
        return bigEndian;
    }

    public int getFrameSize() {
        return frameSize;
    }

    public float getFrameRate() {
        return frameRate;
    }

    public int getPacketDurationMs() {
        return packetDurationMs;
    }

    /**
     * Sets the expected packet duration in milliseconds. This is primarily for
     * managing the size of audio buffers during capture/encoding.
     * Zello supports 2.5ms to 60ms.
     *
     * @param packetDurationMs The duration of each audio packet in milliseconds.
     */
    public void setPacketDurationMs(int packetDurationMs) {
        if (packetDurationMs < 2.5 || packetDurationMs > 60) {
            throw new IllegalArgumentException("Packet duration must be between 2.5 and 60 ms.");
        }
        this.packetDurationMs = packetDurationMs;
    }

    @Override
    public String toString() {
        return "AudioFormat{" +
                "encoding=" + encoding +
                ", sampleRate=" + sampleRate +
                ", sampleSizeInBits=" + sampleSizeInBits +
                ", channels=" + channels +
                ", bigEndian=" + bigEndian +
                ", frameSize=" + frameSize +
                ", frameRate=" + frameRate +
                ", packetDurationMs=" + packetDurationMs +
                '}';
    }
}
