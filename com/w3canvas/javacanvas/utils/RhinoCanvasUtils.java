package com.w3canvas.javacanvas.utils;

import java.lang.reflect.Method;

import net.sf.css4j.Value;
import com.w3canvas.javacanvas.js.impl.node.ProjectScriptableObject;
import com.w3canvas.javacanvas.rt.RhinoRuntime;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

public class RhinoCanvasUtils
{

    private RhinoCanvasUtils()
    {
    }

    private static Integer exchange2Int(Object value)
    {
        Integer result = 0;

        if (value == null)
        {
            return result;
        }
        else if (value instanceof Integer)
        {
            result = (Integer) value;
        }
        else if (value instanceof String)
        {
            Value cssValue = new Value((String) value);
            result = (int) Math.round(cssValue.getNumValue());
        }

        return result;
    }

    public static Integer getIntValue(Object obj, String methodName)
    {
        Integer intResult = 0;
        try
        {
            Method method = obj.getClass().getMethod(methodName);
            Object result = method.invoke(obj);
            intResult = RhinoCanvasUtils.exchange2Int(result);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return intResult;
    }

    private static Scriptable getScope()
    {
        RhinoRuntime runtime = (RhinoRuntime)Context.getCurrentContext().getThreadLocal("runtime");
        assert (runtime != null);
        return runtime.getScope();
    }

    @SuppressWarnings("unchecked")
    public static <T extends ProjectScriptableObject> T getScriptableInstance(Class<T> className, Object[] cParams)
    {
        return (T) Context.getCurrentContext().newObject(getScope(), className.getSimpleName(), cParams);
    }
}
