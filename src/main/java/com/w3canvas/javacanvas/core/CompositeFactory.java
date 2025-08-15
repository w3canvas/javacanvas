package com.w3canvas.javacanvas.core;

import com.w3canvas.javacanvas.interfaces.IComposite;
import com.w3canvas.javacanvas.interfaces.IGraphicsBackend;
import com.w3canvas.javacanvas.backend.awt.AwtComposite;
import com.w3canvas.javacanvas.backend.awt.AwtGraphicsBackend;
import com.w3canvas.javacanvas.backend.javafx.JavaFXComposite;
import com.w3canvas.javacanvas.backend.javafx.JavaFXGraphicsBackend;
import java.awt.AlphaComposite;
import javafx.scene.effect.BlendMode;

public class CompositeFactory {

    public static IComposite createComposite(String operation, double alpha, IGraphicsBackend backend) {
        if (backend instanceof AwtGraphicsBackend) {
            return createAwtComposite(operation, alpha);
        } else if (backend instanceof JavaFXGraphicsBackend) {
            return createJavaFXComposite(operation, alpha);
        }
        return null;
    }

    private static AwtComposite createAwtComposite(String operation, double alpha) {
        if ("copy".equalsIgnoreCase(operation)) {
            return new AwtComposite(AlphaComposite.Src);
        }
        int rule = AlphaComposite.SRC_OVER;
        // Add other composite types here
        return new AwtComposite(AlphaComposite.getInstance(rule, (float) alpha));
    }

    private static JavaFXComposite createJavaFXComposite(String operation, double alpha) {
        // JavaFX blend mode does not have a separate alpha. It's handled by the color.
        // We will ignore the alpha here and assume it's part of the paint.
        BlendMode mode = BlendMode.SRC_OVER;
        if ("copy".equalsIgnoreCase(operation)) {
            // There is no direct "copy" in JavaFX BlendMode. Using SRC_OVER as a placeholder.
            mode = BlendMode.SRC_OVER;
        }
        return new JavaFXComposite(mode);
    }
}
