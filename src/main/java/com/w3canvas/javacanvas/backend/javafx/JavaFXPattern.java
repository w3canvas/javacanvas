package com.w3canvas.javacanvas.backend.javafx;

import com.w3canvas.javacanvas.interfaces.ICanvasPattern;
import com.w3canvas.javacanvas.interfaces.IPaint;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.ImagePattern;
import javafx.scene.paint.Paint;
import javafx.scene.transform.Affine;

public class JavaFXPattern implements ICanvasPattern, IPaint {

    private final Image image;
    private final String repetition;
    private final Paint paint;
    private Affine transform = new Affine();

    // Cache for generated paint to reduce memory allocation
    private Paint cachedBoundsPaint = null;
    private double cachedBoundsWidth = -1;
    private double cachedBoundsHeight = -1;

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

        // Check if we can use cached paint
        if (isCacheValid(boundsWidth, boundsHeight)) {
            return cachedBoundsPaint;
        }

        // Generate new paint
        Paint generatedPaint = generatePaint(boundsWidth, boundsHeight);

        // Cache the paint
        cachePaint(boundsWidth, boundsHeight, generatedPaint);

        return generatedPaint;
    }

    private boolean isCacheValid(double boundsWidth, double boundsHeight) {
        return cachedBoundsPaint != null
            && cachedBoundsWidth == boundsWidth
            && cachedBoundsHeight == boundsHeight;
    }

    private void cachePaint(double boundsWidth, double boundsHeight, Paint paint) {
        cachedBoundsWidth = boundsWidth;
        cachedBoundsHeight = boundsHeight;
        cachedBoundsPaint = paint;
    }

    @Override
    public void setTransform(Object transform) {
        if (transform instanceof java.awt.geom.AffineTransform) {
            java.awt.geom.AffineTransform at = (java.awt.geom.AffineTransform) transform;
            this.transform = new Affine(
                at.getScaleX(), at.getShearX(), at.getTranslateX(),
                at.getShearY(), at.getScaleY(), at.getTranslateY()
            );
            // Invalidate cache
            cachedBoundsPaint = null;
        }
    }

    private Paint generatePaint(double boundsWidth, double boundsHeight) {
        double imgWidth = image.getWidth();
        double imgHeight = image.getHeight();

        Canvas tempCanvas = new Canvas(boundsWidth, boundsHeight);
        GraphicsContext gc = tempCanvas.getGraphicsContext2D();
        gc.setTransform(transform);

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

        // Use SnapshotParameters for consistent rendering
        javafx.scene.SnapshotParameters params = new javafx.scene.SnapshotParameters();
        params.setFill(javafx.scene.paint.Color.TRANSPARENT);
        tempCanvas.snapshot(params, snapshot);

        return new ImagePattern(snapshot, 0, 0, boundsWidth, boundsHeight, false);
    }
}
