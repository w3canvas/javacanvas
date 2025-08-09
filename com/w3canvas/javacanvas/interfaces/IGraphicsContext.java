package com.w3canvas.javacanvas.interfaces;

// A simplified abstraction of java.awt.Graphics2D
public interface IGraphicsContext {

    // Transformations
    void scale(double x, double y);
    void rotate(double theta);
    void translate(double tx, double ty);
    void transform(double m11, double m12, double m21, double m22, double dx, double dy);
    void setTransform(double m11, double m12, double m21, double m22, double dx, double dy);
    void resetTransform();

    // Drawing properties
    void setPaint(Object paint); // Could be a color, gradient, or pattern
    void setStroke(Object stroke); // Could be a BasicStroke or similar
    void setComposite(Object comp); // Could be an AlphaComposite or similar
    void setFont(Object font); // Could be a Font object

    // Drawing operations
    void draw(Object shape); // Could be a Shape object
    void fill(Object shape); // Could be a Shape object
    void drawImage(Object img, int x, int y);
    void drawString(String str, int x, int y);

    // Clipping
    void setClip(Object shape);
    void clip(Object shape);
}
