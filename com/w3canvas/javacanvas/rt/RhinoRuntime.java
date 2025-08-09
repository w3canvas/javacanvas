package com.w3canvas.javacanvas.rt;

import java.util.Hashtable;

import org.mozilla.javascript.Callable;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.Scriptable;

public class RhinoRuntime
{

    private Hashtable intervals = new Hashtable();
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
            exec("importPackage(Packages.com.w3canvas.javacanvas.js.impl)");

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
                    Integer id = new Integer(intervalId++);
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
                    Integer id = new Integer(((Number) args[0]).intValue());
                    RhinoScheduler e = (RhinoScheduler) intervals.get(id);
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

    // FIXME restore accessor type to "private"
    protected Object exec(String expression)
    {
        return new RhinoScriptRunner(this, expression).run(Context.getCurrentContext());
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
