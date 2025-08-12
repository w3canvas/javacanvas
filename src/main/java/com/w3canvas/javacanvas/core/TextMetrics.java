package com.w3canvas.javacanvas.core;

import com.w3canvas.javacanvas.interfaces.ITextMetrics;

public class TextMetrics implements ITextMetrics {
    private final double width;

    public TextMetrics(double width) {
        this.width = width;
    }

    @Override
    public double getWidth() {
        return width;
    }
}
