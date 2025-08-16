package com.w3canvas.javacanvas.interfaces;

import org.mozilla.javascript.Scriptable;

public interface ICanvasRenderingContext2D {
    ICanvasSurface getSurface();

    void save();
    void restore();

    void scale(double x, double y);
    void rotate(double angle);
    void translate(double x, double y);
    void transform(double m11, double m12, double m21, double m22, double dx, double dy);
    void setTransform(double m11, double m12, double m21, double m22, double dx, double dy);
    void resetTransform();
    Object getTransform(); // Should return a DOMMatrix-like object

    double getGlobalAlpha();
    void setGlobalAlpha(double globalAlpha);

    String getGlobalCompositeOperation();
    void setGlobalCompositeOperation(String op);

    Object getFillStyle();
    void setFillStyle(Object fillStyle);

    Object getStrokeStyle();
    void setStrokeStyle(Object strokeStyle);

    ICanvasGradient createLinearGradient(double x0, double y0, double x1, double y1);
    ICanvasGradient createRadialGradient(double x0, double y0, double r0, double x1, double y1, double r1);
    ICanvasPattern createPattern(Object image, String repetition);

    double getLineWidth();
    void setLineWidth(double lw);

    String getLineCap();
    void setLineCap(String cap);

    String getLineJoin();
    void setLineJoin(String join);

    double getMiterLimit();
    void setMiterLimit(double miterLimit);

    void setLineDash(Object dash);
    Object getLineDash();
    double getLineDashOffset();
    void setLineDashOffset(double offset);

    void clearRect(double x, double y, double w, double h);
    void fillRect(double x, double y, double w, double h);
    void strokeRect(double x, double y, double w, double h);

    void beginPath();
    void closePath();
    void moveTo(double x, double y);
    void lineTo(double x, double y);
    void quadraticCurveTo(double cpx, double cpy, double x, double y);
    void bezierCurveTo(double cp1x, double cp1y, double cp2x, double cp2y, double x, double y);
    void arcTo(double x1, double y1, double x2, double y2, double radius);
    void rect(double x, double y, double w, double h);
    void arc(double x, double y, double radius, double startAngle, double endAngle, boolean counterclockwise);
    void ellipse(double x, double y, double radiusX, double radiusY, double rotation, double startAngle, double endAngle, boolean counterclockwise);

    void fill();
    void stroke();
    void clip();

    boolean isPointInPath(double x, double y);
    boolean isPointInStroke(double x, double y);

    void drawImage(Object image, double dx, double dy);
    void drawImage(Object image, double dx, double dy, double dWidth, double dHeight);
    void drawImage(Object image, double sx, double sy, double sWidth, double sHeight, double dx, double dy, double dWidth, double dHeight);

    ITextMetrics measureText(String text);

    String getFont();
    void setFont(String font);

    String getTextAlign();
    void setTextAlign(String textAlign);

    String getTextBaseline();
    void setTextBaseline(String textBaseline);

    void fillText(String text, double x, double y, double maxWidth);
    void strokeText(String text, double x, double y, double maxWidth);

    IImageData createImageData(int width, int height);
    IImageData getImageData(int x, int y, int width, int height);
    void putImageData(IImageData imagedata, int dx, int dy, int dirtyX, int dirtyY, int dirtyWidth, int dirtyHeight);

    boolean isContextLost();
    Scriptable getContextAttributes();

    String getFilter();
    void setFilter(String filter);

    void reset();
}
