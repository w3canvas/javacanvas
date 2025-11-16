package com.w3canvas.javacanvas.backend.javafx;

import com.w3canvas.javacanvas.backend.ConicGradientHelper;
import com.w3canvas.javacanvas.core.ColorParser;
import com.w3canvas.javacanvas.interfaces.ICanvasGradient;
import com.w3canvas.javacanvas.interfaces.IPaint;

import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * JavaFX implementation of conic gradients using a pixel-based WritableImage.
 * Creates an image with colors computed for each pixel based on angle from center,
 * with colors interpolated between color stops.
 */
public class JavaFXConicGradient implements ICanvasGradient, IPaint {
    private final double startAngle;
    private final double centerX;
    private final double centerY;
    private final List<ConicGradientHelper.ColorStop> stops = new ArrayList<>();

    // Default pattern size (will be adjusted as needed)
    private static final int DEFAULT_SIZE = 512;

    public JavaFXConicGradient(double startAngle, double x, double y) {
        this.startAngle = startAngle;
        this.centerX = x;
        this.centerY = y;
    }

    @Override
    public void addColorStop(double offset, String colorStr) {
        JavaFXPaint paint = (JavaFXPaint) ColorParser.parse(colorStr, new JavaFXGraphicsBackend());
        Color color = (Color) paint.getPaint();

        stops.add(new ConicGradientHelper.ColorStop(
                offset,
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255),
                (int) (color.getOpacity() * 255)
        ));

        // Keep stops sorted by offset
        Collections.sort(stops);
    }

    @Override
    public Object getPaint() {
        if (stops.isEmpty()) {
            return Color.TRANSPARENT;
        }

        // Create a WritableImage with the gradient using a pixel-based approach
        // This ensures proper positioning at (centerX, centerY)
        WritableImage gradientImage = createGradientImagePixelBased(DEFAULT_SIZE * 2);

        // Return it as an ImagePattern positioned at the origin
        return new ImagePattern(gradientImage, 0, 0, DEFAULT_SIZE * 2, DEFAULT_SIZE * 2, false);
    }

    /**
     * Creates a WritableImage containing the conic gradient using a pixel-by-pixel approach.
     * This allows for precise positioning of the gradient center.
     */
    private WritableImage createGradientImagePixelBased(int size) {
        WritableImage image = new WritableImage(size, size);
        javafx.scene.image.PixelWriter pw = image.getPixelWriter();

        // The gradient center in the image coordinate system
        // We offset by centerX and centerY to position the gradient correctly
        double imageCenterX = size / 2.0;
        double imageCenterY = size / 2.0;

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                // Calculate the actual canvas coordinates
                double canvasX = x - imageCenterX + centerX;
                double canvasY = y - imageCenterY + centerY;

                // Calculate angle from the gradient center
                double dx = canvasX - centerX;
                double dy = canvasY - centerY;
                double angle = Math.atan2(dy, dx);

                // Interpolate color for this angle
                int[] rgba = ConicGradientHelper.interpolateColor(angle, startAngle, stops);
                Color color = Color.rgb(rgba[0], rgba[1], rgba[2], rgba[3] / 255.0);

                pw.setColor(x, y, color);
            }
        }

        return image;
    }
}
