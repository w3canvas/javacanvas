package com.w3canvas.javacanvas.backend.awt;

import com.w3canvas.javacanvas.interfaces.ITextMetrics;

public class AwtTextMetrics implements ITextMetrics {
    private final double width;

    public AwtTextMetrics(double width) {
        this.width = width;
    }

    @Override
    public double getWidth() {
        return width;
    }

    @Override
    public double getActualBoundingBoxLeft() {
        // TODO: Implement this properly
        return 0;
    }

    @Override
    public double getActualBoundingBoxRight() {
        // TODO: Implement this properly
        return width;
    }

    @Override
    public double getActualBoundingBoxAscent() {
        // TODO: Implement this properly
        return 0;
    }

    @Override
    public double getActualBoundingBoxDescent() {
        // TODO: Implement this properly
        return 0;
    }
}
