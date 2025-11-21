package com.w3canvas.javacanvas.test;

import com.w3canvas.javacanvas.backend.awt.AwtCanvasSurface;
import com.w3canvas.javacanvas.backend.awt.AwtGraphicsContext;
import com.w3canvas.javacanvas.backend.awt.AwtPaint;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestAwtStrokeWithFilter {

    @Test
    public void testStrokeWithBlurFilter() {
        int width = 200;
        int height = 200;
        AwtCanvasSurface surface = new AwtCanvasSurface(width, height);
        BufferedImage bufferedImage = (BufferedImage) surface.getNativeImage();
        AwtGraphicsContext context = new AwtGraphicsContext(bufferedImage.createGraphics(), surface);

        context.setFilter("blur(5px)");
        context.setStrokePaint(new AwtPaint(Color.RED));
        context.setLineWidth(10);

        context.beginPath();
        context.moveTo(100, 50);
        context.lineTo(100, 150);
        context.stroke();

        BufferedImage image = (BufferedImage) surface.getNativeImage();

        // Check center of the line (should be red)
        int centerPixel = image.getRGB(100, 100);
        int red = (centerPixel >> 16) & 0xff;
        int alpha = (centerPixel >> 24) & 0xff;

        assertTrue(alpha > 0, "Center pixel should not be transparent");
        assertTrue(red > 50, "Center pixel should be red-ish");

        // Check pixel outside the line width but within blur radius
        int blurredPixel = image.getRGB(106, 100);
        int blurredRed = (blurredPixel >> 16) & 0xff;
        int blurredAlpha = (blurredPixel >> 24) & 0xff;

        assertTrue(blurredAlpha > 0, "Blurred pixel should not be transparent (actual alpha: " + blurredAlpha + ")");
        assertTrue(blurredRed > 0, "Blurred pixel should have some red");
    }
}
