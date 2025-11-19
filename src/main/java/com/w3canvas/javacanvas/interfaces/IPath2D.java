package com.w3canvas.javacanvas.interfaces;

import java.util.List;

/**
 * Interface for Path2D objects that can store and replay path commands.
 */
public interface IPath2D {

    /**
     * Move to a specific point without drawing.
     */
    void moveTo(double x, double y);

    /**
     * Draw a line to a specific point.
     */
    void lineTo(double x, double y);

    /**
     * Draw a quadratic bezier curve.
     */
    void quadraticCurveTo(double cpx, double cpy, double x, double y);

    /**
     * Draw a cubic bezier curve.
     */
    void bezierCurveTo(double cp1x, double cp1y, double cp2x, double cp2y, double x, double y);

    /**
     * Draw an arc between two points with a given radius.
     */
    void arcTo(double x1, double y1, double x2, double y2, double radius);

    /**
     * Add a rectangle to the path.
     */
    void rect(double x, double y, double w, double h);

    /**
     * Add a rounded rectangle to the path.
     * The radii parameter can be a single number, an array of numbers,
     * or an array of DOMPointInit-like objects.
     */
    void roundRect(double x, double y, double w, double h, Object radii);

    /**
     * Draw an arc.
     */
    void arc(double x, double y, double radius, double startAngle, double endAngle, boolean counterclockwise);

    /**
     * Draw an ellipse.
     */
    void ellipse(double x, double y, double radiusX, double radiusY, double rotation,
                 double startAngle, double endAngle, boolean counterclockwise);

    /**
     * Close the current path by drawing a line to the starting point.
     */
    void closePath();

    /**
     * Add another path to this path.
     */
    void addPath(IPath2D path);

    /**
     * Get the list of path elements for replaying.
     */
    List<PathElement> getElements();

    /**
     * Represents a single path command/element.
     */
    public static class PathElement {
        public enum Type {
            MOVE_TO, LINE_TO, QUADRATIC_CURVE_TO, BEZIER_CURVE_TO,
            ARC_TO, RECT, ROUND_RECT, ARC, ELLIPSE, CLOSE_PATH
        }

        private final Type type;
        private final double[] params;
        private final Object extra; // For storing additional data like radii in roundRect

        public PathElement(Type type, double... params) {
            this.type = type;
            this.params = params;
            this.extra = null;
        }

        public PathElement(Type type, Object extra, double... params) {
            this.type = type;
            this.params = params;
            this.extra = extra;
        }

        public Type getType() {
            return type;
        }

        public double[] getParams() {
            return params;
        }

        public Object getExtra() {
            return extra;
        }
    }
}
