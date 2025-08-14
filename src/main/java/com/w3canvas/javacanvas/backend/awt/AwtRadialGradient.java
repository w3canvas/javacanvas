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
        AwtPaint paint = (AwtPaint) new ColorParser().parse(colorStr, backend);
        colors.add((Color) paint.getPaint());
    }

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
