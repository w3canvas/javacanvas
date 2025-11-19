package com.w3canvas.javacanvas.backend.awt;

import com.w3canvas.javacanvas.core.ColorParser;
import com.w3canvas.javacanvas.interfaces.ICanvasGradient;
import com.w3canvas.javacanvas.interfaces.IGraphicsBackend;
import com.w3canvas.javacanvas.interfaces.IPaint;

import java.awt.Color;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * AWT implementation of radial gradient for Canvas 2D API.
 *
 * <p><strong>Backend Limitation:</strong> The HTML Canvas 2D API supports radial gradients
 * defined by two circles (x0, y0, r0) to (x1, y1, r1), which allows for complex cone-shaped
 * gradients. However, Java AWT's {@link RadialGradientPaint} only supports a simpler model
 * with a single circle (center and radius) and an optional focus point.
 *
 * <p><strong>Implementation Notes:</strong>
 * <ul>
 *   <li>The {@code r0} parameter (starting circle radius) is <strong>ignored</strong></li>
 *   <li>Only the ending circle (x1, y1, r1) is used as the gradient's outer boundary</li>
 *   <li>The starting circle center (x0, y0) is used as the focus point for the gradient</li>
 *   <li>This approximation works well for gradients where r0 is 0 (point to circle)</li>
 *   <li>For gradients where r0 > 0, the visual result may differ from browser implementations</li>
 * </ul>
 *
 * <p><strong>Example Impact:</strong><br>
 * A Canvas 2D gradient from circle (50, 50, 20) to (100, 100, 80) will be rendered
 * as if it were from point (50, 50) to circle (100, 100, 80), effectively ignoring
 * the inner radius of 20 pixels.
 *
 * @see ICanvasGradient
 * @see java.awt.RadialGradientPaint
 * @since 1.0
 */
public class AwtRadialGradient implements ICanvasGradient, IPaint {
    private final double x0, y0, r0, x1, y1, r1;
    private final List<Double> offsets = new ArrayList<>();
    private final List<Color> colors = new ArrayList<>();
    private final IGraphicsBackend backend;

    public AwtRadialGradient(double x0, double y0, double r0, double x1, double y1, double r1, IGraphicsBackend backend) {
        this.x0 = x0;
        this.y0 = y0;
        this.r0 = r0;
        this.x1 = x1;
        this.y1 = y1;
        this.r1 = r1;
        this.backend = backend;
    }

    @Override
    public void addColorStop(double offset, String colorStr) {
        offsets.add(offset);
        AwtPaint paint = (AwtPaint) ColorParser.parse(colorStr, backend);
        colors.add((Color) paint.getPaint());
    }

    /**
     * Creates the AWT Paint object for this radial gradient.
     *
     * <p><strong>AWT Backend Limitation:</strong> This method implements the Canvas 2D
     * radial gradient using AWT's {@link RadialGradientPaint}, which does not support
     * the full two-circle gradient model defined by the Canvas 2D specification.
     *
     * <p>Specifically:
     * <ul>
     *   <li>The {@code r0} parameter is <strong>ignored</strong> - AWT cannot render
     *       gradients between two circles of different radii</li>
     *   <li>The ending circle (x1, y1, r1) defines the gradient's outer boundary</li>
     *   <li>The starting circle center (x0, y0) becomes the gradient's focus point</li>
     * </ul>
     *
     * @return AWT {@link RadialGradientPaint} object, or transparent color if no color stops defined
     * @see java.awt.RadialGradientPaint
     */
    public Paint getPaint() {
        if (offsets.isEmpty()) {
            return new Color(0, 0, 0, 0); // Transparent
        }

        float[] fractions = new float[offsets.size()];
        for (int i = 0; i < offsets.size(); i++) {
            fractions[i] = offsets.get(i).floatValue();
        }

        Color[] colorArray = colors.toArray(new Color[0]);

        // AWT's RadialGradientPaint is less flexible than the canvas API's.
        // It doesn't support two circles, only a center and a radius with a focus point.
        // We will use the second circle's center as the focus point and its radius as the gradient radius.
        return new RadialGradientPaint(
                new Point2D.Double(x1, y1),
                (float) r1,
                new Point2D.Double(x0, y0),
                fractions,
                colorArray,
                RadialGradientPaint.CycleMethod.NO_CYCLE
        );
    }
}
