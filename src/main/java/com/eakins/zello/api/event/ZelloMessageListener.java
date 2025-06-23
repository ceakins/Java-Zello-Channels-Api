// ZelloMessageListener.java
package com.eakins.zello.api.event;

// import com.eakins.zello.api.ZelloChannelClient; // No longer strictly needed if not directly used in default methods

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Interface for receiving various messages and status updates from the Zello channel.
 * Implement this interface to handle incoming text messages, audio streams,
 * and channel status changes.
 */
public interface ZelloMessageListener {

    Logger logger = LoggerFactory.getLogger(ZelloMessageListener.class);

    /**
     * Called when a text message is received on a channel.
     *
     * @param channel The name of the channel the message was received on.
     * @param sender The username of the sender.
     * @param text The content of the text message.
     */
    default void onTextMessageReceived(String channel, String sender, String text) {
        // Default empty implementation
    }

    /**
     * Called when the status of a channel changes (e.g., "connected", "disconnected", "error").
     *
     * @param channel The name of the channel whose status changed.
     * @param status A string indicating the new status.
     */
    default void onChannelStatusChanged(String channel, String status) {
        // Default empty implementation
    }

    /**
     * Called when an audio stream from another user starts on a channel.
     * This indicates that a user has started speaking.
     *
     * @param channel The name of the channel.
     * @param sender The username of the sender whose audio stream started.
     */
    default void onAudioStreamStarted(String channel, String sender) {
        // Default empty implementation
    }

    /**
     * Called when an audio stream from another user stops on a channel.
     * This indicates that a user has stopped speaking.
     *
     * @param channel The name of the channel.
     * @param sender The username of the sender whose audio stream stopped.
     */
    default void onAudioStreamStopped(String channel, String sender) {
        // Default empty implementation
    }

    /**
     * Called when a segment of incoming audio data is received.
     * This data would typically be Opus-encoded and needs to be decoded for playback.
     *
     * @param channel The name of the channel the audio was received on.
     * @param sender The username of the sender of the audio.
     * @param audioData The raw byte array of the received audio segment (Opus encoded).
     */
    default void onAudioStreamReceived(String channel, String sender, byte[] audioData) {
        // Default empty implementation
    }

    /**
     * Called when an error occurs within the ZelloChannelClient.
     *
     * @param error The Throwable representing the error.
     */
    default void onError(Throwable error) {
        // Default implementation logs the error using SLF4J
        logger.error("ZelloClient Error: {}", error.getMessage(), error);
    }
}
