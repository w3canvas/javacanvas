package com.w3canvas.javacanvas.backend.awt;

import com.w3canvas.javacanvas.interfaces.IComposite;
import com.w3canvas.javacanvas.interfaces.CompositeOperation;
import java.awt.AlphaComposite;
import java.awt.Composite;

/**
 * AWT implementation of IComposite.
 * Converts backend-agnostic CompositeOperation enum to AWT AlphaComposite.
 *
 * <p><strong>AWT Backend Limitations:</strong>
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
 * @since 1.0
 */
public class AwtComposite implements IComposite {
    private final Composite composite;

    /**
     * Creates an AWT composite from a backend-agnostic composite operation.
     *
     * @param operation the composite operation to convert
     * @param alpha the global alpha value (0.0 to 1.0)
     */
    public AwtComposite(CompositeOperation operation, double alpha) {
        this.composite = convertToAlphaComposite(operation, alpha);
    }

    public Composite getComposite() {
        return composite;
    }

    /**
     * Converts a backend-agnostic CompositeOperation to AWT AlphaComposite.
     *
     * @param operation the operation to convert
     * @param alpha the global alpha value
     * @return the corresponding AWT Composite
     */
    private static Composite convertToAlphaComposite(CompositeOperation operation, double alpha) {
        int rule;
        boolean useAlpha = true;

        switch (operation) {
            // Porter-Duff composite operations
            case SOURCE_OVER:
                rule = AlphaComposite.SRC_OVER;
                break;
            case SOURCE_IN:
                rule = AlphaComposite.SRC_IN;
                break;
            case SOURCE_OUT:
                rule = AlphaComposite.SRC_OUT;
                break;
            case SOURCE_ATOP:
                rule = AlphaComposite.SRC_ATOP;
                break;
            case DESTINATION_OVER:
                rule = AlphaComposite.DST_OVER;
                break;
            case DESTINATION_IN:
                rule = AlphaComposite.DST_IN;
                break;
            case DESTINATION_OUT:
                rule = AlphaComposite.DST_OUT;
                break;
            case DESTINATION_ATOP:
                rule = AlphaComposite.DST_ATOP;
                break;
            case LIGHTER:
                // "lighter" is like additive blending - closest AWT equivalent
                rule = AlphaComposite.SRC_OVER;
                // TODO: Implement custom composite for true additive blending
                break;
            case COPY:
                rule = AlphaComposite.SRC;
                useAlpha = false; // Copy ignores alpha
                break;
            case XOR:
                rule = AlphaComposite.XOR;
                break;

            // CSS blend modes - AWT doesn't support these natively
            // These would require custom Composite implementations
            case MULTIPLY:
            case SCREEN:
            case OVERLAY:
            case DARKEN:
            case LIGHTEN:
            case COLOR_DODGE:
            case COLOR_BURN:
            case HARD_LIGHT:
            case SOFT_LIGHT:
            case DIFFERENCE:
            case EXCLUSION:
            case HUE:
            case SATURATION:
            case COLOR:
            case LUMINOSITY:
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
            return AlphaComposite.getInstance(rule, (float) alpha);
        } else {
            return AlphaComposite.getInstance(rule);
        }
    }
}
