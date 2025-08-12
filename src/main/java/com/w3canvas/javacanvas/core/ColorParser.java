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
            java.awt.Color color;
            if (colorString.startsWith("#")) {
                color = java.awt.Color.decode(colorString);
            } else {
                try {
                    color = (java.awt.Color) java.awt.Color.class.getField(colorString.toLowerCase()).get(null);
                } catch (Exception e) {
                    color = java.awt.Color.BLACK;
                }
            }
            return new AwtPaint(color);
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
