package com.w3canvas.javacanvas.backend.javafx;

import com.w3canvas.javacanvas.interfaces.IComposite;
import com.w3canvas.javacanvas.interfaces.CompositeOperation;
import javafx.scene.effect.BlendMode;

/**
 * JavaFX implementation of IComposite.
 * Converts backend-agnostic CompositeOperation enum to JavaFX BlendMode.
 *
 * <p>
 * <strong>JavaFX Backend Blend Mode Support:</strong>
 * <ul>
 * <li><strong>Fully Supported CSS Blend Modes:</strong> multiply, screen,
 * overlay, darken,
 * lighten, color-dodge, color-burn, hard-light, soft-light, difference,
 * exclusion (Native JavaFX support)</li>
 * <li><strong>Custom Rendered Modes:</strong>
 * <ul>
 * <li>HSL Modes: hue, saturation, color, luminosity</li>
 * <li>Porter-Duff Modes: source-in, source-out, destination-in,
 * destination-out, destination-atop, destination-over, xor, copy</li>
 * </ul>
 * These are handled by {@code JavaFXBlendRenderer} for pixel-perfect
 * correctness.
 * </li>
 * </ul>
 *
 * @since 1.0
 */
public class JavaFXComposite implements IComposite {
    private final BlendMode blendMode;
    private final CompositeOperation operation;

    /**
     * Creates a JavaFX composite from a backend-agnostic composite operation.
     *
     * @param operation the composite operation to convert
     */
    public JavaFXComposite(CompositeOperation operation) {
        this.operation = operation;
        this.blendMode = convertToBlendMode(operation);
    }

    public BlendMode getBlendMode() {
        return blendMode;
    }

    public CompositeOperation getOperation() {
        return operation;
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
                // SRC_IN: The source is copied only where it overlaps the destination.
                // SRC_ATOP: The source is copied only where it overlaps the destination.
                return BlendMode.SRC_ATOP;
            case SOURCE_OUT:
                // Not directly supported, fallback to SRC_OVER
                // SRC_OUT: The source is copied only where it does not overlap the destination.
                return BlendMode.SRC_OVER;
            case SOURCE_ATOP:
                return BlendMode.SRC_ATOP;
            case DESTINATION_OVER:
                // Not directly supported, fallback to SRC_OVER
                // DESTINATION_OVER: The destination is copied over the source.
                return BlendMode.SRC_OVER;
            case DESTINATION_IN:
                // Not directly supported, fallback to SRC_OVER
                // DESTINATION_IN: The destination is copied only where it overlaps the source.
                return BlendMode.SRC_OVER;
            case DESTINATION_OUT:
                // Not directly supported, fallback to SRC_OVER
                // DESTINATION_OUT: The destination is copied only where it does not overlap the
                // source.
                return BlendMode.SRC_OVER;
            case DESTINATION_ATOP:
                // Not directly supported, fallback to SRC_OVER
                // DESTINATION_ATOP: The destination is copied only where it overlaps the
                // source.
                return BlendMode.SRC_OVER;
            case LIGHTER:
                return BlendMode.ADD;
            case COPY:
                // JavaFX doesn't have a direct copy mode, SRC_OVER is closest
                // COPY: The source is copied to the destination.
                return BlendMode.SRC_OVER;
            case XOR:
                // Not directly supported, fallback to SRC_OVER
                // XOR: The source and destination are combined using an exclusive OR.
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

            // HSL Blend Modes - Not supported in JavaFX BlendMode enum
            // These require pixel-level manipulation which is not efficient with standard
            // JavaFX nodes.
            // We fallback to SRC_OVER to ensure content is at least visible.
            case HUE:
                return BlendMode.SRC_OVER;
            case SATURATION:
                return BlendMode.SRC_OVER;
            case COLOR:
                return BlendMode.SRC_OVER;
            case LUMINOSITY:
                return BlendMode.SRC_OVER;

            default:
                // Unknown operation, default to source-over
                return BlendMode.SRC_OVER;
        }
    }
}
