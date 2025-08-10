package com.w3canvas.javacanvas.interfaces;

public interface ICanvasSurface {
    int getWidth();
    int getHeight();
    IGraphicsContext getGraphicsContext();
    Object getNativeImage();
    int[] getPixelData(int x, int y, int width, int height);
}
