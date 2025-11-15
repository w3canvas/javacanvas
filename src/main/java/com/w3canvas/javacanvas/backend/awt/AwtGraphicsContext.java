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
    private java.awt.geom.Path2D.Double path;
    private double[] lastPoint = new double[2];
    private IPaint fillPaint;
    private IPaint strokePaint;

    // Shadow properties
    private double shadowBlur = 0;
    private String shadowColor = "rgba(0, 0, 0, 0)";
    private double shadowOffsetX = 0;
    private double shadowOffsetY = 0;

    // Image smoothing
    private boolean imageSmoothingEnabled = true;
    private String imageSmoothingQuality = "low";

    // Filter
    private String filter = "none";

    public AwtGraphicsContext(Graphics2D g2d, AwtCanvasSurface surface) {
        this.g2d = g2d;
        this.surface = surface;

        // Configure rendering hints for consistent behavior across environments
        // In headless mode, use more predictable rendering settings for test consistency
        boolean isHeadless = "true".equals(System.getProperty("java.awt.headless"));

        if (isHeadless) {
            // Headless mode: prioritize consistency over quality for pixel-perfect tests
            this.g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            this.g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            this.g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            this.g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            this.g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        } else {
            // GUI mode: prioritize visual quality
            this.g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            this.g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            this.g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            this.g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
            this.g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
            this.g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            this.g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        }

        this.path = new java.awt.geom.Path2D.Double();
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
        return new AwtTextMetrics(text, g2d.getFont(), g2d);
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
        path = new java.awt.geom.Path2D.Double();
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
        // Use false for connect parameter - rect() creates a new subpath, not connected to existing path
        path.append(g2d.getTransform().createTransformedShape(new java.awt.geom.Rectangle2D.Double(x, y, w, h)), false);
    }

    @Override
    public void roundRect(double x, double y, double w, double h, Object radii) {
        double[] cornerRadii = parseRoundRectRadii(radii);

        // Extract individual corner radii (CSS order: TL, TR, BR, BL)
        double tlRadius = cornerRadii[0];
        double trRadius = cornerRadii[1];
        double brRadius = cornerRadii[2];
        double blRadius = cornerRadii[3];

        // Clamp radii to not exceed half of width or height
        double maxRadius = Math.min(Math.abs(w) / 2, Math.abs(h) / 2);
        tlRadius = Math.min(tlRadius, maxRadius);
        trRadius = Math.min(trRadius, maxRadius);
        brRadius = Math.min(brRadius, maxRadius);
        blRadius = Math.min(blRadius, maxRadius);

        // If all radii are zero, just draw a regular rect
        if (tlRadius == 0 && trRadius == 0 && brRadius == 0 && blRadius == 0) {
            rect(x, y, w, h);
            return;
        }

        // Build the rounded rectangle path manually
        java.awt.geom.Path2D.Double roundedRect = new java.awt.geom.Path2D.Double();

        // Start at top-left corner (after the radius)
        roundedRect.moveTo(x + tlRadius, y);

        // Top edge and top-right corner
        roundedRect.lineTo(x + w - trRadius, y);
        if (trRadius > 0) {
            roundedRect.quadTo(x + w, y, x + w, y + trRadius);
        }

        // Right edge and bottom-right corner
        roundedRect.lineTo(x + w, y + h - brRadius);
        if (brRadius > 0) {
            roundedRect.quadTo(x + w, y + h, x + w - brRadius, y + h);
        }

        // Bottom edge and bottom-left corner
        roundedRect.lineTo(x + blRadius, y + h);
        if (blRadius > 0) {
            roundedRect.quadTo(x, y + h, x, y + h - blRadius);
        }

        // Left edge and top-left corner
        roundedRect.lineTo(x, y + tlRadius);
        if (tlRadius > 0) {
            roundedRect.quadTo(x, y, x + tlRadius, y);
        }

        roundedRect.closePath();

        // Transform and append to path
        path.append(g2d.getTransform().createTransformedShape(roundedRect), true);
    }

    /**
     * Parse roundRect radii parameter according to Canvas 2D spec.
     * Returns array of 4 corner radii: [top-left, top-right, bottom-right, bottom-left]
     */
    private double[] parseRoundRectRadii(Object radii) {
        if (radii == null) {
            return new double[]{0, 0, 0, 0};
        }

        // Handle single number
        if (radii instanceof Number) {
            double r = ((Number) radii).doubleValue();
            return new double[]{r, r, r, r};
        }

        // Handle arrays (both Java arrays and Rhino NativeArray)
        double[] values = null;

        if (radii instanceof double[]) {
            values = (double[]) radii;
        } else if (radii instanceof Object[]) {
            Object[] arr = (Object[]) radii;
            values = new double[arr.length];
            for (int i = 0; i < arr.length; i++) {
                if (arr[i] instanceof Number) {
                    values[i] = ((Number) arr[i]).doubleValue();
                }
            }
        } else if (radii instanceof org.mozilla.javascript.NativeArray) {
            org.mozilla.javascript.NativeArray arr = (org.mozilla.javascript.NativeArray) radii;
            int len = (int) arr.getLength();
            values = new double[len];
            for (int i = 0; i < len; i++) {
                Object val = arr.get(i);
                if (val instanceof Number) {
                    values[i] = ((Number) val).doubleValue();
                }
            }
        }

        if (values != null && values.length > 0) {
            // CSS-style corner radius specification
            switch (values.length) {
                case 1:
                    // All corners
                    return new double[]{values[0], values[0], values[0], values[0]};
                case 2:
                    // [top-left & bottom-right, top-right & bottom-left]
                    return new double[]{values[0], values[1], values[0], values[1]};
                case 3:
                    // [top-left, top-right & bottom-left, bottom-right]
                    return new double[]{values[0], values[1], values[2], values[1]};
                case 4:
                default:
                    // [top-left, top-right, bottom-right, bottom-left]
                    return new double[]{values[0], values[1], values[2], values[3]};
            }
        }

        // Default: no rounding
        return new double[]{0, 0, 0, 0};
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
        // Apply shadow first
        applyShadow(this.path, true);
        // Then draw the actual shape
        g2d.fill(this.path);
    }

    @Override
    public void stroke() {
        // Apply shadow first
        applyShadow(this.path, false);
        // Then draw the actual shape
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

    // Shadow property setters
    @Override
    public void setShadowBlur(double blur) {
        this.shadowBlur = Math.max(0, blur);
    }

    @Override
    public void setShadowColor(String color) {
        this.shadowColor = color != null ? color : "rgba(0, 0, 0, 0)";
    }

    @Override
    public void setShadowOffsetX(double offsetX) {
        this.shadowOffsetX = offsetX;
    }

    @Override
    public void setShadowOffsetY(double offsetY) {
        this.shadowOffsetY = offsetY;
    }

    // Image smoothing setters
    @Override
    public void setImageSmoothingEnabled(boolean enabled) {
        this.imageSmoothingEnabled = enabled;
        updateImageSmoothingHints();
    }

    @Override
    public void setImageSmoothingQuality(String quality) {
        this.imageSmoothingQuality = quality;
        updateImageSmoothingHints();
    }

    private void updateImageSmoothingHints() {
        if (!imageSmoothingEnabled) {
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        } else {
            if ("low".equals(imageSmoothingQuality)) {
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            } else if ("medium".equals(imageSmoothingQuality) || "high".equals(imageSmoothingQuality)) {
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            }
        }
    }

    // Helper method to apply shadow effect
    private void applyShadow(Shape shape, boolean isFill) {
        // Check if shadow is active (non-zero offset or blur, and non-transparent color)
        boolean hasShadow = (shadowBlur > 0 || shadowOffsetX != 0 || shadowOffsetY != 0)
                          && shadowColor != null && !shadowColor.equals("rgba(0, 0, 0, 0)");

        if (!hasShadow) {
            return;
        }

        // Parse shadow color
        java.awt.Color shadowCol = parseColor(shadowColor);
        if (shadowCol.getAlpha() == 0) {
            return; // Fully transparent shadow
        }

        // Save current state
        Paint oldPaint = g2d.getPaint();
        Composite oldComposite = g2d.getComposite();

        // Create transformed shape for shadow
        AffineTransform shadowTransform = AffineTransform.getTranslateInstance(shadowOffsetX, shadowOffsetY);
        Shape shadowShape = shadowTransform.createTransformedShape(shape);

        // Apply shadow with blur approximation
        if (shadowBlur > 0) {
            // Simple blur approximation: draw multiple times with decreasing opacity
            int blurSteps = Math.min((int) Math.ceil(shadowBlur / 2), 5);
            float baseAlpha = shadowCol.getAlpha() / 255.0f;

            for (int i = 0; i < blurSteps; i++) {
                float alpha = baseAlpha * (1.0f - (float) i / blurSteps) / blurSteps;
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                g2d.setPaint(new java.awt.Color(shadowCol.getRed(), shadowCol.getGreen(), shadowCol.getBlue()));

                double spread = i * shadowBlur / blurSteps;
                AffineTransform blurTransform = AffineTransform.getTranslateInstance(
                    shadowOffsetX - spread / 2, shadowOffsetY - spread / 2
                );
                Shape blurredShape = blurTransform.createTransformedShape(shape);

                if (isFill) {
                    g2d.fill(blurredShape);
                } else {
                    g2d.draw(blurredShape);
                }
            }
        } else {
            // No blur, just draw shadow once
            g2d.setPaint(shadowCol);
            if (isFill) {
                g2d.fill(shadowShape);
            } else {
                g2d.draw(shadowShape);
            }
        }

        // Restore state
        g2d.setPaint(oldPaint);
        g2d.setComposite(oldComposite);
    }

    private java.awt.Color parseColor(String color) {
        try {
            // Use existing ColorParser if available, otherwise parse basic colors
            if (color.startsWith("rgba(")) {
                String[] parts = color.substring(5, color.length() - 1).split(",");
                int r = Integer.parseInt(parts[0].trim());
                int g = Integer.parseInt(parts[1].trim());
                int b = Integer.parseInt(parts[2].trim());
                float a = Float.parseFloat(parts[3].trim());
                return new java.awt.Color(r, g, b, (int) (a * 255));
            } else if (color.startsWith("rgb(")) {
                String[] parts = color.substring(4, color.length() - 1).split(",");
                int r = Integer.parseInt(parts[0].trim());
                int g = Integer.parseInt(parts[1].trim());
                int b = Integer.parseInt(parts[2].trim());
                return new java.awt.Color(r, g, b);
            } else if (color.startsWith("#")) {
                return java.awt.Color.decode(color);
            }
        } catch (Exception e) {
            // Fallback to black
        }
        return java.awt.Color.BLACK;
    }

    // Filter methods
    @Override
    public void setFilter(String filter) {
        this.filter = (filter == null || filter.trim().isEmpty()) ? "none" : filter;
    }

    @Override
    public String getFilter() {
        return this.filter;
    }

    /**
     * Apply CSS filters to an image using AWT BufferedImageOp operations.
     * This creates a filtered version of the current canvas content.
     * Note: Filters are applied during rendering operations in the fill/stroke methods.
     */
    private BufferedImage applyFiltersToImage(BufferedImage source) {
        if (filter == null || "none".equals(filter)) {
            return source;
        }

        java.util.List<com.w3canvas.javacanvas.core.FilterFunction> filters =
            com.w3canvas.javacanvas.core.CSSFilterParser.parse(filter);

        if (filters.isEmpty()) {
            return source;
        }

        BufferedImage result = source;

        for (com.w3canvas.javacanvas.core.FilterFunction filterFunc : filters) {
            result = applySingleFilter(result, filterFunc);
        }

        return result;
    }

    /**
     * Apply a single filter function to an image
     */
    private BufferedImage applySingleFilter(BufferedImage source, com.w3canvas.javacanvas.core.FilterFunction filter) {
        switch (filter.getType()) {
            case BLUR:
                return applyBlurFilter(source, filter.getDoubleParam(0));
            case BRIGHTNESS:
                return applyBrightnessFilter(source, filter.getDoubleParam(0));
            case CONTRAST:
                return applyContrastFilter(source, filter.getDoubleParam(0));
            case GRAYSCALE:
                return applyGrayscaleFilter(source, filter.getDoubleParam(0));
            case SEPIA:
                return applySepiaFilter(source, filter.getDoubleParam(0));
            case SATURATE:
                return applySaturateFilter(source, filter.getDoubleParam(0));
            case HUE_ROTATE:
                return applyHueRotateFilter(source, filter.getDoubleParam(0));
            case INVERT:
                return applyInvertFilter(source, filter.getDoubleParam(0));
            case OPACITY:
                return applyOpacityFilter(source, filter.getDoubleParam(0));
            default:
                return source;
        }
    }

    /**
     * Apply blur filter using ConvolveOp
     */
    private BufferedImage applyBlurFilter(BufferedImage source, double radius) {
        if (radius <= 0) {
            return source;
        }

        // Create a simple box blur kernel
        int size = Math.max(3, (int) Math.ceil(radius) * 2 + 1);
        float weight = 1.0f / (size * size);
        float[] kernelData = new float[size * size];
        for (int i = 0; i < kernelData.length; i++) {
            kernelData[i] = weight;
        }

        java.awt.image.Kernel kernel = new java.awt.image.Kernel(size, size, kernelData);
        java.awt.image.ConvolveOp convolve = new java.awt.image.ConvolveOp(kernel, java.awt.image.ConvolveOp.EDGE_NO_OP, null);

        BufferedImage result = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
        convolve.filter(source, result);
        return result;
    }

    /**
     * Apply brightness filter using RescaleOp
     */
    private BufferedImage applyBrightnessFilter(BufferedImage source, double amount) {
        float scale = (float) amount;
        float offset = 0.0f;

        java.awt.image.RescaleOp rescale = new java.awt.image.RescaleOp(scale, offset, null);
        BufferedImage result = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
        rescale.filter(source, result);
        return result;
    }

    /**
     * Apply contrast filter
     */
    private BufferedImage applyContrastFilter(BufferedImage source, double amount) {
        float scale = (float) amount;
        float offset = 128.0f * (1.0f - scale);

        java.awt.image.RescaleOp rescale = new java.awt.image.RescaleOp(scale, offset, null);
        BufferedImage result = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
        rescale.filter(source, result);
        return result;
    }

    /**
     * Apply grayscale filter
     */
    private BufferedImage applyGrayscaleFilter(BufferedImage source, double amount) {
        BufferedImage result = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                int rgb = source.getRGB(x, y);
                int a = (rgb >> 24) & 0xff;
                int r = (rgb >> 16) & 0xff;
                int g = (rgb >> 8) & 0xff;
                int b = rgb & 0xff;

                // Calculate grayscale value
                int gray = (int) (0.299 * r + 0.587 * g + 0.114 * b);

                // Interpolate between original and grayscale
                r = (int) (r + amount * (gray - r));
                g = (int) (g + amount * (gray - g));
                b = (int) (b + amount * (gray - b));

                rgb = (a << 24) | (r << 16) | (g << 8) | b;
                result.setRGB(x, y, rgb);
            }
        }

        return result;
    }

    /**
     * Apply sepia filter
     */
    private BufferedImage applySepiaFilter(BufferedImage source, double amount) {
        BufferedImage result = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                int rgb = source.getRGB(x, y);
                int a = (rgb >> 24) & 0xff;
                int r = (rgb >> 16) & 0xff;
                int g = (rgb >> 8) & 0xff;
                int b = rgb & 0xff;

                // Calculate sepia values
                int sr = (int) Math.min(255, (r * 0.393 + g * 0.769 + b * 0.189));
                int sg = (int) Math.min(255, (r * 0.349 + g * 0.686 + b * 0.168));
                int sb = (int) Math.min(255, (r * 0.272 + g * 0.534 + b * 0.131));

                // Interpolate between original and sepia
                r = (int) (r + amount * (sr - r));
                g = (int) (g + amount * (sg - g));
                b = (int) (b + amount * (sb - b));

                rgb = (a << 24) | (r << 16) | (g << 8) | b;
                result.setRGB(x, y, rgb);
            }
        }

        return result;
    }

    /**
     * Apply saturate filter
     */
    private BufferedImage applySaturateFilter(BufferedImage source, double amount) {
        BufferedImage result = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                int rgb = source.getRGB(x, y);
                int a = (rgb >> 24) & 0xff;
                int r = (rgb >> 16) & 0xff;
                int g = (rgb >> 8) & 0xff;
                int b = rgb & 0xff;

                // Calculate grayscale for saturation adjustment
                int gray = (int) (0.299 * r + 0.587 * g + 0.114 * b);

                // Adjust saturation
                r = (int) Math.max(0, Math.min(255, gray + amount * (r - gray)));
                g = (int) Math.max(0, Math.min(255, gray + amount * (g - gray)));
                b = (int) Math.max(0, Math.min(255, gray + amount * (b - gray)));

                rgb = (a << 24) | (r << 16) | (g << 8) | b;
                result.setRGB(x, y, rgb);
            }
        }

        return result;
    }

    /**
     * Apply hue-rotate filter
     */
    private BufferedImage applyHueRotateFilter(BufferedImage source, double degrees) {
        BufferedImage result = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);

        double radians = Math.toRadians(degrees);
        double cosA = Math.cos(radians);
        double sinA = Math.sin(radians);

        // Rotation matrix for hue
        double[][] matrix = {
            {0.213 + cosA * 0.787 - sinA * 0.213, 0.715 - cosA * 0.715 - sinA * 0.715, 0.072 - cosA * 0.072 + sinA * 0.928},
            {0.213 - cosA * 0.213 + sinA * 0.143, 0.715 + cosA * 0.285 + sinA * 0.140, 0.072 - cosA * 0.072 - sinA * 0.283},
            {0.213 - cosA * 0.213 - sinA * 0.787, 0.715 - cosA * 0.715 + sinA * 0.715, 0.072 + cosA * 0.928 + sinA * 0.072}
        };

        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                int rgb = source.getRGB(x, y);
                int a = (rgb >> 24) & 0xff;
                int r = (rgb >> 16) & 0xff;
                int g = (rgb >> 8) & 0xff;
                int b = rgb & 0xff;

                int nr = (int) Math.max(0, Math.min(255, r * matrix[0][0] + g * matrix[0][1] + b * matrix[0][2]));
                int ng = (int) Math.max(0, Math.min(255, r * matrix[1][0] + g * matrix[1][1] + b * matrix[1][2]));
                int nb = (int) Math.max(0, Math.min(255, r * matrix[2][0] + g * matrix[2][1] + b * matrix[2][2]));

                rgb = (a << 24) | (nr << 16) | (ng << 8) | nb;
                result.setRGB(x, y, rgb);
            }
        }

        return result;
    }

    /**
     * Apply invert filter
     */
    private BufferedImage applyInvertFilter(BufferedImage source, double amount) {
        BufferedImage result = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                int rgb = source.getRGB(x, y);
                int a = (rgb >> 24) & 0xff;
                int r = (rgb >> 16) & 0xff;
                int g = (rgb >> 8) & 0xff;
                int b = rgb & 0xff;

                // Invert
                int ir = 255 - r;
                int ig = 255 - g;
                int ib = 255 - b;

                // Interpolate
                r = (int) (r + amount * (ir - r));
                g = (int) (g + amount * (ig - g));
                b = (int) (b + amount * (ib - b));

                rgb = (a << 24) | (r << 16) | (g << 8) | b;
                result.setRGB(x, y, rgb);
            }
        }

        return result;
    }

    /**
     * Apply opacity filter
     */
    private BufferedImage applyOpacityFilter(BufferedImage source, double amount) {
        BufferedImage result = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                int rgb = source.getRGB(x, y);
                int a = (rgb >> 24) & 0xff;
                int r = (rgb >> 16) & 0xff;
                int g = (rgb >> 8) & 0xff;
                int b = rgb & 0xff;

                // Adjust alpha
                a = (int) (a * amount);

                rgb = (a << 24) | (r << 16) | (g << 8) | b;
                result.setRGB(x, y, rgb);
            }
        }

        return result;
    }
}
