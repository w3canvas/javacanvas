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
        // Validate bounds
        if (x < 0 || y < 0 || x + width > this.width || y + height > this.height) {
            throw new IllegalArgumentException(
                String.format("Requested region [%d, %d, %d, %d] is out of bounds for array [%d, %d]",
                    x, y, width, height, this.width, this.height));
        }

        // If requesting the entire array, return it directly
        if (x == 0 && y == 0 && width == this.width && height == this.height) {
            return data;
        }

        // Extract the subregion (dirty rectangle)
        int[] result = new int[width * height];
        for (int row = 0; row < height; row++) {
            int srcPos = (y + row) * this.width + x;
            int destPos = row * width;
            System.arraycopy(data, srcPos, result, destPos, width);
        }

        return result;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
