package com.w3canvas.javacanvas.backend.awt;

import com.w3canvas.javacanvas.interfaces.IComposite;
import com.w3canvas.javacanvas.interfaces.CompositeOperation;
import java.awt.AlphaComposite;
import java.awt.Composite;

/**
 * AWT implementation of IComposite.
 * Converts backend-agnostic CompositeOperation enum to AWT Composite implementations.
 *
 * <p><strong>Supported Operations:</strong>
 * <ul>
 *   <li><strong>Porter-Duff Operations (via AlphaComposite):</strong> source-over, source-in,
 *       source-out, source-atop, destination-over, destination-in, destination-out,
 *       destination-atop, copy, xor</li>
 *   <li><strong>CSS Blend Modes (via AwtBlendComposite):</strong> multiply, screen, overlay,
 *       darken, lighten, color-dodge, color-burn, hard-light, soft-light, difference,
 *       exclusion, hue, saturation, color, luminosity, and lighter (additive blending)</li>
 * </ul>
 *
 * @see AwtBlendComposite
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
     * Converts a backend-agnostic CompositeOperation to AWT Composite.
     * Uses AlphaComposite for Porter-Duff operations and AwtBlendComposite for CSS blend modes.
     *
     * @param operation the operation to convert
     * @param alpha the global alpha value
     * @return the corresponding AWT Composite
     */
    private static Composite convertToAlphaComposite(CompositeOperation operation, double alpha) {
        // CSS blend modes use custom AwtBlendComposite
        switch (operation) {
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
            case LIGHTER:
                // Use custom blend composite for CSS blend modes
                return new AwtBlendComposite(operation, (float) alpha);

            default:
                // Porter-Duff operations use AlphaComposite
                break;
        }

        // Porter-Duff composite operations
        int rule;
        boolean useAlpha = true;

        switch (operation) {
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
            case COPY:
                rule = AlphaComposite.SRC;
                useAlpha = false; // Copy ignores alpha
                break;
            case XOR:
                rule = AlphaComposite.XOR;
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
