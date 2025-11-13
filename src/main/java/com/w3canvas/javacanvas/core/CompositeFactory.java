package com.w3canvas.javacanvas.core;

import com.w3canvas.javacanvas.interfaces.IComposite;
import com.w3canvas.javacanvas.interfaces.IGraphicsBackend;
import com.w3canvas.javacanvas.backend.awt.AwtComposite;
import com.w3canvas.javacanvas.backend.awt.AwtGraphicsBackend;
import com.w3canvas.javacanvas.backend.javafx.JavaFXComposite;
import com.w3canvas.javacanvas.backend.javafx.JavaFXGraphicsBackend;
import java.awt.AlphaComposite;
import javafx.scene.effect.BlendMode;

/**
 * Factory for creating compositing operations according to Canvas 2D API spec.
 * Supports both Porter-Duff composite operations and CSS blend modes.
 */
public class CompositeFactory {

    public static IComposite createComposite(String operation, double alpha, IGraphicsBackend backend) {
        if (backend instanceof AwtGraphicsBackend) {
            return createAwtComposite(operation, alpha);
        } else if (backend instanceof JavaFXGraphicsBackend) {
            return createJavaFXComposite(operation, alpha);
        }
        return null;
    }

    /**
     * Create AWT composite for Porter-Duff operations.
     * Note: CSS blend modes are not directly supported in AWT and would require
     * custom Composite implementations for pixel-level blending.
     */
    private static AwtComposite createAwtComposite(String operation, double alpha) {
        if (operation == null || operation.isEmpty()) {
            operation = "source-over";
        }

        int rule;
        boolean useAlpha = true;

        // Porter-Duff composite operations
        switch (operation.toLowerCase()) {
            case "source-over":
                rule = AlphaComposite.SRC_OVER;
                break;
            case "source-in":
                rule = AlphaComposite.SRC_IN;
                break;
            case "source-out":
                rule = AlphaComposite.SRC_OUT;
                break;
            case "source-atop":
                rule = AlphaComposite.SRC_ATOP;
                break;
            case "destination-over":
                rule = AlphaComposite.DST_OVER;
                break;
            case "destination-in":
                rule = AlphaComposite.DST_IN;
                break;
            case "destination-out":
                rule = AlphaComposite.DST_OUT;
                break;
            case "destination-atop":
                rule = AlphaComposite.DST_ATOP;
                break;
            case "lighter":
                // "lighter" is like additive blending - closest AWT equivalent
                rule = AlphaComposite.SRC_OVER;
                // TODO: Implement custom composite for true additive blending
                break;
            case "copy":
                rule = AlphaComposite.SRC;
                useAlpha = false; // Copy ignores alpha
                break;
            case "xor":
                rule = AlphaComposite.XOR;
                break;

            // CSS blend modes - AWT doesn't support these natively
            // These would require custom Composite implementations
            case "multiply":
            case "screen":
            case "overlay":
            case "darken":
            case "lighten":
            case "color-dodge":
            case "color-burn":
            case "hard-light":
            case "soft-light":
            case "difference":
            case "exclusion":
            case "hue":
            case "saturation":
            case "color":
            case "luminosity":
                // Fall back to SRC_OVER for unsupported blend modes
                // TODO: Implement custom Composite for CSS blend modes
                rule = AlphaComposite.SRC_OVER;
                break;

            default:
                // Unknown operation, default to source-over
                rule = AlphaComposite.SRC_OVER;
                break;
        }

        if (useAlpha) {
            return new AwtComposite(AlphaComposite.getInstance(rule, (float) alpha));
        } else {
            return new AwtComposite(AlphaComposite.getInstance(rule));
        }
    }

    /**
     * Create JavaFX composite. JavaFX has excellent built-in support for
     * both Porter-Duff operations and CSS blend modes.
     */
    private static JavaFXComposite createJavaFXComposite(String operation, double alpha) {
        if (operation == null || operation.isEmpty()) {
            operation = "source-over";
        }

        BlendMode mode;

        switch (operation.toLowerCase()) {
            // Porter-Duff composite operations
            case "source-over":
                mode = BlendMode.SRC_OVER;
                break;
            case "source-in":
                // JavaFX doesn't have exact SRC_IN, use SRC_ATOP as approximation
                mode = BlendMode.SRC_ATOP;
                break;
            case "source-out":
                // Not directly supported, fallback to SRC_OVER
                mode = BlendMode.SRC_OVER;
                break;
            case "source-atop":
                mode = BlendMode.SRC_ATOP;
                break;
            case "destination-over":
                // Not directly supported, fallback to SRC_OVER
                mode = BlendMode.SRC_OVER;
                break;
            case "destination-in":
                // Not directly supported, fallback to SRC_OVER
                mode = BlendMode.SRC_OVER;
                break;
            case "destination-out":
                // Not directly supported, fallback to SRC_OVER
                mode = BlendMode.SRC_OVER;
                break;
            case "destination-atop":
                // Not directly supported, fallback to SRC_OVER
                mode = BlendMode.SRC_OVER;
                break;
            case "lighter":
                mode = BlendMode.ADD;
                break;
            case "copy":
                // JavaFX doesn't have a direct copy mode, SRC_OVER is closest
                mode = BlendMode.SRC_OVER;
                break;
            case "xor":
                // Not directly supported, fallback to SRC_OVER
                mode = BlendMode.SRC_OVER;
                break;

            // CSS blend modes - JavaFX has excellent support
            case "multiply":
                mode = BlendMode.MULTIPLY;
                break;
            case "screen":
                mode = BlendMode.SCREEN;
                break;
            case "overlay":
                mode = BlendMode.OVERLAY;
                break;
            case "darken":
                mode = BlendMode.DARKEN;
                break;
            case "lighten":
                mode = BlendMode.LIGHTEN;
                break;
            case "color-dodge":
                mode = BlendMode.COLOR_DODGE;
                break;
            case "color-burn":
                mode = BlendMode.COLOR_BURN;
                break;
            case "hard-light":
                mode = BlendMode.HARD_LIGHT;
                break;
            case "soft-light":
                mode = BlendMode.SOFT_LIGHT;
                break;
            case "difference":
                mode = BlendMode.DIFFERENCE;
                break;
            case "exclusion":
                mode = BlendMode.EXCLUSION;
                break;
            case "hue":
                // Not directly supported in JavaFX
                mode = BlendMode.SRC_OVER;
                break;
            case "saturation":
                // Not directly supported in JavaFX
                mode = BlendMode.SRC_OVER;
                break;
            case "color":
                // Not directly supported in JavaFX
                mode = BlendMode.SRC_OVER;
                break;
            case "luminosity":
                // Not directly supported in JavaFX
                mode = BlendMode.SRC_OVER;
                break;

            default:
                // Unknown operation, default to source-over
                mode = BlendMode.SRC_OVER;
                break;
        }

        return new JavaFXComposite(mode);
    }
}
