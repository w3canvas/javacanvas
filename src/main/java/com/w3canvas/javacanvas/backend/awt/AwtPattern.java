package com.w3canvas.javacanvas.backend.awt;

import com.w3canvas.javacanvas.interfaces.ICanvasPattern;
import com.w3canvas.javacanvas.interfaces.IPaint;

import java.awt.Paint;
import java.awt.PaintContext;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.TexturePaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

public class AwtPattern implements ICanvasPattern, IPaint {
    private final BufferedImage image;
    private final String repetition;

    public AwtPattern(Object image, String repetition) {
        if (image instanceof BufferedImage) {
            this.image = (BufferedImage) image;
            this.repetition = repetition;
        } else {
            throw new IllegalArgumentException("Image must be a BufferedImage for AWT backend.");
        }
    }

    public Paint getPaint() {
        return new CustomPatternPaint(image, repetition);
    }

    private static class CustomPatternPaint implements Paint {
        private final BufferedImage image;
        private final String repetition;

        public CustomPatternPaint(BufferedImage image, String repetition) {
            this.image = image;
            this.repetition = repetition;
        }

        @Override
        public PaintContext createContext(ColorModel cm, Rectangle deviceBounds, Rectangle2D userBounds, AffineTransform xform, RenderingHints hints) {
            return new CustomPatternPaintContext(image, repetition);
        }

        @Override
        public int getTransparency() {
            return image.getColorModel().getTransparency();
        }
    }

    private static class CustomPatternPaintContext implements PaintContext {
        private final BufferedImage image;
        private final String repetition;
        private final int width;
        private final int height;

        public CustomPatternPaintContext(BufferedImage image, String repetition) {
            this.image = image;
            this.repetition = repetition;
            this.width = image.getWidth();
            this.height = image.getHeight();
        }

        @Override
        public void dispose() {
        }

        @Override
        public ColorModel getColorModel() {
            return image.getColorModel();
        }

        @Override
        public Raster getRaster(int x, int y, int w, int h) {
            WritableRaster raster = getColorModel().createCompatibleWritableRaster(w, h);
            int[] data = new int[w * h * 4];
            for (int j = 0; j < h; j++) {
                for (int i = 0; i < w; i++) {
                    int currentX = x + i;
                    int currentY = y + j;

                    int imageX = currentX;
                    int imageY = currentY;

                    boolean draw = true;
                    if ("repeat-x".equalsIgnoreCase(repetition)) {
                        if (imageY < 0 || imageY >= height) {
                            draw = false;
                        }
                        imageX %= width;
                        if (imageX < 0) {
                            imageX += width;
                        }
                    } else if ("repeat-y".equalsIgnoreCase(repetition)) {
                        if (imageX < 0 || imageX >= width) {
                            draw = false;
                        }
                        imageY %= height;
                        if (imageY < 0) {
                            imageY += height;
                        }
                    } else if ("no-repeat".equalsIgnoreCase(repetition)) {
                        if (imageX < 0 || imageX >= width || imageY < 0 || imageY >= height) {
                            draw = false;
                        }
                    } else { // "repeat" or invalid/default
                        imageX %= width;
                        if (imageX < 0) {
                            imageX += width;
                        }
                        imageY %= height;
                        if (imageY < 0) {
                            imageY += height;
                        }
                    }

                    if (draw) {
                        int rgb = image.getRGB(imageX, imageY);
                        int base = (j * w + i) * 4;
                        data[base + 0] = (rgb >> 16) & 0xFF; // R
                        data[base + 1] = (rgb >> 8) & 0xFF;  // G
                        data[base + 2] = (rgb >> 0) & 0xFF;  // B
                        data[base + 3] = (rgb >> 24) & 0xFF; // A
                    } else {
                        int base = (j * w + i) * 4;
                        data[base + 0] = 0;
                        data[base + 1] = 0;
                        data[base + 2] = 0;
                        data[base + 3] = 0;
                    }
                }
            }
            raster.setPixels(0, 0, w, h, data);
            return raster;
        }
    }
}
