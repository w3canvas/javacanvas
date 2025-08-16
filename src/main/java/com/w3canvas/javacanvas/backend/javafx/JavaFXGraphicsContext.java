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
        Text t = new Text(text);
        t.setFont(gc.getFont());
        return new com.w3canvas.javacanvas.core.TextMetrics(t.getLayoutBounds().getWidth());
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
        System.out.println("JavaFXGraphicsContext.setFillPaint: " + paint);
        this.fillPaint = paint;
        if (paint instanceof JavaFXPaint) {
            gc.setFill(((JavaFXPaint) paint).getPaint());
        } else if (paint instanceof JavaFXLinearGradient) {
            gc.setFill((Paint) ((JavaFXLinearGradient) paint).getPaint());
        } else if (paint instanceof JavaFXRadialGradient) {
            gc.setFill((Paint) ((JavaFXRadialGradient) paint).getPaint());
        } else if (paint instanceof JavaFXPattern) {
            gc.setFill((Paint) ((JavaFXPattern) paint).getPaint());
        }
    }

    @Override
    public void setStrokePaint(IPaint paint) {
        System.out.println("JavaFXGraphicsContext.setStrokePaint: " + paint);
        this.strokePaint = paint;
        if (paint instanceof JavaFXPaint) {
            gc.setStroke(((JavaFXPaint) paint).getPaint());
        } else if (paint instanceof JavaFXLinearGradient) {
            gc.setStroke((Paint) ((JavaFXLinearGradient) paint).getPaint());
        } else if (paint instanceof JavaFXRadialGradient) {
            gc.setStroke((Paint) ((JavaFXRadialGradient) paint).getPaint());
        } else if (paint instanceof JavaFXPattern) {
            gc.setStroke((Paint) ((JavaFXPattern) paint).getPaint());
        }
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
        boolean sweepFlag = (dx01 * dy12 - dy01 * dx12) < 0;
        ArcTo arcTo = new ArcTo(radius, radius, 0, t2x, t2y, false, sweepFlag);
        path.getElements().add(arcTo);

        lastPoint[0] = t2x;
        lastPoint[1] = t2y;
    }

    @Override
    public void rect(double x, double y, double w, double h) {
        gc.rect(x, y, w, h);
        path.getElements().add(new javafx.scene.shape.MoveTo(x, y));
        path.getElements().add(new javafx.scene.shape.LineTo(x + w, y));
        path.getElements().add(new javafx.scene.shape.LineTo(x + w, y + h));
        path.getElements().add(new javafx.scene.shape.LineTo(x, y + h));
        path.getElements().add(new javafx.scene.shape.ClosePath());
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
        gc.fill();
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
                double extent = Math.toDegrees(angle(1, 0, vx, vy));

                if (!sweepFlag && extent > 0) {
                    extent -= 360;
                } else if (sweepFlag && extent < 0) {
                    extent += 360;
                }

                awtPath.append(new java.awt.geom.Arc2D.Double(cx - radiusX, cy - radiusY, 2 * radiusX, 2 * radiusY, -startAngle, -extent, java.awt.geom.Arc2D.OPEN), true);

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
}
