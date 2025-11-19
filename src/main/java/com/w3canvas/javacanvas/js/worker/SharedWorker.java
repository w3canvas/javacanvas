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

        // Set up scope and prototype for main thread's port
        Scriptable mainScope = ScriptableObject.getTopLevelScope(this);
        port.setParentScope(mainScope);

        try {
            Scriptable proto = ScriptableObject.getClassPrototype(mainScope, "MessagePort");
            if (proto != null) {
                port.setPrototype(proto);
            }
        } catch (Exception e) {
            // Prototype not found, port will still work
        }

        // Notify the worker of the new connection
        // The worker thread will set up the workerPort's scope
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
        private final List<MessagePort> pendingConnections = new ArrayList<>();
        private RhinoRuntime workerRuntime;
        private Scriptable workerScope;
        private Context workerContext;
        private volatile boolean scriptLoaded = false;

        public SharedWorkerThread(RhinoRuntime mainRuntime, String scriptUrl) {
            this.mainRuntime = mainRuntime;
            this.scriptUrl = scriptUrl;
            setDaemon(true);
        }

        public void addConnection(MessagePort port) {
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

        private void dispatchConnectEvent(MessagePort port) {
            if (workerContext != null && workerScope != null) {
                // Set up the port's scope to the worker scope so onmessage handlers work correctly
                port.setParentScope(workerScope);
                try {
                    Scriptable proto = ScriptableObject.getClassPrototype(workerScope, "MessagePort");
                    if (proto != null) {
                        port.setPrototype(proto);
                    }
                } catch (Exception e) {
                    // Prototype not found, port will still work
                }

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
            // Set context classloader to ensure inner classes can be loaded
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());

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

            // Add createImageBitmap global function
            ScriptableObject.putProperty(workerScope, "createImageBitmap", new Callable() {
                @Override
                public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
                    if (args.length == 0) {
                        throw new IllegalArgumentException("createImageBitmap requires at least 1 argument");
                    }

                    Object source = args[0];

                    // Create core ImageBitmap from various source types
                    com.w3canvas.javacanvas.core.ImageBitmap coreImageBitmap = null;

                    try {
                        if (source instanceof OffscreenCanvas) {
                            // OffscreenCanvas - get BufferedImage
                            java.awt.image.BufferedImage img =
                                ((OffscreenCanvas) source).getImage();
                            coreImageBitmap = new com.w3canvas.javacanvas.core.ImageBitmap(img);
                        } else if (source instanceof com.w3canvas.javacanvas.backend.rhino.impl.node.ImageData) {
                            // ImageData - unwrap to core
                            com.w3canvas.javacanvas.interfaces.IImageData coreImageData =
                                ((com.w3canvas.javacanvas.backend.rhino.impl.node.ImageData) source);
                            coreImageBitmap = new com.w3canvas.javacanvas.core.ImageBitmap(coreImageData);
                        } else if (source instanceof com.w3canvas.javacanvas.backend.rhino.impl.node.ImageBitmap) {
                            // ImageBitmap - create copy
                            com.w3canvas.javacanvas.interfaces.IImageBitmap sourceImageBitmap =
                                (com.w3canvas.javacanvas.backend.rhino.impl.node.ImageBitmap) source;
                            coreImageBitmap = new com.w3canvas.javacanvas.core.ImageBitmap(sourceImageBitmap);
                        } else if (source instanceof com.w3canvas.javacanvas.backend.rhino.impl.node.Blob) {
                            // Blob - decode image from blob data
                            com.w3canvas.javacanvas.backend.rhino.impl.node.Blob blob =
                                (com.w3canvas.javacanvas.backend.rhino.impl.node.Blob) source;
                            byte[] data = blob.getData();
                            java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(data);
                            java.awt.image.BufferedImage img = javax.imageio.ImageIO.read(bais);
                            if (img == null) {
                                throw new IllegalArgumentException("Failed to decode image from Blob");
                            }
                            coreImageBitmap = new com.w3canvas.javacanvas.core.ImageBitmap(img);
                        } else {
                            throw new IllegalArgumentException(
                                "createImageBitmap: unsupported source type: " +
                                (source != null ? source.getClass().getName() : "null"));
                        }

                        // Create Rhino wrapper
                        com.w3canvas.javacanvas.backend.rhino.impl.node.ImageBitmap rhinoImageBitmap =
                            new com.w3canvas.javacanvas.backend.rhino.impl.node.ImageBitmap();
                        rhinoImageBitmap.init(coreImageBitmap);

                        // Set up scope and prototype
                        rhinoImageBitmap.setParentScope(scope);
                        try {
                            Scriptable proto = ScriptableObject.getClassPrototype(scope, "ImageBitmap");
                            if (proto != null) {
                                rhinoImageBitmap.setPrototype(proto);
                            }
                        } catch (Exception e) {
                            // Prototype not found, object will still work
                        }

                        return rhinoImageBitmap;
                    } catch (Exception e) {
                        throw new RuntimeException("createImageBitmap failed: " + e.getMessage(), e);
                    }
                }
            });

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
                // Get documentBase from main runtime to resolve script path
                String documentBase = null;
                try {
                    Scriptable mainScope = mainRuntime.getScope();
                    Object docBaseObj = mainScope.get("documentBase", mainScope);
                    if (docBaseObj != Scriptable.NOT_FOUND && docBaseObj instanceof String) {
                        documentBase = (String) docBaseObj;
                    }
                } catch (Exception e) {
                    // documentBase not available, will try loading as-is
                }

                java.io.Reader reader;
                if (documentBase != null && !scriptUrl.startsWith("http://") && !scriptUrl.startsWith("https://")) {
                    // Resolve relative path using documentBase
                    try {
                        java.net.URI baseURI = new java.net.URI(documentBase);
                        java.net.URI scriptURI = baseURI.resolve(scriptUrl);
                        java.io.File scriptFile = new java.io.File(scriptURI);
                        reader = new java.io.FileReader(scriptFile);
                    } catch (Exception e) {
                        // Fall back to classpath resource
                        reader = new InputStreamReader(
                            SharedWorker.class.getClassLoader().getResourceAsStream(scriptUrl));
                    }
                } else {
                    // Try classpath resource
                    reader = new InputStreamReader(
                        SharedWorker.class.getClassLoader().getResourceAsStream(scriptUrl));
                }

                workerRuntime.exec(reader, scriptUrl);

                // Script loaded successfully, dispatch any pending connections
                scriptLoaded = true;
                synchronized (connections) {
                    for (MessagePort port : pendingConnections) {
                        dispatchConnectEvent(port);
                    }
                    pendingConnections.clear();
                }

                // Keep the worker alive (until interrupted)
                while (!Thread.currentThread().isInterrupted()) {
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                System.err.println("ERROR: SharedWorker failed to load script '" + scriptUrl + "': " + e.getMessage());
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
