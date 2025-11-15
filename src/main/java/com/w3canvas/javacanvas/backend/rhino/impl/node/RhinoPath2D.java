package com.w3canvas.javacanvas.backend.rhino.impl.node;

import com.w3canvas.javacanvas.core.Path2D;
import com.w3canvas.javacanvas.interfaces.IPath2D;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

/**
 * Rhino JavaScript binding for Path2D.
 * Provides JavaScript access to Path2D functionality.
 */
@SuppressWarnings("serial")
public class RhinoPath2D extends ProjectScriptableObject implements IPath2D {

    private Path2D corePath;

    /**
     * Default constructor for Rhino.
     */
    public RhinoPath2D() {
        this.corePath = new Path2D();
    }

    /**
     * JavaScript constructor.
     * Supports: new Path2D() and new Path2D(path)
     */
    public void jsConstructor(Object arg) {
        if (arg == null || arg == Context.getUndefinedValue()) {
            // Empty constructor: new Path2D()
            this.corePath = new Path2D();
        } else if (arg instanceof RhinoPath2D) {
            // Copy constructor: new Path2D(path)
            this.corePath = new Path2D(((RhinoPath2D) arg).corePath);
        } else if (arg instanceof IPath2D) {
            // Copy from any IPath2D implementation
            this.corePath = new Path2D((IPath2D) arg);
        } else {
            // Default to empty if unknown argument
            this.corePath = new Path2D();
        }
    }

    @Override
    public String getClassName() {
        return "Path2D";
    }

    // Path2D methods exposed to JavaScript

    public void jsFunction_moveTo(double x, double y) {
        corePath.moveTo(x, y);
    }

    public void jsFunction_lineTo(double x, double y) {
        corePath.lineTo(x, y);
    }

    public void jsFunction_quadraticCurveTo(double cpx, double cpy, double x, double y) {
        corePath.quadraticCurveTo(cpx, cpy, x, y);
    }

    public void jsFunction_bezierCurveTo(double cp1x, double cp1y, double cp2x, double cp2y, double x, double y) {
        corePath.bezierCurveTo(cp1x, cp1y, cp2x, cp2y, x, y);
    }

    public void jsFunction_arcTo(double x1, double y1, double x2, double y2, double radius) {
        corePath.arcTo(x1, y1, x2, y2, radius);
    }

    public void jsFunction_rect(double x, double y, double w, double h) {
        corePath.rect(x, y, w, h);
    }

    public void jsFunction_arc(double x, double y, double radius, double startAngle, double endAngle, Object counterclockwise) {
        boolean ccw = false;
        if (counterclockwise != null && counterclockwise != Context.getUndefinedValue()) {
            ccw = Context.toBoolean(counterclockwise);
        }
        corePath.arc(x, y, radius, startAngle, endAngle, ccw);
    }

    public void jsFunction_ellipse(double x, double y, double radiusX, double radiusY, double rotation,
                                   double startAngle, double endAngle, Object counterclockwise) {
        boolean ccw = false;
        if (counterclockwise != null && counterclockwise != Context.getUndefinedValue()) {
            ccw = Context.toBoolean(counterclockwise);
        }
        corePath.ellipse(x, y, radiusX, radiusY, rotation, startAngle, endAngle, ccw);
    }

    public void jsFunction_closePath() {
        corePath.closePath();
    }

    public void jsFunction_addPath(Object path) {
        if (path instanceof RhinoPath2D) {
            corePath.addPath(((RhinoPath2D) path).corePath);
        } else if (path instanceof IPath2D) {
            corePath.addPath((IPath2D) path);
        }
    }

    // IPath2D interface implementation (delegates to corePath)

    @Override
    public void moveTo(double x, double y) {
        corePath.moveTo(x, y);
    }

    @Override
    public void lineTo(double x, double y) {
        corePath.lineTo(x, y);
    }

    @Override
    public void quadraticCurveTo(double cpx, double cpy, double x, double y) {
        corePath.quadraticCurveTo(cpx, cpy, x, y);
    }

    @Override
    public void bezierCurveTo(double cp1x, double cp1y, double cp2x, double cp2y, double x, double y) {
        corePath.bezierCurveTo(cp1x, cp1y, cp2x, cp2y, x, y);
    }

    @Override
    public void arcTo(double x1, double y1, double x2, double y2, double radius) {
        corePath.arcTo(x1, y1, x2, y2, radius);
    }

    @Override
    public void rect(double x, double y, double w, double h) {
        corePath.rect(x, y, w, h);
    }

    @Override
    public void arc(double x, double y, double radius, double startAngle, double endAngle, boolean counterclockwise) {
        corePath.arc(x, y, radius, startAngle, endAngle, counterclockwise);
    }

    @Override
    public void ellipse(double x, double y, double radiusX, double radiusY, double rotation,
                       double startAngle, double endAngle, boolean counterclockwise) {
        corePath.ellipse(x, y, radiusX, radiusY, rotation, startAngle, endAngle, counterclockwise);
    }

    @Override
    public void closePath() {
        corePath.closePath();
    }

    @Override
    public void addPath(IPath2D path) {
        corePath.addPath(path);
    }

    @Override
    public java.util.List<PathElement> getElements() {
        return corePath.getElements();
    }

    /**
     * Get the core Path2D instance for internal use.
     */
    public Path2D getCorePath() {
        return corePath;
    }
}
