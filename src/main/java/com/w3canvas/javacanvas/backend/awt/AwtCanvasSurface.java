package com.w3canvas.javacanvas.backend.awt;

import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.awt.Image;
import java.awt.image.ImageObserver;

import com.w3canvas.javacanvas.interfaces.ICanvasSurface;
import com.w3canvas.javacanvas.interfaces.IGraphicsContext;

public class AwtCanvasSurface implements ICanvasSurface {
    private final BufferedImage image;
    private AwtGraphicsContext graphicsContext;

    public AwtCanvasSurface(int width, int height) {
        this.image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    }

    @Override
    public int getWidth() {
        return image.getWidth();
    }

    @Override
    public int getHeight() {
        return image.getHeight();
    }

    @Override
    public IGraphicsContext getGraphicsContext() {
        if (graphicsContext == null) {
            graphicsContext = new AwtGraphicsContext(image.createGraphics());
        }
        return graphicsContext;
    }

    @Override
    public Object getNativeImage() {
        return image;
    }

    @Override
    public int[] getPixelData(int x, int y, int width, int height) {
        int[] pixels = new int[width * height];
        PixelGrabber pg = new PixelGrabber(image, x, y, width, height, pixels, 0, width);
        try {
            pg.grabPixels();
            if ((pg.getStatus() & ImageObserver.ABORT) != 0) {
                System.err.println("image fetch aborted or errored");
                return null;
            }
        } catch (InterruptedException e) {
            System.err.println("interrupted waiting for pixels!");
            Thread.currentThread().interrupt();
            return null;
        }
        return pixels;
    }
}
