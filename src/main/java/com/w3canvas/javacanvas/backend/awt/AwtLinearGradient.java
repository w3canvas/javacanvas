package com.w3canvas.javacanvas.backend.awt;

import com.w3canvas.javacanvas.core.ColorParser;
import com.w3canvas.javacanvas.interfaces.ICanvasGradient;
import com.w3canvas.javacanvas.interfaces.IGraphicsBackend;
import com.w3canvas.javacanvas.interfaces.IPaint;

import java.awt.Color;
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class AwtLinearGradient implements ICanvasGradient, IPaint {
    private final double x0, y0, x1, y1;
    private final List<Double> offsets = new ArrayList<>();
    private final List<Color> colors = new ArrayList<>();
    private final IGraphicsBackend backend;

    public AwtLinearGradient(double x0, double y0, double x1, double y1, IGraphicsBackend backend) {
        this.x0 = x0;
        this.y0 = y0;
        this.x1 = x1;
        this.y1 = y1;
        this.backend = backend;
    }

    @Override
    public void addColorStop(double offset, String colorStr) {
        offsets.add(offset);
        AwtPaint paint = (AwtPaint) ColorParser.parse(colorStr, backend);
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

        return new LinearGradientPaint(new Point2D.Double(x0, y0), new Point2D.Double(x1, y1), fractions, colorArray);
    }
}
