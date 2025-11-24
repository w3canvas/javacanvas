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
 */
@SuppressWarnings("serial")
public class MessagePort extends ProjectScriptableObject {

    private Function onmessage;
    private final BlockingQueue<Object> messageQueue = new LinkedBlockingQueue<>();
    private MessagePort otherPort;
    private Thread messageListener;
    private volatile boolean started = false;
    private Scriptable handlerScope;  // Capture scope where onmessage was set
    private com.w3canvas.javacanvas.rt.RhinoRuntime handlerRuntime;  // Capture runtime for thread local
    private Thread handlerThread;  // Capture the Thread where onmessage was set

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
     * @param data The data to send
     */
    public void jsFunction_postMessage(Object data) {
        if (otherPort != null) {
            // Queue the message for async delivery via the event loop
            // This ensures proper Context isolation per the HTML Worker spec
            try {
                otherPort.messageQueue.put(data);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Start the port (begins dispatching messages).
     * In browsers, ports need to be explicitly started.
     * Messages are queued and processed via processPendingMessages() or the event loop.
     */
    public void jsFunction_start() {
        if (started) {
            return;
        }
        started = true;
        // Messages are now processed via processPendingMessages() call from the main thread's event loop
        // No need for a separate listener thread since that causes Context isolation issues
    }

    /**
     * Process any pending messages in the current Context.
     * This should be called from the thread that owns the Context where onmessage was set.
     * This is part of the event loop pattern for Worker message delivery.
     */
    public void processPendingMessages() {
        if (onmessage == null || handlerScope == null) {
            return;
        }

        // Only process messages if we're on the correct thread
        // This ensures proper Context isolation per Rhino's thread-local design
        if (handlerThread != null && Thread.currentThread() != handlerThread) {
            // Different thread - don't process here
            return;
        }

        Context cx = Context.getCurrentContext();
        if (cx == null) {
            return;  // No Context, can't process messages
        }

        // Process all available messages without blocking
        Object data;
        while ((data = messageQueue.poll()) != null) {
            try {
                if (handlerRuntime != null) {
                    cx.putThreadLocal("runtime", handlerRuntime);
                }

                // Create event object
                Scriptable event = cx.newObject(handlerScope);
                event.put("data", event, data);

                // Add ports array
                Scriptable ports = cx.newArray(handlerScope, new Object[]{this});
                event.put("ports", event, ports);

                // Call the handler in the current Context
                onmessage.call(cx, handlerScope, this, new Object[]{event});
            } catch (Exception e) {
                System.err.println("Error processing message: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Close the port and stop message processing.
     */
    public void jsFunction_close() {
        started = false;
        if (messageListener != null) {
            messageListener.interrupt();
            messageListener = null;
        }
        if (otherPort != null) {
            otherPort.otherPort = null;
            otherPort = null;
        }
    }

    /**
     * Set the onmessage event handler.
     * @param onmessage The message handler function
     */
    public void jsSet_onmessage(Function onmessage) {
        this.onmessage = onmessage;
        // Capture the Thread, scope, and runtime where onmessage is being set
        // This is CRITICAL: the handler must execute in the SAME Thread to access document methods
        this.handlerThread = Thread.currentThread();

        Context cx = Context.getCurrentContext();
        if (cx != null) {
            // Get the top-level scope which should have document and other globals
            this.handlerScope = org.mozilla.javascript.ScriptableObject.getTopLevelScope(this);

            // Also capture the runtime so we can set it in the listener thread's context
            Object runtime = cx.getThreadLocal("runtime");
            if (runtime instanceof com.w3canvas.javacanvas.rt.RhinoRuntime) {
                this.handlerRuntime = (com.w3canvas.javacanvas.rt.RhinoRuntime) runtime;
            }
        } else {
            this.handlerScope = getParentScope();
        }
        // Auto-start when onmessage is set
        if (!started) {
            jsFunction_start();
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
