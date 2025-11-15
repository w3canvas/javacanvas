package com.w3canvas.javacanvas.backend.rhino.impl.node;

import com.w3canvas.javacanvas.interfaces.IImageBitmap;

/**
 * Rhino JavaScript wrapper for ImageBitmap.
 *
 * This class exposes the ImageBitmap interface to JavaScript code running in Rhino,
 * providing the standard HTML5 Canvas ImageBitmap API.
 */
@SuppressWarnings("serial")
public class ImageBitmap extends ProjectScriptableObject implements IImageBitmap {

    private IImageBitmap core;

    public ImageBitmap() {
        // No-arg constructor for Rhino
    }

    /**
     * Initializes this Rhino wrapper with a core ImageBitmap implementation.
     *
     * @param core the core ImageBitmap implementation
     */
    public void init(IImageBitmap core) {
        this.core = core;
    }

    /**
     * JavaScript getter for the width property.
     *
     * @return the width of the bitmap in pixels, or 0 if closed
     */
    public Integer jsGet_width() {
        return core.getWidth();
    }

    /**
     * JavaScript getter for the height property.
     *
     * @return the height of the bitmap in pixels, or 0 if closed
     */
    public Integer jsGet_height() {
        return core.getHeight();
    }

    /**
     * JavaScript function to close the bitmap and release resources.
     */
    public void jsFunction_close() {
        core.close();
    }

    // IImageBitmap implementation - delegates to core

    @Override
    public int getWidth() {
        return core.getWidth();
    }

    @Override
    public int getHeight() {
        return core.getHeight();
    }

    @Override
    public void close() {
        core.close();
    }

    @Override
    public boolean isClosed() {
        return core.isClosed();
    }

    @Override
    public Object getNativeImage() {
        return core.getNativeImage();
    }
}
