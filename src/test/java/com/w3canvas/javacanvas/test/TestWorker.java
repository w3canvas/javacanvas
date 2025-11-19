package com.w3canvas.javacanvas.test;

import com.w3canvas.javacanvas.backend.rhino.impl.node.HTMLCanvasElement;
import com.w3canvas.javacanvas.interfaces.ICanvasRenderingContext2D;
import com.w3canvas.javacanvas.rt.JavaCanvas;
import org.junit.jupiter.api.AfterEach;
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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@ExtendWith(ApplicationExtension.class)
public class TestWorker extends ApplicationTest {

    private JavaCanvas javaCanvas;
    private ICanvasRenderingContext2D ctx;
    private HTMLCanvasElement canvas;

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

        Scriptable scope = javaCanvas.getRhinoRuntime().getScope();
        try {
            canvas = (HTMLCanvasElement) javaCanvas.getDocument().jsFunction_createElement("canvas");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        canvas.jsSet_id("canvas");
        // addElement is called by jsSet_id, so this is redundant, but safe.
        javaCanvas.getDocument().addElement("canvas", canvas);
        ScriptableObject.putProperty(scope, "canvas", canvas);
        ctx = (ICanvasRenderingContext2D) canvas.jsFunction_getContext("2d");
    }

    @AfterEach
    public void tearDown() {
        Context.exit();
    }

    /**
     * Waits for a JavaScript global variable to be set to true, with timeout.
     * Uses polling with exponential backoff for better performance.
     *
     * @param varName The name of the global variable to wait for
     * @param timeoutMs Maximum time to wait in milliseconds
     * @throws TimeoutException if the variable is not set within the timeout period
     * @throws InterruptedException if the wait is interrupted
     */
    private void waitForJSFlag(String varName, long timeoutMs) throws TimeoutException, InterruptedException {
        long startTime = System.currentTimeMillis();
        long sleepTime = 10; // Start with 10ms

        while (System.currentTimeMillis() - startTime < timeoutMs) {
            Scriptable scope = javaCanvas.getRhinoRuntime().getScope();
            Object value = scope.get(varName, scope);

            if (value != Scriptable.NOT_FOUND && value instanceof Boolean && (Boolean) value) {
                return; // Success!
            }

            Thread.sleep(sleepTime);
            sleepTime = Math.min(sleepTime * 2, 100); // Exponential backoff, max 100ms
        }

        fail("Timeout waiting for JavaScript flag '" + varName + "' after " + timeoutMs + "ms");
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

    /**
     * Tests Worker API by creating a worker that generates a green square ImageData
     * and sends it back to the main thread. Verifies that:
     * 1. Worker can execute JavaScript code in a separate thread
     * 2. Worker can create an OffscreenCanvas and draw to it
     * 3. ImageData can be posted back from worker to main thread
     * 4. Main thread can receive and draw the ImageData to canvas
     */
    @Test
    public void testWorkerDrawing() throws ExecutionException, InterruptedException, TimeoutException {
        interact(() -> ctx.clearRect(0, 0, 400, 400));

        javaCanvas.executeScript("test/test-worker-main.js");

        // Wait for the worker to finish and the main thread to draw the image.
        // The test script will set 'workerComplete' flag when done.
        waitForJSFlag("workerComplete", 5000);

        // The worker creates a 100x100 green square and posts it back.
        // The main thread draws it at (0,0).
        assertPixel(50, 50, 0, 128, 0, 255); // Green
    }
}
