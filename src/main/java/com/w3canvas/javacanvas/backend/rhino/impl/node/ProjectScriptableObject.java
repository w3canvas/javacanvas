package com.w3canvas.javacanvas.backend.rhino.impl.node;

import org.mozilla.javascript.ScriptableObject;

public abstract class ProjectScriptableObject extends ScriptableObject
{
	@Override
    public String getClassName()
    {
        return this.getClass().getSimpleName();
    }

	@Override
    public String toString()
    {
        return "[object " + getClassName() + "]";
    }

    /**
     * JavaScript-accessible toString() method.
     * This is required for Rhino to be able to convert objects to strings in JavaScript.
     */
    public String jsFunction_toString()
    {
        return toString();
    }

    /**
     * JavaScript-accessible valueOf() method.
     * Returns the object itself, which allows Rhino to call toString() when needed.
     */
    public Object jsFunction_valueOf()
    {
        return toString();
    }

}
