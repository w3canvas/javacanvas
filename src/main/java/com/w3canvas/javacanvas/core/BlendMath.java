package com.w3canvas.javacanvas.core;

import com.w3canvas.javacanvas.interfaces.CompositeOperation;

/**
 * Shared math utilities for CSS blend modes.
 *
 * <p>
 * This class implements the blending formulas specified in the
 * W3C CSS Compositing and Blending Level 1 specification.
 * It is backend-agnostic and operates on float color components (0.0-1.0).
 *
 * @see <a href="https://www.w3.org/TR/compositing-1/">CSS Compositing and
 *      Blending Level 1</a>
 */
public class BlendMath {

    private BlendMath() {
        // Static utility class
    }

    /**
     * Blends a source color with a destination color using the specified mode.
     *
     * @param mode the composite operation (blend mode)
     * @param sR   source red (0.0-1.0)
     * @param sG   source green (0.0-1.0)
     * @param sB   source blue (0.0-1.0)
     * @param dR   destination red (0.0-1.0)
     * @param dG   destination green (0.0-1.0)
     * @param dB   destination blue (0.0-1.0)
     * @return float array [r, g, b] with the blended result (0.0-1.0)
     */
    public static float[] blend(CompositeOperation mode, float sR, float sG, float sB, float dR, float dG, float dB) {
        float outR, outG, outB;

        switch (mode) {
            case MULTIPLY:
                outR = blendMultiply(sR, dR);
                outG = blendMultiply(sG, dG);
                outB = blendMultiply(sB, dB);
                break;

            case SCREEN:
                outR = blendScreen(sR, dR);
                outG = blendScreen(sG, dG);
                outB = blendScreen(sB, dB);
                break;

            case OVERLAY:
                outR = blendOverlay(sR, dR);
                outG = blendOverlay(sG, dG);
                outB = blendOverlay(sB, dB);
                break;

            case DARKEN:
                outR = Math.min(sR, dR);
                outG = Math.min(sG, dG);
                outB = Math.min(sB, dB);
                break;

            case LIGHTEN:
                outR = Math.max(sR, dR);
                outG = Math.max(sG, dG);
                outB = Math.max(sB, dB);
                break;

            case COLOR_DODGE:
                outR = blendColorDodge(sR, dR);
                outG = blendColorDodge(sG, dG);
                outB = blendColorDodge(sB, dB);
                break;

            case COLOR_BURN:
                outR = blendColorBurn(sR, dR);
                outG = blendColorBurn(sG, dG);
                outB = blendColorBurn(sB, dB);
                break;

            case HARD_LIGHT:
                outR = blendHardLight(sR, dR);
                outG = blendHardLight(sG, dG);
                outB = blendHardLight(sB, dB);
                break;

            case SOFT_LIGHT:
                outR = blendSoftLight(sR, dR);
                outG = blendSoftLight(sG, dG);
                outB = blendSoftLight(sB, dB);
                break;

            case DIFFERENCE:
                outR = Math.abs(sR - dR);
                outG = Math.abs(sG - dG);
                outB = Math.abs(sB - dB);
                break;

            case EXCLUSION:
                outR = blendExclusion(sR, dR);
                outG = blendExclusion(sG, dG);
                outB = blendExclusion(sB, dB);
                break;

            case HUE:
                float[] hueResult = blendHue(sR, sG, sB, dR, dG, dB);
                outR = hueResult[0];
                outG = hueResult[1];
                outB = hueResult[2];
                break;

            case SATURATION:
                float[] satResult = blendSaturation(sR, sG, sB, dR, dG, dB);
                outR = satResult[0];
                outG = satResult[1];
                outB = satResult[2];
                break;

            case COLOR:
                float[] colorResult = blendColor(sR, sG, sB, dR, dG, dB);
                outR = colorResult[0];
                outG = colorResult[1];
                outB = colorResult[2];
                break;

            case LUMINOSITY:
                float[] lumResult = blendLuminosity(sR, sG, sB, dR, dG, dB);
                outR = lumResult[0];
                outG = lumResult[1];
                outB = lumResult[2];
                break;

            case LIGHTER:
                // Additive blending
                outR = Math.min(1.0f, sR + dR);
                outG = Math.min(1.0f, sG + dG);
                outB = Math.min(1.0f, sB + dB);
                break;

            default:
                // Fallback to source-over (return source)
                outR = sR;
                outG = sG;
                outB = sB;
                break;
        }

        return new float[] { outR, outG, outB };
    }

    // ========== Simple Blend Mode Functions ==========

    public static float blendMultiply(float src, float dst) {
        return src * dst;
    }

    public static float blendScreen(float src, float dst) {
        return src + dst - src * dst;
    }

    public static float blendOverlay(float src, float dst) {
        if (dst <= 0.5f) {
            return 2.0f * src * dst;
        } else {
            return 1.0f - 2.0f * (1.0f - src) * (1.0f - dst);
        }
    }

    public static float blendColorDodge(float src, float dst) {
        if (dst >= 1.0f) {
            return 1.0f;
        } else if (src < 1.0f) {
            return Math.min(1.0f, dst / (1.0f - src));
        } else {
            return 1.0f;
        }
    }

    public static float blendColorBurn(float src, float dst) {
        if (dst <= 0.0f) {
            return 0.0f;
        } else if (src > 0.0f) {
            return 1.0f - Math.min(1.0f, (1.0f - dst) / src);
        } else {
            return 0.0f;
        }
    }

    public static float blendHardLight(float src, float dst) {
        if (src <= 0.5f) {
            return 2.0f * src * dst;
        } else {
            return 1.0f - 2.0f * (1.0f - src) * (1.0f - dst);
        }
    }

    public static float blendSoftLight(float src, float dst) {
        // W3C soft-light formula
        if (src <= 0.5f) {
            return dst - (1.0f - 2.0f * src) * dst * (1.0f - dst);
        } else {
            float d;
            if (dst <= 0.25f) {
                d = ((16.0f * dst - 12.0f) * dst + 4.0f) * dst;
            } else {
                d = (float) Math.sqrt(dst);
            }
            return dst + (2.0f * src - 1.0f) * (d - dst);
        }
    }

    public static float blendExclusion(float src, float dst) {
        return src + dst - 2.0f * src * dst;
    }

    // ========== HSL-based Blend Modes ==========

    public static float[] blendHue(float sR, float sG, float sB, float dR, float dG, float dB) {
        float[] srcHSL = rgbToHsl(sR, sG, sB);
        float[] dstHSL = rgbToHsl(dR, dG, dB);
        // Use source hue, destination saturation and luminosity
        return hslToRgb(srcHSL[0], dstHSL[1], dstHSL[2]);
    }

    public static float[] blendSaturation(float sR, float sG, float sB, float dR, float dG, float dB) {
        float[] srcHSL = rgbToHsl(sR, sG, sB);
        float[] dstHSL = rgbToHsl(dR, dG, dB);
        // Use destination hue, source saturation, destination luminosity
        return hslToRgb(dstHSL[0], srcHSL[1], dstHSL[2]);
    }

    public static float[] blendColor(float sR, float sG, float sB, float dR, float dG, float dB) {
        float[] srcHSL = rgbToHsl(sR, sG, sB);
        float[] dstHSL = rgbToHsl(dR, dG, dB);
        // Use source hue and saturation, destination luminosity
        return hslToRgb(srcHSL[0], srcHSL[1], dstHSL[2]);
    }

    public static float[] blendLuminosity(float sR, float sG, float sB, float dR, float dG, float dB) {
        float[] srcHSL = rgbToHsl(sR, sG, sB);
        float[] dstHSL = rgbToHsl(dR, dG, dB);
        // Use destination hue and saturation, source luminosity
        return hslToRgb(dstHSL[0], dstHSL[1], srcHSL[2]);
    }

    // ========== RGB-HSL Conversion ==========

    /**
     * Converts RGB to HSL color space.
     * 
     * @return float array [hue (0-360), saturation (0-1), lightness (0-1)]
     */
    public static float[] rgbToHsl(float r, float g, float b) {
        float max = Math.max(Math.max(r, g), b);
        float min = Math.min(Math.min(r, g), b);
        float delta = max - min;

        float h = 0.0f;
        float s = 0.0f;
        float l = (max + min) / 2.0f;

        if (delta != 0.0f) {
            // Calculate saturation
            s = (l < 0.5f) ? delta / (max + min) : delta / (2.0f - max - min);

            // Calculate hue
            if (r == max) {
                h = ((g - b) / delta) + (g < b ? 6.0f : 0.0f);
            } else if (g == max) {
                h = ((b - r) / delta) + 2.0f;
            } else {
                h = ((r - g) / delta) + 4.0f;
            }
            h *= 60.0f;
        }

        return new float[] { h, s, l };
    }

    /**
     * Converts HSL to RGB color space.
     * 
     * @param h hue in degrees (0-360)
     * @param s saturation (0-1)
     * @param l lightness (0-1)
     * @return float array [r, g, b] in range 0-1
     */
    public static float[] hslToRgb(float h, float s, float l) {
        float r, g, b;

        if (s == 0.0f) {
            // Achromatic (gray)
            r = g = b = l;
        } else {
            float q = (l < 0.5f) ? l * (1.0f + s) : l + s - l * s;
            float p = 2.0f * l - q;

            h = h / 360.0f; // Normalize hue to 0-1

            r = hueToRgb(p, q, h + 1.0f / 3.0f);
            g = hueToRgb(p, q, h);
            b = hueToRgb(p, q, h - 1.0f / 3.0f);
        }

        return new float[] {
                Math.max(0.0f, Math.min(1.0f, r)),
                Math.max(0.0f, Math.min(1.0f, g)),
                Math.max(0.0f, Math.min(1.0f, b))
        };
    }

    /**
     * Helper function for HSL to RGB conversion.
     */
    private static float hueToRgb(float p, float q, float t) {
        if (t < 0.0f)
            t += 1.0f;
        if (t > 1.0f)
            t -= 1.0f;
        if (t < 1.0f / 6.0f)
            return p + (q - p) * 6.0f * t;
        if (t < 1.0f / 2.0f)
            return q;
        if (t < 2.0f / 3.0f)
            return p + (q - p) * (2.0f / 3.0f - t) * 6.0f;
        return p;
    }
}
