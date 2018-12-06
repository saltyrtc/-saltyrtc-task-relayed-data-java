package org.saltyrtc.tasks.relayeddata;

/**
 * Handle incoming messages.
 */
public interface MessageHandler {
    void onData(Object data);
}
