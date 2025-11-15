package com.w3canvas.javacanvas.rt;

import java.util.Hashtable;

import org.mozilla.javascript.Callable;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.Scriptable;

public class RhinoRuntime
{

    private Hashtable<Integer, RhinoScheduler> intervals = new Hashtable<Integer, RhinoScheduler>();
    private int intervalId;
    private String currentUrl;
    private Scriptable scope;

    public RhinoRuntime()
    {
        Context context = Context.enter();
        try
        {
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
                org.mozilla.javascript.ScriptableObject.defineClass(scope, com.w3canvas.javacanvas.backend.rhino.impl.font.RhinoFontFace.class);
                org.mozilla.javascript.ScriptableObject.defineClass(scope, com.w3canvas.javacanvas.backend.rhino.impl.font.RhinoFontFaceSet.class);
                org.mozilla.javascript.ScriptableObject.defineClass(scope, com.w3canvas.javacanvas.backend.rhino.impl.node.RhinoPath2D.class);
                org.mozilla.javascript.ScriptableObject.defineClass(scope, com.w3canvas.javacanvas.backend.rhino.impl.node.ImageBitmap.class);
                org.mozilla.javascript.ScriptableObject.defineClass(scope, com.w3canvas.javacanvas.backend.rhino.impl.node.Blob.class);
                org.mozilla.javascript.ScriptableObject.defineClass(scope, com.w3canvas.javacanvas.js.worker.OffscreenCanvas.class);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            defineProperty("setTimeout", new Callable()
            {
                // todo: allow function parameter instead of string (!!!)

                public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args)
                {
                    new Thread(new RhinoScheduler(RhinoRuntime.this, args[0], ((Number) args[1]).intValue(), false))
                        .start();
                    return null;
                }
            });

            defineProperty("setInterval", new Callable()
            {

                public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args)
                {
                    // todo: allow function parameter instead of string (!!!)
                    RhinoScheduler e = new RhinoScheduler(RhinoRuntime.this, args[0], ((Number) args[1]).intValue(),
                        true);
                    Integer id = Integer.valueOf(intervalId++);
                    intervals.put(id, e);
                    new Thread(e).start();
                    return id;
                }
            });

            defineProperty("clearInterval", new Callable()
            {

                public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args)
                {
                    // todo: allow function parameter instead of string (!!!)
                    Integer id = Integer.valueOf(((Number) args[0]).intValue());
                    RhinoScheduler e = intervals.get(id);
                    e.stopLoop();
                    intervals.remove(id);
                    return null;
                }
            });
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public void defineProperty(String key, Object value)
    {
        scope.put(key, scope, value);
    }

    public void setSource(String url)
    {
        this.currentUrl = url;
        defineProperty("documentBase", url);
    }

    public Object exec(String expression)
    {
        return new RhinoScriptRunner(this, expression).run(Context.getCurrentContext());
    }

    public Object exec(java.io.Reader reader, String sourceName)
    {
        try {
            return Context.getCurrentContext().evaluateReader(scope, reader, sourceName, 1, null);
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Scriptable getScope()
    {
        return scope;
    }

    public String getCurrentUrl()
    {
        return currentUrl;
    }

}
