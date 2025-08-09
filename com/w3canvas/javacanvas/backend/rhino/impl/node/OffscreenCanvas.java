package com.w3canvas.javacanvas.backend.rhino.impl.node;

import java.awt.image.BufferedImage;

import org.mozilla.javascript.Scriptable;

import com.w3canvas.javacanvas.js.ICanvas;
import com.w3canvas.javacanvas.utils.RhinoCanvasUtils;

@SuppressWarnings("serial")
public class OffscreenCanvas extends ProjectScriptableObject implements ICanvas {

    private Image image;
    private CanvasRenderingContext2D context;

    public OffscreenCanvas() {
        // Default constructor for Rhino
    }

    public void jsConstructor(int width, int height) {
        this.image = new Image(width, height);
    }

    @Override
    public BufferedImage getImage() {
        return image.getImage();
    }

    @Override
    public void dirty() {
        // No-op for offscreen canvas
    }

    @Override
    public Integer getWidth() {
        return image.getRealWidth();
    }

    @Override
    public Integer getHeight() {
        return image.getRealHeight();
    }

    public Scriptable jsFunction_getContext(String type) {
        if ("2d".equals(type)) {
            if (context == null) {
                context = (CanvasRenderingContext2D) RhinoCanvasUtils.getScriptableInstance(CanvasRenderingContext2D.class, null);
                context.initCanvas(this);
            }
            return context;
        }
        return null;
    }

    @Override
    public String getClassName() {
        return "OffscreenCanvas";
    }
}
