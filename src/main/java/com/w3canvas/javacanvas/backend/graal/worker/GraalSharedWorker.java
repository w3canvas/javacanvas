package com.w3canvas.javacanvas.backend.graal.worker;

import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Value;

import com.w3canvas.javacanvas.backend.graal.GraalRuntime;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * GraalJS implementation of SharedWorker for shared background processing.
 *
 * Unlike Worker, SharedWorker creates a single shared instance that
 * multiple scripts can connect to. Communication happens through
 * MessagePort objects rather than direct postMessage.
 *
 * Usage:
 *   var worker = new SharedWorker('worker.js');
 *   worker.port.onmessage = function(e) { ... };
 *   worker.port.postMessage(data);
 */
public class GraalSharedWorker {

    // Global registry of shared workers by script URL
    private static final ConcurrentHashMap<String, SharedWorkerThread> sharedWorkers = new ConcurrentHashMap<>();

    private GraalMessagePort port;
    private SharedWorkerThread workerThread;

    /**
     * Construct a SharedWorker.
     * If a worker with this URL already exists, connect to it.
     * Otherwise, create a new shared worker.
     *
     * @param scriptUrl The URL/path of the worker script
     * @param mainRuntime The main thread runtime
     */
    public GraalSharedWorker(String scriptUrl, GraalRuntime mainRuntime) {
        // Get or create the shared worker thread
        workerThread = sharedWorkers.computeIfAbsent(scriptUrl, url -> {
            SharedWorkerThread thread = new SharedWorkerThread(mainRuntime, url);
            thread.start();
            return thread;
        });

        // Create a new MessagePort for this connection
        port = new GraalMessagePort();
        GraalMessagePort workerPort = new GraalMessagePort();

        // Entangle the ports
        port.entangle(workerPort);

        // Set runtime for main thread's port
        port.setRuntime(mainRuntime);

        // Per HTML5 spec: Port is implicitly started for the main thread
        port.start();

        // Notify the worker of the new connection
        // The worker thread will set up the workerPort's runtime
        workerThread.addConnection(workerPort);
    }

    /**
     * Get the MessagePort for communication with the shared worker.
     * @return The MessagePort
     */
    @HostAccess.Export
    public GraalMessagePort getPort() {
        return port;
    }

    /**
     * Terminate all shared workers (for cleanup).
     */
    public static void terminateAll() {
        for (SharedWorkerThread thread : sharedWorkers.values()) {
            thread.interrupt();
        }
        sharedWorkers.clear();
    }

    /**
     * Get the number of active shared workers (for testing).
     */
    public static int getActiveWorkerCount() {
        return sharedWorkers.size();
    }

    /**
     * SharedWorker thread that runs the worker script.
     * Maintains a list of connected ports and dispatches connect events.
     */
    private static class SharedWorkerThread extends Thread {
        private final GraalRuntime mainRuntime;
        private final String scriptUrl;
        private final List<GraalMessagePort> connections = new ArrayList<>();
        private final List<GraalMessagePort> pendingConnections = new ArrayList<>();
        private GraalRuntime workerRuntime;
        private Value workerBindings;
        private volatile boolean scriptLoaded = false;

        public SharedWorkerThread(GraalRuntime mainRuntime, String scriptUrl) {
            this.mainRuntime = mainRuntime;
            this.scriptUrl = scriptUrl;
            setDaemon(true);
            setName("GraalSharedWorker-" + scriptUrl);
        }

        public void addConnection(GraalMessagePort port) {
            synchronized (connections) {
                connections.add(port);
                if (scriptLoaded) {
                    // Script already loaded, dispatch immediately
                    dispatchConnectEvent(port);
                } else {
                    // Queue for later dispatch after script loads
                    pendingConnections.add(port);
                }
            }
        }

        private void dispatchConnectEvent(GraalMessagePort port) {
            if (workerBindings != null) {
                // Set the runtime for this port so it can process messages
                port.setRuntime(workerRuntime);

                // Get the onconnect handler
                if (workerBindings.hasMember("onconnect")) {
                    Value onconnect = workerBindings.getMember("onconnect");
                    if (onconnect != null && onconnect.canExecute()) {
                        // Create event object
                        Map<String, Object> event = new HashMap<>();
                        event.put("port", port);
                        event.put("ports", new Object[]{port});

                        try {
                            onconnect.execute(event);
                        } catch (Exception e) {
                            System.err.println("ERROR [Graal]: Error dispatching connect event: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        @Override
        public void run() {
            // Set context classloader
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());

            // Create worker runtime with WorkerThreadEventLoop
            workerRuntime = new GraalRuntime(true);  // true = worker context
            workerBindings = (Value) workerRuntime.getScope();

            try {
                // Add console for debugging
                workerBindings.putMember("console", new com.w3canvas.javacanvas.utils.ScriptLogger());

                // Expose self reference
                workerBindings.putMember("self", workerBindings);

                // Load and execute worker script
                File scriptFile = new File(scriptUrl);
                if (!scriptFile.exists()) {
                    // Try relative to test directory
                    scriptFile = new File(".", scriptUrl);
                }

                if (scriptFile.exists()) {
                    workerRuntime.exec(new FileReader(scriptFile), scriptUrl);
                    System.out.println("DEBUG [Graal]: SharedWorker script loaded successfully: " + scriptUrl);

                    scriptLoaded = true;

                    // Dispatch pending connections
                    synchronized (connections) {
                        for (GraalMessagePort pendingPort : pendingConnections) {
                            dispatchConnectEvent(pendingPort);
                        }
                        pendingConnections.clear();
                    }
                } else {
                    System.err.println("ERROR [Graal]: SharedWorker script not found: " + scriptUrl);
                }

                // Keep worker alive to process events
                while (!Thread.currentThread().isInterrupted()) {
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                System.out.println("DEBUG [Graal]: SharedWorker thread interrupted");
            } catch (Exception e) {
                System.err.println("ERROR [Graal]: SharedWorker script failed: " + e.getMessage());
                e.printStackTrace();
            } finally {
                if (workerRuntime != null) {
                    workerRuntime.close();
                }
            }
        }
    }
}
