package com.w3canvas.javacanvas.backend.javafx;

import com.w3canvas.javacanvas.core.ColorParser;
import com.w3canvas.javacanvas.interfaces.ICanvasGradient;
import com.w3canvas.javacanvas.interfaces.IPaint;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;

import java.util.ArrayList;
import java.util.List;

public class JavaFXRadialGradient implements ICanvasGradient, IPaint {

    private final double x0, y0, r0, x1, y1, r1;
    private final List<Stop> stops = new ArrayList<>();

    public JavaFXRadialGradient(double x0, double y0, double r0, double x1, double y1, double r1) {
        this.x0 = x0;
        this.y0 = y0;
        this.r0 = r0;
        this.x1 = x1;
        this.y1 = y1;
        this.r1 = r1;
    }

    @Override
    public void addColorStop(double offset, String color) {
        stops.add(new Stop(offset, (Color) ((JavaFXPaint) ColorParser.parse(color, new JavaFXGraphicsBackend())).getPaint()));
    }

    public Object getPaint() {
        // The angle and distance calculations are based on the whatwg spec for radial gradients.
        // https://html.spec.whatwg.org/multipage/canvas.html#dom-context-2d-createradialgradient
        double angle = Math.atan2(y1 - y0, x1 - x0);
        double distance = Math.sqrt(Math.pow(x1 - x0, 2) + Math.pow(y1 - y0, 2));

        return new RadialGradient(
                angle,
                distance,
                x0,
                y0,
                r1,
                false,
                CycleMethod.NO_CYCLE,
                stops
        );
    }
}
