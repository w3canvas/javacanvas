package com.w3canvas.javacanvas.backend.rhino.impl.node;

import com.w3canvas.javacanvas.interfaces.ICanvasPattern;
import org.mozilla.javascript.Scriptable;

@SuppressWarnings("serial")
public class RhinoCanvasPattern extends ProjectScriptableObject implements ICanvasPattern {

    private ICanvasPattern backendPattern;

    public RhinoCanvasPattern() {
        // for Rhino
    }

    public void init(ICanvasPattern backendPattern) {
        this.backendPattern = backendPattern;
        setParentScope(getParentScope());
        setPrototype(getClassPrototype(getParentScope(), "CanvasPattern"));
    }

    public Object getBackendPattern() {
        return backendPattern;
    }
}
