package com.w3canvas.javacanvas.interfaces;

/**
 * ImageBitmap interface representing an immutable bitmap image.
 *
 * According to the HTML5 Canvas specification, an ImageBitmap represents
 * an immutable bitmap image that can be painted to a canvas without undue
 * latency. It can be created from various sources including HTMLCanvasElement,
 * HTMLImageElement, ImageData, and other ImageBitmap objects.
 */
public interface IImageBitmap {
    /**
     * Returns the intrinsic width of the image in CSS pixels.
     * @return the width of the bitmap
     */
    int getWidth();

    /**
     * Returns the intrinsic height of the image in CSS pixels.
     * @return the height of the bitmap
     */
    int getHeight();

    /**
     * Releases the underlying bitmap data and resources.
     * After calling close(), the width and height should return 0.
     */
    void close();

    /**
     * Returns whether the bitmap has been closed.
     * @return true if close() has been called, false otherwise
     */
    boolean isClosed();

    /**
     * Returns the native image data (e.g., BufferedImage for AWT backend).
     * @return the native image object
     */
    Object getNativeImage();
}
