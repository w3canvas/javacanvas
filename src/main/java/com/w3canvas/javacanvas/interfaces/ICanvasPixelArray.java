package com.w3canvas.javacanvas.interfaces;

public interface ICanvasPixelArray {
    int getWidth();
    int getHeight();
    int[] getPixels(int x, int y, int width, int height);
}
