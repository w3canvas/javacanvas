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
}
