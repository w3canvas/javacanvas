package com.w3canvas.javacanvas.backend.graal.worker;

import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Value;

import com.w3canvas.javacanvas.rt.GraalRuntime;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * GraalJS implementation of MessagePort for SharedWorker communication.
 *
 * MessagePort provides a two-way communication channel between
 * different contexts (e.g., main thread and SharedWorker).
 * Part of the HTML5 Channel Messaging API.
 *
 * Messages are queued in the port's message queue and delivered asynchronously
 * via the event loop.
 */
public class GraalMessagePort {

    private Value onmessage;
    private GraalMessagePort otherPort;
    private volatile boolean started = false;
    private GraalRuntime handlerRuntime;  // Runtime for event loop

    // Message queue - messages can be queued before handlerRuntime is set
    private final BlockingQueue<Object> messageQueue = new LinkedBlockingQueue<>();

    public GraalMessagePort() {
    }

    /**
     * Entangle this port with another port for bidirectional communication.
     * @param other The other MessagePort to entangle with
     */
    public void entangle(GraalMessagePort other) {
        this.otherPort = other;
        other.otherPort = this;
    }

    /**
     * Post a message through this port to the entangled port.
     * Messages are queued to the other port's message queue immediately.
     *
     * @param data The data to send
     */
    @HostAccess.Export
    public void postMessage(Object data) {
        System.out.println("DEBUG [Graal]: postMessage() called, otherPort=" + (otherPort != null));
        if (otherPort == null) {
            return;  // Port not entangled
        }

        // Queue the message to the other port's queue
        otherPort.messageQueue.offer(data);
        System.out.println("DEBUG [Graal]: Message queued, queue size=" + otherPort.messageQueue.size());

        // If the other port already has a handler and runtime, process pending messages
        if (otherPort.handlerRuntime != null && otherPort.started && otherPort.onmessage != null) {
            otherPort.processPendingMessages();
        }
    }

    /**
     * Process pending messages from the message queue.
     * Called when onmessage is set or when new messages arrive.
     */
    private void processPendingMessages() {
        if (handlerRuntime == null) {
            System.out.println("DEBUG [Graal]: processPendingMessages called but no handlerRuntime yet");
            return;  // Can't process without runtime
        }

        // Count pending messages
        int pendingCount = messageQueue.size();
        if (pendingCount > 0) {
            System.out.println("DEBUG [Graal]: Processing " + pendingCount + " pending messages");
        }

        // Queue a task to drain all pending messages
        Runnable drainTask = () -> {
            System.out.println("DEBUG [Graal]: drainTask executing on thread: " + Thread.currentThread().getName());

            // Process all messages in the queue
            Object data;
            while ((data = messageQueue.poll()) != null) {
                System.out.println("DEBUG [Graal]: Processing message from queue on thread: " + Thread.currentThread().getName());

                // Check if port is started and has a handler
                if (!started || onmessage == null) {
                    System.out.println("DEBUG [Graal]: Port not ready, re-queuing message");
                    // Put the message back
                    messageQueue.offer(data);
                    break;
                }

                try {
                    // Create event object
                    Map<String, Object> event = new HashMap<>();
                    event.put("data", data);

                    // Call the handler
                    if (onmessage.canExecute()) {
                        onmessage.execute(event);
                        System.out.println("DEBUG [Graal]: Message handler called successfully");
                    }
                } catch (Exception e) {
                    System.err.println("ERROR [Graal]: Error processing message: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        };

        handlerRuntime.getEventLoop().queueTask(drainTask);
    }

    /**
     * Set the message handler.
     * JavaScript: port.onmessage = function(e) { ... }
     *
     * @param handler JavaScript function to handle messages
     */
    @HostAccess.Export
    public void setOnmessage(Value handler) {
        System.out.println("DEBUG [Graal]: setOnmessage called on thread: " + Thread.currentThread().getName());
        this.onmessage = handler;

        // Try to get the runtime from thread local
        // This is set by GraalRuntime.exec() before executing scripts
        if (this.handlerRuntime == null) {
            // Try to infer runtime - for now, we'll need to set it explicitly
            System.out.println("DEBUG [Graal]: onmessage set, but runtime not yet captured");
        } else {
            System.out.println("DEBUG [Graal]: onmessage set, handlerRuntime captured, queue size=" + messageQueue.size());
        }

        // If we have pending messages and a runtime, process them
        if (handlerRuntime != null && started && !messageQueue.isEmpty()) {
            processPendingMessages();
        }
    }

    @HostAccess.Export
    public Value getOnmessage() {
        return onmessage;
    }

    /**
     * Start the port (begins dispatching messages).
     */
    @HostAccess.Export
    public void start() {
        started = true;
        System.out.println("DEBUG [Graal]: MessagePort started");

        // If we have a handler and runtime, process any queued messages
        if (handlerRuntime != null && onmessage != null && !messageQueue.isEmpty()) {
            processPendingMessages();
        }
    }

    /**
     * Close the port and stop message processing.
     */
    @HostAccess.Export
    public void close() {
        started = false;
        if (otherPort != null) {
            otherPort.otherPort = null;
            otherPort = null;
        }
    }

    /**
     * Set the runtime for this port.
     * This is called internally when the port is created.
     *
     * @param runtime The GraalRuntime to use for event loop
     */
    public void setRuntime(GraalRuntime runtime) {
        this.handlerRuntime = runtime;
        System.out.println("DEBUG [Graal]: Runtime set for MessagePort");
    }

    /**
     * Get the runtime (for internal use).
     */
    public GraalRuntime getRuntime() {
        return handlerRuntime;
    }
}
