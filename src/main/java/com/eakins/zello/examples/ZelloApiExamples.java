// ZelloApiExamples.java
package com.eakins.zello.examples;

import com.eakins.zello.api.ZelloChannelClient;
import com.eakins.zello.api.model.AudioFormat;
import com.eakins.zello.api.model.VoxMode; // Enum for VAD modes like AGGRESSIVE, QUALITY
import com.eakins.zello.api.event.ZelloMessageListener;
import com.eakins.zello.api.ptt.CustomPttHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZelloApiExamples {

    private static final Logger logger = LoggerFactory.getLogger(ZelloApiExamples.class);

    public static void main(String[] args) throws Exception {

        // --- Example 1: Basic PTT (manual start/stop stream) ---
        logger.info("--- Running Basic PTT Example ---");
        ZelloChannelClient basicPttClient = ZelloChannelClient.builder()
                .serverUri("wss://channels.zello.com/ws")
                .credentials("myUser", "myPass") // Authenticate with username and password
                .channel("myChannel")
                .audioFormat(AudioFormat.Encoding.PCM_SIGNED, 16000, 16, 1) // Zello Opus typically uses 16kHz, 16-bit, mono audio
                .addMessageListener(new ZelloMessageListener() {
                    @Override
                    public void onTextMessageReceived(String channel, String sender, String text) {
                        logger.info("[BasicPttClient] Received Text from {} on {}: {}", sender, channel, text);
                    }
                    @Override
                    public void onChannelStatusChanged(String channel, String status) {
                        logger.info("[BasicPttClient] Channel {} status: {}", channel, status);
                    }
                    @Override
                    public void onAudioStreamReceived(String channel, String sender, byte[] audioData) {
                        // This method would internally handle Opus decoding and playback
                        logger.info("[BasicPttClient] Received audio from {} on {} (size: {} bytes)", sender, channel, audioData.length);
                    }
                    @Override
                    public void onAudioStreamStarted(String channel, String sender) {
                        logger.info("[BasicPttClient] Outgoing audio stream started.");
                    }
                    @Override
                    public void onAudioStreamStopped(String channel, String sender) {
                        logger.info("[BasicPttClient] Outgoing audio stream stopped.");
                    }
                })
                .build();

        basicPttClient.connect(); // Establish WebSocket connection and send logon command
        logger.info("Basic PTT Client connecting...");

        Thread.sleep(5000); // Simulate waiting for connection and logon completion

        if (basicPttClient.isConnected()) {
            logger.info("Starting Push-To-Talk (manual)...");
            basicPttClient.startPushToTalk(); // Begins capturing, encoding, and streaming audio
            Thread.sleep(7000); // Simulate 7 seconds of speaking
            logger.info("Stopping Push-To-Talk (manual)...");
            basicPttClient.stopPushToTalk();  // Stops audio capture and streaming by sending stop_stream command
        } else {
            logger.warn("Basic PTT Client failed to connect.");
        }


        Thread.sleep(2000); // Allow time for final packets to be sent/received
        basicPttClient.disconnect(); // Close the WebSocket connection gracefully
        logger.info("Basic PTT Client disconnected.");
        Thread.sleep(1000); // Give time for disconnect tasks to finish


        // --- Example 2: Connecting with VOX Feature ---
        logger.info("\n--- Running VOX Example ---");
        ZelloChannelClient voxClient = ZelloChannelClient.builder()
                .serverUri("wss://channels.zello.com/ws")
                .accessToken("your_jwt_token_here") // Authenticate with a JWT token for anonymous or consumer Zello
                .channel("publicChannel")
                .audioFormat(AudioFormat.Encoding.PCM_SIGNED, 16000, 16, 1)
                .enableVox(VoxMode.AGGRESSIVE) // Use AGGRESSIVE mode for higher precision, reducing false positives
                .addMessageListener(new ZelloMessageListener() {
                    @Override
                    public void onAudioStreamStarted(String channel, String sender) {
                        if (!sender.equals("anonymous")) { // Check if it's an incoming stream or our own VOX
                            logger.info("[VoxClient] Incoming VOX detected speech from {} on {}. Streaming started.", sender, channel);
                        } else {
                            logger.info("[VoxClient] Outgoing VOX detected speech. Streaming started.");
                        }
                    }
                    @Override
                    public void onAudioStreamStopped(String channel, String sender) {
                        if (!sender.equals("anonymous")) {
                            logger.info("[VoxClient] Incoming VOX detected silence from {} on {}. Streaming stopped.", sender, channel);
                        } else {
                            logger.info("[VoxClient] Outgoing VOX detected silence. Streaming stopped.");
                        }
                    }
                    @Override
                    public void onChannelStatusChanged(String channel, String status) {
                        logger.info("[VoxClient] Channel {} status: {}", channel, status);
                    }
                    @Override
                    public void onTextMessageReceived(String channel, String sender, String text) {
                        logger.info("[VoxClient] Received Text from {} on {}: {}", sender, channel, text);
                    }
                })
                .build();

        voxClient.connect();
        logger.info("VOX Client connecting with VOX enabled. Speak to start streaming (simulated).");
        // Audio streaming starts/stops automatically based on voice activity detection (simulated)
        Thread.sleep(15000); // Keep client running for a minute to observe VOX behavior
        if (voxClient.isConnected()) {
            logger.info("Simulating speaking for 5 seconds (VOX should activate).");
            // In a real scenario, you'd actually speak into the microphone.
            // Here, the internal VOX simulation in ZelloChannelClient will handle it.
            Thread.sleep(5000);
            logger.info("Simulating silence for 5 seconds (VOX should deactivate).");
            Thread.sleep(5000);
        } else {
            logger.warn("VOX Client failed to connect.");
        }
        voxClient.disconnect();
        logger.info("VOX Client disconnected.");
        Thread.sleep(1000);


        // --- Example 3: Custom PTT Implementation ---
        logger.info("\n--- Running Custom PTT Example ---");
        MyCustomPttHandler customPttHandler = new MyCustomPttHandler();
        ZelloChannelClient customPttClient = ZelloChannelClient.builder()
                .serverUri("wss://channels.zello.com/ws")
                .credentials("anotherUser", "anotherPass")
                .channel("privateGroup")
                .audioFormat(AudioFormat.Encoding.PCM_SIGNED, 16000, 16, 1)
                .withCustomPttHandler(customPttHandler) // Inject custom PTT logic
                .addMessageListener(new ZelloMessageListener() {
                    @Override
                    public void onChannelStatusChanged(String channel, String status) {
                        logger.info("[CustomPttClient] Channel {} status: {}", channel, status);
                    }
                    @Override
                    public void onAudioStreamStarted(String channel, String sender) {
                        logger.info("[CustomPttClient] Outgoing audio stream started by custom PTT.");
                    }
                    @Override
                    public void onAudioStreamStopped(String channel, String sender) {
                        logger.info("[CustomPttClient] Outgoing audio stream stopped by custom PTT.");
                    }
                    //... implement necessary event handlers
                })
                .build();

        customPttHandler.initialize(customPttClient); // Initialize the custom handler with the client

        customPttClient.connect();
        logger.info("Custom PTT Client connecting. Control streaming via MyCustomPttHandler.");

        Thread.sleep(5000); // Wait for connection

        if (customPttClient.isConnected()) {
            logger.info("Simulating external PTT activation (start)...");
            customPttHandler.simulatePttPress(); // Simulate PTT press
            Thread.sleep(7000); // Simulate speaking
            logger.info("Simulating external PTT deactivation (stop)...");
            customPttHandler.simulatePttRelease(); // Simulate PTT release
        } else {
            logger.warn("Custom PTT Client failed to connect.");
        }


        Thread.sleep(2000);
        customPttClient.disconnect();
        customPttHandler.cleanup(); // Clean up the custom handler
        logger.info("Custom PTT Client disconnected.");
    }

    // Custom PTT Handler implementation (nested for example, would be top-level in real project)
    static class MyCustomPttHandler implements CustomPttHandler {
        private static final Logger logger = LoggerFactory.getLogger(MyCustomPttHandler.class);
        private ZelloChannelClient client;

        @Override
        public void initialize(ZelloChannelClient client) {
            this.client = client;
            logger.info("MyCustomPttHandler initialized.");
            // In a real app, set up AWT/Swing/JavaFX listeners for a button here
            // e.g., myButton.addActionListener(e -> simulatePttPress());
        }

        // This method would typically be called by an external event listener (e.g., AWT, Swing, Android UI)
        public void simulatePttPress() { // Removed ZelloChannelClient client parameter as it's a field now
            if (client != null) {
                client.startPushToTalk(); // API method to start audio streaming
            }
        }

        public void simulatePttRelease() { // Removed ZelloChannelClient client parameter as it's a field now
            if (client != null) {
                client.stopPushToTalk(); // API method to stop audio streaming
            }
        }

        @Override
        public void cleanup() {
            logger.info("MyCustomPttHandler cleaned up.");
            this.client = null;
        }
    }
}
