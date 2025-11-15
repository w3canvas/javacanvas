package com.w3canvas.javacanvas.core;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses CSS filter syntax into FilterFunction objects.
 * Supports: blur, brightness, contrast, grayscale, sepia, saturate,
 *           hue-rotate, invert, opacity, drop-shadow
 */
public class CSSFilterParser {

    // Pattern to match filter functions: functionName(args)
    private static final Pattern FILTER_PATTERN = Pattern.compile(
        "(blur|brightness|contrast|grayscale|sepia|saturate|hue-rotate|invert|opacity|drop-shadow)\\s*\\(([^)]*)\\)"
    );

    /**
     * Parse a CSS filter string into a list of FilterFunction objects
     * @param filterString CSS filter string (e.g., "blur(5px) brightness(150%)")
     * @return List of FilterFunction objects
     */
    public static List<FilterFunction> parse(String filterString) {
        List<FilterFunction> filters = new ArrayList<>();

        if (filterString == null || filterString.trim().isEmpty() || "none".equals(filterString.trim())) {
            return filters;
        }

        Matcher matcher = FILTER_PATTERN.matcher(filterString);
        while (matcher.find()) {
            String functionName = matcher.group(1);
            String args = matcher.group(2).trim();

            FilterFunction.FilterType type = getFilterType(functionName);
            if (type != FilterFunction.FilterType.NONE) {
                FilterFunction filter = new FilterFunction(type);
                parseArguments(filter, args, type);
                filters.add(filter);
            }
        }

        return filters;
    }

    /**
     * Convert function name string to FilterType enum
     */
    private static FilterFunction.FilterType getFilterType(String name) {
        switch (name.toLowerCase()) {
            case "blur": return FilterFunction.FilterType.BLUR;
            case "brightness": return FilterFunction.FilterType.BRIGHTNESS;
            case "contrast": return FilterFunction.FilterType.CONTRAST;
            case "grayscale": return FilterFunction.FilterType.GRAYSCALE;
            case "sepia": return FilterFunction.FilterType.SEPIA;
            case "saturate": return FilterFunction.FilterType.SATURATE;
            case "hue-rotate": return FilterFunction.FilterType.HUE_ROTATE;
            case "invert": return FilterFunction.FilterType.INVERT;
            case "opacity": return FilterFunction.FilterType.OPACITY;
            case "drop-shadow": return FilterFunction.FilterType.DROP_SHADOW;
            default: return FilterFunction.FilterType.NONE;
        }
    }

    /**
     * Parse the arguments for a filter function
     */
    private static void parseArguments(FilterFunction filter, String args, FilterFunction.FilterType type) {
        if (args.isEmpty()) {
            // Set defaults for filters with no arguments
            setDefaultValue(filter, type);
            return;
        }

        switch (type) {
            case BLUR:
                // blur(radius) - expects length (px)
                filter.addParam(parseLength(args));
                break;

            case BRIGHTNESS:
            case CONTRAST:
            case SATURATE:
            case GRAYSCALE:
            case SEPIA:
            case INVERT:
            case OPACITY:
                // These expect percentage or number
                filter.addParam(parsePercentageOrNumber(args));
                break;

            case HUE_ROTATE:
                // hue-rotate(angle) - expects angle (deg, rad, turn)
                filter.addParam(parseAngle(args));
                break;

            case DROP_SHADOW:
                // drop-shadow(offset-x offset-y blur-radius color)
                parseDropShadow(filter, args);
                break;

            default:
                break;
        }
    }

    /**
     * Set default values for filters called without arguments
     */
    private static void setDefaultValue(FilterFunction filter, FilterFunction.FilterType type) {
        switch (type) {
            case BLUR:
                filter.addParam(0.0); // 0px
                break;
            case BRIGHTNESS:
            case CONTRAST:
            case SATURATE:
            case OPACITY:
                filter.addParam(1.0); // 100%
                break;
            case GRAYSCALE:
            case SEPIA:
            case INVERT:
                filter.addParam(1.0); // 100%
                break;
            case HUE_ROTATE:
                filter.addParam(0.0); // 0deg
                break;
            default:
                break;
        }
    }

    /**
     * Parse a CSS length value (e.g., "5px", "10")
     * Returns value in pixels
     */
    private static double parseLength(String value) {
        if (value == null || value.isEmpty()) {
            return 0.0;
        }

        value = value.trim();

        // Remove 'px' suffix if present
        if (value.endsWith("px")) {
            value = value.substring(0, value.length() - 2).trim();
        }

        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    /**
     * Parse a percentage or number value
     * Percentage is converted to decimal (e.g., 150% -> 1.5)
     * Number is used as-is
     */
    private static double parsePercentageOrNumber(String value) {
        if (value == null || value.isEmpty()) {
            return 1.0;
        }

        value = value.trim();

        if (value.endsWith("%")) {
            // Remove % and convert to decimal
            String number = value.substring(0, value.length() - 1).trim();
            try {
                return Double.parseDouble(number) / 100.0;
            } catch (NumberFormatException e) {
                return 1.0;
            }
        } else {
            // Direct number
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException e) {
                return 1.0;
            }
        }
    }

    /**
     * Parse an angle value (deg, rad, grad, turn)
     * Returns value in degrees
     */
    private static double parseAngle(String value) {
        if (value == null || value.isEmpty()) {
            return 0.0;
        }

        value = value.trim();

        // Check for different angle units
        if (value.endsWith("deg")) {
            String number = value.substring(0, value.length() - 3).trim();
            try {
                return Double.parseDouble(number);
            } catch (NumberFormatException e) {
                return 0.0;
            }
        } else if (value.endsWith("rad")) {
            String number = value.substring(0, value.length() - 3).trim();
            try {
                return Math.toDegrees(Double.parseDouble(number));
            } catch (NumberFormatException e) {
                return 0.0;
            }
        } else if (value.endsWith("grad")) {
            String number = value.substring(0, value.length() - 4).trim();
            try {
                return Double.parseDouble(number) * 0.9; // grad to deg
            } catch (NumberFormatException e) {
                return 0.0;
            }
        } else if (value.endsWith("turn")) {
            String number = value.substring(0, value.length() - 4).trim();
            try {
                return Double.parseDouble(number) * 360.0; // turn to deg
            } catch (NumberFormatException e) {
                return 0.0;
            }
        } else {
            // Assume degrees if no unit
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }
    }

    /**
     * Parse drop-shadow arguments: offset-x offset-y [blur-radius] [color]
     */
    private static void parseDropShadow(FilterFunction filter, String args) {
        String[] parts = args.trim().split("\\s+");

        if (parts.length >= 2) {
            // offset-x
            filter.addParam(parseLength(parts[0]));
            // offset-y
            filter.addParam(parseLength(parts[1]));

            // Check for optional blur-radius
            if (parts.length >= 3) {
                // Try to parse as length, if it fails it might be a color
                try {
                    double blur = parseLength(parts[2]);
                    filter.addParam(blur);

                    // Check for optional color
                    if (parts.length >= 4) {
                        filter.addParam(parts[3]);
                    } else {
                        filter.addParam("black");
                    }
                } catch (Exception e) {
                    // parts[2] is probably a color
                    filter.addParam(0.0); // default blur
                    filter.addParam(parts[2]);
                }
            } else {
                filter.addParam(0.0); // default blur
                filter.addParam("black"); // default color
            }
        }
    }

    /**
     * Check if a filter string is valid
     */
    public static boolean isValid(String filterString) {
        if (filterString == null || filterString.trim().isEmpty()) {
            return true;
        }

        if ("none".equals(filterString.trim())) {
            return true;
        }

        Matcher matcher = FILTER_PATTERN.matcher(filterString);
        return matcher.find();
    }
}
