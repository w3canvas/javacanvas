package com.w3canvas.javacanvas.backend.graal;

import com.w3canvas.javacanvas.rt.IGraalRuntime;
import com.w3canvas.javacanvas.rt.JSRuntime;
import com.w3canvas.javacanvas.rt.MainThreadEventLoop;
import com.w3canvas.javacanvas.rt.WorkerThreadEventLoop;
import com.w3canvas.javacanvas.rt.EventLoop;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Value;

public class GraalRuntime implements IGraalRuntime {
    private Context context;
    private final EventLoop eventLoop;
    private final boolean isWorker;

    public GraalRuntime() {
        this(false);
    }

    /**
     * Create a GraalRuntime with optional worker event loop.
     * 
     * @param isWorker true if this runtime is for a Worker/SharedWorker context
     */
    public GraalRuntime(boolean isWorker) {
        this.isWorker = isWorker;
        this.eventLoop = isWorker ? new WorkerThreadEventLoop() : new MainThreadEventLoop();
        // Start the event loop - it will block on the queue until work arrives
        this.eventLoop.start();

        this.context = Context.newBuilder("js")
                .allowHostAccess(HostAccess.newBuilder(HostAccess.ALL)
                        .targetTypeMapping(Double.class, Float.class, null, x -> x.floatValue())
                        .build())
                .build();

        // Expose console for logging
        Value bindings = context.getBindings("js");
        bindings.putMember("console", new com.w3canvas.javacanvas.utils.ScriptLogger());
    }

    @Override
    public Object exec(String script) {
        try {
            return context.eval("js", script);
        } catch (Exception e) {
            throw new RuntimeException("Script execution failed", e);
        }
    }

    @Override
    public Object exec(java.io.Reader reader, String sourceName) {
        try {
            // Graal Context.eval accepts a Source object which can be built from a Reader
            org.graalvm.polyglot.Source source = org.graalvm.polyglot.Source.newBuilder("js", reader, sourceName)
                    .build();
            return context.eval(source);
        } catch (Exception e) {
            throw new RuntimeException("Script execution failed", e);
        }
    }

    @Override
    public void putProperty(String name, Object value) {
        context.getBindings("js").putMember(name, value);
    }

    @Override
    public Object getProperty(String name) {
        Value value = context.getBindings("js").getMember(name);
        if (value == null || value.isNull()) {
            return null;
        }
        return value;
    }

    @Override
    public void close() {
        context.close();
    }

    @Override
    public Object getScope() {
        return context.getBindings("js");
    }

    /**
     * Get the event loop for this runtime.
     * The event loop processes messages from MessagePorts and other async tasks.
     * 
     * @return The EventLoop instance
     */
    public EventLoop getEventLoop() {
        return eventLoop;
    }

    /**
     * Check if this is a worker runtime.
     * 
     * @return true if this runtime is for a Worker/SharedWorker context
     */
    public boolean isWorker() {
        return isWorker;
    }

    /**
     * Expose SharedWorker constructor to JavaScript.
     * This creates a JavaScript constructor function that can be used with 'new'.
     */
    public void exposeSharedWorker() {
        Value bindings = context.getBindings("js");

        // Create a JavaScript constructor function
        // GraalJS doesn't directly support 'new' on Java objects, so we create a JS
        // function
        String constructorCode = "(function(scriptUrl) {" +
                "  return Java.type('com.w3canvas.javacanvas.backend.graal.worker.GraalSharedWorkerWrapper')" +
                "    .create(scriptUrl, _runtime);" +
                "})";

        // Store runtime reference for the constructor
        bindings.putMember("_runtime", this);

        // Evaluate and expose the constructor
        Value constructor = context.eval("js", constructorCode);
        bindings.putMember("SharedWorker", constructor);
    }
}
