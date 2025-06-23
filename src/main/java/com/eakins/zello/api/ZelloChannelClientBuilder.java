// ZelloChannelClientBuilder.java
package com.eakins.zello.api;

import com.eakins.zello.api.event.ZelloMessageListener;
import com.eakins.zello.api.model.AudioFormat;
import com.eakins.zello.api.model.VoxMode;
import com.eakins.zello.api.ptt.CustomPttHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder class for constructing ZelloChannelClient instances.
 * Provides a fluent API for configuring various client parameters.
 */
public class ZelloChannelClientBuilder {
    String serverUri;
    String username;
    String password;
    String accessToken;
    String channelName;
    AudioFormat audioFormat;
    VoxMode voxMode;
    CustomPttHandler customPttHandler;
    List<ZelloMessageListener> messageListeners = new ArrayList<>();

    /**
     * Sets the WebSocket server URI for the Zello API.
     * Example: "wss://channels.zello.com/ws"
     *
     * @param serverUri The server URI.
     * @return The builder instance.
     */
    public ZelloChannelClientBuilder serverUri(String serverUri) {
        this.serverUri = serverUri;
        return this;
    }

    /**
     * Sets credentials (username and password) for authentication.
     * This is typically used for Zello Work accounts.
     * Cannot be used with accessToken simultaneously.
     *
     * @param username The Zello username.
     * @param password The Zello password.
     * @return The builder instance.
     */
    public ZelloChannelClientBuilder credentials(String username, String password) {
        if (this.accessToken != null) {
            throw new IllegalArgumentException("Cannot use both credentials and accessToken for authentication.");
        }
        this.username = username;
        this.password = password;
        return this;
    }

    /**
     * Sets an access token (JWT) for authentication.
     * This is typically used for anonymous or consumer Zello accounts.
     * Cannot be used with username/password simultaneously.
     *
     * @param accessToken The JWT access token.
     * @return The builder instance.
     */
    public ZelloChannelClientBuilder accessToken(String accessToken) {
        if (this.username != null || this.password != null) {
            throw new IllegalArgumentException("Cannot use both credentials and accessToken for authentication.");
        }
        this.accessToken = accessToken;
        return this;
    }

    /**
     * Sets the name of the Zello channel to connect to.
     *
     * @param channelName The name of the channel.
     * @return The builder instance.
     */
    public ZelloChannelClientBuilder channel(String channelName) {
        this.channelName = channelName;
        return this;
    }

    /**
     * Sets the audio format for microphone capture and speaker playback.
     * Zello Opus typically uses 16kHz sample rate, 16-bit, mono audio.
     *
     * @param encoding The audio encoding (e.g., AudioFormat.Encoding.PCM_SIGNED).
     * @param sampleRate The sample rate in Hz (e.g., 16000).
     * @param sampleSizeInBits The number of bits per sample (e.g., 16).
     * @param channels The number of channels (e.g., 1 for mono).
     * @return The builder instance.
     */
    public ZelloChannelClientBuilder audioFormat(AudioFormat.Encoding encoding, float sampleRate, int sampleSizeInBits, int channels) {
        this.audioFormat = new AudioFormat(encoding, sampleRate, sampleSizeInBits, channels);
        return this;
    }

    /**
     * Enables Voice Activity Detection (VOX) and sets its sensitivity mode.
     * When enabled, audio streaming will automatically start and stop based on speech detection.
     *
     * @param voxMode The desired VOX sensitivity mode.
     * @return The builder instance.
     */
    public ZelloChannelClientBuilder enableVox(VoxMode voxMode) {
        this.voxMode = voxMode;
        return this;
    }

    /**
     * Provides a custom implementation for Push-To-Talk control.
     * If set, your application will be responsible for calling ZelloChannelClient.startPushToTalk()
     * and ZelloChannelClient.stopPushToTalk() based on your custom logic (e.g., UI button events).
     *
     * @param handler An implementation of the CustomPttHandler interface.
     * @return The builder instance.
     */
    public ZelloChannelClientBuilder withCustomPttHandler(CustomPttHandler handler) {
        this.customPttHandler = handler;
        return this;
    }

    /**
     * Adds a listener to receive messages and status updates from the Zello channel.
     * Multiple listeners can be added.
     *
     * @param listener An implementation of the ZelloMessageListener interface.
     * @return The builder instance.
     */
    public ZelloChannelClientBuilder addMessageListener(ZelloMessageListener listener) {
        if (listener != null) {
            this.messageListeners.add(listener);
        }
        return this;
    }

    /**
     * Builds and returns a new ZelloChannelClient instance with the configured parameters.
     *
     * @return A new ZelloChannelClient.
     * @throws IllegalArgumentException If required parameters are missing or invalid.
     */
    public ZelloChannelClient build() {
        if (serverUri == null || serverUri.isEmpty()) {
            throw new IllegalArgumentException("Server URI must be specified.");
        }
        if (channelName == null || channelName.isEmpty()) {
            throw new IllegalArgumentException("Channel name must be specified.");
        }
        if ((username == null || password == null) && accessToken == null) {
            throw new IllegalArgumentException("Either username/password or an access token must be provided for authentication.");
        }
        if (audioFormat == null) {
            // Default audio format if not specified, matching Zello's typical Opus settings
            this.audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 16000, 16, 1);
            System.out.println("Audio format not specified. Using default: PCM_SIGNED, 16kHz, 16-bit, mono.");
        }
        return new ZelloChannelClient(this);
    }
}
