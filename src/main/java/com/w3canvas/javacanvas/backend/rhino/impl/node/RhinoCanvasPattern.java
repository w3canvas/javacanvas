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
        setPrototype(getClassPrototype(getParentScope(), "RhinoCanvasPattern"));
    }

    public Object getBackendPattern() {
        return backendPattern;
    }

    public void jsFunction_setTransform(Object matrix) {
        if (matrix instanceof DOMMatrix) {
            backendPattern.setTransform(((DOMMatrix) matrix).getTransform());
        }
    }

    @Override
    public void setTransform(Object transform) {
        backendPattern.setTransform(transform);
    }
}
