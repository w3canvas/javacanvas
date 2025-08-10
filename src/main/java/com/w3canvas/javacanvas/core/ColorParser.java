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
                    color = (java.awt.Color) java.awt.Color.class.getField(colorString.toUpperCase()).get(null);
                } catch (Exception e) {
                    color = java.awt.Color.BLACK;
                }
            }
            return new AwtPaint(color);
        } else if (backend instanceof JavaFXGraphicsBackend) {
            javafx.scene.paint.Color color;
            try {
                color = javafx.scene.paint.Color.valueOf(colorString.toUpperCase());
            } catch (Exception e) {
                color = javafx.scene.paint.Color.BLACK;
            }
            return new JavaFXPaint(color);
        }
        return null;
    }
}
