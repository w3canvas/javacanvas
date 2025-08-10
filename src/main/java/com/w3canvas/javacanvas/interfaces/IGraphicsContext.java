package com.w3canvas.javacanvas.interfaces;

public interface IGraphicsContext {
    void scale(double x, double y);
    void rotate(double theta);
    void translate(double tx, double ty);
    void transform(double m11, double m12, double m21, double m22, double dx, double dy);
    void setTransform(double m11, double m12, double m21, double m22, double dx, double dy);
    void setTransform(Object transform); // Should accept a backend-specific transform object
    void resetTransform();
    Object getTransform(); // Should return a backend-specific transform object

    void setPaint(IPaint paint);
    void setComposite(IComposite comp);
    void setLineWidth(double width);
    void setLineCap(String cap);
    void setLineJoin(String join);
    void setMiterLimit(double limit);
    void setLineDash(double[] dash);
    void setLineDashOffset(double offset);
    void setFont(IFont font);

    void draw(IShape shape);
    void fill(IShape shape);
    void drawImage(Object img, int x, int y); // img is a native image object
    void drawString(String str, int x, int y);

    void setClip(IShape shape);
    void clip(IShape shape);

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

    IShape getPath();
}
