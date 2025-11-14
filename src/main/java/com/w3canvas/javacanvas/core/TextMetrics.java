package com.w3canvas.javacanvas.core;

import com.w3canvas.javacanvas.interfaces.ITextMetrics;

public class TextMetrics implements ITextMetrics {
    private final double width;
    private final double actualBoundingBoxLeft;
    private final double actualBoundingBoxRight;
    private final double actualBoundingBoxAscent;
    private final double actualBoundingBoxDescent;
    private final double fontBoundingBoxAscent;
    private final double fontBoundingBoxDescent;
    private final double emHeightAscent;
    private final double emHeightDescent;
    private final double hangingBaseline;
    private final double alphabeticBaseline;
    private final double ideographicBaseline;

    public TextMetrics(
            double width,
            double actualBoundingBoxLeft,
            double actualBoundingBoxRight,
            double actualBoundingBoxAscent,
            double actualBoundingBoxDescent,
            double fontBoundingBoxAscent,
            double fontBoundingBoxDescent,
            double emHeightAscent,
            double emHeightDescent,
            double hangingBaseline,
            double alphabeticBaseline,
            double ideographicBaseline) {
        this.width = width;
        this.actualBoundingBoxLeft = actualBoundingBoxLeft;
        this.actualBoundingBoxRight = actualBoundingBoxRight;
        this.actualBoundingBoxAscent = actualBoundingBoxAscent;
        this.actualBoundingBoxDescent = actualBoundingBoxDescent;
        this.fontBoundingBoxAscent = fontBoundingBoxAscent;
        this.fontBoundingBoxDescent = fontBoundingBoxDescent;
        this.emHeightAscent = emHeightAscent;
        this.emHeightDescent = emHeightDescent;
        this.hangingBaseline = hangingBaseline;
        this.alphabeticBaseline = alphabeticBaseline;
        this.ideographicBaseline = ideographicBaseline;
    }

    public TextMetrics(double width) {
        this(width, 0, width, 0, 0, 0, 0, 0, 0, 0, 0, 0);
    }

    @Override
    public double getWidth() {
        return width;
    }

    @Override
    public double getActualBoundingBoxLeft() {
        return actualBoundingBoxLeft;
    }

    @Override
    public double getActualBoundingBoxRight() {
        return actualBoundingBoxRight;
    }

    @Override
    public double getActualBoundingBoxAscent() {
        return actualBoundingBoxAscent;
    }

    @Override
    public double getActualBoundingBoxDescent() {
        return actualBoundingBoxDescent;
    }

    @Override
    public double getFontBoundingBoxAscent() {
        return fontBoundingBoxAscent;
    }

    @Override
    public double getFontBoundingBoxDescent() {
        return fontBoundingBoxDescent;
    }

    @Override
    public double getEmHeightAscent() {
        return emHeightAscent;
    }

    @Override
    public double getEmHeightDescent() {
        return emHeightDescent;
    }

    @Override
    public double getHangingBaseline() {
        return hangingBaseline;
    }

    @Override
    public double getAlphabeticBaseline() {
        return alphabeticBaseline;
    }

    @Override
    public double getIdeographicBaseline() {
        return ideographicBaseline;
    }
}
