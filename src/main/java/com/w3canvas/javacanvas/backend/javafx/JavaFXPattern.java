package com.w3canvas.javacanvas.backend.javafx;

import com.w3canvas.javacanvas.interfaces.ICanvasPattern;
import com.w3canvas.javacanvas.interfaces.IPaint;
import com.w3canvas.javacanvas.interfaces.ICanvasPattern;
import com.w3canvas.javacanvas.interfaces.IPaint;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.ImagePattern;
import javafx.scene.paint.Paint;

public class JavaFXPattern implements ICanvasPattern, IPaint {

    private final Image image;
    private final String repetition;
    private final Paint paint;

    public JavaFXPattern(Image image, String repetition) {
        this.image = image;
        this.repetition = repetition;
        this.paint = createPaint();
    }

    private Paint createPaint() {
        if (repetition == null || "repeat".equals(repetition)) {
            return new ImagePattern(image, 0, 0, image.getWidth(), image.getHeight(), false);
        }
        // For other repetition types, we need to generate a custom image.
        // This is complex and requires knowing the dimensions of the area to be filled.
        // We will need to adjust the drawing logic in JavaFXGraphicsContext.
        return null; // Indicates special handling is needed.
    }

    public Image getImage() {
        return image;
    }

    public String getRepetition() {
        return repetition;
    }

    public Object getPaint() {
        return paint;
    }

    public Paint getPaint(double boundsWidth, double boundsHeight) {
        if (paint != null) {
            return paint;
        }

        double imgWidth = image.getWidth();
        double imgHeight = image.getHeight();

        Canvas tempCanvas = new Canvas(boundsWidth, boundsHeight);
        GraphicsContext gc = tempCanvas.getGraphicsContext2D();

        switch (repetition) {
            case "repeat":
                for (double y = 0; y < boundsHeight; y += imgHeight) {
                    for (double x = 0; x < boundsWidth; x += imgWidth) {
                        gc.drawImage(image, x, y);
                    }
                }
                break;
            case "repeat-x":
                for (double x = 0; x < boundsWidth; x += imgWidth) {
                    gc.drawImage(image, x, 0);
                }
                break;
            case "repeat-y":
                for (double y = 0; y < boundsHeight; y += imgHeight) {
                    gc.drawImage(image, 0, y);
                }
                break;
            case "no-repeat":
                gc.drawImage(image, 0, 0);
                break;
        }

        WritableImage snapshot = new WritableImage((int) boundsWidth, (int) boundsHeight);
        tempCanvas.snapshot(null, snapshot);
        return new ImagePattern(snapshot, 0, 0, boundsWidth, boundsHeight, false);
    }
}
