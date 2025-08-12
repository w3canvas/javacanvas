package com.w3canvas.javacanvas.backend.javafx;

import com.w3canvas.javacanvas.interfaces.ICanvasPattern;
import com.w3canvas.javacanvas.interfaces.IPaint;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;

public class JavaFXPattern implements ICanvasPattern, IPaint {

    private final ImagePattern paint;

    public JavaFXPattern(Image image, String repetition) {
        // The repetition parameter is not fully supported in this implementation.
        // It's a complex feature to implement correctly.
        this.paint = new ImagePattern(image, 0, 0, image.getWidth(), image.getHeight(), false);
    }

    public Object getPaint() {
        return paint;
    }
}
