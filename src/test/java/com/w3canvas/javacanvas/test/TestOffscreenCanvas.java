package com.w3canvas.javacanvas.test;

import com.w3canvas.javacanvas.rt.RhinoRuntime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptableObject;

import java.io.FileReader;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for OffscreenCanvas API implementation.
 * Tests the complete OffscreenCanvas functionality including:
 * - Creation and initialization
 * - 2D rendering context
 * - convertToBlob() method
 * - transferToImageBitmap() method
 * - Width/height getters and setters
 */
public class TestOffscreenCanvas {

    private RhinoRuntime runtime;

    @BeforeEach
    public void setUp() {
        System.setProperty("w3canvas.backend", "awt");
        runtime = new RhinoRuntime();

        Context rhinoContext = Context.enter();
        try {
            // Note: OffscreenCanvas, Blob, ImageBitmap, ImageData, and CanvasRenderingContext2D
            // are already registered by RhinoRuntime
            ScriptableObject.putProperty(runtime.getScope(), "console",
                new com.w3canvas.javacanvas.utils.ScriptLogger());
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize test environment", e);
        }
    }

    @AfterEach
    public void tearDown() {
        Context.exit();
    }

    @Test
    public void testOffscreenCanvasCreation() {
        String script =
            "var canvas = new OffscreenCanvas(640, 480);" +
            "var width = canvas.width;" +
            "var height = canvas.height;" +
            "width === 640 && height === 480;";

        Object result = runtime.exec(script);
        assertTrue((Boolean) result, "OffscreenCanvas should be created with correct dimensions");
    }

    @Test
    public void testOffscreenCanvasGetContext() {
        String script =
            "var canvas = new OffscreenCanvas(300, 200);" +
            "var ctx = canvas.getContext('2d');" +
            "ctx !== null && ctx !== undefined;";

        Object result = runtime.exec(script);
        assertTrue((Boolean) result, "getContext('2d') should return a valid context");
    }

    @Test
    public void testOffscreenCanvasDrawing() {
        String script =
            "var canvas = new OffscreenCanvas(100, 100);" +
            "var ctx = canvas.getContext('2d');" +
            "ctx.fillStyle = '#FF0000';" +
            "ctx.fillRect(0, 0, 50, 50);" +
            "ctx.fillStyle = '#00FF00';" +
            "ctx.fillRect(50, 50, 50, 50);" +
            "var imageData = ctx.getImageData(25, 25, 1, 1);" +
            "imageData.width === 1 && imageData.height === 1;";

        Object result = runtime.exec(script);
        assertTrue((Boolean) result, "Should be able to draw to OffscreenCanvas and read pixel data");
    }

    @Test
    public void testConvertToBlobSync() {
        String script =
            "var canvas = new OffscreenCanvas(200, 200);" +
            "var ctx = canvas.getContext('2d');" +
            "ctx.fillStyle = '#0000FF';" +
            "ctx.fillRect(0, 0, 200, 200);" +
            "var blob = canvas.convertToBlobSync('image/png');" +
            "blob !== null && blob.type === 'image/png' && blob.size > 0;";

        Object result = runtime.exec(script);
        assertTrue((Boolean) result, "convertToBlobSync() should create a valid Blob");
    }

    @Test
    public void testConvertToBlobSyncDefaultType() {
        String script =
            "var canvas = new OffscreenCanvas(100, 100);" +
            "var ctx = canvas.getContext('2d');" +
            "ctx.fillRect(0, 0, 100, 100);" +
            "var blob = canvas.convertToBlobSync();" +
            "blob !== null && blob.type === 'image/png';";

        Object result = runtime.exec(script);
        assertTrue((Boolean) result, "convertToBlobSync() should default to image/png");
    }

    @Test
    public void testTransferToImageBitmap() {
        String script =
            "var canvas = new OffscreenCanvas(150, 100);" +
            "var ctx = canvas.getContext('2d');" +
            "ctx.fillStyle = '#FFFF00';" +
            "ctx.fillRect(0, 0, 150, 100);" +
            "var imageBitmap = canvas.transferToImageBitmap();" +
            "imageBitmap !== null && imageBitmap.width === 150 && imageBitmap.height === 100;";

        Object result = runtime.exec(script);
        assertTrue((Boolean) result, "transferToImageBitmap() should create a valid ImageBitmap");
    }

    @Test
    public void testImageBitmapClose() {
        String script =
            "var canvas = new OffscreenCanvas(100, 100);" +
            "var ctx = canvas.getContext('2d');" +
            "ctx.fillRect(0, 0, 100, 100);" +
            "var imageBitmap = canvas.transferToImageBitmap();" +
            "var widthBefore = imageBitmap.width;" +
            "imageBitmap.close();" +
            "var widthAfter = imageBitmap.width;" +
            "widthBefore === 100 && widthAfter === 0;";

        Object result = runtime.exec(script);
        assertTrue((Boolean) result, "ImageBitmap.close() should set width/height to 0");
    }

    @Test
    public void testWidthHeightSetters() {
        String script =
            "var canvas = new OffscreenCanvas(100, 100);" +
            "canvas.width = 300;" +
            "canvas.height = 200;" +
            "canvas.width === 300 && canvas.height === 200;";

        Object result = runtime.exec(script);
        assertTrue((Boolean) result, "Width and height setters should work correctly");
    }

    @Test
    public void testResizeClearsCanvas() {
        String script =
            "var canvas = new OffscreenCanvas(100, 100);" +
            "var ctx = canvas.getContext('2d');" +
            "ctx.fillStyle = '#FF0000';" +
            "ctx.fillRect(0, 0, 100, 100);" +
            "canvas.width = 200;" +  // Resizing should clear the canvas
            "ctx = canvas.getContext('2d');" +
            "ctx.fillRect(0, 0, 200, 200);" +  // Should be able to draw after resize
            "canvas.width === 200 && canvas.height === 100;";

        Object result = runtime.exec(script);
        assertTrue((Boolean) result, "Resizing canvas should allow continued drawing");
    }

    @Test
    public void testCompleteAPIFromScript() {
        try {
            FileReader reader = new FileReader("test/test-offscreencanvas-api.js");
            runtime.exec(reader, "test-offscreencanvas-api.js");
            // If the script executes without exceptions, the test passes
            assertTrue(true, "Complete API test script should execute successfully");
        } catch (IOException e) {
            fail("Failed to load test script: " + e.getMessage());
        }
    }
}
