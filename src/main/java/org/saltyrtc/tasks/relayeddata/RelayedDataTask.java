/*
 * Copyright (c) 2018 Threema GmbH
 *
 * Licensed under the Apache License, Version 2.0, <see LICENSE-APACHE file>
 * or the MIT license <see LICENSE-MIT file>, at your option. This file may not be
 * copied, modified, or distributed except according to those terms.
 */

package org.saltyrtc.tasks.relayeddata;

import org.saltyrtc.client.annotations.NonNull;
import org.saltyrtc.client.annotations.Nullable;
import org.saltyrtc.client.exceptions.ConnectionException;
import org.saltyrtc.client.exceptions.SignalingException;
import org.saltyrtc.client.messages.c2c.TaskMessage;
import org.saltyrtc.client.signaling.CloseCode;
import org.saltyrtc.client.signaling.SignalingInterface;
import org.saltyrtc.client.tasks.Task;
import org.slf4j.Logger;

import java.util.*;

/**
 * Relayed Data Task.
 *
 * This task uses the end-to-end encrypted WebSocket connection set up by the
 * SaltyRTC protocol to send user defined messages.
 */
public class RelayedDataTask implements Task {
    // Constants as defined by the specification
    private static final String PROTOCOL_NAME = "v0.relayed-data.tasks.saltyrtc.org";
    private static final String TYPE_DATA = "data";
    private static final String KEY_PAYLOAD = "p";

    // Signaling
    private SignalingInterface signaling;

    // Callbacks
    @Nullable
    private MessageHandler messageHandler = null;

    public RelayedDataTask() {}

    public RelayedDataTask(MessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }

    // Return logger instance
    @NonNull
    private Logger getLogger() {
        String name;
        if (this.signaling == null) {
            name = "SaltyRTC.RelayedData";
        } else {
            name = "SaltyRTC.RelayedData." + this.signaling.getRole().name();
        }
        return org.slf4j.LoggerFactory.getLogger(name);
    }

    /**
     * Set the message handler.
     */
    public synchronized void setMessageHandler(@NonNull MessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }

    /**
     * Unset the message handler.
     */
    public synchronized void clearMessageHandler() {
        this.messageHandler = null;
    }

    @Override
    public void init(SignalingInterface signaling, Map<Object, Object> data) {
        this.getLogger().info("Initializing");
        this.signaling = signaling;
    }

    @Override
    public void onPeerHandshakeDone() {
        this.getLogger().info("Taking over");
    }

    /**
     * Validate data messages.
     */
    private boolean isValidData(TaskMessage message) {
        final Map<String, Object> data = message.getData();
        if (data == null) {
            this.getLogger().warn("Data message has no payload!");
            return false;
        }
        if (data.get(KEY_PAYLOAD) == null) {
            this.getLogger().warn("Data message has empty payload!");
            return false;
        }
        return true;
    }

    @Override
    public void onTaskMessage(TaskMessage message) {
        this.getLogger().info("New task message arrived: " + message.getType());
        switch (message.getType()) {
            case TYPE_DATA:
                if (!this.isValidData(message)) {
                    return;
                }
                if (this.messageHandler != null) {
                    this.messageHandler.onData(message.getData().get(KEY_PAYLOAD));
                }
                break;
            default:
                this.getLogger().error("Received message with invalid type: " + message.getType());
        }
    }

    @Override
    public void sendSignalingMessage(byte[] payload) throws SignalingException {
        throw new SignalingException(CloseCode.INTERNAL_ERROR, "Task does not support handover");
    }

    @NonNull
    @Override
    public String getName() {
        return PROTOCOL_NAME;
    }

    @NonNull
    @Override
    public List<String> getSupportedMessageTypes() {
        return Collections.singletonList(TYPE_DATA);
    }

    @Nullable
    @Override
    public Map<Object, Object> getData() {
        return new HashMap<>();
    }

    /**
     * Send a task message through the peer.
     * @param data A map with data that can be serialized to MessagePack.
     * @throws ConnectionException
     * @throws SignalingException
     */
    public void sendMessage(@Nullable String data) throws ConnectionException, SignalingException {
        final Map<String, Object> map = new HashMap<>(1);
        map.put(KEY_PAYLOAD, data);
        this.signaling.sendTaskMessage(new TaskMessage(TYPE_DATA, map));
    }

    @Override
    public void close(int reason) {
        this.getLogger().info("Closing connection: " + CloseCode.explain(reason));
    }
}
