package com.w3canvas.javacanvas.test;

import com.w3canvas.javacanvas.backend.rhino.impl.node.HTMLCanvasElement;
import com.w3canvas.javacanvas.backend.rhino.impl.node.ImageBitmap;
import com.w3canvas.javacanvas.backend.rhino.impl.node.ImageData;
import com.w3canvas.javacanvas.backend.rhino.impl.node.Blob;
import com.w3canvas.javacanvas.interfaces.ICanvasRenderingContext2D;
import com.w3canvas.javacanvas.js.worker.OffscreenCanvas;
import com.w3canvas.javacanvas.rt.JavaCanvas;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.framework.junit5.ApplicationTest;

import javafx.stage.Stage;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for ImageBitmap API.
 * Tests createImageBitmap() global function and ImageBitmap operations.
 */
@ExtendWith(ApplicationExtension.class)
@org.junit.jupiter.api.Timeout(value = 60, unit = java.util.concurrent.TimeUnit.SECONDS)
public class TestImageBitmap extends ApplicationTest {

    private JavaCanvas javaCanvas;
    private ICanvasRenderingContext2D ctx;
    private HTMLCanvasElement canvas;
    private Scriptable scope;

    @BeforeAll
    public static void warmUp() {
        try {
            javafx.application.Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Platform already started
        }
    }

    @Start
    public void start(Stage stage) {
    }

    @BeforeEach
    public void setUp() {
        System.setProperty("w3canvas.backend", "awt");

        String basePath = ".";
        javaCanvas = new JavaCanvas(basePath, true);
        javaCanvas.initializeBackend();

        Context.enter();

        scope = javaCanvas.getRhinoRuntime().getScope();
        try {
            canvas = (HTMLCanvasElement) javaCanvas.getDocument().jsFunction_createElement("canvas");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        canvas.jsSet_id("canvas");
        canvas.jsSet_width(400);
        canvas.jsSet_height(400);
        javaCanvas.getDocument().addElement("canvas", canvas);
        ScriptableObject.putProperty(scope, "canvas", canvas);
        ctx = (ICanvasRenderingContext2D) canvas.jsFunction_getContext("2d");
        ScriptableObject.putProperty(scope, "ctx", ctx);
    }

    @AfterEach
    public void tearDown() {
        Context.exit();
    }

    private void assertPixel(int x, int y, int r, int g, int b, int a) throws ExecutionException, InterruptedException {
        CompletableFuture<int[]> future = new CompletableFuture<>();
        interact(() -> {
            future.complete(ctx.getSurface().getPixelData(x, y, 1, 1));
        });
        int[] pixelData = future.get();
        int pixel = pixelData[0];

        int actualA = (pixel >> 24) & 0xff;
        int actualR = (pixel >> 16) & 0xff;
        int actualG = (pixel >> 8) & 0xff;
        int actualB = pixel & 0xff;

        assertEquals(r, actualR, "Red component mismatch at (" + x + "," + y + ")");
        assertEquals(g, actualG, "Green component mismatch at (" + x + "," + y + ")");
        assertEquals(b, actualB, "Blue component mismatch at (" + x + "," + y + ")");
        assertEquals(a, actualA, "Alpha component mismatch at (" + x + "," + y + ")");
    }

    @Test
    public void testCreateImageBitmapFromCanvas() throws ExecutionException, InterruptedException {
        // Draw a red rectangle on canvas
        interact(() -> {
            ctx.setFillStyle("red");
            ctx.fillRect(0, 0, 50, 50);
        });

        // Create ImageBitmap from canvas using global function
        String script = "var imageBitmap = createImageBitmap(canvas);";
        javaCanvas.executeCode(script);

        ImageBitmap imageBitmap = (ImageBitmap) scope.get("imageBitmap", scope);
        assertNotNull(imageBitmap, "ImageBitmap should be created");
        assertEquals(400, imageBitmap.jsGet_width(), "Width should match canvas");
        assertEquals(400, imageBitmap.jsGet_height(), "Height should match canvas");
        assertFalse(imageBitmap.isClosed(), "ImageBitmap should not be closed");
    }

    @Test
    public void testCreateImageBitmapFromOffscreenCanvas() {
        String script =
            "var offscreen = new OffscreenCanvas(100, 100);" +
            "var offscreenCtx = offscreen.getContext('2d');" +
            "offscreenCtx.fillStyle = 'blue';" +
            "offscreenCtx.fillRect(0, 0, 100, 100);" +
            "var imageBitmap = createImageBitmap(offscreen);";

        javaCanvas.executeCode(script);

        ImageBitmap imageBitmap = (ImageBitmap) scope.get("imageBitmap", scope);
        assertNotNull(imageBitmap, "ImageBitmap should be created from OffscreenCanvas");
        assertEquals(100, imageBitmap.jsGet_width());
        assertEquals(100, imageBitmap.jsGet_height());
    }

    @Test
    public void testCreateImageBitmapFromImageData() {
        String script =
            "var imageData = ctx.createImageData(50, 50);" +
            "for (var i = 0; i < imageData.data.length; i += 4) {" +
            "  imageData.data[i] = 255;" +     // Red
            "  imageData.data[i+1] = 0;" +     // Green
            "  imageData.data[i+2] = 0;" +     // Blue
            "  imageData.data[i+3] = 255;" +   // Alpha
            "}" +
            "var imageBitmap = createImageBitmap(imageData);";

        javaCanvas.executeCode(script);

        ImageBitmap imageBitmap = (ImageBitmap) scope.get("imageBitmap", scope);
        assertNotNull(imageBitmap, "ImageBitmap should be created from ImageData");
        assertEquals(50, imageBitmap.jsGet_width());
        assertEquals(50, imageBitmap.jsGet_height());
    }

    @Test
    public void testCreateImageBitmapCopy() {
        String script =
            "var offscreen = new OffscreenCanvas(60, 60);" +
            "var imageBitmap1 = createImageBitmap(offscreen);" +
            "var imageBitmap2 = createImageBitmap(imageBitmap1);";

        javaCanvas.executeCode(script);

        ImageBitmap imageBitmap1 = (ImageBitmap) scope.get("imageBitmap1", scope);
        ImageBitmap imageBitmap2 = (ImageBitmap) scope.get("imageBitmap2", scope);

        assertNotNull(imageBitmap1, "Original ImageBitmap should exist");
        assertNotNull(imageBitmap2, "Copied ImageBitmap should exist");
        assertEquals(imageBitmap1.jsGet_width(), imageBitmap2.jsGet_width());
        assertEquals(imageBitmap1.jsGet_height(), imageBitmap2.jsGet_height());
        assertNotSame(imageBitmap1, imageBitmap2, "Should be different objects");
    }

    @Test
    public void testImageBitmapClose() {
        String script =
            "var offscreen = new OffscreenCanvas(50, 50);" +
            "var imageBitmap = createImageBitmap(offscreen);" +
            "imageBitmap.close();";

        javaCanvas.executeCode(script);

        ImageBitmap imageBitmap = (ImageBitmap) scope.get("imageBitmap", scope);
        assertTrue(imageBitmap.isClosed(), "ImageBitmap should be closed");
        assertEquals(0, imageBitmap.jsGet_width(), "Width should be 0 when closed");
        assertEquals(0, imageBitmap.jsGet_height(), "Height should be 0 when closed");
    }

    @Test
    public void testDrawImageWithImageBitmap() throws ExecutionException, InterruptedException {
        // Create an OffscreenCanvas with green fill
        String script =
            "var offscreen = new OffscreenCanvas(100, 100);" +
            "var offscreenCtx = offscreen.getContext('2d');" +
            "offscreenCtx.fillStyle = '#00FF00';" +  // Green
            "offscreenCtx.fillRect(0, 0, 100, 100);" +
            "var imageBitmap = createImageBitmap(offscreen);" +
            "ctx.clearRect(0, 0, 400, 400);" +
            "ctx.drawImage(imageBitmap, 10, 10);";

        interact(() -> javaCanvas.executeCode(script));

        // Verify green pixel was drawn
        assertPixel(50, 50, 0, 255, 0, 255);
    }

    @Test
    public void testOffscreenCanvasTransferToImageBitmap() {
        String script =
            "var offscreen = new OffscreenCanvas(80, 80);" +
            "var offscreenCtx = offscreen.getContext('2d');" +
            "offscreenCtx.fillStyle = 'red';" +
            "offscreenCtx.fillRect(0, 0, 80, 80);" +
            "var imageBitmap = offscreen.transferToImageBitmap();";

        javaCanvas.executeCode(script);

        ImageBitmap imageBitmap = (ImageBitmap) scope.get("imageBitmap", scope);
        assertNotNull(imageBitmap, "ImageBitmap should be created from transferToImageBitmap");
        assertEquals(80, imageBitmap.jsGet_width());
        assertEquals(80, imageBitmap.jsGet_height());
    }

    @Test
    public void testOffscreenCanvasConvertToBlob() {
        String script =
            "var offscreen = new OffscreenCanvas(100, 100);" +
            "var offscreenCtx = offscreen.getContext('2d');" +
            "offscreenCtx.fillStyle = 'blue';" +
            "offscreenCtx.fillRect(0, 0, 100, 100);" +
            "var blob = offscreen.convertToBlobSync('image/png');";

        javaCanvas.executeCode(script);

        Blob blob = (Blob) scope.get("blob", scope);
        assertNotNull(blob, "Blob should be created");
        assertTrue(blob.jsGet_size() > 0, "Blob should have data");
        assertEquals("image/png", blob.jsGet_type(), "Blob type should be image/png");
    }

    @Test
    public void testCreateImageBitmapFromBlob() {
        // First create a blob from canvas
        String script =
            "var offscreen = new OffscreenCanvas(100, 100);" +
            "var offscreenCtx = offscreen.getContext('2d');" +
            "offscreenCtx.fillStyle = 'red';" +
            "offscreenCtx.fillRect(0, 0, 100, 100);" +
            "var blob = offscreen.convertToBlobSync('image/png');" +
            "var imageBitmap = createImageBitmap(blob);";

        javaCanvas.executeCode(script);

        ImageBitmap imageBitmap = (ImageBitmap) scope.get("imageBitmap", scope);
        assertNotNull(imageBitmap, "ImageBitmap should be created from Blob");
        assertEquals(100, imageBitmap.jsGet_width());
        assertEquals(100, imageBitmap.jsGet_height());
    }

    @Test
    public void testImageBitmapWithDrawImageVariants() throws ExecutionException, InterruptedException {
        // Test all drawImage() signatures with ImageBitmap
        String script =
            "var offscreen = new OffscreenCanvas(100, 100);" +
            "var offscreenCtx = offscreen.getContext('2d');" +
            "offscreenCtx.fillStyle = '#FF0000';" +  // Red
            "offscreenCtx.fillRect(0, 0, 100, 100);" +
            "var imageBitmap = createImageBitmap(offscreen);" +
            "ctx.clearRect(0, 0, 400, 400);" +

            // Test drawImage(image, dx, dy)
            "ctx.drawImage(imageBitmap, 0, 0);" +

            // Test drawImage(image, dx, dy, dWidth, dHeight) - scaled
            "ctx.drawImage(imageBitmap, 150, 0, 50, 50);" +

            // Test drawImage(image, sx, sy, sWidth, sHeight, dx, dy, dWidth, dHeight)
            "ctx.drawImage(imageBitmap, 25, 25, 50, 50, 250, 0, 50, 50);";

        interact(() -> javaCanvas.executeCode(script));

        // Verify pixels at different locations
        assertPixel(50, 50, 255, 0, 0, 255);    // First draw (full size)
        assertPixel(175, 25, 255, 0, 0, 255);   // Second draw (scaled)
        assertPixel(275, 25, 255, 0, 0, 255);   // Third draw (cropped and scaled)
    }

    @Test
    public void testCreateImageBitmapErrorHandling() {
        // Test error when creating from closed ImageBitmap
        String script =
            "var offscreen = new OffscreenCanvas(50, 50);" +
            "var imageBitmap1 = createImageBitmap(offscreen);" +
            "imageBitmap1.close();" +
            "var errorThrown = false;" +
            "try {" +
            "  var imageBitmap2 = createImageBitmap(imageBitmap1);" +
            "} catch (e) {" +
            "  errorThrown = true;" +
            "}";

        javaCanvas.executeCode(script);

        Boolean errorThrown = (Boolean) scope.get("errorThrown", scope);
        assertTrue(errorThrown, "Should throw error when creating from closed ImageBitmap");
    }
}
