package com.w3canvas.javacanvas.backend.javafx;

import com.w3canvas.javacanvas.interfaces.ICanvasPattern;
import com.w3canvas.javacanvas.interfaces.IPaint;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.paint.Paint;

public class JavaFXPattern implements ICanvasPattern, IPaint {

    private final Image image;
    private final String repetition;

    public JavaFXPattern(Image image, String repetition) {
        this.image = image;
        this.repetition = repetition;
    }

    public Image getImage() {
        return image;
    }

    public String getRepetition() {
        return repetition;
    }

    public Object getPaint() {
        // This will be used for stroke for now.
        return new ImagePattern(image, 0, 0, image.getWidth(), image.getHeight(), false);
    }
}
