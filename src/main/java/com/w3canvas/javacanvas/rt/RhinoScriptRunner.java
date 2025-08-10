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
            cx.putThreadLocal("runtime", runtime);

            if (script instanceof String)
            {
                this.script = cx.compileString((String) script, this.runtime.getCurrentUrl(), 1, null);
            }

            if (script instanceof Script)
            {
                return ((Script) script).exec(cx, runtime.getScope());
            }
            else
            {
                Function fn = (Function) script;
                return fn.call(cx, fn, fn, new Object[0]);
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
            return e;
        }
    }
}