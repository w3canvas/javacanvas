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

        int pixel = image.getRGB(15, 45);
        assertEquals(0xff0000ff, pixel); // Blue
    }
}
