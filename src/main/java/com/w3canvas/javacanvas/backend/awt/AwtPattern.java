package com.w3canvas.javacanvas.backend.awt;

import com.w3canvas.javacanvas.interfaces.ICanvasPattern;
import com.w3canvas.javacanvas.interfaces.IPaint;

import java.awt.Paint;
import java.awt.PaintContext;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

public class AwtPattern implements ICanvasPattern, IPaint {
    private final BufferedImage image;
    private final String repetition;
    private AffineTransform transform = new AffineTransform();

    public AwtPattern(Object image, String repetition) {
        if (image instanceof BufferedImage) {
            this.image = (BufferedImage) image;
            this.repetition = repetition;
        } else {
            throw new IllegalArgumentException("Image must be a BufferedImage for AWT backend.");
        }
    }

    @Override
    public void setTransform(Object transform) {
        if (transform instanceof AffineTransform) {
            this.transform = (AffineTransform) transform;
        }
    }

    public Paint getPaint() {
        return new CustomPatternPaint(image, repetition, transform);
    }

    private static class CustomPatternPaint implements Paint {
        private final BufferedImage image;
        private final String repetition;
        private final AffineTransform patternTransform;

        public CustomPatternPaint(BufferedImage image, String repetition, AffineTransform patternTransform) {
            this.image = image;
            this.repetition = repetition;
            this.patternTransform = patternTransform;
        }

        @Override
        public PaintContext createContext(ColorModel cm, Rectangle deviceBounds, Rectangle2D userBounds, AffineTransform xform, RenderingHints hints) {
            return new CustomPatternPaintContext(image, repetition, xform, patternTransform);
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
        private final AffineTransform inverseTransform;

        // Cache for raster data to reduce memory allocation
        private static final int MAX_CACHE_SIZE = 1024 * 1024; // 1M pixels max
        private int[] cachedData = null;
        private int cachedX = Integer.MIN_VALUE;
        private int cachedY = Integer.MIN_VALUE;
        private int cachedW = -1;
        private int cachedH = -1;

        public CustomPatternPaintContext(BufferedImage image, String repetition, AffineTransform deviceTransform, AffineTransform patternTransform) {
            this.image = image;
            this.repetition = repetition;
            this.width = image.getWidth();
            this.height = image.getHeight();

            AffineTransform t = new AffineTransform(deviceTransform);
            t.concatenate(patternTransform);
            AffineTransform inv;
            try {
                inv = t.createInverse();
            } catch (java.awt.geom.NoninvertibleTransformException e) {
                // Fallback to identity if non-invertible
                inv = new AffineTransform();
            }
            this.inverseTransform = inv;
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
            // Check if we can use cached data
            if (isCacheValid(x, y, w, h)) {
                WritableRaster raster = getColorModel().createCompatibleWritableRaster(w, h);
                raster.setPixels(0, 0, w, h, cachedData);
                return raster;
            }

            // Generate new raster data
            WritableRaster raster = getColorModel().createCompatibleWritableRaster(w, h);
            int[] data = generateRasterData(x, y, w, h);

            // Cache the data if size is reasonable
            if (shouldCache(w, h)) {
                cacheRasterData(x, y, w, h, data);
            }

            raster.setPixels(0, 0, w, h, data);
            return raster;
        }

        private boolean isCacheValid(int x, int y, int w, int h) {
            return cachedData != null
                && cachedX == x
                && cachedY == y
                && cachedW == w
                && cachedH == h;
        }

        private boolean shouldCache(int w, int h) {
            // Only cache if raster size is reasonable (not too large)
            return (w * h) <= MAX_CACHE_SIZE;
        }

        private void cacheRasterData(int x, int y, int w, int h, int[] data) {
            cachedX = x;
            cachedY = y;
            cachedW = w;
            cachedH = h;
            cachedData = data;
        }

        private int[] generateRasterData(int x, int y, int w, int h) {
            int[] data = new int[w * h * 4];
            Point2D.Double pt = new Point2D.Double();
            Point2D.Double srcPt = new Point2D.Double();

            for (int j = 0; j < h; j++) {
                for (int i = 0; i < w; i++) {
                    // Use center of pixel for sampling
                    pt.x = x + i + 0.5;
                    pt.y = y + j + 0.5;

                    inverseTransform.transform(pt, srcPt);

                    int imageX = (int) Math.floor(srcPt.x);
                    int imageY = (int) Math.floor(srcPt.y);

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
            return data;
        }
    }
}
