package com.w3canvas.javacanvas.interfaces;

/**
 * Backend-agnostic enumeration of composite operations and blend modes
 * as defined by the HTML5 Canvas 2D API and CSS Compositing and Blending specifications.
 *
 * <p>This enum provides a unified representation of both Porter-Duff composite operations
 * and CSS blend modes, allowing the core layer to remain backend-agnostic.
 *
 * @see <a href="https://www.w3.org/TR/compositing-1/">CSS Compositing and Blending Level 1</a>
 * @see <a href="https://html.spec.whatwg.org/multipage/canvas.html#dom-context-2d-globalcompositeoperation">Canvas 2D globalCompositeOperation</a>
 * @since 1.0
 */
public enum CompositeOperation {
    // Porter-Duff composite operations
    SOURCE_OVER,
    SOURCE_IN,
    SOURCE_OUT,
    SOURCE_ATOP,
    DESTINATION_OVER,
    DESTINATION_IN,
    DESTINATION_OUT,
    DESTINATION_ATOP,
    LIGHTER,
    COPY,
    XOR,

    // CSS blend modes
    MULTIPLY,
    SCREEN,
    OVERLAY,
    DARKEN,
    LIGHTEN,
    COLOR_DODGE,
    COLOR_BURN,
    HARD_LIGHT,
    SOFT_LIGHT,
    DIFFERENCE,
    EXCLUSION,
    HUE,
    SATURATION,
    COLOR,
    LUMINOSITY;

    /**
     * Parse a composite operation string from the Canvas API into an enum value.
     *
     * @param operation the operation string (e.g., "source-over", "multiply")
     * @return the corresponding CompositeOperation, or SOURCE_OVER if unknown
     */
    public static CompositeOperation fromString(String operation) {
        if (operation == null || operation.isEmpty()) {
            return SOURCE_OVER;
        }

        switch (operation.toLowerCase()) {
            case "source-over":
                return SOURCE_OVER;
            case "source-in":
                return SOURCE_IN;
            case "source-out":
                return SOURCE_OUT;
            case "source-atop":
                return SOURCE_ATOP;
            case "destination-over":
                return DESTINATION_OVER;
            case "destination-in":
                return DESTINATION_IN;
            case "destination-out":
                return DESTINATION_OUT;
            case "destination-atop":
                return DESTINATION_ATOP;
            case "lighter":
                return LIGHTER;
            case "copy":
                return COPY;
            case "xor":
                return XOR;
            case "multiply":
                return MULTIPLY;
            case "screen":
                return SCREEN;
            case "overlay":
                return OVERLAY;
            case "darken":
                return DARKEN;
            case "lighten":
                return LIGHTEN;
            case "color-dodge":
                return COLOR_DODGE;
            case "color-burn":
                return COLOR_BURN;
            case "hard-light":
                return HARD_LIGHT;
            case "soft-light":
                return SOFT_LIGHT;
            case "difference":
                return DIFFERENCE;
            case "exclusion":
                return EXCLUSION;
            case "hue":
                return HUE;
            case "saturation":
                return SATURATION;
            case "color":
                return COLOR;
            case "luminosity":
                return LUMINOSITY;
            default:
                return SOURCE_OVER;
        }
    }
}
