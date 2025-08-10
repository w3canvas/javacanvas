package com.w3canvas.javacanvas.backend.awt;

import com.w3canvas.javacanvas.interfaces.IShape;
import java.awt.Shape;

public class AwtShape implements IShape {
    private final Shape shape;

    public AwtShape(Shape shape) {
        this.shape = shape;
    }

    public Shape getShape() {
        return shape;
    }
}
