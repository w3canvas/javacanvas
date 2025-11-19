package com.w3canvas.javacanvas.backend.javafx;

import com.w3canvas.javacanvas.interfaces.ICanvasGradient;
import com.w3canvas.javacanvas.interfaces.ICanvasPattern;
import com.w3canvas.javacanvas.interfaces.ICanvasSurface;
import com.w3canvas.javacanvas.interfaces.IFont;
import com.w3canvas.javacanvas.interfaces.IGraphicsBackend;

public class JavaFXGraphicsBackend implements IGraphicsBackend {

    /**
     * Maximum allowed font data size: 10MB (10485760 bytes).
     * This limit prevents potential memory exhaustion attacks from malicious font files.
     */
    private static final int MAX_FONT_SIZE = 10485760; // 10MB

    @Override
    public ICanvasSurface createCanvasSurface(int width, int height) {
        return new JavaFXCanvasSurface(width, height);
    }

    @Override
    public ICanvasGradient createLinearGradient(double x0, double y0, double x1, double y1) {
        return new JavaFXLinearGradient(x0, y0, x1, y1);
    }

    @Override
    public ICanvasGradient createRadialGradient(double x0, double y0, double r0, double x1, double y1, double r1) {
        return new JavaFXRadialGradient(x0, y0, r0, x1, y1, r1);
    }

    @Override
    public ICanvasGradient createConicGradient(double startAngle, double x, double y) {
        return new JavaFXConicGradient(startAngle, x, y);
    }

    @Override
    public ICanvasPattern createPattern(Object image, String repetition) {
        return new JavaFXPattern((javafx.scene.image.Image) image, repetition);
    }

    @Override
    public IFont createFont(String family, double size) {
        return new JavaFXFont(family, size);
    }

    @Override
    public IFont createFont(String family, double size, String style, String weight) {
        return new JavaFXFont(family, size, style, weight);
    }

    /**
     * Creates a font from binary font data (e.g., TrueType or OpenType font file).
     * <p>
     * This method validates the font data before attempting to load it:
     * <ul>
     *   <li>Ensures fontData is not null or empty</li>
     *   <li>Ensures fontData does not exceed {@value #MAX_FONT_SIZE} bytes (10MB)</li>
     * </ul>
     *
     * @param fontData the binary font data to load
     * @param size the font size in points
     * @param style the font style (e.g., "normal", "italic", "oblique")
     * @param weight the font weight (e.g., "normal", "bold")
     * @return a new IFont instance, or null if font creation fails
     * @throws IllegalArgumentException if fontData is null or empty
     * @throws IllegalArgumentException if fontData exceeds the maximum allowed size
     */
    @Override
    public IFont createFont(byte[] fontData, float size, String style, String weight) {
        // Validate font data is not null or empty
        if (fontData == null || fontData.length == 0) {
            throw new IllegalArgumentException("Font data cannot be null or empty");
        }

        // Validate font data size doesn't exceed maximum limit
        if (fontData.length > MAX_FONT_SIZE) {
            throw new IllegalArgumentException(
                String.format("Font data size (%d bytes) exceeds maximum allowed size (%d bytes / 10MB)",
                    fontData.length, MAX_FONT_SIZE)
            );
        }

        try {
            javafx.scene.text.Font fxFont = javafx.scene.text.Font.loadFont(new java.io.ByteArrayInputStream(fontData), size);
            return new JavaFXFont(fxFont, size, style, weight);
        } catch (Exception e) {
            System.err.println("ERROR: Failed to load JavaFX font: " + e.getMessage());
            return null;
        }
    }
}
