package com.w3canvas.javacanvas.backend.javafx;

import com.w3canvas.javacanvas.core.BlendMath;
import com.w3canvas.javacanvas.interfaces.CompositeOperation;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

/**
 * Helper class for rendering custom blend modes in JavaFX.
 *
 * <p>
 * Since JavaFX does not support all CSS blend modes (e.g. HSL modes) natively,
 * this class performs pixel-level blending using {@link BlendMath}.
 * This is a slow fallback path but ensures correctness.
 */
public class JavaFXBlendRenderer {

    private JavaFXBlendRenderer() {
        // Static utility
    }

    /**
     * Blends a source image onto a destination image using the specified composite
     * operation.
     *
     * @param src       the source image (what is being drawn)
     * @param dst       the destination image (current canvas content)
     * @param operation the composite operation to apply
     * @param alpha     global alpha to apply to the source
     * @return a new WritableImage containing the blended result
     */
    public static WritableImage blend(Image src, Image dst, CompositeOperation operation, double alpha) {
        int width = (int) Math.min(src.getWidth(), dst.getWidth());
        int height = (int) Math.min(src.getHeight(), dst.getHeight());

        WritableImage result = new WritableImage(width, height);
        PixelReader srcReader = src.getPixelReader();
        PixelReader dstReader = dst.getPixelReader();
        PixelWriter writer = result.getPixelWriter();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color srcColor = srcReader.getColor(x, y);
                Color dstColor = dstReader.getColor(x, y);

                // Apply global alpha to source
                double srcA = srcColor.getOpacity() * alpha;
                double srcR = srcColor.getRed();
                double srcG = srcColor.getGreen();
                double srcB = srcColor.getBlue();

                double dstA = dstColor.getOpacity();
                double dstR = dstColor.getRed();
                double dstG = dstColor.getGreen();
                double dstB = dstColor.getBlue();

                if (srcA == 0 && isSourceOver(operation)) {
                    // Optimization for common case: Source is transparent and mode is Source-Over
                    writer.setColor(x, y, dstColor);
                    continue;
                }

                // Determine blending and compositing logic
                double outR, outG, outB, outA;

                if (isSeparableBlendMode(operation)) {
                    // Separable Blend Modes (Multiply, Screen, HSL, etc.)
                    // These always use Source-Over composition but with a custom blend function.
                    // Formula: Cr = (1 - ab) * as * Cs + (1 - as) * ab * Cb + as * ab * B(Cb, Cs)

                    float[] blended = BlendMath.blend(
                            operation,
                            (float) srcR, (float) srcG, (float) srcB,
                            (float) dstR, (float) dstG, (float) dstB);

                    double bR = blended[0];
                    double bG = blended[1];
                    double bB = blended[2];

                    // Full W3C formula
                    outA = srcA + dstA * (1.0 - srcA);

                    if (outA > 0) {
                        outR = ((1.0 - dstA) * srcA * srcR + (1.0 - srcA) * dstA * dstR + srcA * dstA * bR) / outA;
                        outG = ((1.0 - dstA) * srcA * srcG + (1.0 - srcA) * dstA * dstG + srcA * dstA * bG) / outA;
                        outB = ((1.0 - dstA) * srcA * srcB + (1.0 - srcA) * dstA * dstB + srcA * dstA * bB) / outA;
                    } else {
                        outR = outG = outB = 0;
                    }

                } else {
                    // Porter-Duff Composite Modes (Source-In, Copy, XOR, etc.)
                    // Formula: Co = as * Fa * Cs + ab * Fb * Cb
                    // ao = as * Fa + ab * Fb

                    double[] coeffs = getCoefficients(operation, srcA, dstA);
                    double Fa = coeffs[0];
                    double Fb = coeffs[1];

                    outA = srcA * Fa + dstA * Fb;

                    if (outA > 0) {
                        outR = (srcA * Fa * srcR + dstA * Fb * dstR) / outA;
                        outG = (srcA * Fa * srcG + dstA * Fb * dstG) / outA;
                        outB = (srcA * Fa * srcB + dstA * Fb * dstB) / outA;
                    } else {
                        outR = outG = outB = 0;
                    }
                }

                writer.setColor(x, y, new Color(
                        clamp(outR), clamp(outG), clamp(outB), clamp(outA)));
            }
        }

        return result;
    }

    private static double clamp(double val) {
        return Math.max(0.0, Math.min(1.0, val));
    }

    private static boolean isSourceOver(CompositeOperation op) {
        return op == CompositeOperation.SOURCE_OVER;
    }

    private static boolean isSeparableBlendMode(CompositeOperation op) {
        switch (op) {
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
                return true;
            default:
                return false;
        }
    }

    private static double[] getCoefficients(CompositeOperation op, double srcA, double dstA) {
        double Fa = 0;
        double Fb = 0;

        switch (op) {
            case SOURCE_OVER:
                Fa = 1.0;
                Fb = 1.0 - srcA;
                break;
            case SOURCE_IN:
                Fa = dstA;
                Fb = 0.0;
                break;
            case SOURCE_OUT:
                Fa = 1.0 - dstA;
                Fb = 0.0;
                break;
            case SOURCE_ATOP:
                Fa = dstA;
                Fb = 1.0 - srcA;
                break;
            case DESTINATION_OVER:
                Fa = 1.0 - dstA;
                Fb = 1.0;
                break;
            case DESTINATION_IN:
                Fa = 0.0;
                Fb = srcA;
                break;
            case DESTINATION_OUT:
                Fa = 0.0;
                Fb = 1.0 - srcA;
                break;
            case DESTINATION_ATOP:
                Fa = 1.0 - dstA;
                Fb = srcA;
                break;
            case XOR:
                Fa = 1.0 - dstA;
                Fb = 1.0 - srcA;
                break;
            case COPY:
                Fa = 1.0;
                Fb = 0.0;
                break;
            case LIGHTER:
                Fa = 1.0;
                Fb = 1.0;
                break;
            default:
                // Default to Source-Over
                Fa = 1.0;
                Fb = 1.0 - srcA;
                break;
        }
        return new double[] { Fa, Fb };
    }
}
