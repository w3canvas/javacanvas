package com.w3canvas.javacanvas.core;

import com.w3canvas.javacanvas.backend.awt.AwtGraphicsBackend;
import com.w3canvas.javacanvas.backend.awt.AwtPaint;
import com.w3canvas.javacanvas.backend.javafx.JavaFXGraphicsBackend;
import com.w3canvas.javacanvas.backend.javafx.JavaFXPaint;
import com.w3canvas.javacanvas.interfaces.IGraphicsBackend;
import com.w3canvas.javacanvas.interfaces.IPaint;

public class ColorParser {
    public static IPaint parse(String colorString, IGraphicsBackend backend) {
        if (backend instanceof AwtGraphicsBackend) {
            javafx.scene.paint.Color fxColor;
            try {
                fxColor = javafx.scene.paint.Color.web(colorString);
            } catch (Exception e) {
                fxColor = javafx.scene.paint.Color.BLACK;
            }
            java.awt.Color awtColor = new java.awt.Color((float) fxColor.getRed(),
                                                         (float) fxColor.getGreen(),
                                                         (float) fxColor.getBlue(),
                                                         (float) fxColor.getOpacity());
            return new AwtPaint(awtColor);
        } else if (backend instanceof JavaFXGraphicsBackend) {
            javafx.scene.paint.Color color;
            try {
                // Use Color.web() to parse CSS color strings, which supports rgba() and named colors correctly.
                color = javafx.scene.paint.Color.web(colorString);
            } catch (Exception e) {
                // Fallback to black if parsing fails
                color = javafx.scene.paint.Color.BLACK;
            }
            return new JavaFXPaint(color);
        }
        return null;
    }
}
