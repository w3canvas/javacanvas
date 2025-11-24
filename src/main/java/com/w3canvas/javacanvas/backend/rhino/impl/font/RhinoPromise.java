package com.w3canvas.javacanvas.backend.rhino.impl.font;

import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import java.util.concurrent.CompletableFuture;

public class RhinoPromise extends ScriptableObject {

    private CompletableFuture<?> future;

    public RhinoPromise() {
        // Required for defineClass
    }

    public RhinoPromise(Object future) {
        this.future = (CompletableFuture<?>) future;
    }

    @Override
    public String getClassName() {
        return "Promise";
    }

    public void jsFunction_then(Function onFulfilled, Function onRejected) {
        Context cx = Context.getCurrentContext();
        Scriptable scope = getParentScope();

        future.thenAccept(result -> {
            if (onFulfilled != null) {
                // We need to be careful about threading here.
                // Rhino contexts are thread-local.
                // For this simple test case, we might get away with it if the future completes
                // immediately
                // or if we ignore the thread safety for a moment.
                // Ideally, we should post this back to the main loop.
                try {
                    // This is dangerous if called from a different thread!
                    // But for FontFace.load() which spawns a thread, the callback happens on that
                    // thread.
                    // We'll need to enter a context.
                    Context callbackCx = Context.enter();
                    try {
                        onFulfilled.call(callbackCx, scope, this, new Object[] { result });
                    } finally {
                        Context.exit();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).exceptionally(ex -> {
            if (onRejected != null) {
                try {
                    Context callbackCx = Context.enter();
                    try {
                        onRejected.call(callbackCx, scope, this, new Object[] { ex.getMessage() });
                    } finally {
                        Context.exit();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return null;
        });
    }

    @Override
    public Object getDefaultValue(Class<?> typeHint) {
        return "[object Promise]";
    }
}
