// CustomPttHandler.java
package com.eakins.zello.api.ptt;

import com.eakins.zello.api.ZelloChannelClient;

/**
 * Interface for implementing custom Push-To-Talk (PTT) logic.
 * Developers can implement this to integrate their application's UI or hardware
 * events with the ZelloChannelClient's PTT functionality.
 *
 * The implementation of this interface would be responsible for calling
 * {@code ZelloChannelClient.startPushToTalk()} and {@code ZelloChannelClient.stopPushToTalk()}
 * based on its own event detection (e.g., button presses, external signals).
 */
public interface CustomPttHandler {

    /**
     * Initializes the custom PTT handler. This method can be used to set up
     * event listeners or connect to hardware.
     *
     * @param client The ZelloChannelClient instance that this handler will control.
     */
    default void initialize(ZelloChannelClient client) {
        // Default empty implementation
    }

    /**
     * Cleans up any resources used by the custom PTT handler.
     * This method should be called when the ZelloChannelClient is disconnected.
     */
    default void cleanup() {
        // Default empty implementation
    }

    // No specific methods like 'onPttPress' are defined here, as the handler
    // is expected to *call* the ZelloChannelClient's methods based on its
    // internal event loop or listeners. This provides maximum flexibility.
}
