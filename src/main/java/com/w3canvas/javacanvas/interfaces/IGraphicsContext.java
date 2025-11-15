package com.w3canvas.javacanvas.interfaces;

public interface IGraphicsContext {
    // Transformations
    void scale(double x, double y);
    void rotate(double theta);
    void translate(double tx, double ty);
    void transform(double m11, double m12, double m21, double m22, double dx, double dy);
    void setTransform(double m11, double m12, double m21, double m22, double dx, double dy);
    void setTransform(Object transform);
    void resetTransform();
    Object getTransform();

    // Drawing properties
    void setFillPaint(IPaint paint);
    void setStrokePaint(IPaint paint);
    ICanvasPattern createPattern(Object image, String repetition);
    void setLineWidth(double width);
    void setLineCap(String cap);
    void setLineJoin(String join);
    void setMiterLimit(double limit);
    void setLineDash(double[] dash);
    void setLineDashOffset(double offset);
    void setComposite(IComposite comp);
    void setGlobalAlpha(double alpha);
    void setFont(IFont font);
    void setTextAlign(String textAlign);
    void setTextBaseline(String textBaseline);

    // Shadow properties
    void setShadowBlur(double blur);
    void setShadowColor(String color);
    void setShadowOffsetX(double offsetX);
    void setShadowOffsetY(double offsetY);

    // Image smoothing
    void setImageSmoothingEnabled(boolean enabled);
    void setImageSmoothingQuality(String quality);

    // Filter
    void setFilter(String filter);
    String getFilter();

    // Drawing operations
    void clearRect(double x, double y, double w, double h);
    void draw(IShape shape);
    void fill(IShape shape);
    void drawImage(Object img, int x, int y);
    void drawImage(Object img, int sx, int sy, int sw, int sh, int dx, int dy, int dw, int dh);
    void drawImage(int[] pixels, int x, int y, int width, int height);
    void drawString(String str, int x, int y);
    ITextMetrics measureText(String text);
    void fillText(String text, double x, double y, double maxWidth);
    void strokeText(String text, double x, double y, double maxWidth);
    IImageData createImageData(int width, int height);
    IImageData getImageData(int x, int y, int width, int height);

    // Clipping
    void clip();

    // Path methods
    void beginPath();
    void closePath();
    void moveTo(double x, double y);
    void lineTo(double x, double y);
    void quadraticCurveTo(double cpx, double cpy, double x, double y);
    void bezierCurveTo(double cp1x, double cp1y, double cp2x, double cp2y, double x, double y);
    void arcTo(double x1, double y1, double x2, double y2, double radius);
    void rect(double x, double y, double w, double h);
    void roundRect(double x, double y, double w, double h, Object radii);
    void arc(double x, double y, double radius, double startAngle, double endAngle, boolean counterclockwise);
    void ellipse(double x, double y, double radiusX, double radiusY, double rotation, double startAngle, double endAngle, boolean counterclockwise);
    void fill();
    void stroke();
    boolean isPointInPath(double x, double y);
    boolean isPointInStroke(double x, double y);
    IShape getPath();
    double[] getLastPoint();
}
