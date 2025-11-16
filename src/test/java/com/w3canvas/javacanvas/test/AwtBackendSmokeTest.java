package com.w3canvas.javacanvas.test;

import com.w3canvas.javacanvas.backend.awt.AwtCanvasSurface;
import com.w3canvas.javacanvas.backend.awt.AwtGraphicsContext;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AwtBackendSmokeTest {

    @Test
    public void testAwtBackendInitialization() {
        AwtCanvasSurface surface = new AwtCanvasSurface(100, 100);
        assertNotNull(surface);
        BufferedImage image = (BufferedImage) surface.getNativeImage();
        assertNotNull(image);
        AwtGraphicsContext context = new AwtGraphicsContext(image.createGraphics(), surface);
        assertNotNull(context);
    }

    @Test
    public void testFillTextAwt() {
        AwtCanvasSurface surface = new AwtCanvasSurface(100, 100);
        BufferedImage image = (BufferedImage) surface.getNativeImage();
        AwtGraphicsContext context = new AwtGraphicsContext(image.createGraphics(), surface);

        context.setFillPaint(new com.w3canvas.javacanvas.backend.awt.AwtPaint(java.awt.Color.BLUE));
        context.fillText("Hello", 10, 50, 0);

        // Check that some pixels in the text area contain blue color
        // Text rendering may have anti-aliasing, so we check for bluish pixels
        boolean foundBluePixel = false;
        for (int x = 10; x < 60; x++) {
            for (int y = 35; y < 55; y++) {
                int pixel = image.getRGB(x, y);
                int blue = pixel & 0xff;
                int alpha = (pixel >> 24) & 0xff;
                // If we find a pixel with significant blue component and some alpha, consider it a match
                if (blue > 100 && alpha > 100) {
                    foundBluePixel = true;
                    break;
                }
            }
            if (foundBluePixel) break;
        }
        assertEquals(true, foundBluePixel, "Should find blue pixels in the text rendering area");
    }
}
