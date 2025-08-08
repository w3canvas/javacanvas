package com.w3canvas.javacanvas.js.impl.node;

import org.mozilla.javascript.ScriptableObject;

public abstract class ProjectScriptableObject extends ScriptableObject
{
	@Override
    public String getClassName()
    {
        return toString();
    }

	@Override
    public String toString()
    {
        return this.getClass().getSimpleName();
    }

}
