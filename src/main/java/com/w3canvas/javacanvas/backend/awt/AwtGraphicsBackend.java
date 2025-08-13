package com.w3canvas.javacanvas.backend.awt;

import com.w3canvas.javacanvas.interfaces.ICanvasGradient;
import com.w3canvas.javacanvas.interfaces.ICanvasPattern;
import com.w3canvas.javacanvas.interfaces.ICanvasSurface;
import com.w3canvas.javacanvas.interfaces.IGraphicsBackend;
import com.w3canvas.javacanvas.interfaces.ITextMetrics;

public class AwtGraphicsBackend implements IGraphicsBackend {

    @Override
    public ICanvasSurface createCanvasSurface(int width, int height) {
        return new AwtCanvasSurface(width, height);
    }

    @Override
    public ICanvasGradient createLinearGradient(double x0, double y0, double x1, double y1) {
        // Not implemented for AWT backend
        return null;
    }

    @Override
    public ICanvasGradient createRadialGradient(double x0, double y0, double r0, double x1, double y1, double r1) {
        // Not implemented for AWT backend
        return null;
    }

    @Override
    public ICanvasPattern createPattern(Object image, String repetition) {
        // Not implemented for AWT backend
        return null;
    }

    public ITextMetrics measureText(String text) {
        // Not implemented for AWT backend
        return null;
    }

    @Override
    public com.w3canvas.javacanvas.interfaces.IFont createFont(String family, double size) {
        // Not implemented for AWT backend
        return null;
    }
}
