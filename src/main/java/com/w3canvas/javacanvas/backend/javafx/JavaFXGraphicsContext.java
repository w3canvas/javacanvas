package com.w3canvas.javacanvas.backend.javafx;

import com.w3canvas.javacanvas.interfaces.*;
import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.image.WritableImage;
import javafx.scene.image.PixelFormat;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Affine;
import javafx.scene.image.Image;
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.Path;
import java.awt.geom.Path2D;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.QuadCurveTo;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.ClosePath;
import java.awt.geom.PathIterator;


public class JavaFXGraphicsContext implements IGraphicsContext {

    private final GraphicsContext gc;
    private double[] lastPoint = new double[2];
    private Path path;
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

    // Fill rule
    private String fillRule = "nonzero";

    public JavaFXGraphicsContext(GraphicsContext gc) {
        this.gc = gc;
        this.path = new Path();
        this.fillPaint = new JavaFXPaint(javafx.scene.paint.Color.BLACK);
        this.strokePaint = new JavaFXPaint(javafx.scene.paint.Color.BLACK);
    }

    @Override
    public double[] getLastPoint() {
        return lastPoint;
    }

    @Override
    public void fillText(String text, double x, double y, double maxWidth) {
        gc.save();
        if (this.fillPaint instanceof JavaFXPaint) {
            gc.setFill(((JavaFXPaint) this.fillPaint).getPaint());
        }
        gc.fillText(text, x, y);
        gc.restore();
    }

    @Override
    public void strokeText(String text, double x, double y, double maxWidth) {
        if (this.strokePaint instanceof JavaFXPaint) {
            gc.setStroke(((JavaFXPaint) this.strokePaint).getPaint());
        }
        gc.strokeText(text, x, y);
    }

    @Override
    public IImageData createImageData(int width, int height) {
        int[] data = new int[width * height];
        return new com.w3canvas.javacanvas.core.ImageData(width, height, new com.w3canvas.javacanvas.core.CanvasPixelArray(data, width, height));
    }

    @Override
    public IImageData getImageData(int x, int y, int width, int height) {
        WritableImage snapshot = new WritableImage(width, height);
        gc.getCanvas().snapshot(null, snapshot);
        int[] pixels = new int[width * height];
        snapshot.getPixelReader().getPixels(0, 0, width, height, PixelFormat.getIntArgbInstance(), pixels, 0, width);
        return new com.w3canvas.javacanvas.core.ImageData(width, height, new com.w3canvas.javacanvas.core.CanvasPixelArray(pixels, width, height));
    }

    @Override
    public ITextMetrics measureText(String text) {
        return new JavaFXTextMetrics(gc.getFont(), text);
    }

    @Override
    public void scale(double x, double y) {
        gc.scale(x, y);
    }

    @Override
    public void rotate(double theta) {
        gc.rotate(Math.toDegrees(theta));
    }

    @Override
    public void translate(double tx, double ty) {
        gc.translate(tx, ty);
    }

    @Override
    public void transform(double m11, double m12, double m21, double m22, double dx, double dy) {
        gc.transform(new Affine(m11, m21, dx, m12, m22, dy));
    }

    @Override
    public void setTransform(double m11, double m12, double m21, double m22, double dx, double dy) {
        gc.setTransform(new Affine(m11, m21, dx, m12, m22, dy));
    }

    @Override
    public void setTransform(Object transform) {
        if (transform instanceof Affine) {
            gc.setTransform((Affine) transform);
        }
    }

    @Override
    public void resetTransform() {
        gc.setTransform(new Affine());
    }

    @Override
    public Object getTransform() {
        return gc.getTransform();
    }

    @Override
    public void setFillPaint(IPaint paint) {
        this.fillPaint = paint;
        if (paint instanceof JavaFXPaint) {
            gc.setFill(((JavaFXPaint) paint).getPaint());
        } else if (paint instanceof JavaFXLinearGradient) {
            gc.setFill((Paint) ((JavaFXLinearGradient) paint).getPaint());
        } else if (paint instanceof JavaFXRadialGradient) {
            gc.setFill((Paint) ((JavaFXRadialGradient) paint).getPaint());
        } else if (paint instanceof JavaFXConicGradient) {
            gc.setFill((Paint) ((JavaFXConicGradient) paint).getPaint());
        } else if (paint instanceof JavaFXPattern) {
            JavaFXPattern pattern = (JavaFXPattern) paint;
            double width = gc.getCanvas().getWidth();
            double height = gc.getCanvas().getHeight();
            gc.setFill(pattern.getPaint(width, height));
        }
    }

    @Override
    public void setStrokePaint(IPaint paint) {
        this.strokePaint = paint;
        if (paint instanceof JavaFXPaint) {
            gc.setStroke(((JavaFXPaint) paint).getPaint());
        } else if (paint instanceof JavaFXLinearGradient) {
            gc.setStroke((Paint) ((JavaFXLinearGradient) paint).getPaint());
        } else if (paint instanceof JavaFXRadialGradient) {
            gc.setStroke((Paint) ((JavaFXRadialGradient) paint).getPaint());
        } else if (paint instanceof JavaFXConicGradient) {
            gc.setStroke((Paint) ((JavaFXConicGradient) paint).getPaint());
        } else if (paint instanceof JavaFXPattern) {
            JavaFXPattern pattern = (JavaFXPattern) paint;
            double width = gc.getCanvas().getWidth();
            double height = gc.getCanvas().getHeight();
            gc.setStroke(pattern.getPaint(width, height));
        }
    }

    @Override
    public ICanvasPattern createPattern(Object image, String repetition) {
        if (image instanceof Image) {
            return new JavaFXPattern((Image) image, repetition);
        } else if (image instanceof java.awt.image.BufferedImage) {
            // Convert BufferedImage to JavaFX Image
            java.awt.image.BufferedImage awtImage = (java.awt.image.BufferedImage) image;
            WritableImage fxImage = new WritableImage(awtImage.getWidth(), awtImage.getHeight());
            for (int x = 0; x < awtImage.getWidth(); x++) {
                for (int y = 0; y < awtImage.getHeight(); y++) {
                    fxImage.getPixelWriter().setArgb(x, y, awtImage.getRGB(x, y));
                }
            }
            return new JavaFXPattern(fxImage, repetition);
        }
        return null;
    }

    @Override
    public void setComposite(IComposite comp) {
        if (comp instanceof JavaFXComposite) {
            gc.setGlobalBlendMode(((JavaFXComposite) comp).getBlendMode());
        }
    }

    @Override
    public void setGlobalAlpha(double alpha) {
        gc.setGlobalAlpha(alpha);
    }

    @Override
    public void setLineWidth(double width) {
        gc.setLineWidth(width);
    }

    @Override
    public void setLineCap(String cap) {
        if ("round".equals(cap)) gc.setLineCap(StrokeLineCap.ROUND);
        else if ("square".equals(cap)) gc.setLineCap(StrokeLineCap.SQUARE);
        else gc.setLineCap(StrokeLineCap.BUTT);
    }

    @Override
    public void setLineJoin(String join) {
        if ("round".equals(join)) gc.setLineJoin(StrokeLineJoin.ROUND);
        else if ("bevel".equals(join)) gc.setLineJoin(StrokeLineJoin.BEVEL);
        else gc.setLineJoin(StrokeLineJoin.MITER);
    }

    @Override
    public void setMiterLimit(double limit) {
        gc.setMiterLimit(limit);
    }

    @Override
    public void setLineDash(double[] dash) {
        gc.setLineDashes(dash);
    }

    @Override
    public void setLineDashOffset(double offset) {
        gc.setLineDashOffset(offset);
    }

    @Override
    public void setFont(IFont font) {
        if (font instanceof JavaFXFont) {
            gc.setFont(((JavaFXFont) font).getFont());
        }
    }

    @Override
    public void setTextAlign(String textAlign) {
        switch (textAlign) {
            case "right":
            case "end":
                gc.setTextAlign(TextAlignment.RIGHT);
                break;
            case "center":
                gc.setTextAlign(TextAlignment.CENTER);
                break;
            default:
                gc.setTextAlign(TextAlignment.LEFT);
                break;
        }
    }

    @Override
    public void setTextBaseline(String textBaseline) {
        switch (textBaseline) {
            case "top":
            case "hanging":
                gc.setTextBaseline(VPos.TOP);
                break;
            case "middle":
                gc.setTextBaseline(VPos.CENTER);
                break;
            case "alphabetic":
            case "ideographic":
                gc.setTextBaseline(VPos.BASELINE);
                break;
            case "bottom":
                gc.setTextBaseline(VPos.BOTTOM);
                break;
            default:
                gc.setTextBaseline(VPos.BASELINE);
                break;
        }
    }

    @Override
    public void clearRect(double x, double y, double w, double h) {
        // gc.clearRect(x, y, w, h); // Original implementation

        // New, more robust implementation that is not affected by transform
        gc.save();
        gc.setTransform(new Affine()); // Identity transform
        gc.clearRect(x, y, w, h);
        gc.restore();
    }

    @Override
    public void fillRectDirect(double x, double y, double w, double h) {
        // Use JavaFX's native fillRect method which bypasses the path system
        // This is needed because JavaFX's path system doesn't properly handle
        // multiple rect() calls within the same path
        //
        // WORKAROUND for Path2D.addPath() with multiple rectangles:
        // When CoreCanvasRenderingContext2D detects a Path2D containing multiple
        // RECT elements, it calls this method for each rectangle individually.
        // The fill paint and transform are already set by the caller, so we just
        // need to draw the rectangle directly without save/restore which would
        // interfere with the state.
        gc.fillRect(x, y, w, h);
    }

    @Override
    public void draw(IShape shape) {
        gc.stroke();
    }

    @Override
    public void fill(IShape shape) {
        gc.fill();
    }

    @Override
    public void drawImage(Object img, int x, int y) {
        if (img instanceof Image) {
            gc.drawImage((Image) img, x, y);
        }
    }

    @Override
    public void drawImage(int[] pixels, int x, int y, int width, int height) {
        if (pixels == null || pixels.length == 0) {
            return;
        }
        WritableImage image = new WritableImage(width, height);
        image.getPixelWriter().setPixels(0, 0, width, height, PixelFormat.getIntArgbInstance(), pixels, 0, width);
        gc.drawImage(image, x, y, width, height);
    }

    @Override
    public void drawImage(Object img, int sx, int sy, int sw, int sh, int dx, int dy, int dw, int dh) {
        if (img instanceof Image) {
            gc.drawImage((Image) img, sx, sy, sw, sh, dx, dy, dw, dh);
        }
    }

    @Override
    public void drawString(String str, int x, int y) {
        gc.strokeText(str, x, y);
    }

    @Override
    public void clip() {
        gc.clip();
    }

    @Override
    public void beginPath() {
        gc.beginPath();
        path = new Path();
    }

    @Override
    public void closePath() {
        gc.closePath();
        path.getElements().add(new javafx.scene.shape.ClosePath());
    }

    @Override
    public void moveTo(double x, double y) {
        gc.moveTo(x, y);
        path.getElements().add(new javafx.scene.shape.MoveTo(x, y));
        lastPoint[0] = x;
        lastPoint[1] = y;
    }

    @Override
    public void lineTo(double x, double y) {
        gc.lineTo(x, y);
        path.getElements().add(new javafx.scene.shape.LineTo(x, y));
        lastPoint[0] = x;
        lastPoint[1] = y;
    }

    @Override
    public void quadraticCurveTo(double cpx, double cpy, double x, double y) {
        gc.quadraticCurveTo(cpx, cpy, x, y);
        path.getElements().add(new javafx.scene.shape.QuadCurveTo(cpx, cpy, x, y));
        lastPoint[0] = x;
        lastPoint[1] = y;
    }

    @Override
    public void bezierCurveTo(double cp1x, double cp1y, double cp2x, double cp2y, double x, double y) {
        gc.bezierCurveTo(cp1x, cp1y, cp2x, cp2y, x, y);
        path.getElements().add(new javafx.scene.shape.CubicCurveTo(cp1x, cp1y, cp2x, cp2y, x, y));
        lastPoint[0] = x;
        lastPoint[1] = y;
    }

    @Override
    public void arcTo(double x1, double y1, double x2, double y2, double radius) {
        double x0 = lastPoint[0];
        double y0 = lastPoint[1];

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

        // We use the JavaFX GraphicsContext's arcTo to draw on the canvas.
        gc.arcTo(x1, y1, x2, y2, radius);

        // And we add a JavaFX ArcTo path element to our own path object.
        // The sweep flag is determined by the sign of the cross product of the two vectors.
        // Positive cross product (left turn) requires sweepFlag=true to select the correct arc center
        boolean sweepFlag = (dx01 * dy12 - dy01 * dx12) > 0;
        ArcTo arcTo = new ArcTo(radius, radius, 0, t2x, t2y, false, sweepFlag);
        path.getElements().add(arcTo);

        lastPoint[0] = t2x;
        lastPoint[1] = t2y;
    }

    @Override
    public void rect(double x, double y, double w, double h) {
        // JavaFX's gc.rect() doesn't properly add multiple rectangles to the same path.
        // Instead, we manually add the path elements using moveTo/lineTo.
        //
        // According to Canvas 2D spec, rect() should create a new subpath containing
        // just the four points of the rectangle, with the subpath closed.

        // Add to JavaFX's built-in path using moveTo/lineTo
        // This builds up the GraphicsContext's current path for rendering
        gc.moveTo(x, y);
        gc.lineTo(x + w, y);
        gc.lineTo(x + w, y + h);
        gc.lineTo(x, y + h);
        gc.lineTo(x, y);  // Explicitly close by going back to start
        // DON'T call gc.closePath() - it prevents multiple rectangles from working

        // Also maintain the separate Path object for getPath() and isPointInPath()
        path.getElements().add(new javafx.scene.shape.MoveTo(x, y));
        path.getElements().add(new javafx.scene.shape.LineTo(x + w, y));
        path.getElements().add(new javafx.scene.shape.LineTo(x + w, y + h));
        path.getElements().add(new javafx.scene.shape.LineTo(x, y + h));
        path.getElements().add(new javafx.scene.shape.ClosePath());
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

        // Build the rounded rectangle path using JavaFX path elements
        // Start at top-left corner (after the radius)
        path.getElements().add(new javafx.scene.shape.MoveTo(x + tlRadius, y));

        // Top edge and top-right corner
        path.getElements().add(new javafx.scene.shape.LineTo(x + w - trRadius, y));
        if (trRadius > 0) {
            path.getElements().add(new javafx.scene.shape.QuadCurveTo(x + w, y, x + w, y + trRadius));
        }

        // Right edge and bottom-right corner
        path.getElements().add(new javafx.scene.shape.LineTo(x + w, y + h - brRadius));
        if (brRadius > 0) {
            path.getElements().add(new javafx.scene.shape.QuadCurveTo(x + w, y + h, x + w - brRadius, y + h));
        }

        // Bottom edge and bottom-left corner
        path.getElements().add(new javafx.scene.shape.LineTo(x + blRadius, y + h));
        if (blRadius > 0) {
            path.getElements().add(new javafx.scene.shape.QuadCurveTo(x, y + h, x, y + h - blRadius));
        }

        // Left edge and top-left corner
        path.getElements().add(new javafx.scene.shape.LineTo(x, y + tlRadius));
        if (tlRadius > 0) {
            path.getElements().add(new javafx.scene.shape.QuadCurveTo(x, y, x + tlRadius, y));
        }

        path.getElements().add(new javafx.scene.shape.ClosePath());

        // Also add to GraphicsContext for immediate rendering
        gc.beginPath();
        gc.moveTo(x + tlRadius, y);
        gc.lineTo(x + w - trRadius, y);
        if (trRadius > 0) {
            gc.quadraticCurveTo(x + w, y, x + w, y + trRadius);
        }
        gc.lineTo(x + w, y + h - brRadius);
        if (brRadius > 0) {
            gc.quadraticCurveTo(x + w, y + h, x + w - brRadius, y + h);
        }
        gc.lineTo(x + blRadius, y + h);
        if (blRadius > 0) {
            gc.quadraticCurveTo(x, y + h, x, y + h - blRadius);
        }
        gc.lineTo(x, y + tlRadius);
        if (tlRadius > 0) {
            gc.quadraticCurveTo(x, y, x + tlRadius, y);
        }
        gc.closePath();
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
        double sweep = endAngle - startAngle;
        if (counterclockwise) {
            if (sweep > 0) {
                sweep = sweep - 2 * Math.PI;
            }
        } else {
            if (sweep < 0) {
                sweep = sweep + 2 * Math.PI;
            }
        }

        double startX = x + radius * Math.cos(startAngle);
        double startY = y + radius * Math.sin(startAngle);
        if (path.getElements().isEmpty()) {
            moveTo(startX, startY);
        } else {
            lineTo(startX, startY);
        }

        gc.arc(x, y, radius, radius, Math.toDegrees(startAngle), Math.toDegrees(sweep));

        double endX = x + radius * Math.cos(startAngle + sweep);
        double endY = y + radius * Math.sin(startAngle + sweep);
        path.getElements().add(new ArcTo(radius, radius, 0, endX, endY, Math.abs(sweep) > Math.PI, !counterclockwise));
        lastPoint[0] = endX;
        lastPoint[1] = endY;
    }

    @Override
    public void ellipse(double x, double y, double radiusX, double radiusY, double rotation, double startAngle, double endAngle, boolean counterclockwise) {
        // This is more complex in JavaFX, requires saving state, rotating, and drawing arc
        gc.save();
        gc.translate(x, y);
        gc.rotate(Math.toDegrees(rotation));
        double length = endAngle - startAngle;
        if(counterclockwise) {
            if(length < 0) length += 2 * Math.PI;
            length = -(length);
        }
        // Manually calculate the start point of the ellipse arc
        double startX = radiusX * Math.cos(startAngle);
        double startY = radiusY * Math.sin(startAngle);

        // Move to the start point of the arc
        gc.moveTo(startX, startY);
        path.getElements().add(new javafx.scene.shape.MoveTo(startX, startY));

        gc.arc(0, 0, radiusX, radiusY, Math.toDegrees(startAngle), Math.toDegrees(length));
        path.getElements().add(new javafx.scene.shape.ArcTo(radiusX, radiusY, Math.toDegrees(rotation), x + radiusX * Math.cos(endAngle), y + radiusY * Math.sin(endAngle), false, !counterclockwise));
        gc.restore();
    }

    @Override
    public void fill() {
        fill(this.fillRule);
    }

    @Override
    public void fill(String fillRule) {
        // Set the fill rule on the path based on the fillRule parameter
        if (path != null) {
            if ("evenodd".equals(fillRule)) {
                path.setFillRule(javafx.scene.shape.FillRule.EVEN_ODD);
            } else {
                path.setFillRule(javafx.scene.shape.FillRule.NON_ZERO);
            }
        }
        gc.fill();
    }

    @Override
    public void setFillRule(String fillRule) {
        this.fillRule = fillRule != null ? fillRule : "nonzero";
    }

    @Override
    public void stroke() {
        gc.stroke();
    }

    @Override
    public boolean isPointInPath(double x, double y) {
        return gc.isPointInPath(x, y);
    }

    @Override
    public boolean isPointInStroke(double x, double y) {
        // 1. Convert JavaFX Path to AWT Shape
        Path2D.Double awtPath = convertFxPathToAwtPath(this.path);

        // 2. Create AWT BasicStroke
        int cap = java.awt.BasicStroke.CAP_BUTT;
        if (gc.getLineCap() == StrokeLineCap.ROUND) {
            cap = java.awt.BasicStroke.CAP_ROUND;
        } else if (gc.getLineCap() == StrokeLineCap.SQUARE) {
            cap = java.awt.BasicStroke.CAP_SQUARE;
        }

        int join = java.awt.BasicStroke.JOIN_MITER;
        if (gc.getLineJoin() == StrokeLineJoin.BEVEL) {
            join = java.awt.BasicStroke.JOIN_BEVEL;
        } else if (gc.getLineJoin() == StrokeLineJoin.ROUND) {
            join = java.awt.BasicStroke.JOIN_ROUND;
        }

        java.awt.BasicStroke stroke = new java.awt.BasicStroke(
                (float) gc.getLineWidth(),
                cap,
                join,
                (float) gc.getMiterLimit()
        );

        // 3. Create stroked shape
        java.awt.Shape strokedShape = stroke.createStrokedShape(awtPath);

        // 4. Check if the point is in the stroked shape
        return strokedShape.contains(x, y);
    }

    private Path2D.Double convertFxPathToAwtPath(Path fxPath) {
        Path2D.Double awtPath = new Path2D.Double();
        for (var element : fxPath.getElements()) {
            if (element instanceof MoveTo) {
                MoveTo moveTo = (MoveTo) element;
                awtPath.moveTo(moveTo.getX(), moveTo.getY());
            } else if (element instanceof LineTo) {
                LineTo lineTo = (LineTo) element;
                awtPath.lineTo(lineTo.getX(), lineTo.getY());
            } else if (element instanceof QuadCurveTo) {
                QuadCurveTo quadTo = (QuadCurveTo) element;
                awtPath.quadTo(quadTo.getControlX(), quadTo.getControlY(), quadTo.getX(), quadTo.getY());
            } else if (element instanceof CubicCurveTo) {
                CubicCurveTo cubicTo = (CubicCurveTo) element;
                awtPath.curveTo(cubicTo.getControlX1(), cubicTo.getControlY1(),
                        cubicTo.getControlX2(), cubicTo.getControlY2(),
                        cubicTo.getX(), cubicTo.getY());
            } else if (element instanceof ArcTo) {
                ArcTo arcTo = (ArcTo) element;
                double x = arcTo.getX();
                double y = arcTo.getY();
                double radiusX = arcTo.getRadiusX();
                double radiusY = arcTo.getRadiusY();
                boolean sweepFlag = arcTo.isSweepFlag();
                boolean largeArcFlag = arcTo.isLargeArcFlag();
                double xAxisRotation = arcTo.getXAxisRotation();

                double x0 = awtPath.getCurrentPoint().getX();
                double y0 = awtPath.getCurrentPoint().getY();

                // SVG arc to center-parameterized arc conversion
                // See https://www.w3.org/TR/SVG/implnote.html#ArcConversionEndpointToCenter
                double cosr = Math.cos(Math.toRadians(xAxisRotation));
                double sinr = Math.sin(Math.toRadians(xAxisRotation));

                double dx = (x0 - x) / 2;
                double dy = (y0 - y) / 2;

                double x1p = cosr * dx + sinr * dy;
                double y1p = -sinr * dx + cosr * dy;

                double rx_sq = radiusX * radiusX;
                double ry_sq = radiusY * radiusY;
                double x1p_sq = x1p * x1p;
                double y1p_sq = y1p * y1p;

                double lambda = x1p_sq / rx_sq + y1p_sq / ry_sq;
                if (lambda > 1) {
                    radiusX *= Math.sqrt(lambda);
                    radiusY *= Math.sqrt(lambda);
                    rx_sq = radiusX * radiusX;
                    ry_sq = radiusY * radiusY;
                }

                double sign = (largeArcFlag == sweepFlag) ? -1 : 1;
                double c_sq_num = rx_sq * ry_sq - rx_sq * y1p_sq - ry_sq * x1p_sq;
                double c_sq_den = rx_sq * y1p_sq + ry_sq * x1p_sq;
                double c_rad = Math.sqrt(Math.max(0, c_sq_num / c_sq_den));

                double cxp = sign * c_rad * (radiusX * y1p / radiusY);
                double cyp = sign * c_rad * -(radiusY * x1p / radiusX);

                double cx = cosr * cxp - sinr * cyp + (x0 + x) / 2;
                double cy = sinr * cxp + cosr * cyp + (y0 + y) / 2;

                double ux = (x1p - cxp) / radiusX;
                double uy = (y1p - cyp) / radiusY;
                double vx = (-x1p - cxp) / radiusX;
                double vy = (-y1p - cyp) / radiusY;

                double startAngle = Math.toDegrees(Math.atan2(uy, ux));
                double extent = Math.toDegrees(angle(ux, uy, vx, vy));

                // Adjust sweep direction based on sweepFlag
                // SVG/Canvas: sweepFlag=true means clockwise, sweepFlag=false means counter-clockwise
                // AWT Arc2D: positive extent is counter-clockwise, negative extent is clockwise
                // So for clockwise (sweepFlag=true), we need negative extent
                if (sweepFlag) {
                    extent = -extent;
                }

                awtPath.append(new java.awt.geom.Arc2D.Double(cx - radiusX, cy - radiusY, 2 * radiusX, 2 * radiusY, startAngle, extent, java.awt.geom.Arc2D.OPEN), true);

            } else if (element instanceof ClosePath) {
                awtPath.closePath();
            }
        }
        return awtPath;
    }

    private double angle(double ux, double uy, double vx, double vy) {
        double dot = ux * vx + uy * vy;
        double len = Math.sqrt(ux * ux + uy * uy) * Math.sqrt(vx * vx + vy * vy);
        double angle = Math.acos(Math.min(Math.max(dot / len, -1), 1));
        if ((ux * vy - uy * vx) < 0) {
            angle = -angle;
        }
        return angle;
    }

    private Path convertAwtShapeToFxPath(java.awt.Shape awtShape) {
        Path fxPath = new Path();
        PathIterator pathIterator = awtShape.getPathIterator(null);
        double[] coords = new double[6];
        while (!pathIterator.isDone()) {
            int segmentType = pathIterator.currentSegment(coords);
            switch (segmentType) {
                case PathIterator.SEG_MOVETO:
                    fxPath.getElements().add(new MoveTo(coords[0], coords[1]));
                    break;
                case PathIterator.SEG_LINETO:
                    fxPath.getElements().add(new LineTo(coords[0], coords[1]));
                    break;
                case PathIterator.SEG_QUADTO:
                    fxPath.getElements().add(new QuadCurveTo(coords[0], coords[1], coords[2], coords[3]));
                    break;
                case PathIterator.SEG_CUBICTO:
                    fxPath.getElements().add(new CubicCurveTo(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]));
                    break;
                case PathIterator.SEG_CLOSE:
                    fxPath.getElements().add(new ClosePath());
                    break;
            }
            pathIterator.next();
        }
        return fxPath;
    }


    @Override
    public IShape getPath() {
        return new JavaFXShape(new Path(path.getElements()));
    }

    @Override
    public void setPath(IShape shape) {
        if (shape instanceof JavaFXShape) {
            Object nativeShape = ((JavaFXShape) shape).getShape();
            if (nativeShape instanceof Path) {
                Path sourcePath = (Path) nativeShape;
                this.path.getElements().clear();
                this.path.getElements().addAll(sourcePath.getElements());
            }
        }
    }

    // Shadow property setters
    @Override
    public void setShadowBlur(double blur) {
        this.shadowBlur = Math.max(0, blur);
        applyShadowEffect();
    }

    @Override
    public void setShadowColor(String color) {
        this.shadowColor = color != null ? color : "rgba(0, 0, 0, 0)";
        applyShadowEffect();
    }

    @Override
    public void setShadowOffsetX(double offsetX) {
        this.shadowOffsetX = offsetX;
        applyShadowEffect();
    }

    @Override
    public void setShadowOffsetY(double offsetY) {
        this.shadowOffsetY = offsetY;
        applyShadowEffect();
    }

    // Image smoothing setters
    @Override
    public void setImageSmoothingEnabled(boolean enabled) {
        this.imageSmoothingEnabled = enabled;
        gc.setImageSmoothing(enabled);
    }

    @Override
    public void setImageSmoothingQuality(String quality) {
        this.imageSmoothingQuality = quality;
        // JavaFX doesn't have a direct quality setting, but we store it for consistency
    }

    private void applyShadowEffect() {
        boolean hasShadow = (shadowBlur > 0 || shadowOffsetX != 0 || shadowOffsetY != 0)
                          && shadowColor != null && !shadowColor.equals("rgba(0, 0, 0, 0)");

        if (hasShadow) {
            // Set shadow effect on JavaFX GraphicsContext
            javafx.scene.effect.DropShadow shadow = new javafx.scene.effect.DropShadow();
            shadow.setRadius(shadowBlur);
            shadow.setOffsetX(shadowOffsetX);
            shadow.setOffsetY(shadowOffsetY);

            // Parse color
            Color shadowCol = parseColor(shadowColor);
            shadow.setColor(shadowCol);

            gc.setEffect(shadow);
        } else {
            gc.setEffect(null);
        }
    }

    private Color parseColor(String color) {
        try {
            if (color.startsWith("rgba(")) {
                String[] parts = color.substring(5, color.length() - 1).split(",");
                double r = Double.parseDouble(parts[0].trim()) / 255.0;
                double g = Double.parseDouble(parts[1].trim()) / 255.0;
                double b = Double.parseDouble(parts[2].trim()) / 255.0;
                double a = Double.parseDouble(parts[3].trim());
                return new Color(r, g, b, a);
            } else if (color.startsWith("rgb(")) {
                String[] parts = color.substring(4, color.length() - 1).split(",");
                double r = Double.parseDouble(parts[0].trim()) / 255.0;
                double g = Double.parseDouble(parts[1].trim()) / 255.0;
                double b = Double.parseDouble(parts[2].trim()) / 255.0;
                return new Color(r, g, b, 1.0);
            } else if (color.startsWith("#")) {
                return Color.web(color);
            }
        } catch (Exception e) {
            // Fallback
        }
        return Color.BLACK;
    }

    // Filter methods
    @Override
    public void setFilter(String filter) {
        this.filter = (filter == null || filter.trim().isEmpty()) ? "none" : filter;
        applyFilterEffect();
    }

    @Override
    public String getFilter() {
        return this.filter;
    }

    /**
     * Apply CSS filters using JavaFX Effect classes
     */
    private void applyFilterEffect() {
        if (filter == null || "none".equals(filter)) {
            // Clear filter effects (but preserve shadow if active)
            if (shadowBlur > 0 || shadowOffsetX != 0 || shadowOffsetY != 0) {
                applyShadowEffect();
            } else {
                gc.setEffect(null);
            }
            return;
        }

        java.util.List<com.w3canvas.javacanvas.core.FilterFunction> filters =
            com.w3canvas.javacanvas.core.CSSFilterParser.parse(filter);

        if (filters.isEmpty()) {
            // No valid filters, just apply shadow if active
            applyShadowEffect();
            return;
        }

        // Chain multiple effects together
        javafx.scene.effect.Effect effect = null;

        for (com.w3canvas.javacanvas.core.FilterFunction filterFunc : filters) {
            javafx.scene.effect.Effect newEffect = createFilterEffect(filterFunc);
            if (newEffect != null) {
                if (effect == null) {
                    effect = newEffect;
                } else {
                    // Chain effects by setting the input
                    if (newEffect instanceof javafx.scene.effect.GaussianBlur) {
                        ((javafx.scene.effect.GaussianBlur) newEffect).setInput(effect);
                        effect = newEffect;
                    } else if (newEffect instanceof javafx.scene.effect.ColorAdjust) {
                        ((javafx.scene.effect.ColorAdjust) newEffect).setInput(effect);
                        effect = newEffect;
                    } else if (newEffect instanceof javafx.scene.effect.SepiaTone) {
                        ((javafx.scene.effect.SepiaTone) newEffect).setInput(effect);
                        effect = newEffect;
                    }
                }
            }
        }

        // Apply the chained effect or fallback to shadow
        if (effect != null) {
            // If shadow is active, chain it with the filter effects
            if (shadowBlur > 0 || shadowOffsetX != 0 || shadowOffsetY != 0) {
                javafx.scene.effect.DropShadow shadow = new javafx.scene.effect.DropShadow();
                shadow.setRadius(shadowBlur);
                shadow.setOffsetX(shadowOffsetX);
                shadow.setOffsetY(shadowOffsetY);
                Color shadowCol = parseColor(shadowColor);
                shadow.setColor(shadowCol);
                shadow.setInput(effect);
                gc.setEffect(shadow);
            } else {
                gc.setEffect(effect);
            }
        } else {
            applyShadowEffect();
        }
    }

    /**
     * Create a JavaFX Effect from a FilterFunction
     */
    private javafx.scene.effect.Effect createFilterEffect(com.w3canvas.javacanvas.core.FilterFunction filter) {
        switch (filter.getType()) {
            case BLUR:
                double radius = filter.getDoubleParam(0);
                if (radius > 0) {
                    javafx.scene.effect.GaussianBlur blur = new javafx.scene.effect.GaussianBlur();
                    blur.setRadius(Math.min(radius, 63)); // JavaFX has a max radius of 63
                    return blur;
                }
                break;

            case BRIGHTNESS:
            case CONTRAST:
            case SATURATE:
            case HUE_ROTATE:
                javafx.scene.effect.ColorAdjust colorAdjust = new javafx.scene.effect.ColorAdjust();

                if (filter.getType() == com.w3canvas.javacanvas.core.FilterFunction.FilterType.BRIGHTNESS) {
                    // JavaFX brightness: -1 to 1, where 0 is normal
                    // CSS brightness: 0 to infinity, where 1 is normal
                    double brightness = filter.getDoubleParam(0);
                    colorAdjust.setBrightness(brightness - 1.0);
                } else if (filter.getType() == com.w3canvas.javacanvas.core.FilterFunction.FilterType.CONTRAST) {
                    // JavaFX contrast: -1 to 1, where 0 is normal
                    // CSS contrast: 0 to infinity, where 1 is normal
                    double contrast = filter.getDoubleParam(0);
                    colorAdjust.setContrast(contrast - 1.0);
                } else if (filter.getType() == com.w3canvas.javacanvas.core.FilterFunction.FilterType.SATURATE) {
                    // JavaFX saturation: -1 to 1, where 0 is normal
                    // CSS saturate: 0 to infinity, where 1 is normal
                    double saturate = filter.getDoubleParam(0);
                    colorAdjust.setSaturation(saturate - 1.0);
                } else if (filter.getType() == com.w3canvas.javacanvas.core.FilterFunction.FilterType.HUE_ROTATE) {
                    double degrees = filter.getDoubleParam(0);
                    colorAdjust.setHue(degrees / 360.0);
                }

                return colorAdjust;

            case GRAYSCALE:
                double grayscaleAmount = filter.getDoubleParam(0);
                javafx.scene.effect.ColorAdjust grayscale = new javafx.scene.effect.ColorAdjust();
                grayscale.setSaturation(-grayscaleAmount); // -1 is full grayscale
                return grayscale;

            case SEPIA:
                double sepiaAmount = filter.getDoubleParam(0);
                if (sepiaAmount > 0) {
                    javafx.scene.effect.SepiaTone sepia = new javafx.scene.effect.SepiaTone();
                    sepia.setLevel(sepiaAmount);
                    return sepia;
                }
                break;

            case INVERT:
                // JavaFX doesn't have a direct invert effect
                // We can approximate with color adjustments, but it's not perfect
                double invertAmount = filter.getDoubleParam(0);
                if (invertAmount > 0) {
                    // This is an approximation - JavaFX doesn't have true invert
                    javafx.scene.effect.ColorAdjust invert = new javafx.scene.effect.ColorAdjust();
                    invert.setBrightness(-invertAmount);
                    invert.setHue(invertAmount > 0.5 ? 0.5 : 0);
                    return invert;
                }
                break;

            case OPACITY:
                // Opacity is typically handled via globalAlpha, not as a filter effect
                // But we can note it here for completeness
                break;

            case DROP_SHADOW:
                // drop-shadow is handled separately from the main shadow properties
                if (filter.getParamCount() >= 2) {
                    javafx.scene.effect.DropShadow dropShadow = new javafx.scene.effect.DropShadow();
                    dropShadow.setOffsetX(filter.getDoubleParam(0));
                    dropShadow.setOffsetY(filter.getDoubleParam(1));

                    if (filter.getParamCount() >= 3) {
                        dropShadow.setRadius(filter.getDoubleParam(2));
                    }

                    if (filter.getParamCount() >= 4) {
                        Color color = parseColor(filter.getStringParam(3));
                        dropShadow.setColor(color);
                    }

                    return dropShadow;
                }
                break;

            default:
                break;
        }

        return null;
    }
}
