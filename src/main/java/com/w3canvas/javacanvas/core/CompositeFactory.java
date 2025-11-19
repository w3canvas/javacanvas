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
 *
 * <p><strong>Backend-Specific Blend Mode Support:</strong>
 *
 * <p><strong>AWT Backend:</strong>
 * <ul>
 *   <li><strong>Fully Supported Porter-Duff Operations:</strong> source-over, source-in,
 *       source-out, source-atop, destination-over, destination-in, destination-out,
 *       destination-atop, copy, xor</li>
 *   <li><strong>Approximated:</strong>
 *     <ul>
 *       <li>"lighter" - Mapped to SRC_OVER instead of true additive blending.
 *           A proper implementation would require custom Composite for pixel-level addition.</li>
 *     </ul>
 *   </li>
 *   <li><strong>Not Supported (fallback to source-over):</strong> All CSS blend modes including
 *       multiply, screen, overlay, darken, lighten, color-dodge, color-burn, hard-light,
 *       soft-light, difference, exclusion, hue, saturation, color, and luminosity.
 *       These require custom {@link java.awt.Composite} implementations with pixel-level blending.</li>
 * </ul>
 *
 * <p><strong>JavaFX Backend:</strong>
 * <ul>
 *   <li><strong>Fully Supported CSS Blend Modes:</strong> multiply, screen, overlay, darken,
 *       lighten, color-dodge, color-burn, hard-light, soft-light, difference, exclusion</li>
 *   <li><strong>Approximated Porter-Duff Operations:</strong>
 *     <ul>
 *       <li>"source-in" - Uses SRC_ATOP as closest approximation</li>
 *       <li>"copy" - Uses SRC_OVER (JavaFX lacks direct copy mode)</li>
 *     </ul>
 *   </li>
 *   <li><strong>Not Supported (fallback to source-over):</strong> source-out, destination-over,
 *       destination-in, destination-out, destination-atop, xor, hue, saturation, color, luminosity</li>
 * </ul>
 *
 * <p><strong>Recommendation:</strong> For maximum compatibility with CSS blend modes, consider
 * using the JavaFX backend. For server-side rendering with AWT, be aware that CSS blend modes
 * will fall back to standard alpha compositing.
 *
 * @see java.awt.AlphaComposite
 * @see javafx.scene.effect.BlendMode
 * @since 1.0
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
     *
     * <p><strong>AWT Backend Limitations:</strong>
     * <ul>
     *   <li>CSS blend modes (multiply, screen, overlay, etc.) are <strong>NOT supported</strong>
     *       by AWT's {@link java.awt.AlphaComposite} and fall back to source-over</li>
     *   <li>The "lighter" blend mode is <strong>approximated</strong> using SRC_OVER instead of
     *       true additive blending (would require custom Composite implementation)</li>
     *   <li>All Porter-Duff operations (source-*, destination-*, copy, xor) are fully supported</li>
     * </ul>
     *
     * <p>To implement true CSS blend modes in AWT, a custom {@link java.awt.Composite}
     * implementation would be required that performs pixel-level blending operations
     * according to the CSS Compositing and Blending Level 1 specification.
     *
     * @param operation The blend mode or composite operation name (e.g., "source-over", "multiply")
     * @param alpha The global alpha value (0.0 to 1.0)
     * @return AWT composite with the requested operation, or source-over fallback for unsupported modes
     * @see java.awt.AlphaComposite
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
