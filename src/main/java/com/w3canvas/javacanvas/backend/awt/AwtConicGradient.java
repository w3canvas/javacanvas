package com.w3canvas.javacanvas.backend.awt;

import com.w3canvas.javacanvas.backend.ConicGradientHelper;
import com.w3canvas.javacanvas.core.ColorParser;
import com.w3canvas.javacanvas.interfaces.ICanvasGradient;
import com.w3canvas.javacanvas.interfaces.IGraphicsBackend;
import com.w3canvas.javacanvas.interfaces.IPaint;

import java.awt.Color;
import java.awt.Paint;
import java.awt.PaintContext;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * AWT implementation of conic gradients using a custom Paint.
 * Computes colors on-the-fly based on the angle from the center point,
 * with colors interpolated between color stops.
 */
public class AwtConicGradient implements ICanvasGradient, IPaint {
    private final double startAngle;
    private final double centerX;
    private final double centerY;
    private final List<ConicGradientHelper.ColorStop> stops = new ArrayList<>();
    private final IGraphicsBackend backend;

    public AwtConicGradient(double startAngle, double x, double y, IGraphicsBackend backend) {
        this.startAngle = startAngle;
        this.centerX = x;
        this.centerY = y;
        this.backend = backend;
    }

    @Override
    public void addColorStop(double offset, String colorStr) {
        AwtPaint paint = (AwtPaint) new ColorParser().parse(colorStr, backend);
        Color color = (Color) paint.getPaint();

        stops.add(new ConicGradientHelper.ColorStop(
                offset,
                color.getRed(),
                color.getGreen(),
                color.getBlue(),
                color.getAlpha()
        ));

        // Keep stops sorted by offset
        Collections.sort(stops);
    }

    @Override
    public Paint getPaint() {
        if (stops.isEmpty()) {
            return new Color(0, 0, 0, 0); // Transparent
        }

        // Return a custom Paint implementation
        return new ConicGradientPaint();
    }

    /**
     * Custom Paint implementation for conic gradients.
     * This properly handles the positioning of the gradient at (centerX, centerY).
     */
    private class ConicGradientPaint implements Paint {
        @Override
        public PaintContext createContext(ColorModel cm, Rectangle deviceBounds,
                                          Rectangle2D userBounds, AffineTransform xform,
                                          RenderingHints hints) {
            return new ConicGradientPaintContext(centerX, centerY, startAngle, stops);
        }

        @Override
        public int getTransparency() {
            return java.awt.Transparency.TRANSLUCENT;
        }
    }

    /**
     * PaintContext for the conic gradient.
     * Computes the color for each pixel based on its angle from the center.
     */
    private static class ConicGradientPaintContext implements PaintContext {
        private final double centerX;
        private final double centerY;
        private final double startAngle;
        private final List<ConicGradientHelper.ColorStop> stops;

        public ConicGradientPaintContext(double centerX, double centerY, double startAngle,
                                         List<ConicGradientHelper.ColorStop> stops) {
            this.centerX = centerX;
            this.centerY = centerY;
            this.startAngle = startAngle;
            this.stops = stops;
        }

        @Override
        public void dispose() {
        }

        @Override
        public ColorModel getColorModel() {
            return ColorModel.getRGBdefault();
        }

        @Override
        public Raster getRaster(int x, int y, int w, int h) {
            WritableRaster raster = getColorModel().createCompatibleWritableRaster(w, h);
            int[] data = new int[w * h * 4];

            for (int j = 0; j < h; j++) {
                for (int i = 0; i < w; i++) {
                    double px = x + i;
                    double py = y + j;

                    // Calculate angle from center
                    double dx = px - centerX;
                    double dy = py - centerY;
                    double angle = Math.atan2(dy, dx);

                    // Interpolate color for this angle
                    int[] rgba = ConicGradientHelper.interpolateColor(angle, startAngle, stops);

                    int base = (j * w + i) * 4;
                    data[base + 0] = rgba[0]; // R
                    data[base + 1] = rgba[1]; // G
                    data[base + 2] = rgba[2]; // B
                    data[base + 3] = rgba[3]; // A
                }
            }

            raster.setPixels(0, 0, w, h, data);
            return raster;
        }
    }

}
