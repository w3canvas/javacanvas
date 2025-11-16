package com.w3canvas.javacanvas.backend;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Helper class for generating conic gradient patterns.
 * This implements a polyfill-like approach that creates an image with radial wedges
 * at regular angular intervals, with interpolated colors between color stops.
 */
public class ConicGradientHelper {

    /**
     * Represents a color stop in the gradient.
     */
    public static class ColorStop implements Comparable<ColorStop> {
        public final double offset;
        public final int r;
        public final int g;
        public final int b;
        public final int a;

        public ColorStop(double offset, int r, int g, int b, int a) {
            this.offset = offset;
            this.r = r;
            this.g = g;
            this.b = b;
            this.a = a;
        }

        @Override
        public int compareTo(ColorStop other) {
            return Double.compare(this.offset, other.offset);
        }
    }

    /**
     * Calculates the pattern size needed to cover a canvas area.
     * Returns a reasonable size that's large enough but not excessive.
     */
    public static int calculatePatternSize(double canvasWidth, double canvasHeight, double centerX, double centerY) {
        // Calculate the maximum distance from center to any corner
        double maxDist = 0;
        maxDist = Math.max(maxDist, Math.sqrt(centerX * centerX + centerY * centerY));
        maxDist = Math.max(maxDist, Math.sqrt((canvasWidth - centerX) * (canvasWidth - centerX) + centerY * centerY));
        maxDist = Math.max(maxDist, Math.sqrt(centerX * centerX + (canvasHeight - centerY) * (canvasHeight - centerY)));
        maxDist = Math.max(maxDist, Math.sqrt((canvasWidth - centerX) * (canvasWidth - centerX) +
                                               (canvasHeight - centerY) * (canvasHeight - centerY)));

        // Pattern needs to be 2x the max distance (diameter)
        int size = (int) Math.ceil(maxDist * 2);

        // Clamp to reasonable bounds
        size = Math.max(2, Math.min(4096, size));

        return size;
    }

    /**
     * Interpolates a color value for a given angle (in radians) based on color stops.
     * The angle is normalized to [0, 1] range relative to the full circle.
     */
    public static int[] interpolateColor(double angle, double startAngle, List<ColorStop> stops) {
        if (stops.isEmpty()) {
            return new int[]{0, 0, 0, 0}; // Transparent
        }

        if (stops.size() == 1) {
            ColorStop stop = stops.get(0);
            return new int[]{stop.r, stop.g, stop.b, stop.a};
        }

        // Normalize angle to [0, 2π)
        angle = angle % (2 * Math.PI);
        if (angle < 0) angle += 2 * Math.PI;

        // Normalize startAngle to [0, 2π)
        startAngle = startAngle % (2 * Math.PI);
        if (startAngle < 0) startAngle += 2 * Math.PI;

        // Calculate position in gradient [0, 1]
        double relativeAngle = angle - startAngle;
        if (relativeAngle < 0) relativeAngle += 2 * Math.PI;
        double position = relativeAngle / (2 * Math.PI);

        // Find the two stops to interpolate between
        ColorStop prevStop = stops.get(0);
        ColorStop nextStop = stops.get(stops.size() - 1);

        for (int i = 0; i < stops.size() - 1; i++) {
            if (position >= stops.get(i).offset && position <= stops.get(i + 1).offset) {
                prevStop = stops.get(i);
                nextStop = stops.get(i + 1);
                break;
            }
        }

        // Handle edge cases
        if (position < stops.get(0).offset) {
            // Before first stop - use first color
            return new int[]{prevStop.r, prevStop.g, prevStop.b, prevStop.a};
        }

        if (position > stops.get(stops.size() - 1).offset) {
            // After last stop - use last color
            return new int[]{nextStop.r, nextStop.g, nextStop.b, nextStop.a};
        }

        // Interpolate between the two stops
        double range = nextStop.offset - prevStop.offset;
        double t = range == 0 ? 0 : (position - prevStop.offset) / range;

        int r = (int) (prevStop.r + (nextStop.r - prevStop.r) * t);
        int g = (int) (prevStop.g + (nextStop.g - prevStop.g) * t);
        int b = (int) (prevStop.b + (nextStop.b - prevStop.b) * t);
        int a = (int) (prevStop.a + (nextStop.a - prevStop.a) * t);

        return new int[]{r, g, b, a};
    }

    /**
     * Creates a list of angles (in radians) for drawing wedges.
     * Uses 1-degree intervals for smooth gradients.
     */
    public static List<Double> generateWedgeAngles() {
        List<Double> angles = new ArrayList<>();
        int degreesPerStep = 1;
        int totalDegrees = 360;

        for (int i = 0; i < totalDegrees; i += degreesPerStep) {
            angles.add(Math.toRadians(i));
        }

        return angles;
    }
}
