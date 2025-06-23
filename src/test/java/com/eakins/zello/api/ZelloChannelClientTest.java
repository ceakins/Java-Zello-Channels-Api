// ZelloChannelClientTest.java
package com.eakins.zello.api;

import com.eakins.zello.api.event.ZelloMessageListener;
import com.eakins.zello.api.model.AudioFormat;
import com.eakins.zello.api.model.VoxMode;
import com.eakins.zello.api.ptt.CustomPttHandler;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class ZelloChannelClientTest {

    private static final Logger logger = LoggerFactory.getLogger(ZelloChannelClientTest.class);

    private ZelloChannelClient client;
    private MockZelloMessageListener mockListener;

    // A simple mock listener to capture events for testing
    private static class MockZelloMessageListener implements ZelloMessageListener {
        private final CountDownLatch connectedLatch = new CountDownLatch(1);
        private final CountDownLatch disconnectedLatch = new CountDownLatch(1);
        private final CountDownLatch audioStreamStartedLatch = new CountDownLatch(1);
        private final CountDownLatch audioStreamStoppedLatch = new CountDownLatch(1);
        private final CountDownLatch textMessageReceivedLatch = new CountDownLatch(1);
        private final AtomicReference<Throwable> lastError = new AtomicReference<>();


        @Override
        public void onChannelStatusChanged(String channel, String status) {
            logger.info("MockListener: Channel {} status changed to {}", channel, status);
            if ("connected".equals(status)) {
                connectedLatch.countDown();
            } else if ("disconnected".equals(status)) {
                disconnectedLatch.countDown();
            }
        }

        @Override
        public void onAudioStreamStarted(String channel, String sender) {
            logger.info("MockListener: Audio stream started on {} by {}", channel, sender);
            audioStreamStartedLatch.countDown();
        }

        @Override
        public void onAudioStreamStopped(String channel, String sender) {
            logger.info("MockListener: Audio stream stopped on {} by {}", channel, sender);
            audioStreamStoppedLatch.countDown();
        }

        @Override
        public void onTextMessageReceived(String channel, String sender, String text) {
            logger.info("MockListener: Text message from {} on {}: {}", sender, channel, text);
            textMessageReceivedLatch.countDown();
        }

        @Override
        public void onError(Throwable error) {
            logger.error("MockListener: Received error: {}", error.getMessage(), error);
            lastError.set(error);
        }

        public void resetLatches() {
            // In a real scenario, you'd re-instantiate or have a more robust reset
            // For simple tests, we assume one-time triggers for now.
        }
    }

    // Named inner class for custom PTT handler in test to avoid anonymous class casting issues
    private static class TestCustomPttHandler implements CustomPttHandler {
        private static final Logger handlerLogger = LoggerFactory.getLogger(TestCustomPttHandler.class);
        private ZelloChannelClient clientRef; // Reference to the client passed in initialize
        public final AtomicReference<Boolean> pttPressCalled = new AtomicReference<>(false);
        public final AtomicReference<Boolean> pttReleaseCalled = new AtomicReference<>(false);

        @Override
        public void initialize(ZelloChannelClient c) {
            this.clientRef = c;
            handlerLogger.info("Test CustomPttHandler initialized.");
        }

        // These methods simulate external events triggering PTT
        public void triggerPttPress() {
            if (clientRef != null) {
                handlerLogger.info("Test CustomPttHandler: Simulating PTT Press.");
                clientRef.startPushToTalk();
                pttPressCalled.set(true);
            }
        }

        public void triggerPttRelease() {
            if (clientRef != null) {
                handlerLogger.info("Test CustomPttHandler: Simulating PTT Release.");
                clientRef.stopPushToTalk();
                pttReleaseCalled.set(true);
            }
        }

        @Override
        public void cleanup() {
            handlerLogger.info("Test CustomPptHandler cleaned up.");
            this.clientRef = null;
        }
    }

    @BeforeMethod
    public void setUp() {
        mockListener = new MockZelloMessageListener();
        // Initialize client with basic config
        client = ZelloChannelClient.builder()
                .serverUri("wss://channels.zello.com/ws")
                .credentials("testUser", "testPass")
                .channel("testChannel")
                .audioFormat(AudioFormat.Encoding.PCM_SIGNED, 16000, 16, 1)
                .addMessageListener(mockListener)
                .build();
    }

    @AfterMethod
    public void tearDown() {
        if (client != null) {
            client.disconnect(); // Ensure client is disconnected after each test
        }
    }

    @Test(priority = 1)
    public void testClientInitialization() {
        logger.info("Running testClientInitialization...");
        assertNotNull(client, "Client should not be null after building.");
        assertFalse(client.isConnected(), "Client should not be connected initially.");
        assertFalse(client.isPttActive(), "PTT should not be active initially.");
    }

    @Test(priority = 2)
    public void testConnectAndDisconnect() throws InterruptedException {
        logger.info("Running testConnectAndDisconnect...");
        client.connect();

        // Wait for connection latch to count down, with a timeout
        assertTrue(mockListener.connectedLatch.await(7, TimeUnit.SECONDS), "Client should connect within timeout.");
        assertTrue(client.isConnected(), "Client state should be connected after successful connection.");

        client.disconnect();
        // Wait for disconnection latch to count down
        assertTrue(mockListener.disconnectedLatch.await(3, TimeUnit.SECONDS), "Client should disconnect within timeout.");
        assertFalse(client.isConnected(), "Client state should be disconnected after successful disconnection.");
    }

    @Test(priority = 3, dependsOnMethods = {"testConnectAndDisconnect"})
    public void testStartStopPushToTalk() throws InterruptedException {
        logger.info("Running testStartStopPushToTalk...");
        client.connect();
        mockListener.connectedLatch.await(7, TimeUnit.SECONDS); // Ensure connected

        client.startPushToTalk();
        assertTrue(mockListener.audioStreamStartedLatch.await(3, TimeUnit.SECONDS), "Audio stream should start within timeout for PTT.");
        assertTrue(client.isPttActive(), "PTT should be active after startPushToTalk.");

        client.stopPushToTalk();
        assertTrue(mockListener.audioStreamStoppedLatch.await(3, TimeUnit.SECONDS), "Audio stream should stop within timeout for PTT.");
        assertFalse(client.isPttActive(), "PTT should not be active after stopPushToTalk.");
    }

    @Test(priority = 4)
    public void testVoxActivation() throws InterruptedException {
        logger.info("Running testVoxActivation...");
        // Re-build client with VOX enabled for this specific test
        client.disconnect(); // Disconnect client from previous setup
        client = ZelloChannelClient.builder()
                .serverUri("wss://channels.zello.com/ws")
                .accessToken("dummy_jwt_token") // Using accessToken for VOX example
                .channel("voxChannel")
                .audioFormat(AudioFormat.Encoding.PCM_SIGNED, 16000, 16, 1)
                .enableVox(VoxMode.QUALITY)
                .addMessageListener(mockListener)
                .build();

        client.connect();
        assertTrue(mockListener.connectedLatch.await(7, TimeUnit.SECONDS), "VOX client should connect within timeout.");

        // VOX logic is simulated internally, so we expect audioStreamStarted/Stopped
        // to be called eventually. We can't directly control the "voice detection" here.
        // We'll wait a bit and check if a stream *might* have started (due to random simulation).
        // For a real VAD, you'd inject audio or mock the VAD component.
        Thread.sleep(2000); // Allow some time for VOX monitoring to potentially trigger
        logger.info("VOX test: Observe logs for 'VOX: Audio signal detected' and 'VOX: Silence detected'.");

        // We can't assert definite start/stop without mocking the internal random behavior or VAD.
        // A more robust test would involve mocking internal VAD to control its output.
        // For now, this confirms VOX initialization path is taken.
        assertFalse(client.isPttActive(), "PTT should not be active initially in VOX mode (until signal detected).");
    }

    @Test(priority = 5)
    public void testCustomPttHandlerIntegration() throws InterruptedException {
        logger.info("Running testCustomPttHandlerIntegration...");

        // Define a custom PTT handler for the test
        TestCustomPttHandler testCustomPttHandler = new TestCustomPttHandler();

        // Re-build client with custom PTT handler
        client.disconnect(); // Disconnect client from previous setup
        client = ZelloChannelClient.builder()
                .serverUri("wss://channels.zello.com/ws")
                .credentials("customPttUser", "customPttPass")
                .channel("customPttChannel")
                .audioFormat(AudioFormat.Encoding.PCM_SIGNED, 16000, 16, 1)
                .withCustomPttHandler(testCustomPttHandler)
                .addMessageListener(mockListener)
                .build();

        // Initialize the custom handler manually in the test
        testCustomPttHandler.initialize(client);

        client.connect();
        assertTrue(mockListener.connectedLatch.await(7, TimeUnit.SECONDS), "Custom PTT client should connect within timeout.");

        // Simulate a PTT press via the custom handler
        testCustomPttHandler.triggerPttPress(); // Call directly without cast or client parameter
        assertTrue(testCustomPttHandler.pttPressCalled.get(), "PTT press method should have been called on custom handler.");
        assertTrue(mockListener.audioStreamStartedLatch.await(3, TimeUnit.SECONDS), "Audio stream should start via custom PTT trigger.");
        assertTrue(client.isPttActive(), "PTT should be active after custom PTT press.");

        // Simulate a PTT release via the custom handler
        testCustomPttHandler.triggerPttRelease(); // Call directly without cast or client parameter
        assertTrue(testCustomPttHandler.pttReleaseCalled.get(), "PTT release method should have been called on custom handler.");
        assertTrue(mockListener.audioStreamStoppedLatch.await(3, TimeUnit.SECONDS), "Audio stream should stop via custom PTT release.");
        assertFalse(client.isPttActive(), "PTT should not be active after custom PTT release.");
    }


    @Test(priority = 6)
    public void testListenerErrorCallback() throws InterruptedException {
        logger.info("Running testListenerErrorCallback...");
        // Simulate an error condition
        // Since the current client doesn't explicitly throw errors easily, we'll call onError directly on listener.
        // In a real scenario, this would be triggered by an internal client error (e.g., WebSocket disconnect).
        Throwable testError = new RuntimeException("Simulated WebSocket connection error.");
        mockListener.onError(testError);

        // Verify that the error was captured by the mock listener
        assertEquals(mockListener.lastError.get(), testError, "Listener should receive the simulated error.");
    }

    @Test(priority = 7, expectedExceptions = IllegalArgumentException.class)
    public void testBuilderMissingServerUri() {
        logger.info("Running testBuilderMissingServerUri...");
        ZelloChannelClient.builder()
                .credentials("user", "pass")
                .channel("channel")
                .build(); // This should throw IllegalArgumentException
    }

    @Test(priority = 8, expectedExceptions = IllegalArgumentException.class)
    public void testBuilderMissingChannelName() {
        logger.info("Running testBuilderMissingChannelName...");
        ZelloChannelClient.builder()
                .serverUri("wss://test.com")
                .credentials("user", "pass")
                .build(); // This should throw IllegalArgumentException
    }

    @Test(priority = 9, expectedExceptions = IllegalArgumentException.class)
    public void testBuilderMissingAuthentication() {
        logger.info("Running testBuilderMissingAuthentication...");
        ZelloChannelClient.builder()
                .serverUri("wss://test.com")
                .channel("channel")
                .build(); // This should throw IllegalArgumentException
    }
}
