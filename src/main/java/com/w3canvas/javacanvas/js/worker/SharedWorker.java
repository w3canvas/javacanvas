package com.w3canvas.javacanvas.js.worker;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.mozilla.javascript.Callable;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import com.w3canvas.javacanvas.backend.rhino.impl.node.ProjectScriptableObject;
import com.w3canvas.javacanvas.rt.RhinoRuntime;

/**
 * SharedWorker implementation for shared background processing.
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
@SuppressWarnings("serial")
public class SharedWorker extends ProjectScriptableObject {

    // Global registry of shared workers by script URL
    private static final ConcurrentHashMap<String, SharedWorkerThread> sharedWorkers = new ConcurrentHashMap<>();

    private MessagePort port;
    private SharedWorkerThread workerThread;

    public SharedWorker() {
    }

    /**
     * Construct a SharedWorker.
     * If a worker with this URL already exists, connect to it.
     * Otherwise, create a new shared worker.
     *
     * @param scriptUrl The URL/path of the worker script
     */
    public void jsConstructor(String scriptUrl) {
        RhinoRuntime mainRuntime = (RhinoRuntime) Context.getCurrentContext().getThreadLocal("runtime");

        // Get or create the shared worker thread
        workerThread = sharedWorkers.computeIfAbsent(scriptUrl, url -> {
            SharedWorkerThread thread = new SharedWorkerThread(mainRuntime, url);
            thread.start();
            return thread;
        });

        // Create a new MessagePort for this connection
        port = new MessagePort();
        MessagePort workerPort = new MessagePort();

        // Entangle the ports
        port.entangle(workerPort);

        // Set up scopes and prototypes
        Scriptable scope = ScriptableObject.getTopLevelScope(this);
        port.setParentScope(scope);
        workerPort.setParentScope(scope);

        try {
            Scriptable proto = ScriptableObject.getClassPrototype(scope, "MessagePort");
            if (proto != null) {
                port.setPrototype(proto);
                workerPort.setPrototype(proto);
            }
        } catch (Exception e) {
            // Prototype not found, ports will still work
        }

        // Notify the worker of the new connection
        workerThread.addConnection(workerPort);
    }

    @Override
    public String getClassName() {
        return "SharedWorker";
    }

    /**
     * Get the MessagePort for communication with the shared worker.
     * @return The MessagePort
     */
    public MessagePort jsGet_port() {
        return port;
    }

    /**
     * SharedWorker thread that runs the worker script.
     * Maintains a list of connected ports and dispatches connect events.
     */
    private static class SharedWorkerThread extends Thread {
        private final RhinoRuntime mainRuntime;
        private final String scriptUrl;
        private final List<MessagePort> connections = new ArrayList<>();
        private RhinoRuntime workerRuntime;
        private Scriptable workerScope;
        private Context workerContext;

        public SharedWorkerThread(RhinoRuntime mainRuntime, String scriptUrl) {
            this.mainRuntime = mainRuntime;
            this.scriptUrl = scriptUrl;
            setDaemon(true);
        }

        public void addConnection(MessagePort port) {
            synchronized (connections) {
                connections.add(port);
                // Dispatch connect event to the worker
                dispatchConnectEvent(port);
            }
        }

        private void dispatchConnectEvent(MessagePort port) {
            if (workerContext != null && workerScope != null) {
                // Get the onconnect handler
                Object onconnect = workerScope.get("onconnect", workerScope);
                if (onconnect instanceof Function) {
                    Scriptable event = workerContext.newObject(workerScope);

                    // Create ports array with the connecting port
                    Scriptable ports = workerContext.newArray(workerScope, new Object[]{port});
                    event.put("ports", event, ports);

                    // The event also has a direct port reference
                    event.put("port", event, port);

                    ((Function) onconnect).call(workerContext, workerScope, workerScope, new Object[]{event});
                }
            }
        }

        @Override
        public void run() {
            workerRuntime = new RhinoRuntime();
            workerContext = Context.enter();
            workerContext.putThreadLocal("runtime", workerRuntime);
            workerScope = workerRuntime.getScope();

            try {
                // Define classes available in SharedWorker context
                ScriptableObject.defineClass(workerScope, OffscreenCanvas.class);
                ScriptableObject.defineClass(workerScope, MessagePort.class);
                ScriptableObject.defineClass(workerScope, com.w3canvas.javacanvas.backend.rhino.impl.node.CanvasRenderingContext2D.class);
                ScriptableObject.defineClass(workerScope, com.w3canvas.javacanvas.backend.rhino.impl.node.ImageData.class);
                ScriptableObject.defineClass(workerScope, com.w3canvas.javacanvas.backend.rhino.impl.node.TextMetrics.class);
                ScriptableObject.defineClass(workerScope, com.w3canvas.javacanvas.backend.rhino.impl.node.DOMMatrix.class);
                ScriptableObject.defineClass(workerScope, com.w3canvas.javacanvas.backend.rhino.impl.node.Blob.class);
                ScriptableObject.defineClass(workerScope, com.w3canvas.javacanvas.backend.rhino.impl.node.ImageBitmap.class);
            } catch (Exception e) {
                throw new RuntimeException("Failed to initialize SharedWorker context", e);
            }

            // Add console for debugging
            ScriptableObject.putProperty(workerScope, "console", new com.w3canvas.javacanvas.utils.ScriptLogger());

            // Define close() function to terminate the worker
            ScriptableObject.putProperty(workerScope, "close", new Callable() {
                @Override
                public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
                    // Close all connections
                    synchronized (connections) {
                        for (MessagePort port : connections) {
                            port.jsFunction_close();
                        }
                        connections.clear();
                    }
                    // Remove from registry
                    sharedWorkers.remove(scriptUrl);
                    // Interrupt this thread
                    Thread.currentThread().interrupt();
                    return null;
                }
            });

            try {
                // Load and execute the worker script
                InputStreamReader reader = new InputStreamReader(
                    SharedWorker.class.getClassLoader().getResourceAsStream(scriptUrl));
                workerRuntime.exec(reader, scriptUrl);

                // Keep the worker alive (until interrupted)
                while (!Thread.currentThread().isInterrupted()) {
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                Context.exit();
                // Clean up
                synchronized (connections) {
                    for (MessagePort port : connections) {
                        port.jsFunction_close();
                    }
                    connections.clear();
                }
                sharedWorkers.remove(scriptUrl);
            }
        }
    }

    /**
     * Utility method to get the number of active shared workers.
     * Useful for testing and debugging.
     * @return The count of active shared workers
     */
    public static int getActiveWorkerCount() {
        return sharedWorkers.size();
    }

    /**
     * Utility method to terminate all shared workers.
     * Useful for cleanup in tests.
     */
    public static void terminateAll() {
        for (SharedWorkerThread worker : sharedWorkers.values()) {
            worker.interrupt();
        }
        sharedWorkers.clear();
    }
}
