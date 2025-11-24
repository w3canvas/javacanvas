package com.w3canvas.javacanvas.rt;

import java.util.Hashtable;

import org.mozilla.javascript.Callable;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.Scriptable;

public class RhinoRuntime implements JSRuntime {

    private Hashtable<Integer, RhinoScheduler> intervals = new Hashtable<Integer, RhinoScheduler>();
    private int intervalId;
    private String currentUrl;
    private Scriptable scope;
    private final EventLoop eventLoop;
    private final boolean isWorker;

    // Main thread globals that need to be accessible across Contexts
    // Workers don't have these (they have WorkerGlobalScope instead)
    private Object mainThreadDocument;
    private Object mainThreadWindow;

    // Store the main thread's Context for cross-Context calling
    // This allows message handlers on different threads to reuse the original Context
    // where document/window/canvas were created, enabling proper method resolution
    private Context mainThreadContext;

    public RhinoRuntime() {
        this(false);
    }

    /**
     * Create a RhinoRuntime with optional worker event loop.
     * @param isWorker true if this runtime is for a Worker/SharedWorker context
     */
    public RhinoRuntime(boolean isWorker) {
        this.isWorker = isWorker;

        // Check if we should use synchronous mode for tests
        // This solves Rhino Context thread-locality by keeping all main thread code in same Context
        boolean synchronousMode = "true".equalsIgnoreCase(System.getProperty("javacanvas.test.synchronous", "false"));

        if (isWorker) {
            this.eventLoop = new WorkerThreadEventLoop();
        } else {
            this.eventLoop = new MainThreadEventLoop(synchronousMode);
        }

        // Start the event loop - it will block on the queue until work arrives
        this.eventLoop.start();

        Context context = Context.enter();
        try {
            // Initialize the standard objects (Object, Function, etc.)
            // This must be done before scripts can be executed. Returns
            // a scope object that we use in later calls.

            scope = new ImporterTopLevel(context);

            exec("importPackage(Packages.com.w3canvas.javacanvas.js)");
            exec("importPackage(Packages.com.w3canvas.javacanvas.backend.rhino.impl.node)");
            exec("importPackage(Packages.com.w3canvas.javacanvas.backend.rhino.impl.event)");
            exec("importPackage(Packages.com.w3canvas.javacanvas.backend.rhino.impl.gradient)");
            exec("importPackage(Packages.com.w3canvas.javacanvas.backend.rhino.impl.font)");

            try {
                org.mozilla.javascript.ScriptableObject.defineClass(scope,
                        com.w3canvas.javacanvas.backend.rhino.impl.font.RhinoFontFace.class);
                org.mozilla.javascript.ScriptableObject.defineClass(scope,
                        com.w3canvas.javacanvas.backend.rhino.impl.font.RhinoFontFaceSet.class);
                org.mozilla.javascript.ScriptableObject.defineClass(scope,
                        com.w3canvas.javacanvas.backend.rhino.impl.node.RhinoPath2D.class);
                org.mozilla.javascript.ScriptableObject.defineClass(scope,
                        com.w3canvas.javacanvas.backend.rhino.impl.node.ImageBitmap.class);
                org.mozilla.javascript.ScriptableObject.defineClass(scope,
                        com.w3canvas.javacanvas.backend.rhino.impl.node.Blob.class);
                org.mozilla.javascript.ScriptableObject.defineClass(scope,
                        com.w3canvas.javacanvas.backend.rhino.impl.node.ImageData.class);
                org.mozilla.javascript.ScriptableObject.defineClass(scope,
                        com.w3canvas.javacanvas.js.worker.OffscreenCanvas.class);
                org.mozilla.javascript.ScriptableObject.defineClass(scope,
                        com.w3canvas.javacanvas.backend.rhino.impl.node.CanvasRenderingContext2D.class);
                org.mozilla.javascript.ScriptableObject.defineClass(scope,
                        com.w3canvas.javacanvas.backend.rhino.impl.node.Document.class);
                // Register HTMLCanvasElement for cross-Context method access
                org.mozilla.javascript.ScriptableObject.defineClass(scope,
                        com.w3canvas.javacanvas.backend.rhino.impl.node.HTMLCanvasElement.class);
                // Register Worker, SharedWorker, and MessagePort classes
                org.mozilla.javascript.ScriptableObject.defineClass(scope,
                        com.w3canvas.javacanvas.js.worker.Worker.class);
                org.mozilla.javascript.ScriptableObject.defineClass(scope,
                        com.w3canvas.javacanvas.js.worker.SharedWorker.class);
                org.mozilla.javascript.ScriptableObject.defineClass(scope,
                        com.w3canvas.javacanvas.js.worker.MessagePort.class);
                org.mozilla.javascript.ScriptableObject.defineClass(scope,
                        com.w3canvas.javacanvas.backend.rhino.impl.font.RhinoPromise.class);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            defineProperty("setTimeout", new Callable() {
                public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
                    // Accepts both function and string parameters
                    new Thread(new RhinoScheduler(RhinoRuntime.this, args[0], ((Number) args[1]).intValue(), false))
                            .start();
                    return null;
                }
            });

            defineProperty("setInterval", new Callable() {
                public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
                    // Accepts both function and string parameters
                    RhinoScheduler e = new RhinoScheduler(RhinoRuntime.this, args[0], ((Number) args[1]).intValue(),
                            true);
                    Integer id = Integer.valueOf(intervalId++);
                    intervals.put(id, e);
                    new Thread(e).start();
                    return id;
                }
            });

            defineProperty("clearInterval", new Callable() {
                public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
                    Integer id = Integer.valueOf(((Number) args[0]).intValue());
                    RhinoScheduler e = intervals.get(id);
                    if (e != null) {
                        e.stopLoop();
                        intervals.remove(id);
                    }
                    return null;
                }
            });

            defineProperty("createImageBitmap", new Callable() {
                public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
                    if (args.length == 0) {
                        throw new IllegalArgumentException("createImageBitmap requires at least 1 argument");
                    }

                    Object source = args[0];

                    // Create core ImageBitmap from various source types
                    com.w3canvas.javacanvas.core.ImageBitmap coreImageBitmap = null;

                    try {
                        if (source instanceof com.w3canvas.javacanvas.backend.rhino.impl.node.Image) {
                            // HTMLImageElement
                            coreImageBitmap = new com.w3canvas.javacanvas.core.ImageBitmap(
                                    (com.w3canvas.javacanvas.backend.rhino.impl.node.Image) source);
                        } else if (source instanceof com.w3canvas.javacanvas.backend.rhino.impl.node.HTMLCanvasElement) {
                            // HTMLCanvasElement
                            coreImageBitmap = new com.w3canvas.javacanvas.core.ImageBitmap(
                                    (com.w3canvas.javacanvas.backend.rhino.impl.node.HTMLCanvasElement) source);
                        } else if (source instanceof com.w3canvas.javacanvas.js.worker.OffscreenCanvas) {
                            // OffscreenCanvas - get BufferedImage
                            java.awt.image.BufferedImage img = ((com.w3canvas.javacanvas.js.worker.OffscreenCanvas) source)
                                    .getImage();
                            coreImageBitmap = new com.w3canvas.javacanvas.core.ImageBitmap(img);
                        } else if (source instanceof com.w3canvas.javacanvas.backend.rhino.impl.node.ImageData) {
                            // ImageData - unwrap to core
                            com.w3canvas.javacanvas.interfaces.IImageData coreImageData = ((com.w3canvas.javacanvas.backend.rhino.impl.node.ImageData) source);
                            coreImageBitmap = new com.w3canvas.javacanvas.core.ImageBitmap(coreImageData);
                        } else if (source instanceof com.w3canvas.javacanvas.backend.rhino.impl.node.ImageBitmap) {
                            // ImageBitmap - create copy
                            com.w3canvas.javacanvas.interfaces.IImageBitmap sourceImageBitmap = (com.w3canvas.javacanvas.backend.rhino.impl.node.ImageBitmap) source;
                            coreImageBitmap = new com.w3canvas.javacanvas.core.ImageBitmap(sourceImageBitmap);
                        } else if (source instanceof com.w3canvas.javacanvas.backend.rhino.impl.node.Blob) {
                            // Blob - decode image from blob data
                            com.w3canvas.javacanvas.backend.rhino.impl.node.Blob blob = (com.w3canvas.javacanvas.backend.rhino.impl.node.Blob) source;
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
                        com.w3canvas.javacanvas.backend.rhino.impl.node.ImageBitmap rhinoImageBitmap = new com.w3canvas.javacanvas.backend.rhino.impl.node.ImageBitmap();
                        rhinoImageBitmap.init(coreImageBitmap);

                        // Set up scope and prototype
                        rhinoImageBitmap.setParentScope(scope);
                        try {
                            Scriptable proto = org.mozilla.javascript.ScriptableObject.getClassPrototype(
                                    scope, "ImageBitmap");
                            if (proto != null) {
                                rhinoImageBitmap.setPrototype(proto);
                            }
                        } catch (Exception e) {
                            // Prototype not found, object will still work
                        }

                        return rhinoImageBitmap;
                    } catch (IllegalArgumentException e) {
                        // Throw as JavaScript error so it can be caught by try-catch
                        throw Context.reportRuntimeError(e.getMessage());
                    } catch (Exception e) {
                        throw new RuntimeException("createImageBitmap failed: " + e.getMessage(), e);
                    }
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void defineProperty(String key, Object value) {
        // If the value is a Scriptable, set its parent scope
        if (value instanceof org.mozilla.javascript.Scriptable) {
            org.mozilla.javascript.Scriptable scriptable = (org.mozilla.javascript.Scriptable) value;
            if (scriptable.getParentScope() == null) {
                scriptable.setParentScope(scope);
            }
        }
        scope.put(key, scope, value);
    }

    @Override
    public void putProperty(String name, Object value) {
        defineProperty(name, value);
    }

    public void setSource(String url) {
        this.currentUrl = url;
        defineProperty("documentBase", url);
    }

    @Override
    public Object exec(String expression) {
        return new RhinoScriptRunner(this, expression).run(Context.getCurrentContext());
    }

    @Override
    public Object exec(java.io.Reader reader, String sourceName) {
        try {
            Context cx = Context.getCurrentContext();
            // Set runtime in thread local so Worker/SharedWorker constructors can access it
            cx.putThreadLocal("runtime", this);
            return cx.evaluateReader(scope, reader, sourceName, 1, null);
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Scriptable getScope() {
        return scope;
    }

    public String getCurrentUrl() {
        return currentUrl;
    }

    @Override
    public void close() {
        Context.exit();
    }

    /**
     * Get the event loop for this runtime.
     * The event loop processes messages from MessagePorts and other async tasks.
     * @return The EventLoop instance
     */
    public EventLoop getEventLoop() {
        return eventLoop;
    }

    /**
     * Check if this is a worker runtime.
     * @return true if this runtime is for a Worker/SharedWorker context
     */
    public boolean isWorker() {
        return isWorker;
    }

    /**
     * Set the main thread document object.
     * Only valid for main thread runtimes (not workers).
     * @param document The document object
     */
    public void setMainThreadDocument(Object document) {
        if (!isWorker) {
            this.mainThreadDocument = document;
        }
    }

    /**
     * Set the main thread window object.
     * Only valid for main thread runtimes (not workers).
     * @param window The window object
     */
    public void setMainThreadWindow(Object window) {
        if (!isWorker) {
            this.mainThreadWindow = window;
        }
    }

    /**
     * Set the main thread Context.
     * This allows cross-Context calling by reusing the original Context where
     * document/window/canvas were created.
     * Only valid for main thread runtimes (not workers).
     * @param context The main thread Context
     */
    public void setMainThreadContext(Context context) {
        if (!isWorker) {
            this.mainThreadContext = context;
            System.out.println("DEBUG: Stored main thread Context for cross-Context calling");
        }
    }

    /**
     * Get the main thread Context.
     * @return The stored main thread Context, or null if not set
     */
    public Context getMainThreadContext() {
        return mainThreadContext;
    }

    /**
     * Ensure main thread globals (document, window) are present in the given scope.
     * This is called when a new Context is created on a different thread to ensure
     * the main thread globals remain accessible.
     *
     * Workers don't have document/window, they have WorkerGlobalScope.
     *
     * @param targetScope The scope to inject globals into
     */
    public void ensureMainThreadGlobals(Scriptable targetScope) {
        if (!isWorker && targetScope != null) {
            // Only inject if they're not already there
            if (mainThreadDocument != null && !targetScope.has("document", targetScope)) {
                targetScope.put("document", targetScope, mainThreadDocument);
            }
            if (mainThreadWindow != null && !targetScope.has("window", targetScope)) {
                targetScope.put("window", targetScope, mainThreadWindow);
            }
        }
    }

}
