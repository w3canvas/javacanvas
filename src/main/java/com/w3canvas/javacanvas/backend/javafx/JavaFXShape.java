package com.w3canvas.javacanvas.backend.javafx;

import com.w3canvas.javacanvas.interfaces.IShape;

public class JavaFXShape implements IShape {
    private final Object shape;

    public JavaFXShape(Object shape) {
        this.shape = shape;
    }

    public Object getShape() {
        return shape;
    }
}
