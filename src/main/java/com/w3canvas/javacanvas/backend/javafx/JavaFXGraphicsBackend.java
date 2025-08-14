package com.w3canvas.javacanvas.backend.javafx;

import com.w3canvas.javacanvas.interfaces.ICanvasGradient;
import com.w3canvas.javacanvas.interfaces.ICanvasPattern;
import com.w3canvas.javacanvas.interfaces.ICanvasSurface;
import com.w3canvas.javacanvas.interfaces.IFont;
import com.w3canvas.javacanvas.interfaces.IGraphicsBackend;

public class JavaFXGraphicsBackend implements IGraphicsBackend {

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

    @Override
    public IFont createFont(byte[] fontData, float size, String style, String weight) {
        try {
            javafx.scene.text.Font fxFont = javafx.scene.text.Font.loadFont(new java.io.ByteArrayInputStream(fontData), size);
            return new JavaFXFont(fxFont, size, style, weight);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
