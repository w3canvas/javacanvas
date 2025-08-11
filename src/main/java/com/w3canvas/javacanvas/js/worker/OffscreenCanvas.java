package com.w3canvas.javacanvas.js.worker;

import com.w3canvas.javacanvas.backend.awt.AwtGraphicsBackend;
import com.w3canvas.javacanvas.backend.rhino.impl.node.CanvasRenderingContext2D;
import com.w3canvas.javacanvas.backend.rhino.impl.node.ProjectScriptableObject;
import com.w3canvas.javacanvas.core.CoreCanvasRenderingContext2D;
import com.w3canvas.javacanvas.interfaces.ICanvasSurface;
import com.w3canvas.javacanvas.interfaces.IGraphicsBackend;
import com.w3canvas.javacanvas.interfaces.ICanvasRenderingContext2D;
import com.w3canvas.javacanvas.js.ICanvas;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import java.awt.image.BufferedImage;

@SuppressWarnings("serial")
public class OffscreenCanvas extends ProjectScriptableObject implements ICanvas {
    private int width;
    private int height;
    private IGraphicsBackend backend;
    private ICanvasSurface surface;
    private CanvasRenderingContext2D context;

    public OffscreenCanvas() {}

    public void jsConstructor(int width, int height) {
        this.width = width;
        this.height = height;
        this.backend = new AwtGraphicsBackend();
        this.surface = this.backend.createCanvasSurface(width, height);
    }

    @Override
    public String getClassName() {
        return "OffscreenCanvas";
    }

    public Scriptable jsFunction_getContext(String type) {
        if (context == null && "2d".equals(type)) {
            ICanvasRenderingContext2D coreContext = new CoreCanvasRenderingContext2D(this.backend, getWidth(), getHeight());

            context = new CanvasRenderingContext2D();
            context.init(coreContext);

            Scriptable scope = ScriptableObject.getTopLevelScope(this);
            Context rhinoContext = Context.getCurrentContext();

            context.setParentScope(scope);
            context.setPrototype(ScriptableObject.getClassPrototype(scope, "CanvasRenderingContext2D"));
            context.initCanvas(this);
        }
        return context;
    }

    @Override
    public Integer getWidth() {
        return width;
    }

    @Override
    public Integer getHeight() {
        return height;
    }

    @Override
    public BufferedImage getImage() {
        return (BufferedImage) surface.getNativeImage();
    }

    @Override
    public void dirty() {
        // Not needed for offscreen canvas, as there is no screen to repaint.
    }
}
