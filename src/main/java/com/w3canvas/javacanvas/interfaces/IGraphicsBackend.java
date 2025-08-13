package com.w3canvas.javacanvas.interfaces;

public interface IGraphicsBackend {
    /**
     * Creates a new drawable surface.
     * @param width the width of the surface
     * @param height the height of the surface
     * @return a new ICanvasSurface
     */
    ICanvasSurface createCanvasSurface(int width, int height);

    ICanvasGradient createLinearGradient(double x0, double y0, double x1, double y1);

    ICanvasGradient createRadialGradient(double x0, double y0, double r0, double x1, double y1, double r1);

    ICanvasPattern createPattern(Object image, String repetition);

    IFont createFont(String family, double size);
}
