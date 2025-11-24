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
            try {
                otherPort.messageQueue.put(data);
                // Don't auto-start the other port - it should be started explicitly
                // by calling start() or setting onmessage
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Start the port (begins dispatching messages).
     * In browsers, ports need to be explicitly started.
     */
    public void jsFunction_start() {
        if (started) {
            return;
        }
        started = true;

        // Start listening for messages
        messageListener = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Object data = messageQueue.take();

                    if (onmessage != null && handlerScope != null) {
                        Context cx = Context.enter();
                        try {
                            // Set the runtime in thread local so document, etc. are available
                            if (handlerRuntime != null) {
                                cx.putThreadLocal("runtime", handlerRuntime);
                            }

                            // Use the captured handlerScope instead of getParentScope()
                            // This ensures we use the scope from where onmessage was set
                            Scriptable event = cx.newObject(handlerScope);
                            event.put("data", event, data);

                            // Add ports array to event (this port)
                            Scriptable ports = cx.newArray(handlerScope, new Object[]{this});
                            event.put("ports", event, ports);

                            onmessage.call(cx, handlerScope, this, new Object[]{event});
                        } finally {
                            Context.exit();
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        messageListener.setDaemon(true);
        messageListener.start();
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
        // Capture the scope from the current context where onmessage is being set
        // This ensures the handler has access to the correct scope (e.g., document in main thread)
        this.handlerScope = getParentScope();
        // Also capture the runtime so we can set it in the listener thread's context
        Context cx = Context.getCurrentContext();
        if (cx != null) {
            Object runtime = cx.getThreadLocal("runtime");
            if (runtime instanceof com.w3canvas.javacanvas.rt.RhinoRuntime) {
                this.handlerRuntime = (com.w3canvas.javacanvas.rt.RhinoRuntime) runtime;
            }
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
