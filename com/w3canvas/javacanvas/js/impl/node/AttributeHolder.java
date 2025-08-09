package com.w3canvas.javacanvas.js.impl.node;

import java.util.HashMap;
import java.util.Map;

import org.mozilla.javascript.ScriptableObject;

public class AttributeHolder extends ScriptableObject
{

    private Map<Object, Object> params;

    public AttributeHolder()
    {
        params = new HashMap<Object, Object>();
    }

    public Object put(Object key, Object value)
    {
        params.put(key, value);
        return value;
    }

    public Object get(Object key)
    {
        return params.get(key);
    }

    public String getClassName()
    {
        return toString();
    }
/*
    public Object getDefaultValue(Class<?> typeHint)
    {
        return toString();
    }
*/
    public String toString()
    {
        return this.getClass().getSimpleName();
    }

}
