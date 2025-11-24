package com.w3canvas.javacanvas.test;

import com.w3canvas.javacanvas.backend.awt.AwtCanvasSurface;
import com.w3canvas.javacanvas.backend.awt.AwtGraphicsBackend;
import com.w3canvas.javacanvas.core.CoreCanvasRenderingContext2D;
import com.w3canvas.javacanvas.rt.GraalRuntime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Disabled("GraalJS requires additional classpath configuration - see build.gradle")
public class TestGraal {

    private GraalRuntime runtime;

    @BeforeEach
    public void setUp() {
        runtime = new GraalRuntime();
    }

    @AfterEach
    public void tearDown() {
        if (runtime != null) {
            runtime.close();
        }
    }

    @Test
    public void testGraalCanvas() {
        // Setup Core Context with AWT backend
        AwtGraphicsBackend backend = new AwtGraphicsBackend();
        CoreCanvasRenderingContext2D ctx = new CoreCanvasRenderingContext2D(null, backend, 200, 200);

        runtime.putProperty("ctx", ctx);

        // Run script
        // GraalJS maps explicit setters cleanly
        runtime.exec("ctx.setFillStyle('red');");
        runtime.exec("ctx.fillRect(10.0, 10.0, 50.0, 50.0);");

        // Verify result (check pixel)
        AwtCanvasSurface surface = (AwtCanvasSurface) ctx.getSurface();
        BufferedImage image = (BufferedImage) surface.getNativeImage();
        int pixel = image.getRGB(35, 35);
        int red = (pixel >> 16) & 0xff;

        assertTrue(red > 200, "Pixel should be red");
    }
}
