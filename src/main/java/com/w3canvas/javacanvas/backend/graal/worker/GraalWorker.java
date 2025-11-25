package com.w3canvas.javacanvas.backend.graal.worker;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Value;

import com.w3canvas.javacanvas.rt.EventLoop;
import com.w3canvas.javacanvas.backend.graal.GraalRuntime;
import com.w3canvas.javacanvas.rt.WorkerThreadEventLoop;

import java.io.File;
import java.io.FileReader;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Proof-of-concept GraalJS Worker implementation.
 *
 * This is a minimal implementation to validate that:
 * 1. GraalJS can use the existing EventLoop architecture
 * 2. Workers can communicate via message passing
 * 3. The hybrid approach (Core APIs + minimal adapters) works
 *
 * This is NOT a complete HTML5 Worker implementation - it's a proof of concept
 * to identify what shared architecture can support both Rhino and GraalJS.
 */
public class GraalWorker {

    private final GraalRuntime workerRuntime;
    private final Thread workerThread;
    private final String scriptUrl;
    private final BlockingQueue<Object> messageQueue;
    private Value onmessageHandler;

    /**
     * Create a new GraalWorker.
     *
     * @param scriptUrl Path to the worker script
     */
    public GraalWorker(String scriptUrl) {
        this.scriptUrl = scriptUrl;
        this.messageQueue = new LinkedBlockingQueue<>();

        // Create worker runtime with event loop (isWorker=true)
        this.workerRuntime = new GraalRuntime(true);

        // Start worker thread
        this.workerThread = new Thread(this::run, "GraalWorker-" + scriptUrl);
        this.workerThread.setDaemon(true);
        this.workerThread.start();
    }

    /**
     * Worker thread entry point.
     * Loads and executes the worker script.
     */
    private void run() {
        try {
            // Get GraalJS bindings (scope)
            Value bindings = (Value) workerRuntime.getScope();

            // Expose worker global scope
            bindings.putMember("self", this);
            bindings.putMember("postMessage", new PostMessageHandler());

            // Load and execute worker script
            File scriptFile = new File(scriptUrl);
            if (!scriptFile.exists()) {
                // Try relative to test directory
                scriptFile = new File(".", scriptUrl);
            }

            if (scriptFile.exists()) {
                workerRuntime.exec(new FileReader(scriptFile), scriptUrl);
                System.out.println("GraalWorker: Script loaded successfully: " + scriptUrl);
            } else {
                System.err.println("GraalWorker: Script not found: " + scriptUrl);
            }

            // Keep worker alive to process messages
            // In a real implementation, this would be handled by the event loop
            while (!Thread.currentThread().isInterrupted()) {
                Thread.sleep(100);
            }
        } catch (InterruptedException e) {
            System.out.println("GraalWorker thread interrupted");
        } catch (Exception e) {
            System.err.println("GraalWorker script failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Post a message to the worker.
     * This is called from the main thread.
     *
     * @param message The message to send
     */
    @HostAccess.Export
    public void postMessage(Object message) {
        System.out.println("GraalWorker: Main thread posting message to worker");

        // Queue message to worker event loop
        workerRuntime.getEventLoop().queueTask(() -> {
            try {
                if (onmessageHandler != null && onmessageHandler.canExecute()) {
                    // Create message event
                    MessageEvent event = new MessageEvent(message);
                    onmessageHandler.execute(event);
                    System.out.println("GraalWorker: Message delivered to worker");
                }
            } catch (Exception e) {
                System.err.println("GraalWorker: Error delivering message: " + e.getMessage());
            }
        });
    }

    /**
     * Set the message handler.
     * This is called from worker script: worker.onmessage = function(e) { ... }
     *
     * @param handler JavaScript function to handle messages
     */
    @HostAccess.Export
    public void setOnmessage(Value handler) {
        this.onmessageHandler = handler;
        System.out.println("GraalWorker: onmessage handler set");
    }

    /**
     * Get the message handler.
     *
     * @return The current message handler
     */
    @HostAccess.Export
    public Value getOnmessage() {
        return onmessageHandler;
    }

    /**
     * Terminate the worker.
     */
    @HostAccess.Export
    public void terminate() {
        workerThread.interrupt();
        workerRuntime.close();
    }

    /**
     * PostMessage handler for worker script to send messages to main thread.
     * In worker script: postMessage(data)
     */
    public class PostMessageHandler {
        @HostAccess.Export
        public void call(Object message) {
            System.out.println("GraalWorker: Worker posting message to main thread");
            // TODO: Implement message passing to main thread
            // This would require a reference to the main thread's runtime/event loop
        }
    }

    /**
     * Simple message event object.
     */
    public static class MessageEvent {
        private final Object data;

        public MessageEvent(Object data) {
            this.data = data;
        }

        @HostAccess.Export
        public Object getData() {
            return data;
        }

        // Allow 'e.data' property access in JavaScript
        @HostAccess.Export
        public Object data() {
            return data;
        }
    }
}
