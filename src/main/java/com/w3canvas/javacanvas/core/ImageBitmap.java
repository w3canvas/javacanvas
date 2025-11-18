package com.w3canvas.javacanvas.core;

import com.w3canvas.javacanvas.interfaces.IImageBitmap;
import com.w3canvas.javacanvas.interfaces.IImageData;
import com.w3canvas.javacanvas.interfaces.ICanvasRenderingContext2D;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;

/**
 * Core implementation of the ImageBitmap interface.
 *
 * This class represents an immutable bitmap image that can be created from
 * various sources and used as an image source for canvas drawing operations.
 */
public class ImageBitmap implements IImageBitmap {
    private BufferedImage image;
    private int width;
    private int height;
    private boolean closed;

    /**
     * Creates an ImageBitmap from a BufferedImage.
     * Creates a copy of the image to ensure immutability.
     *
     * @param image the source BufferedImage
     */
    public ImageBitmap(BufferedImage image) {
        if (image == null) {
            throw new IllegalArgumentException("Image cannot be null");
        }
        this.width = image.getWidth();
        this.height = image.getHeight();

        // Debug: Check source image pixel values
        if (width > 0 && height > 0) {
            int centerX = width / 2;
            int centerY = height / 2;
            int rgb = image.getRGB(centerX, centerY);
            int a = (rgb >> 24) & 0xff;
            int r = (rgb >> 16) & 0xff;
            int green = (rgb >> 8) & 0xff;
            int b = rgb & 0xff;
            System.out.println("[DEBUG ImageBitmap constructor] Source image center pixel (" + centerX + "," + centerY + ") ARGB=(" + a + "," + r + "," + green + "," + b + ")");
        }

        // Create a copy of the image to ensure immutability
        this.image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = this.image.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();

        // Debug: Check copied image pixel values
        if (width > 0 && height > 0) {
            int centerX = width / 2;
            int centerY = height / 2;
            int rgb = this.image.getRGB(centerX, centerY);
            int a = (rgb >> 24) & 0xff;
            int r = (rgb >> 16) & 0xff;
            int green = (rgb >> 8) & 0xff;
            int b = rgb & 0xff;
            System.out.println("[DEBUG ImageBitmap constructor] Copied image center pixel (" + centerX + "," + centerY + ") ARGB=(" + a + "," + r + "," + green + "," + b + ")");
        }

        this.closed = false;
    }

    /**
     * Creates an ImageBitmap from an HTMLCanvasElement.
     *
     * @param canvas the source canvas element
     */
    public ImageBitmap(com.w3canvas.javacanvas.backend.rhino.impl.node.HTMLCanvasElement canvas) {
        if (canvas == null) {
            throw new IllegalArgumentException("Canvas cannot be null");
        }
        this.image = canvas.getImage();
        this.width = canvas.getWidth();
        this.height = canvas.getHeight();
        this.closed = false;
    }

    /**
     * Creates an ImageBitmap from an Image (HTMLImageElement).
     *
     * @param img the source image element
     */
    public ImageBitmap(com.w3canvas.javacanvas.backend.rhino.impl.node.Image img) {
        if (img == null) {
            throw new IllegalArgumentException("Image cannot be null");
        }
        this.image = img.getImage();
        this.width = img.getRealWidth();
        this.height = img.getRealHeight();
        this.closed = false;
    }

    /**
     * Creates an ImageBitmap from ImageData.
     *
     * @param imageData the source ImageData
     */
    public ImageBitmap(IImageData imageData) {
        if (imageData == null) {
            throw new IllegalArgumentException("ImageData cannot be null");
        }
        this.width = imageData.getWidth();
        this.height = imageData.getHeight();

        // Convert ImageData to BufferedImage
        this.image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        // Get pixel data from ImageData - it's stored as int[] in ARGB format
        int[] pixels = imageData.getData().getPixels(0, 0, width, height);
        image.setRGB(0, 0, width, height, pixels, 0, width);
        this.closed = false;
    }

    /**
     * Creates an ImageBitmap as a copy of another ImageBitmap.
     *
     * @param other the source ImageBitmap to copy
     */
    public ImageBitmap(IImageBitmap other) {
        if (other == null) {
            throw new IllegalArgumentException("ImageBitmap cannot be null");
        }
        if (other.isClosed()) {
            throw new IllegalArgumentException("Cannot create ImageBitmap from a closed ImageBitmap");
        }

        this.width = other.getWidth();
        this.height = other.getHeight();

        // Create a copy of the image
        BufferedImage sourceImage = (BufferedImage) other.getNativeImage();
        this.image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = this.image.createGraphics();
        g.drawImage(sourceImage, 0, 0, null);
        g.dispose();
        this.closed = false;
    }

    @Override
    public int getWidth() {
        return closed ? 0 : width;
    }

    @Override
    public int getHeight() {
        return closed ? 0 : height;
    }

    @Override
    public void close() {
        if (!closed) {
            this.image = null;
            this.closed = true;
        }
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    @Override
    public Object getNativeImage() {
        if (!closed && image != null && image.getWidth() > 0 && image.getHeight() > 0) {
            int centerX = image.getWidth() / 2;
            int centerY = image.getHeight() / 2;
            int rgb = image.getRGB(centerX, centerY);
            int a = (rgb >> 24) & 0xff;
            int r = (rgb >> 16) & 0xff;
            int g = (rgb >> 8) & 0xff;
            int b = rgb & 0xff;
            System.out.println("[DEBUG ImageBitmap.getNativeImage] Returning image, center pixel (" + centerX + "," + centerY + ") ARGB=(" + a + "," + r + "," + g + "," + b + ")");
        }
        return closed ? null : image;
    }
}
