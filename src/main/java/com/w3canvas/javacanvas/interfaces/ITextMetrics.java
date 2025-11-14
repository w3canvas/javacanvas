package com.w3canvas.javacanvas.interfaces;

public interface ITextMetrics {
    double getWidth();
    double getActualBoundingBoxLeft();
    double getActualBoundingBoxRight();
    double getActualBoundingBoxAscent();
    double getActualBoundingBoxDescent();
    double getFontBoundingBoxAscent();
    double getFontBoundingBoxDescent();
    double getEmHeightAscent();
    double getEmHeightDescent();
    double getHangingBaseline();
    double getAlphabeticBaseline();
    double getIdeographicBaseline();
}
