package com.w3canvas.javacanvas.core;

import com.w3canvas.javacanvas.interfaces.ICanvasPixelArray;
import com.w3canvas.javacanvas.interfaces.IImageData;

public class ImageData implements IImageData {
    private final int width;
    private final int height;
    private final ICanvasPixelArray data;

    public ImageData(int width, int height, ICanvasPixelArray data) {
        this.width = width;
        this.height = height;
        this.data = data;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public ICanvasPixelArray getData() {
        return data;
    }
}
