package com.w3canvas.javacanvas.js.worker;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import com.w3canvas.javacanvas.backend.rhino.impl.node.ProjectScriptableObject;

/**
 * MessagePort implementation for SharedWorker communication.
 *
 * MessagePort provides a two-way communication channel between
 * different contexts (e.g., main thread and SharedWorker).
 * Part of the HTML5 Channel Messaging API.
 *
 * Messages are queued in the port's message queue and delivered asynchronously
 * via the event loop, ensuring proper Context isolation per the HTML Worker spec.
 */
@SuppressWarnings("serial")
public class MessagePort extends ProjectScriptableObject {

    private Function onmessage;
    private MessagePort otherPort;
    private volatile boolean started = false;
    private Scriptable handlerScope;  // Capture scope where onmessage was set
    private com.w3canvas.javacanvas.rt.RhinoRuntime handlerRuntime;  // Capture runtime for event loop

    // Message queue - messages can be queued before handlerRuntime is set
    private final BlockingQueue<Object> messageQueue = new LinkedBlockingQueue<>();

    public MessagePort() {
    }

    @Override
    public String getClassName() {
        return "MessagePort";
    }

    /**
     * Entangle this port with another port for bidirectional communication.
     * @param other The other MessagePort to entangle with
     */
    public void entangle(MessagePort other) {
        this.otherPort = other;
        other.otherPort = this;
    }

    /**
     * Post a message through this port to the entangled port.
     * Messages are queued to the other port's message queue immediately.
     * They will be processed when the port has a handler and is started.
     *
     * @param data The data to send
     */
    public void jsFunction_postMessage(Object data) {
        System.out.println("DEBUG: postMessage() called, otherPort=" + (otherPort != null));
        if (otherPort == null) {
            return;  // Port not entangled
        }

        // Queue the message to the other port's queue
        // This works immediately, even before the other port has handlerRuntime set
        otherPort.messageQueue.offer(data);
        System.out.println("DEBUG: Message queued, queue size=" + otherPort.messageQueue.size());

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
            System.out.println("DEBUG: processPendingMessages called but no handlerRuntime yet");
            return;  // Can't process without runtime
        }

        // Queue a task to drain all pending messages
        Runnable drainTask = () -> {
            System.out.println("DEBUG: drainTask executing on thread: " + Thread.currentThread().getName() +
                ", id=" + Thread.currentThread().getId());
            // Check if there's already a Context on this thread
            Context cx = Context.getCurrentContext();
            System.out.println("DEBUG: getCurrentContext() returned: " + (cx != null ? "existing Context" : "null"));
            boolean needToExit = false;
            if (cx == null) {
                cx = Context.enter();
                needToExit = true;
                System.out.println("DEBUG: Entered new Context");
            }
            try {
                cx.putThreadLocal("runtime", handlerRuntime);
                System.out.println("DEBUG: Set runtime in thread local");

                // Process all messages in the queue
                Object data;
                while ((data = messageQueue.poll()) != null) {
                    System.out.println("DEBUG: Processing message from queue on thread: " + Thread.currentThread().getName());

                    // Check if port is started and has a handler
                    if (!started || onmessage == null || handlerScope == null) {
                        System.out.println("DEBUG: Port not ready, re-queuing message");
                        // Put the message back at the front
                        messageQueue.offer(data);
                        break;
                    }

                    try {
                        // Create event object
                        Scriptable event = cx.newObject(handlerScope);
                        event.put("data", event, data);

                        // Add ports array
                        Scriptable ports = cx.newArray(handlerScope, new Object[]{this});
                        event.put("ports", event, ports);

                        // Call the handler
                        onmessage.call(cx, handlerScope, this, new Object[]{event});
                        System.out.println("DEBUG: Message handler called successfully");
                    } catch (Exception e) {
                        System.err.println("Error processing message: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            } finally {
                if (needToExit) {
                    Context.exit();
                }
            }
        };

        handlerRuntime.getEventLoop().queueTask(drainTask);
    }

    /**
     * Start the port (begins dispatching messages).
     * In browsers, ports need to be explicitly started.
     *
     * Per HTML spec: "The implicit MessagePort used by dedicated workers has its
     * port message queue implicitly enabled when it is created, so there is no
     * equivalent to the MessagePort interface's start() method on the Worker interface."
     */
    public void jsFunction_start() {
        started = true;
    }

    /**
     * Close the port and stop message processing.
     */
    public void jsFunction_close() {
        started = false;
        if (otherPort != null) {
            otherPort.otherPort = null;
            otherPort = null;
        }
    }

    /**
     * Set the onmessage event handler.
     * Captures the scope and runtime where the handler is set.
     * Processes any pending messages that arrived before the handler was set.
     *
     * @param onmessage The message handler function
     */
    public void jsSet_onmessage(Function onmessage) {
        this.onmessage = onmessage;
        System.out.println("DEBUG: jsSet_onmessage called on thread: " + Thread.currentThread().getName() +
            ", id=" + Thread.currentThread().getId());

        Context cx = Context.getCurrentContext();
        if (cx != null) {
            // Get the top-level scope which should have document and other globals
            this.handlerScope = org.mozilla.javascript.ScriptableObject.getTopLevelScope(this);

            // Capture the runtime for event loop access
            Object runtime = cx.getThreadLocal("runtime");
            if (runtime instanceof com.w3canvas.javacanvas.rt.RhinoRuntime) {
                this.handlerRuntime = (com.w3canvas.javacanvas.rt.RhinoRuntime) runtime;
                System.out.println("DEBUG: onmessage set, handlerRuntime captured, queue size=" + messageQueue.size());
            }
        } else {
            this.handlerScope = getParentScope();
        }

        // Auto-start when onmessage is set (per spec for dedicated workers)
        if (!started) {
            jsFunction_start();
        }

        // Process any messages that were queued before the handler was set
        if (handlerRuntime != null && !messageQueue.isEmpty()) {
            System.out.println("DEBUG: Processing " + messageQueue.size() + " pending messages");
            processPendingMessages();
        }
    }

    /**
     * Get the onmessage event handler.
     * @return The message handler function
     */
    public Function jsGet_onmessage() {
        return onmessage;
    }
}
