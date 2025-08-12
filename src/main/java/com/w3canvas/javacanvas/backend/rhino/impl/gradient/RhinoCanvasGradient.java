package com.w3canvas.javacanvas.backend.rhino.impl.gradient;

import com.w3canvas.javacanvas.interfaces.ICanvasGradient;
import com.w3canvas.javacanvas.backend.rhino.impl.node.ProjectScriptableObject;
import org.mozilla.javascript.Scriptable;

@SuppressWarnings("serial")
public class RhinoCanvasGradient extends ProjectScriptableObject implements ICanvasGradient {

    private ICanvasGradient backendGradient;

    public RhinoCanvasGradient() {
        // for Rhino
    }

    public void init(ICanvasGradient backendGradient) {
        this.backendGradient = backendGradient;
        setParentScope(getParentScope());
        setPrototype(getClassPrototype(getParentScope(), "CanvasGradient"));
    }

    @Override
    public void addColorStop(double offset, String color) {
        if (backendGradient != null) {
            backendGradient.addColorStop(offset, color);
        }
    }

    public void jsFunction_addColorStop(Double offset, String color) {
        addColorStop(offset, color);
    }

    public Object getBackendGradient() {
        return backendGradient;
    }
}
