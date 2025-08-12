package com.w3canvas.javacanvas.backend.javafx;

import com.w3canvas.javacanvas.interfaces.*;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.transform.Affine;
import javafx.scene.image.Image;


public class JavaFXGraphicsContext implements IGraphicsContext {

    private final GraphicsContext gc;

    public JavaFXGraphicsContext(GraphicsContext gc) {
        this.gc = gc;
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
        if (paint instanceof JavaFXPaint) {
            gc.setFill(((JavaFXPaint) paint).getPaint());
        }
    }

    @Override
    public void setStrokePaint(IPaint paint) {
        if (paint instanceof JavaFXPaint) {
            gc.setStroke(((JavaFXPaint) paint).getPaint());
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
    public void clearRect(double x, double y, double w, double h) {
        gc.clearRect(x, y, w, h);
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
        // Not implemented for JavaFX backend
    }

    @Override
    public void drawString(String str, int x, int y) {
        gc.strokeText(str, x, y);
    }

    @Override
    public void setClip(IShape shape) {
        // To be implemented
    }

    @Override
    public void clip(IShape shape) {
        // To be implemented
    }

    @Override
    public void beginPath() {
        gc.beginPath();
    }

    @Override
    public void closePath() {
        gc.closePath();
    }

    @Override
    public void moveTo(double x, double y) {
        gc.moveTo(x, y);
    }

    @Override
    public void lineTo(double x, double y) {
        gc.lineTo(x, y);
    }

    @Override
    public void quadraticCurveTo(double cpx, double cpy, double x, double y) {
        gc.quadraticCurveTo(cpx, cpy, x, y);
    }

    @Override
    public void bezierCurveTo(double cp1x, double cp1y, double cp2x, double cp2y, double x, double y) {
        gc.bezierCurveTo(cp1x, cp1y, cp2x, cp2y, x, y);
    }

    @Override
    public void arcTo(double x1, double y1, double x2, double y2, double radius) {
        gc.arcTo(x1, y1, x2, y2, radius);
    }

    @Override
    public void rect(double x, double y, double w, double h) {
        gc.rect(x, y, w, h);
    }

    @Override
    public void arc(double x, double y, double radius, double startAngle, double endAngle, boolean counterclockwise) {
        // JavaFX arc angles are in degrees, and it's extent-based, not end-angle based.
        double length = endAngle - startAngle;
        if(counterclockwise) {
            if(length < 0) length += 2 * Math.PI;
            length = -(length);
        }
        gc.arc(x, y, radius, radius, Math.toDegrees(startAngle), Math.toDegrees(length));
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
        gc.arc(0, 0, radiusX, radiusY, Math.toDegrees(startAngle), Math.toDegrees(length));
        gc.restore();
    }

    @Override
    public IShape getPath() {
        // To be implemented
        return null;
    }
}
