package com.w3canvas.javacanvas.backend.javafx;

import javafx.geometry.Point2D;

public class Math2D {
    public static Point2D subtract(Point2D p1, Point2D p2) {
        return p1.subtract(p2);
    }

    public static double dot(Point2D p1, Point2D p2) {
        return p1.dotProduct(p2);
    }

    public static Point2D linePointAt(Point2D p, double t, Point2D dir) {
        return p.add(dir.multiply(t));
    }
}
