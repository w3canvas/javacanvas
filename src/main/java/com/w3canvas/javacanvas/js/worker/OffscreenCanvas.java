package com.w3canvas.javacanvas.js.worker;

import com.w3canvas.javacanvas.backend.awt.AwtGraphicsBackend;
import com.w3canvas.javacanvas.backend.rhino.impl.node.CanvasRenderingContext2D;
import com.w3canvas.javacanvas.backend.rhino.impl.node.ProjectScriptableObject;
import com.w3canvas.javacanvas.backend.rhino.impl.node.Blob;
import com.w3canvas.javacanvas.backend.rhino.impl.node.ImageBitmap;
import com.w3canvas.javacanvas.core.CoreCanvasRenderingContext2D;
import com.w3canvas.javacanvas.interfaces.ICanvasSurface;
import com.w3canvas.javacanvas.interfaces.IGraphicsBackend;
import com.w3canvas.javacanvas.interfaces.ICanvasRenderingContext2D;
import com.w3canvas.javacanvas.js.ICanvas;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

@SuppressWarnings("serial")
public class OffscreenCanvas extends ProjectScriptableObject implements ICanvas {
    private int width;
    private int height;
    private IGraphicsBackend backend;
    private ICanvasSurface surface;
    private CanvasRenderingContext2D context;

    public OffscreenCanvas() {}

    public void jsConstructor(int width, int height) {
        this.width = width;
        this.height = height;
        this.backend = new AwtGraphicsBackend();
        // Don't create surface here - let the context create it
    }

    @Override
    public String getClassName() {
        return "OffscreenCanvas";
    }

    public Scriptable jsFunction_getContext(String type) {
        if (context == null && "2d".equals(type)) {
            try {
                ICanvasRenderingContext2D coreContext = new CoreCanvasRenderingContext2D(null, this.backend, getWidth(), getHeight());

                // Get the surface that CoreCanvasRenderingContext2D created
                this.surface = coreContext.getSurface();

                context = new CanvasRenderingContext2D();
                context.init(coreContext);

                Scriptable scope = ScriptableObject.getTopLevelScope(this);
                Context rhinoContext = Context.getCurrentContext();

                context.setParentScope(scope);
                // Try to get the CanvasRenderingContext2D prototype if available
                try {
                    Scriptable proto = ScriptableObject.getClassPrototype(scope, "CanvasRenderingContext2D");
                    if (proto != null) {
                        context.setPrototype(proto);
                    }
                } catch (Exception e) {
                    // Prototype not found, that's OK - the object will still work
                }
                context.initCanvas(this);
            } catch (Exception e) {
                throw new RuntimeException("Failed to create 2d context for OffscreenCanvas: " + e.getMessage(), e);
            }
        }
        return context;
    }

    /**
     * Convert the canvas contents to a Blob.
     * Returns a Promise that resolves to a Blob containing the image data.
     *
     * @param type The image MIME type (default: "image/png")
     * @param quality The image quality for lossy formats (0.0 to 1.0)
     * @return A Promise that resolves to a Blob
     */
    public void jsFunction_convertToBlob(Function callback, Object type, Object quality) {
        String mimeType = "image/png";

        if (type != null && type != Context.getUndefinedValue() && type instanceof String) {
            mimeType = (String) type;
        }

        try {
            Blob blob = createBlob(mimeType);

            if (callback != null && callback instanceof Function) {
                Context cx = Context.getCurrentContext();
                Scriptable scope = ScriptableObject.getTopLevelScope(this);
                callback.call(cx, scope, this, new Object[]{blob});
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to create blob: " + e.getMessage(), e);
        }
    }

    /**
     * Synchronous version of convertToBlob for simplified use.
     * @param type The image MIME type
     * @return A Blob containing the image data
     */
    public Scriptable jsFunction_convertToBlobSync(Object type) {
        String mimeType = "image/png";

        if (type != null && type != Context.getUndefinedValue() && type instanceof String) {
            mimeType = (String) type;
        }

        try {
            return createBlob(mimeType);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create blob: " + e.getMessage(), e);
        }
    }

    /**
     * Helper method to create a Blob from the canvas image.
     * @param mimeType The MIME type for the blob
     * @return A Blob containing the image data
     */
    private Blob createBlob(String mimeType) throws IOException {
        BufferedImage image = getImage();
        if (image == null) {
            throw new IllegalStateException("No image available");
        }

        // Default to PNG if MIME type is not supported
        String outputFormat = "png";
        String outputMimeType = "image/png";

        mimeType = mimeType.toLowerCase();
        if (!mimeType.isEmpty() && Arrays.asList(ImageIO.getWriterMIMETypes()).contains(mimeType)) {
            outputMimeType = mimeType;
            if (mimeType.equals("image/png") || mimeType.equals("image/x-png")) {
                outputFormat = "png";
            } else if (mimeType.equals("image/jpeg") || mimeType.equals("image/jpg")) {
                outputFormat = "jpeg";
            } else if (mimeType.equals("image/gif")) {
                outputFormat = "gif";
            }
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, outputFormat, bos);
            byte[] imageData = bos.toByteArray();

            // Create the core Blob
            com.w3canvas.javacanvas.core.Blob coreBlob =
                new com.w3canvas.javacanvas.core.Blob(imageData, outputMimeType);

            // Create the Rhino wrapper
            Blob rhinoBlob = new Blob();
            rhinoBlob.init(coreBlob);

            // Set up the Rhino wrapper's scope and prototype
            Scriptable scope = ScriptableObject.getTopLevelScope(this);
            rhinoBlob.setParentScope(scope);
            rhinoBlob.setPrototype(ScriptableObject.getClassPrototype(scope, "Blob"));

            return rhinoBlob;
        } finally {
            bos.close();
        }
    }

    /**
     * Transfer the canvas contents to an ImageBitmap.
     * Returns an ImageBitmap and detaches the OffscreenCanvas.
     * Note: In this implementation, we create a copy rather than truly transferring,
     * as Java doesn't support the same memory model as JavaScript.
     *
     * @return An ImageBitmap containing the canvas image data
     */
    public Scriptable jsFunction_transferToImageBitmap() {
        BufferedImage image = getImage();
        if (image == null) {
            throw new IllegalStateException("No image available");
        }

        // Create a copy of the current image for the ImageBitmap
        BufferedImage copiedImage = new BufferedImage(
            image.getWidth(),
            image.getHeight(),
            BufferedImage.TYPE_INT_ARGB
        );
        java.awt.Graphics2D g = copiedImage.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();

        // Create the core ImageBitmap
        com.w3canvas.javacanvas.core.ImageBitmap coreImageBitmap =
            new com.w3canvas.javacanvas.core.ImageBitmap(copiedImage);

        // Create the Rhino wrapper
        ImageBitmap rhinoImageBitmap = new ImageBitmap();
        rhinoImageBitmap.init(coreImageBitmap);

        // Set up the Rhino wrapper's scope and prototype
        Scriptable scope = ScriptableObject.getTopLevelScope(this);
        rhinoImageBitmap.setParentScope(scope);
        rhinoImageBitmap.setPrototype(ScriptableObject.getClassPrototype(scope, "ImageBitmap"));

        // Clear the current canvas (transfer semantics)
        // Recreate the surface to "detach" the previous image
        this.surface = this.backend.createCanvasSurface(width, height);
        if (context != null) {
            // Reset the context to use the new surface
            ICanvasRenderingContext2D coreContext = new CoreCanvasRenderingContext2D(null, this.backend, getWidth(), getHeight());
            context.init(coreContext);
        }

        return rhinoImageBitmap;
    }

    @Override
    public Integer getWidth() {
        return width;
    }

    @Override
    public Integer getHeight() {
        return height;
    }

    /**
     * JavaScript setter for the width property.
     * Resizing the canvas clears its content.
     * @param newWidth The new width
     */
    public void jsSet_width(int newWidth) {
        if (newWidth != this.width) {
            this.width = newWidth;
            resizeCanvas();
        }
    }

    /**
     * JavaScript getter for the width property.
     * @return The canvas width
     */
    public Integer jsGet_width() {
        return width;
    }

    /**
     * JavaScript setter for the height property.
     * Resizing the canvas clears its content.
     * @param newHeight The new height
     */
    public void jsSet_height(int newHeight) {
        if (newHeight != this.height) {
            this.height = newHeight;
            resizeCanvas();
        }
    }

    /**
     * JavaScript getter for the height property.
     * @return The canvas height
     */
    public Integer jsGet_height() {
        return height;
    }

    /**
     * Helper method to resize the canvas and reset the rendering context.
     */
    private void resizeCanvas() {
        // Recreate the surface with new dimensions
        this.surface = this.backend.createCanvasSurface(width, height);

        // Reset the rendering context if it exists
        if (context != null) {
            ICanvasRenderingContext2D coreContext = new CoreCanvasRenderingContext2D(null, this.backend, width, height);
            context.init(coreContext);
            context.initCanvas(this);
        }
    }

    @Override
    public BufferedImage getImage() {
        System.out.println("[DEBUG OffscreenCanvas.getImage] Called for " + width + "x" + height + " canvas");
        if (surface == null) {
            // Create surface if not yet initialized
            surface = backend.createCanvasSurface(width, height);
        }
        // Get the image from the context's surface, which has the rendered content
        BufferedImage resultImage = null;
        if (context != null) {
            ICanvasSurface contextSurface = context.getSurface();
            if (contextSurface != null && contextSurface.getNativeImage() != null) {
                resultImage = (BufferedImage) contextSurface.getNativeImage();
                System.out.println("[DEBUG OffscreenCanvas.getImage] Got image from context surface");
            }
        }
        if (resultImage == null) {
            resultImage = (BufferedImage) surface.getNativeImage();
            System.out.println("[DEBUG OffscreenCanvas.getImage] Got image from direct surface");
        }

        // Debug: Check pixel values
        if (resultImage != null && resultImage.getWidth() > 0 && resultImage.getHeight() > 0) {
            int centerX = resultImage.getWidth() / 2;
            int centerY = resultImage.getHeight() / 2;
            int rgb = resultImage.getRGB(centerX, centerY);
            int a = (rgb >> 24) & 0xff;
            int r = (rgb >> 16) & 0xff;
            int g = (rgb >> 8) & 0xff;
            int b = rgb & 0xff;
            System.out.println("[DEBUG OffscreenCanvas.getImage] Returning image, center pixel (" + centerX + "," + centerY + ") ARGB=(" + a + "," + r + "," + g + "," + b + ")");
        }

        return resultImage;
    }

    @Override
    public void dirty() {
        // Not needed for offscreen canvas, as there is no screen to repaint.
    }
}
