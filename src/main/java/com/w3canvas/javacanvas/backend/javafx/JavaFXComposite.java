package com.w3canvas.javacanvas.backend.javafx;

import com.w3canvas.javacanvas.interfaces.IComposite;
import com.w3canvas.javacanvas.interfaces.CompositeOperation;
import javafx.scene.effect.BlendMode;

/**
 * JavaFX implementation of IComposite.
 * Converts backend-agnostic CompositeOperation enum to JavaFX BlendMode.
 *
 * <p><strong>JavaFX Backend Blend Mode Support:</strong>
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
 * @since 1.0
 */
public class JavaFXComposite implements IComposite {
    private final BlendMode blendMode;

    /**
     * Creates a JavaFX composite from a backend-agnostic composite operation.
     *
     * @param operation the composite operation to convert
     */
    public JavaFXComposite(CompositeOperation operation) {
        this.blendMode = convertToBlendMode(operation);
    }

    public BlendMode getBlendMode() {
        return blendMode;
    }

    /**
     * Converts a backend-agnostic CompositeOperation to JavaFX BlendMode.
     *
     * @param operation the operation to convert
     * @return the corresponding JavaFX BlendMode
     */
    private static BlendMode convertToBlendMode(CompositeOperation operation) {
        switch (operation) {
            // Porter-Duff composite operations
            case SOURCE_OVER:
                return BlendMode.SRC_OVER;
            case SOURCE_IN:
                // JavaFX doesn't have exact SRC_IN, use SRC_ATOP as approximation
                return BlendMode.SRC_ATOP;
            case SOURCE_OUT:
                // Not directly supported, fallback to SRC_OVER
                return BlendMode.SRC_OVER;
            case SOURCE_ATOP:
                return BlendMode.SRC_ATOP;
            case DESTINATION_OVER:
                // Not directly supported, fallback to SRC_OVER
                return BlendMode.SRC_OVER;
            case DESTINATION_IN:
                // Not directly supported, fallback to SRC_OVER
                return BlendMode.SRC_OVER;
            case DESTINATION_OUT:
                // Not directly supported, fallback to SRC_OVER
                return BlendMode.SRC_OVER;
            case DESTINATION_ATOP:
                // Not directly supported, fallback to SRC_OVER
                return BlendMode.SRC_OVER;
            case LIGHTER:
                return BlendMode.ADD;
            case COPY:
                // JavaFX doesn't have a direct copy mode, SRC_OVER is closest
                return BlendMode.SRC_OVER;
            case XOR:
                // Not directly supported, fallback to SRC_OVER
                return BlendMode.SRC_OVER;

            // CSS blend modes - JavaFX has excellent support
            case MULTIPLY:
                return BlendMode.MULTIPLY;
            case SCREEN:
                return BlendMode.SCREEN;
            case OVERLAY:
                return BlendMode.OVERLAY;
            case DARKEN:
                return BlendMode.DARKEN;
            case LIGHTEN:
                return BlendMode.LIGHTEN;
            case COLOR_DODGE:
                return BlendMode.COLOR_DODGE;
            case COLOR_BURN:
                return BlendMode.COLOR_BURN;
            case HARD_LIGHT:
                return BlendMode.HARD_LIGHT;
            case SOFT_LIGHT:
                return BlendMode.SOFT_LIGHT;
            case DIFFERENCE:
                return BlendMode.DIFFERENCE;
            case EXCLUSION:
                return BlendMode.EXCLUSION;
            case HUE:
                // Not directly supported in JavaFX
                return BlendMode.SRC_OVER;
            case SATURATION:
                // Not directly supported in JavaFX
                return BlendMode.SRC_OVER;
            case COLOR:
                // Not directly supported in JavaFX
                return BlendMode.SRC_OVER;
            case LUMINOSITY:
                // Not directly supported in JavaFX
                return BlendMode.SRC_OVER;

            default:
                // Unknown operation, default to source-over
                return BlendMode.SRC_OVER;
        }
    }
}
