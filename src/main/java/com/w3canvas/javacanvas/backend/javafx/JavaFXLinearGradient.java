package com.w3canvas.javacanvas.backend.javafx;

import com.w3canvas.javacanvas.core.ColorParser;
import com.w3canvas.javacanvas.interfaces.ICanvasGradient;
import com.w3canvas.javacanvas.interfaces.IPaint;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;

import java.util.ArrayList;
import java.util.List;

public class JavaFXLinearGradient implements ICanvasGradient, IPaint {

    private final double x0, y0, x1, y1;
    private final List<Stop> stops = new ArrayList<>();

    public JavaFXLinearGradient(double x0, double y0, double x1, double y1) {
        this.x0 = x0;
        this.y0 = y0;
        this.x1 = x1;
        this.y1 = y1;
    }

    @Override
    public void addColorStop(double offset, String color) {
        stops.add(new Stop(offset, (Color) ((JavaFXPaint) ColorParser.parse(color, new JavaFXGraphicsBackend())).getPaint()));
    }

    public Object getPaint() {
        // The proportional flag should be true if we want the coordinates to be relative to the shape.
        // The spec says the coordinates are relative to the canvas, so it should be false.
        return new LinearGradient(x0, y0, x1, y1, false, CycleMethod.NO_CYCLE, stops);
    }
}
