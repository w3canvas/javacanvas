package com.w3canvas.javacanvas.interfaces;

public interface IGraphicsBackend {
    /**
     * Creates a new drawable surface.
     * @param width the width of the surface
     * @param height the height of the surface
     * @return a new ICanvasSurface
     */
    ICanvasSurface createCanvasSurface(int width, int height);
}
