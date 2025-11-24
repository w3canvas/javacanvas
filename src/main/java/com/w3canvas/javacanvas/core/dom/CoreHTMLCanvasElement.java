package com.w3canvas.javacanvas.core.dom;

import com.w3canvas.javacanvas.interfaces.IDocument;
import com.w3canvas.javacanvas.interfaces.ICanvasRenderingContext2D;
import com.w3canvas.javacanvas.interfaces.IGraphicsBackend;
import com.w3canvas.javacanvas.core.CoreCanvasRenderingContext2D;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;
import java.util.HashMap;
import java.util.Map;

/**
 * Core HTMLCanvasElement implementation - backend agnostic.
 *
 * This class is NOT tied to Rhino Context, so it can be safely accessed
 * from multiple threads and Contexts. This solves the Worker/SharedWorker
 * cross-Context communication issue.
 *
 * The Rhino adapter (HTMLCanvasElement) wraps this core implementation
 * and provides JavaScript bindings.
 */
public class CoreHTMLCanvasElement extends CoreElement {

    private static final int DEFAULT_WIDTH = 300;
    private static final int DEFAULT_HEIGHT = 150;

    private static final Map<String, String> MIME_TO_FORMAT = new HashMap<>();
    static {
        MIME_TO_FORMAT.put("image/png", "png");
        MIME_TO_FORMAT.put("image/jpeg", "jpeg");
        MIME_TO_FORMAT.put("image/x-png", "png");
        MIME_TO_FORMAT.put("image/gif", "gif");
        MIME_TO_FORMAT.put("image/svg+xml", "svg");
    }

    private int width;
    private int height;
    private BufferedImage image;

    // The rendering context is managed by the backend adapter (Rhino, GraalJS)
    // Core just tracks dimensions and image

    /**
     * Create a canvas element with default dimensions.
     * @param ownerDocument the owner document
     */
    public CoreHTMLCanvasElement(IDocument ownerDocument) {
        this(DEFAULT_WIDTH, DEFAULT_HEIGHT, ownerDocument);
    }

    /**
     * Create a canvas element with specified dimensions.
     * @param width the canvas width
     * @param height the canvas height
     * @param ownerDocument the owner document
     */
    public CoreHTMLCanvasElement(int width, int height, IDocument ownerDocument) {
        super("canvas", ownerDocument);
        this.width = width;
        this.height = height;
        createImage();
    }

    /**
     * Create or recreate the underlying BufferedImage.
     */
    private void createImage() {
        if (width > 0 && height > 0) {
            BufferedImage oldImage = image;
            image = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);

            // If we had an old image, copy the content
            if (oldImage != null) {
                java.awt.Graphics g = image.getGraphics();
                g.drawImage(oldImage, 0, 0, null);
                g.dispose();
            }
        }
    }

    /**
     * Get the canvas width.
     * @return the width in pixels
     */
    public int getWidth() {
        return width;
    }

    /**
     * Set the canvas width.
     * Resizing the canvas clears its content.
     * @param width the new width in pixels
     */
    public void setWidth(int width) {
        if (this.width != width) {
            this.width = width;
            createImage();
        }
    }

    /**
     * Get the canvas height.
     * @return the height in pixels
     */
    public int getHeight() {
        return height;
    }

    /**
     * Set the canvas height.
     * Resizing the canvas clears its content.
     * @param height the new height in pixels
     */
    public void setHeight(int height) {
        if (this.height != height) {
            this.height = height;
            createImage();
        }
    }

    /**
     * Get the underlying BufferedImage.
     * This is the canvas pixel data that can be accessed cross-Context.
     * @return the image
     */
    public BufferedImage getImage() {
        return image;
    }

    /**
     * Set the underlying BufferedImage.
     * Used by the rendering context to update the canvas pixels.
     * @param image the new image
     */
    public void setImage(BufferedImage image) {
        this.image = image;
    }

    /**
     * Convert canvas to data URL.
     * @param mimeType the MIME type (e.g., "image/png")
     * @return the data URL string
     */
    public String toDataURL(String mimeType) {
        try {
            String format = MIME_TO_FORMAT.getOrDefault(mimeType, "png");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, format, baos);
            byte[] bytes = baos.toByteArray();

            // Convert to base64
            String base64 = java.util.Base64.getEncoder().encodeToString(bytes);
            return "data:" + mimeType + ";base64," + base64;
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert canvas to data URL: " + e.getMessage(), e);
        }
    }

    /**
     * Check if a context type is supported.
     * @param contextType the context type (e.g., "2d", "webgl")
     * @return true if supported
     */
    public boolean isContextTypeSupported(String contextType) {
        return "2d".equals(contextType);
    }
}
