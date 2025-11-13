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
        return new AwtLinearGradient(x0, y0, x1, y1, this);
    }

    @Override
    public ICanvasGradient createRadialGradient(double x0, double y0, double r0, double x1, double y1, double r1) {
        return new AwtRadialGradient(x0, y0, r0, x1, y1, r1, this);
    }

    @Override
    public ICanvasGradient createConicGradient(double startAngle, double x, double y) {
        // TODO: Implement proper conic gradient for AWT
        // For now, return a radial gradient as temporary fallback
        return new AwtRadialGradient(x, y, 0, x, y, 100, this);
    }

    @Override
    public ICanvasPattern createPattern(Object image, String repetition) {
        return new AwtPattern(image, repetition);
    }

    public ITextMetrics measureText(String text) {
        // This should be implemented in a way that doesn't require a Graphics context,
        // but for now, we'll return null and implement it in AwtGraphicsContext.
        return null;
    }

    @Override
    public com.w3canvas.javacanvas.interfaces.IFont createFont(String family, double size) {
        return new AwtFont(family, (float) size, "normal", "normal");
    }

    @Override
    public com.w3canvas.javacanvas.interfaces.IFont createFont(String family, double size, String style, String weight) {
        return new AwtFont(family, (float) size, style, weight);
    }

    @Override
    public com.w3canvas.javacanvas.interfaces.IFont createFont(byte[] fontData, float size, String style, String weight) {
        try {
            java.awt.Font awtFont = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, new java.io.ByteArrayInputStream(fontData));
            return new AwtFont(awtFont, (float)size, style, weight);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
