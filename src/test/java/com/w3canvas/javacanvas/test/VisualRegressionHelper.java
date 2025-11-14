package com.w3canvas.javacanvas.test;

import com.w3canvas.javacanvas.interfaces.ICanvasPixelArray;
import com.w3canvas.javacanvas.interfaces.ICanvasRenderingContext2D;
import com.w3canvas.javacanvas.interfaces.IImageData;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Helper class for visual regression testing of canvas rendering.
 *
 * Provides utilities to:
 * - Save canvas output as PNG golden master images
 * - Compare canvas output against golden masters with tolerance for rendering differences
 * - Support headless environment testing
 */
public class VisualRegressionHelper {

    private static final String GOLDEN_MASTERS_DIR = "src/test/resources/golden-masters";
    private static final boolean GENERATE_GOLDEN_MASTERS = Boolean.getBoolean("generateGoldenMasters");

    /**
     * Compare canvas output against a golden master image.
     *
     * In headless environments, rendering may differ slightly from hardware-accelerated
     * environments due to anti-aliasing, font rendering, and algorithm differences.
     * This method allows for perceptual differences while ensuring the overall
     * rendering is correct.
     *
     * @param ctx Canvas rendering context
     * @param testName Name of the test (used for golden master filename)
     * @param maxDiffPercentage Maximum allowed percentage of different pixels (0-100)
     * @param pixelTolerance Color tolerance per channel (0-255)
     * @return true if canvas matches golden master within tolerance
     */
    public static boolean compareToGoldenMaster(ICanvasRenderingContext2D ctx, String testName,
                                                double maxDiffPercentage, int pixelTolerance) {
        try {
            // Get current canvas as BufferedImage
            BufferedImage actual = getCanvasImage(ctx);

            Path goldenMasterPath = Paths.get(GOLDEN_MASTERS_DIR, testName + ".png");

            // If in generation mode, save the golden master and return true
            if (GENERATE_GOLDEN_MASTERS) {
                saveGoldenMaster(actual, goldenMasterPath);
                System.out.println("Generated golden master: " + goldenMasterPath);
                return true;
            }

            // Load golden master
            if (!Files.exists(goldenMasterPath)) {
                System.err.println("Golden master not found: " + goldenMasterPath);
                System.err.println("Run with -DgenerateGoldenMasters=true to create it");
                return false;
            }

            BufferedImage expected = ImageIO.read(goldenMasterPath.toFile());

            // Compare images
            return compareImages(expected, actual, maxDiffPercentage, pixelTolerance);

        } catch (IOException e) {
            System.err.println("Error comparing to golden master: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get canvas content as a BufferedImage
     */
    private static BufferedImage getCanvasImage(ICanvasRenderingContext2D ctx) {
        int width = ctx.getSurface().getWidth();
        int height = ctx.getSurface().getHeight();

        IImageData imageData = ctx.getImageData(0, 0, width, height);
        ICanvasPixelArray pixelArray = imageData.getData();
        int[] data = pixelArray.getPixels(0, 0, width, height);

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int offset = (y * width + x) * 4;
                int r = data[offset] & 0xFF;
                int g = data[offset + 1] & 0xFF;
                int b = data[offset + 2] & 0xFF;
                int a = data[offset + 3] & 0xFF;

                int argb = (a << 24) | (r << 16) | (g << 8) | b;
                image.setRGB(x, y, argb);
            }
        }

        return image;
    }

    /**
     * Save a golden master image
     */
    private static void saveGoldenMaster(BufferedImage image, Path path) throws IOException {
        Files.createDirectories(path.getParent());
        ImageIO.write(image, "PNG", path.toFile());
    }

    /**
     * Compare two images with tolerance
     *
     * @param expected Golden master image
     * @param actual Actual canvas output
     * @param maxDiffPercentage Maximum allowed percentage of different pixels
     * @param pixelTolerance Color tolerance per channel (0-255)
     * @return true if images match within tolerance
     */
    private static boolean compareImages(BufferedImage expected, BufferedImage actual,
                                        double maxDiffPercentage, int pixelTolerance) {
        if (expected.getWidth() != actual.getWidth() || expected.getHeight() != actual.getHeight()) {
            System.err.println("Image size mismatch: expected " + expected.getWidth() + "x" + expected.getHeight() +
                             ", got " + actual.getWidth() + "x" + actual.getHeight());
            return false;
        }

        int width = expected.getWidth();
        int height = expected.getHeight();
        int totalPixels = width * height;
        int differentPixels = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int expectedRGB = expected.getRGB(x, y);
                int actualRGB = actual.getRGB(x, y);

                int expectedR = (expectedRGB >> 16) & 0xFF;
                int expectedG = (expectedRGB >> 8) & 0xFF;
                int expectedB = expectedRGB & 0xFF;
                int expectedA = (expectedRGB >> 24) & 0xFF;

                int actualR = (actualRGB >> 16) & 0xFF;
                int actualG = (actualRGB >> 8) & 0xFF;
                int actualB = actualRGB & 0xFF;
                int actualA = (actualRGB >> 24) & 0xFF;

                // Check if pixel differs beyond tolerance
                if (Math.abs(expectedR - actualR) > pixelTolerance ||
                    Math.abs(expectedG - actualG) > pixelTolerance ||
                    Math.abs(expectedB - actualB) > pixelTolerance ||
                    Math.abs(expectedA - actualA) > pixelTolerance) {
                    differentPixels++;
                }
            }
        }

        double diffPercentage = (differentPixels * 100.0) / totalPixels;

        if (diffPercentage > maxDiffPercentage) {
            System.err.println("Image difference: " + String.format("%.2f", diffPercentage) +
                             "% (max allowed: " + maxDiffPercentage + "%)");
            System.err.println("Different pixels: " + differentPixels + " / " + totalPixels);
            return false;
        }

        return true;
    }

    /**
     * Save canvas output for debugging
     */
    public static void saveDebugImage(ICanvasRenderingContext2D ctx, String name) {
        try {
            BufferedImage image = getCanvasImage(ctx);
            Path debugPath = Paths.get("target/test-output", name + ".png");
            Files.createDirectories(debugPath.getParent());
            ImageIO.write(image, "PNG", debugPath.toFile());
            System.out.println("Saved debug image: " + debugPath);
        } catch (IOException e) {
            System.err.println("Error saving debug image: " + e.getMessage());
        }
    }
}
