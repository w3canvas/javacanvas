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
    private double[] lastPoint = new double[2];
    private IPaint fillPaint;
    private IPaint strokePaint;

    public AwtGraphicsContext(Graphics2D g2d, AwtCanvasSurface surface) {
        this.g2d = g2d;
        this.surface = surface;
        this.g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        this.path = new GeneralPath();
        this.fillPaint = new AwtPaint(java.awt.Color.BLACK);
        this.strokePaint = new AwtPaint(java.awt.Color.BLACK);
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
        g2d.drawString(text, (float)x, (float)y);
    }

    @Override
    public void strokeText(String text, double x, double y, double maxWidth) {
        // AWT Graphics2D doesn't have a direct strokeText method.
        // We can simulate it by getting the outline of the text and stroking that.
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
    public ICanvasPattern createPattern(Object image, String repetition) {
        if (image instanceof BufferedImage) {
            return new AwtPattern((BufferedImage) image, repetition);
        }
        return null;
    }

    @Override
    public void setFillPaint(IPaint paint) {
        this.fillPaint = paint;
        if (paint instanceof AwtPaint) {
            g2d.setPaint(((AwtPaint) paint).getPaint());
        } else if (paint instanceof AwtLinearGradient) {
            g2d.setPaint(((AwtLinearGradient) paint).getPaint());
        } else if (paint instanceof AwtRadialGradient) {
            g2d.setPaint(((AwtRadialGradient) paint).getPaint());
        } else if (paint instanceof AwtPattern) {
            g2d.setPaint(((AwtPattern) paint).getPaint());
        }
    }

    @Override
    public void setStrokePaint(IPaint paint) {
        this.strokePaint = paint;
        if (paint instanceof AwtPaint) {
            g2d.setPaint(((AwtPaint) paint).getPaint());
        } else if (paint instanceof AwtLinearGradient) {
            g2d.setPaint(((AwtLinearGradient) paint).getPaint());
        } else if (paint instanceof AwtRadialGradient) {
            g2d.setPaint(((AwtRadialGradient) paint).getPaint());
        } else if (paint instanceof AwtPattern) {
            g2d.setPaint(((AwtPattern) paint).getPaint());
        }
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

    @Override
    public void setTextAlign(String textAlign) {
        // AWT Graphics2D does not have a direct equivalent.
        // This would require manual calculation based on font metrics.
    }

    @Override
    public void setTextBaseline(String textBaseline) {
        // AWT Graphics2D does not have a direct equivalent.
        // This would require manual calculation based on font metrics.
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
            g2d.draw(((AwtShape) shape).getShape());
        }
    }

    @Override
    public void fill(IShape shape) {
        if (shape instanceof AwtShape) {
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
    public void drawImage(Object img, int sx, int sy, int sw, int sh, int dx, int dy, int dw, int dh) {
        if (img instanceof BufferedImage) {
            g2d.drawImage((BufferedImage) img, dx, dy, dx + dw, dy + dh, sx, sy, sx + sw, sy + sh, null);
        }
    }

    @Override
    public void drawString(String str, int x, int y) {
        g2d.drawString(str, x, y);
    }

    @Override
    public ITextMetrics measureText(String text) {
        return new AwtTextMetrics(g2d.getFontMetrics().stringWidth(text));
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
    public void clip() {
        g2d.clip(this.path);
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
        lastPoint[0] = x;
        lastPoint[1] = y;
        Point2D p = new Point2D.Double(x, y);
        g2d.getTransform().transform(p, p);
        path.moveTo(p.getX(), p.getY());
    }

    @Override
    public void lineTo(double x, double y) {
        lastPoint[0] = x;
        lastPoint[1] = y;
        Point2D p = new Point2D.Double(x, y);
        g2d.getTransform().transform(p, p);
        path.lineTo(p.getX(), p.getY());
    }

    @Override
    public void quadraticCurveTo(double cpx, double cpy, double x, double y) {
        lastPoint[0] = x;
        lastPoint[1] = y;
        double[] xy = { cpx, cpy, x, y };
        g2d.getTransform().transform(xy, 0, xy, 0, 2);
        path.quadTo(xy[0], xy[1], xy[2], xy[3]);
    }

    @Override
    public void bezierCurveTo(double cp1x, double cp1y, double cp2x, double cp2y, double x, double y) {
        lastPoint[0] = x;
        lastPoint[1] = y;
        float[] xy = { (float)cp1x, (float)cp1y, (float)cp2x, (float)cp2y, (float)x, (float)y };
        g2d.getTransform().transform(xy, 0, xy, 0, 3);
        path.curveTo(xy[0], xy[1], xy[2], xy[3], xy[4], xy[5]);
    }

    @Override
    public void arcTo(double x1, double y1, double x2, double y2, double radius) {
        Point2D p0 = path.getCurrentPoint();
        if (p0 == null) {
            return;
        }

        double x0 = p0.getX();
        double y0 = p0.getY();

        if (radius == 0 || (x0 == x1 && y0 == y1) || (x1 == x2 && y1 == y2)) {
            lineTo(x1, y1);
            return;
        }

        double dx01 = x1 - x0;
        double dy01 = y1 - y0;
        double len01 = Math.sqrt(dx01 * dx01 + dy01 * dy01);
        dx01 /= len01;
        dy01 /= len01;

        double dx12 = x2 - x1;
        double dy12 = y2 - y1;
        double len12 = Math.sqrt(dx12 * dx12 + dy12 * dy12);
        dx12 /= len12;
        dy12 /= len12;

        double angle = Math.acos(dx01 * dx12 + dy01 * dy12);

        if (Math.abs(angle) < 1e-6 || Math.abs(angle - Math.PI) < 1e-6) {
            lineTo(x1, y1);
            return;
        }

        double tangent = radius / Math.tan(angle / 2.0);

        double t1x = x1 - tangent * dx01;
        double t1y = y1 - tangent * dy01;
        double t2x = x1 + tangent * dx12;
        double t2y = y1 + tangent * dy12;

        lineTo(t1x, t1y);

        double cross_product = dx01 * dy12 - dy01 * dx12;
        double normal_x, normal_y;

        if (cross_product < 0) { // Clockwise
            normal_x = dy01;
            normal_y = -dx01;
        } else { // Counter-clockwise
            normal_x = -dy01;
            normal_y = dx01;
        }

        double cx = t1x + normal_x * radius;
        double cy = t1y + normal_y * radius;

        double startAngle = Math.toDegrees(Math.atan2(t1y - cy, t1x - cx));
        double endAngle = Math.toDegrees(Math.atan2(t2y - cy, t2x - cx));
        double sweepAngle = endAngle - startAngle;

        if (cross_product < 0) { // Clockwise
            if (sweepAngle > 0) {
                sweepAngle -= 360;
            }
        } else { // Counter-clockwise
            if (sweepAngle < 0) {
                sweepAngle += 360;
            }
        }

        path.append(new Arc2D.Double(cx - radius, cy - radius, 2 * radius, 2 * radius, startAngle, sweepAngle, Arc2D.OPEN), true);

        lastPoint[0] = t2x;
        lastPoint[1] = t2y;
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
        double endAngleRad = startAngle + ang;
        lastPoint[0] = x + radius * Math.cos(endAngleRad);
        lastPoint[1] = y + radius * Math.sin(endAngleRad);
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
        g2d.fill(this.path);
    }

    @Override
    public void stroke() {
        g2d.draw(this.path);
    }

    @Override
    public IShape getPath() {
        return new AwtShape(path);
    }

    @Override
    public boolean isPointInPath(double x, double y) {
        return path.contains(x, y);
    }

    @Override
    public boolean isPointInStroke(double x, double y) {
        return g2d.getStroke().createStrokedShape(path).contains(x, y);
    }

    @Override
    public double[] getLastPoint() {
        return lastPoint;
    }
}
