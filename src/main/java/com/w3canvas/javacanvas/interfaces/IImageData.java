package com.w3canvas.javacanvas.interfaces;

public interface IImageData {
    int getWidth();
    int getHeight();
    ICanvasPixelArray getData();
    String getColorSpace();
}
