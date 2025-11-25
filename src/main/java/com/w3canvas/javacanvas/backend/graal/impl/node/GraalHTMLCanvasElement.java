package com.w3canvas.javacanvas.backend.graal.impl.node;

import org.graalvm.polyglot.HostAccess;

import com.w3canvas.javacanvas.backend.awt.AwtGraphicsBackend;
import com.w3canvas.javacanvas.core.CoreCanvasRenderingContext2D;
import com.w3canvas.javacanvas.core.dom.CoreHTMLCanvasElement;

import java.awt.image.BufferedImage;

/**
 * GraalJS adapter for HTMLCanvasElement.
 * Wraps CoreHTMLCanvasElement and exposes it to GraalJS via @HostAccess.
 */
public class GraalHTMLCanvasElement {
    private final CoreHTMLCanvasElement core;
    private CoreCanvasRenderingContext2D context2d;
    private String id;

    public GraalHTMLCanvasElement() {
        this(300, 150);
    }

    public GraalHTMLCanvasElement(int width, int height) {
        this.core = new CoreHTMLCanvasElement(width, height, null);
    }

    @HostAccess.Export
    public int getWidth() {
        return core.getWidth();
    }

    @HostAccess.Export
    public void setWidth(int width) {
        core.setWidth(width);
        // Reset context if it exists
        if (context2d != null) {
            context2d = null;
        }
    }

    @HostAccess.Export
    public int getHeight() {
        return core.getHeight();
    }

    @HostAccess.Export
    public void setHeight(int height) {
        core.setHeight(height);
        // Reset context if it exists
        if (context2d != null) {
            context2d = null;
        }
    }

    @HostAccess.Export
    public String getId() {
        return id;
    }

    @HostAccess.Export
    public void setId(String id) {
        this.id = id;
        core.setId(id);
    }

    @HostAccess.Export
    public Object getContext(String contextType) {
        if ("2d".equals(contextType)) {
            if (context2d == null) {
                // Create context with AWT backend (default)
                AwtGraphicsBackend backend = new AwtGraphicsBackend();
                context2d = new CoreCanvasRenderingContext2D(null, backend,
                    core.getWidth(), core.getHeight());
            }
            return new GraalCanvasRenderingContext2D(context2d);
        }
        return null;
    }

    /**
     * Get the underlying BufferedImage for this canvas.
     * Used for screenshot saving and rendering.
     */
    @HostAccess.Export
    public BufferedImage getImage() {
        if (context2d != null && context2d.getSurface() != null) {
            Object nativeImage = context2d.getSurface().getNativeImage();
            if (nativeImage instanceof BufferedImage) {
                return (BufferedImage) nativeImage;
            }
        }
        return core.getImage();
    }

    /**
     * Get the core element (for internal use).
     */
    public CoreHTMLCanvasElement getCore() {
        return core;
    }

    /**
     * Get the 2D context (for internal use).
     */
    public CoreCanvasRenderingContext2D getContext2D() {
        return context2d;
    }
}
