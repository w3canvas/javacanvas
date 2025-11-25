package com.w3canvas.javacanvas.core;

import com.w3canvas.javacanvas.interfaces.CompositeOperation;
import com.w3canvas.javacanvas.interfaces.IComposite;
import com.w3canvas.javacanvas.interfaces.IGraphicsBackend;

/**
 * Backend-agnostic factory for creating compositing operations according to
 * Canvas 2D API spec.
 * Supports both Porter-Duff composite operations and CSS blend modes.
 *
 * <p>
 * This factory converts Canvas API operation strings (e.g., "source-over",
 * "multiply")
 * into backend-agnostic {@link CompositeOperation} enums, then delegates to the
 * appropriate
 * backend implementation for conversion to backend-specific composite objects.
 *
 * <p>
 * <strong>Backend-Specific Blend Mode Support:</strong>
 *
 * <p>
 * <strong>AWT Backend:</strong>
 * <ul>
 * <li><strong>Fully Supported Porter-Duff Operations:</strong> source-over,
 * source-in,
 * source-out, source-atop, destination-over, destination-in, destination-out,
 * destination-atop, copy, xor</li>
 * <li><strong>Approximated:</strong>
 * <ul>
 * <li>"lighter" - Mapped to SRC_OVER instead of true additive blending.
 * A proper implementation would require custom Composite for pixel-level
 * addition.</li>
 * </ul>
 * </li>
 * <li><strong>Not Supported (fallback to source-over):</strong> All CSS blend
 * modes including
 * multiply, screen, overlay, darken, lighten, color-dodge, color-burn,
 * hard-light,
 * soft-light, difference, exclusion, hue, saturation, color, and luminosity.
 * These require custom {@link java.awt.Composite} implementations with
 * pixel-level blending.</li>
 * </ul>
 *
 * <p>
 * <strong>JavaFX Backend:</strong>
 * <ul>
 * <li><strong>Fully Supported CSS Blend Modes:</strong> multiply, screen,
 * overlay, darken,
 * lighten, color-dodge, color-burn, hard-light, soft-light, difference,
 * exclusion</li>
 * <li><strong>Approximated Porter-Duff Operations:</strong>
 * <ul>
 * <li>"source-in" - Uses SRC_ATOP as closest approximation</li>
 * <li>"copy" - Uses SRC_OVER (JavaFX lacks direct copy mode)</li>
 * </ul>
 * </li>
 * <li><strong>Not Supported (fallback to source-over):</strong> source-out,
 * destination-over,
 * destination-in, destination-out, destination-atop, xor, hue, saturation,
 * color, luminosity</li>
 * </ul>
 *
 * <p>
 * <strong>Recommendation:</strong> For maximum compatibility with CSS blend
 * modes, consider
 * using the JavaFX backend. For server-side rendering with AWT, be aware that
 * CSS blend modes
 * will fall back to standard alpha compositing.
 *
 * @see CompositeOperation
 * @see com.w3canvas.javacanvas.backend.awt.AwtComposite
 * @see com.w3canvas.javacanvas.backend.javafx.JavaFXComposite
 * @since 1.0
 */
public class CompositeFactory {

    /**
     * Creates a backend-specific composite from an operation string.
     *
     * @param operation the composite operation name (e.g., "source-over",
     *                  "multiply")
     * @param alpha     the global alpha value (0.0 to 1.0)
     * @param backend   the graphics backend to create the composite for
     * @return a backend-specific IComposite implementation, or null if backend is
     *         unknown
     */
    public static IComposite createComposite(String operation, double alpha, IGraphicsBackend backend) {
        // Convert string to backend-agnostic enum
        CompositeOperation compositeOp = CompositeOperation.fromString(operation);

        // Delegate to backend-specific composite creation
        if (backend != null) {
            return backend.createComposite(compositeOp, alpha);
        }
        return null;
    }

}
