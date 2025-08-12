package com.w3canvas.javacanvas.core;

import com.w3canvas.javacanvas.interfaces.ICanvasPixelArray;

public class CanvasPixelArray implements ICanvasPixelArray {
    private final int[] data;
    private final int width;
    private final int height;

    public CanvasPixelArray(int[] data, int width, int height) {
        this.data = data;
        this.width = width;
        this.height = height;
    }

    public int[] getPixels(int x, int y, int width, int height) {
        // This is a simplified implementation. A real implementation would
        // need to handle dirty rectangles correctly.
        return data;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
