package org.saltyrtc.tasks.relayeddata.tests;

import org.junit.Test;
import org.saltyrtc.client.SaltyRTC;
import org.saltyrtc.client.SaltyRTCBuilder;
import org.saltyrtc.client.crypto.CryptoProvider;
import org.saltyrtc.client.events.EventHandler;
import org.saltyrtc.client.events.SignalingStateChangedEvent;
import org.saltyrtc.client.exceptions.ConnectionException;
import org.saltyrtc.client.exceptions.SignalingException;
import org.saltyrtc.client.keystore.KeyStore;
import org.saltyrtc.client.signaling.state.SignalingState;
import org.saltyrtc.client.tasks.Task;
import org.saltyrtc.tasks.relayeddata.MessageHandler;
import org.saltyrtc.tasks.relayeddata.RelayedDataTask;
import org.saltyrtc.tasks.relayeddata.tests.crypto.LazysodiumCryptoProvider;
import org.saltyrtc.tasks.relayeddata.tests.helpers.SSLContextHelper;

import javax.net.ssl.SSLContext;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertTrue;

public class IntegrationTest {
    private CryptoProvider cryptoProvider = new LazysodiumCryptoProvider();

    @Test
    public void testPingPong() throws Exception {
        final SSLContext sslContext = SSLContextHelper.getSSLContext();

        // Create client instances
        final RelayedDataTask initiatorTask = new RelayedDataTask();
        final SaltyRTC initiator = new SaltyRTCBuilder(this.cryptoProvider)
            .connectTo(Config.SALTYRTC_HOST, Config.SALTYRTC_PORT, sslContext)
            .withKeyStore(new KeyStore(this.cryptoProvider))
            .usingTasks(new Task[] { initiatorTask })
            .asInitiator();
        final RelayedDataTask responderTask = new RelayedDataTask();
        final SaltyRTC responder = new SaltyRTCBuilder(this.cryptoProvider)
            .connectTo(Config.SALTYRTC_HOST, Config.SALTYRTC_PORT, sslContext)
            .withKeyStore(new KeyStore(this.cryptoProvider))
            .usingTasks(new Task[] { responderTask })
            .initiatorInfo(initiator.getPublicPermanentKey(), initiator.getAuthToken())
            .asResponder();

        // Signaling state should still be NEW
        assertEquals(SignalingState.NEW, initiator.getSignalingState());
        assertEquals(SignalingState.NEW, responder.getSignalingState());

        // Latches to test connection state
        final CountDownLatch connectedPeers = new CountDownLatch(2);

        // Handle connection state changes
        EventHandler<SignalingStateChangedEvent> countDownIfTaskState = event -> {
            if (event.getState() == SignalingState.TASK) {
                connectedPeers.countDown();
            }
            return false;
        };
        initiator.events.signalingStateChanged.register(countDownIfTaskState);
        responder.events.signalingStateChanged.register(countDownIfTaskState);

        // Connect to server
        initiator.connect();
        responder.connect();

        // Wait for full handshake
        final boolean bothConnected = connectedPeers.await(4, TimeUnit.SECONDS);
        assertTrue(bothConnected);

        // Signaling state should be TASK
        assertEquals(SignalingState.TASK, initiator.getSignalingState());
        assertEquals(SignalingState.TASK, responder.getSignalingState());

        // Chosen task should be RelayedDataTask task
        assertTrue(initiator.getTask() instanceof RelayedDataTask);
        assertTrue(responder.getTask() instanceof RelayedDataTask);

        // Set up message handlers
        final CountDownLatch messagesReceived = new CountDownLatch(2);
        initiatorTask.setMessageHandler(data -> {
            if (data instanceof String && data.equals("goodbye")) {
                System.err.println("Received goodbye");
                messagesReceived.countDown();
            } else {
                System.err.println("Initiator received invalid msg: " + data);
            }
        });
        responderTask.setMessageHandler(data -> {
            if (data instanceof String && data.equals("hello")) {
                System.err.println("Received hello");
                messagesReceived.countDown();
                try {
                    System.err.println("Sending goodbye");
                    responderTask.sendMessage("goodbye");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                System.err.println("Responder received invalid msg: " + data);
            }
        });

        // Send messages back and forth
        System.err.println("Sending hello");
        initiatorTask.sendMessage("hello");

        // Await message-received
        final boolean msgExchangeSuccess = messagesReceived.await(4, TimeUnit.SECONDS);
        assertTrue(msgExchangeSuccess);

        // Await something (TODO: WHY?)
        Thread.sleep(500);

        // Disconnect
        initiator.disconnect();
        responder.disconnect();

        // Await close events
        Thread.sleep(500);

        // Signaling state should be CLOSED
        assertEquals(SignalingState.CLOSED, initiator.getSignalingState());
        assertEquals(SignalingState.CLOSED, responder.getSignalingState());
    }
}
