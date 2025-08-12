package com.w3canvas.javacanvas.backend.awt;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.Transparency;

import com.w3canvas.javacanvas.interfaces.*;

public class AwtGraphicsContext implements IGraphicsContext {

    private final Graphics2D g2d;
    private final AwtCanvasSurface surface;
    private GeneralPath path;
    private IPaint fillPaint;
    private IPaint strokePaint;

    public AwtGraphicsContext(Graphics2D g2d, AwtCanvasSurface surface) {
        this.g2d = g2d;
        this.surface = surface;
        this.g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        this.path = new GeneralPath();
    }

    // Transformations
    @Override
    public void scale(double x, double y) {
        g2d.scale(x, y);
    }

    @Override
    public void fillText(String text, double x, double y, double maxWidth) {
        // AWT Graphics2D doesn't have a maxWidth parameter for fillText,
        // so we ignore it. A more complete implementation might manually
        // scale the text or truncate it.
        if (this.fillPaint instanceof AwtPaint) {
            g2d.setPaint(((AwtPaint)this.fillPaint).getPaint());
        }
        g2d.drawString(text, (float)x, (float)y);
    }

    @Override
    public void strokeText(String text, double x, double y, double maxWidth) {
        // AWT Graphics2D doesn't have a direct strokeText method.
        // We can simulate it by getting the outline of the text and stroking that.
        if (this.strokePaint instanceof AwtPaint) {
            g2d.setPaint(((AwtPaint)this.strokePaint).getPaint());
        }
        Font font = g2d.getFont();
        java.awt.font.FontRenderContext frc = g2d.getFontRenderContext();
        java.awt.font.TextLayout tl = new java.awt.font.TextLayout(text, font, frc);
        Shape shape = tl.getOutline(AffineTransform.getTranslateInstance(x, y));
        g2d.draw(shape);
    }

    @Override
    public void rotate(double theta) {
        g2d.rotate(theta);
    }

    @Override
    public void translate(double tx, double ty) {
        g2d.translate(tx, ty);
    }

    @Override
    public void transform(double m11, double m12, double m21, double m22, double dx, double dy) {
        g2d.transform(new AffineTransform(m11, m12, m21, m22, dx, dy));
    }

    @Override
    public void setTransform(double m11, double m12, double m21, double m22, double dx, double dy) {
        g2d.setTransform(new AffineTransform(m11, m12, m21, m22, dx, dy));
    }

    @Override
    public void setTransform(Object transform) {
        if (transform instanceof AffineTransform) {
            g2d.setTransform((AffineTransform) transform);
        }
    }

    @Override
    public void resetTransform() {
        g2d.setTransform(new AffineTransform());
    }

    @Override
    public Object getTransform() {
        return g2d.getTransform();
    }

    // Drawing properties
    @Override
    public void setFillPaint(IPaint paint) {
        this.fillPaint = paint;
    }

    @Override
    public void setStrokePaint(IPaint paint) {
        this.strokePaint = paint;
    }

    private float lineWidth = 1.0f;
    private int lineCap = BasicStroke.CAP_BUTT;
    private int lineJoin = BasicStroke.JOIN_MITER;
    private float miterLimit = 10.0f;
    private float[] lineDash = null;
    private float lineDashOffset = 0.0f;

    private void updateStroke() {
        if (lineDash != null && lineDash.length > 0) {
            g2d.setStroke(new BasicStroke(lineWidth, lineCap, lineJoin, miterLimit, lineDash, lineDashOffset));
        } else {
            g2d.setStroke(new BasicStroke(lineWidth, lineCap, lineJoin, miterLimit));
        }
    }

    @Override
    public void setLineWidth(double width) {
        this.lineWidth = (float) width;
        updateStroke();
    }

    @Override
    public void setLineCap(String cap) {
        if ("round".equals(cap)) this.lineCap = BasicStroke.CAP_ROUND;
        else if ("square".equals(cap)) this.lineCap = BasicStroke.CAP_SQUARE;
        else this.lineCap = BasicStroke.CAP_BUTT;
        updateStroke();
    }

    @Override
    public void setLineJoin(String join) {
        if ("round".equals(join)) this.lineJoin = BasicStroke.JOIN_ROUND;
        else if ("bevel".equals(join)) this.lineJoin = BasicStroke.JOIN_BEVEL;
        else this.lineJoin = BasicStroke.JOIN_MITER;
        updateStroke();
    }

    @Override
    public void setMiterLimit(double limit) {
        this.miterLimit = (float) limit;
        updateStroke();
    }

    @Override
    public void setLineDash(double[] dash) {
        if (dash == null) {
            this.lineDash = null;
        } else {
            this.lineDash = new float[dash.length];
            for (int i = 0; i < dash.length; i++) {
                this.lineDash[i] = (float) dash[i];
            }
        }
        updateStroke();
    }

    @Override
    public void setLineDashOffset(double offset) {
        this.lineDashOffset = (float) offset;
        updateStroke();
    }

    @Override
    public void setComposite(IComposite comp) {
        if (comp instanceof AwtComposite) {
            g2d.setComposite(((AwtComposite) comp).getComposite());
        }
    }

    @Override
    public void setGlobalAlpha(double alpha) {
        // In AWT, alpha is part of the AlphaComposite, which is handled by setComposite.
        // This method is a no-op for the AWT backend.
    }

    @Override
    public void setFont(IFont font) {
        if (font instanceof AwtFont) {
            g2d.setFont(((AwtFont) font).getFont());
        }
    }

    // Drawing operations
    @Override
    public void clearRect(double x, double y, double w, double h) {
        Composite old = g2d.getComposite();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR));
        g2d.fillRect((int) x, (int) y, (int) w, (int) h);
        g2d.setComposite(old);
    }

    @Override
    public void draw(IShape shape) {
        if (shape instanceof AwtShape) {
            if (this.strokePaint instanceof AwtPaint) {
                g2d.setPaint(((AwtPaint)this.strokePaint).getPaint());
            }
            g2d.draw(((AwtShape) shape).getShape());
        }
    }

    @Override
    public void fill(IShape shape) {
        if (shape instanceof AwtShape) {
            if (this.fillPaint instanceof AwtPaint) {
                g2d.setPaint(((AwtPaint)this.fillPaint).getPaint());
            }
            System.out.println("Filling with paint: " + g2d.getPaint());
            g2d.fill(((AwtShape) shape).getShape());
        }
    }

    @Override
    public void drawImage(Object img, int x, int y) {
        if (img instanceof BufferedImage) {
            g2d.drawImage((BufferedImage) img, x, y, null);
        }
    }

    @Override
    public void drawImage(int[] pixels, int x, int y, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        image.setRGB(0, 0, width, height, pixels, 0, width);
        g2d.drawImage(image, x, y, null);
    }

    @Override
    public void drawString(String str, int x, int y) {
        if (this.fillPaint instanceof AwtPaint) {
            g2d.setPaint(((AwtPaint)this.fillPaint).getPaint());
        }
        g2d.drawString(str, x, y);
    }

    @Override
    public ITextMetrics measureText(String text) {
        // Not implemented for AWT backend
        return null;
    }

    @Override
    public IImageData createImageData(int width, int height) {
        int[] data = new int[width * height];
        return new com.w3canvas.javacanvas.core.ImageData(width, height, new com.w3canvas.javacanvas.core.CanvasPixelArray(data, width, height));
    }

    @Override
    public IImageData getImageData(int x, int y, int width, int height) {
        BufferedImage image = (BufferedImage) surface.getNativeImage();
        int[] pixels = new int[width * height];
        image.getRGB(x, y, width, height, pixels, 0, width);
        return new com.w3canvas.javacanvas.core.ImageData(width, height, new com.w3canvas.javacanvas.core.CanvasPixelArray(pixels, width, height));
    }

    // Clipping
    @Override
    public void setClip(IShape shape) {
        if (shape instanceof AwtShape) {
            g2d.setClip(((AwtShape) shape).getShape());
        }
    }

    @Override
    public void clip(IShape shape) {
        if (shape instanceof AwtShape) {
            g2d.clip(((AwtShape) shape).getShape());
        }
    }

    // Path methods
    @Override
    public void beginPath() {
        path = new GeneralPath();
    }

    @Override
    public void closePath() {
        path.closePath();
    }

    @Override
    public void moveTo(double x, double y) {
        Point2D p = new Point2D.Double(x, y);
        g2d.getTransform().transform(p, p);
        path.moveTo(p.getX(), p.getY());
    }

    @Override
    public void lineTo(double x, double y) {
        Point2D p = new Point2D.Double(x, y);
        g2d.getTransform().transform(p, p);
        path.lineTo(p.getX(), p.getY());
    }

    @Override
    public void quadraticCurveTo(double cpx, double cpy, double x, double y) {
        double[] xy = { cpx, cpy, x, y };
        g2d.getTransform().transform(xy, 0, xy, 0, 2);
        path.quadTo(xy[0], xy[1], xy[2], xy[3]);
    }

    @Override
    public void bezierCurveTo(double cp1x, double cp1y, double cp2x, double cp2y, double x, double y) {
        float[] xy = { (float)cp1x, (float)cp1y, (float)cp2x, (float)cp2y, (float)x, (float)y };
        g2d.getTransform().transform(xy, 0, xy, 0, 3);
        path.curveTo(xy[0], xy[1], xy[2], xy[3], xy[4], xy[5]);
    }

    @Override
    public void arcTo(double x1, double y1, double x2, double y2, double radius) {
        // This is a simplified implementation. A proper implementation is more complex.
        Point2D p0 = path.getCurrentPoint();
        if (p0 == null) return;
        path.lineTo(x1, y1);
    }

    @Override
    public void rect(double x, double y, double w, double h) {
        path.append(g2d.getTransform().createTransformedShape(new java.awt.geom.Rectangle2D.Double(x, y, w, h)), true);
    }

    @Override
    public void arc(double x, double y, double radius, double startAngle, double endAngle, boolean counterclockwise) {
        double ang = endAngle - startAngle;
        if (counterclockwise) {
            if (ang < 0) ang += 2 * Math.PI;
        } else {
            if (ang > 0) ang -= 2 * Math.PI;
        }
        path.append(g2d.getTransform().createTransformedShape(
                new Arc2D.Double(x - radius, y - radius, 2 * radius, 2 * radius,
                        Math.toDegrees(startAngle), Math.toDegrees(ang), Arc2D.OPEN)), true);
    }

    @Override
    public void ellipse(double x, double y, double radiusX, double radiusY, double rotation, double startAngle, double endAngle, boolean counterclockwise) {
        double ang = endAngle - startAngle;
        if (counterclockwise) {
            if (ang < 0) {
                ang += 2 * Math.PI;
            }
        } else {
            if (ang > 0) {
                ang -= 2 * Math.PI;
            }
        }

        AffineTransform tx = AffineTransform.getRotateInstance(rotation, x, y);
        Shape ellipse = new Arc2D.Double(x - radiusX, y - radiusY, radiusX * 2, radiusY * 2,
                Math.toDegrees(startAngle), Math.toDegrees(ang), Arc2D.OPEN);
        path.append(tx.createTransformedShape(ellipse), true);
    }

    @Override
    public void fill() {
        if (this.fillPaint instanceof AwtPaint) {
            g2d.setPaint(((AwtPaint)this.fillPaint).getPaint());
        }
        g2d.fill(this.path);
    }

    @Override
    public void stroke() {
        if (this.strokePaint instanceof AwtPaint) {
            g2d.setPaint(((AwtPaint)this.strokePaint).getPaint());
        }
        g2d.draw(this.path);
    }

    @Override
    public IShape getPath() {
        return new AwtShape(path);
    }
}
