package com.w3canvas.javacanvas.rt;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextAction;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Script;

class RhinoScriptRunner implements ContextAction, Runnable
{

    private final RhinoRuntime runtime;
    private Object script;

    RhinoScriptRunner(RhinoRuntime runtime, Object script)
    {
        this.runtime = runtime;
        this.script = script;
    }

    public void run()
    {
        run(Context.enter());
    }

    public Object run(Context cx)
    {
        try
        {
            // If no context provided, enter a new one
            if (cx == null) {
                cx = Context.enter();
            }

            cx.putThreadLocal("runtime", runtime);

            if (script instanceof String)
            {
                this.script = cx.compileString((String) script, this.runtime.getCurrentUrl(), 1, null);
            }

            if (script instanceof Script)
            {
                return ((Script) script).exec(cx, runtime.getScope());
            }
            else if (script instanceof Function)
            {
                Function fn = (Function) script;
                return fn.call(cx, runtime.getScope(), runtime.getScope(), new Object[0]);
            }
            else
            {
                throw new IllegalArgumentException("Script must be a String or Function, got: " +
                    (script != null ? script.getClass().getName() : "null"));
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
            return e;
        }
    }
}