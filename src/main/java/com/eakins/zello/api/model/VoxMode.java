// VoxMode.java
package com.eakins.zello.api.model;

/**
 * Defines the sensitivity modes for Voice Activity Detection (VOX).
 * These modes influence how aggressively speech is detected, impacting
 * the balance between sensitivity and false positives.
 *
 * Corresponds to WebRTC VAD modes or similar configurations.
 */
public enum VoxMode {
    /**
     * Mode 0: "Quality" - Less aggressive, higher recall (more likely to detect all speech),
     * but potentially more false positives (transmitting more background noise).
     * Suitable for quiet environments where capturing every word is critical.
     */
    QUALITY(0),

    /**
     * Mode 1: "Low-bitrate" - A balanced mode, often optimized for lower bandwidth usage,
     * which might imply a slightly more aggressive detection than 'QUALITY'.
     */
    LOW_BITRATE(1),

    /**
     * Mode 2: "Aggressive" - More aggressive, higher precision (less likely to transmit noise),
     * but potentially lower recall (missing some subtle speech starts).
     * Good for moderately noisy environments.
     */
    AGGRESSIVE(2),

    /**
     * Mode 3: "Very Aggressive" - Most aggressive, highest precision, lowest recall.
     * Designed for very noisy environments where only strong, clear speech should trigger transmission.
     */
    VERY_AGGRESSIVE(3);

    private final int value;

    VoxMode(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
