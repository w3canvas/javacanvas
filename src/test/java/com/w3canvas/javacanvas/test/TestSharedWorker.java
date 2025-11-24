package com.w3canvas.javacanvas.test;

import com.w3canvas.javacanvas.backend.rhino.impl.node.HTMLCanvasElement;
import com.w3canvas.javacanvas.interfaces.ICanvasRenderingContext2D;
import com.w3canvas.javacanvas.js.worker.SharedWorker;
import com.w3canvas.javacanvas.rt.JavaCanvas;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Test suite for SharedWorker and MessagePort API.
 * Tests shared worker creation, MessagePort communication, and multi-connection
 * scenarios.
 */
@ExtendWith(ApplicationExtension.class)
@Timeout(value = 60, unit = TimeUnit.SECONDS)
public class TestSharedWorker extends ApplicationTest {

    private JavaCanvas javaCanvas;
    private ICanvasRenderingContext2D ctx;
    private HTMLCanvasElement canvas;

    @BeforeAll
    public static void warmUp() {
        try {
            javafx.application.Platform.startup(() -> {
            });
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

        Scriptable scope = (Scriptable) javaCanvas.getRuntime().getScope();

        // Set documentBase to resolve worker script paths
        try {
            java.io.File baseDir = new java.io.File(basePath).getAbsoluteFile();
            String documentBase = baseDir.toURI().toString();
            scope.put("documentBase", scope, documentBase);
        } catch (Exception e) {
            e.printStackTrace();
        }

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
        // Clean up all shared workers
        SharedWorker.terminateAll();
        Context.exit();
    }

    /**
     * Waits for a JavaScript global variable to be set to true, with timeout.
     * Uses polling with exponential backoff for better performance.
     *
     * @param varName   The name of the global variable to wait for
     * @param timeoutMs Maximum time to wait in milliseconds
     * @throws TimeoutException     if the variable is not set within the timeout
     *                              period
     * @throws InterruptedException if the wait is interrupted
     */
    private void waitForJSFlag(String varName, long timeoutMs) throws TimeoutException, InterruptedException {
        long startTime = System.currentTimeMillis();
        long sleepTime = 10; // Start with 10ms

        while (System.currentTimeMillis() - startTime < timeoutMs) {
            Scriptable scope = (Scriptable) javaCanvas.getRuntime().getScope();
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
     * Tests basic SharedWorker communication by creating a shared worker and
     * verifying
     * that messages can be sent and received through MessagePort. Verifies:
     * 1. SharedWorker can be created and initialized
     * 2. MessagePort communication is functional
     * 3. Worker remains active after message exchange
     */
    @Test
    public void testSharedWorkerBasicCommunication() throws InterruptedException, TimeoutException {
        interact(() -> ctx.clearRect(0, 0, 400, 400));

        javaCanvas.executeScript("test/test-sharedworker-main.js");

        // Wait for the shared worker to process and return the image data
        // The test script will set 'sharedWorkerComplete' flag when done
        waitForJSFlag("sharedWorkerComplete", 5000);

        // The shared worker should return a counter value and draw it
        // Verify that we got a response (implementation specific to test script)
        assertTrue(SharedWorker.getActiveWorkerCount() > 0, "SharedWorker should be active");
    }

    /**
     * Tests SharedWorker behavior with multiple connections to the same worker.
     * Verifies that:
     * 1. Multiple connections can be created to the same SharedWorker
     * 2. Only one SharedWorker instance is created (shared state)
     * 3. Each connection has its own MessagePort
     * 4. Messages are properly routed to the correct ports
     */
    @Test
    public void testSharedWorkerMultipleConnections() throws InterruptedException, TimeoutException {
        interact(() -> ctx.clearRect(0, 0, 400, 400));

        // Create two connections to the same shared worker
        String script = "var worker1 = new SharedWorker('test-sharedworker.js');" +
                "var worker2 = new SharedWorker('test-sharedworker.js');" +
                "var received1 = false;" +
                "var received2 = false;" +
                "var multiConnectionComplete = false;" +
                "" +
                "worker1.port.onmessage = function(e) {" +
                "  console.log('Worker 1 received:', e.data);" +
                "  received1 = true;" +
                "  if (received2) multiConnectionComplete = true;" +
                "};" +
                "" +
                "worker2.port.onmessage = function(e) {" +
                "  console.log('Worker 2 received:', e.data);" +
                "  received2 = true;" +
                "  if (received1) multiConnectionComplete = true;" +
                "};" +
                "" +
                "worker1.port.postMessage('hello from connection 1');" +
                "worker2.port.postMessage('hello from connection 2');";

        javaCanvas.executeCode(script);

        // Wait for messages to be processed
        waitForJSFlag("multiConnectionComplete", 5000);

        // Should only have one shared worker instance (shared between connections)
        assertEquals(1, SharedWorker.getActiveWorkerCount(),
                "Should have exactly one SharedWorker instance for multiple connections");
    }

    /**
     * Tests SharedWorker's ability to transfer ImageBitmap objects between
     * contexts.
     * Verifies that:
     * 1. SharedWorker can create an OffscreenCanvas and draw to it
     * 2. ImageBitmap can be created from OffscreenCanvas
     * 3. ImageBitmap can be transferred through MessagePort
     * 4. Main thread can draw received ImageBitmap to canvas
     */
    @Test
    public void testSharedWorkerWithImageBitmap() throws ExecutionException, InterruptedException, TimeoutException {
        interact(() -> ctx.clearRect(0, 0, 400, 400));

        String script = "var worker = new SharedWorker('test-sharedworker-imagebitmap.js');" +
                "var imageBitmapComplete = false;" +
                "" +
                "worker.port.onmessage = function(e) {" +
                "  var imageBitmap = e.data;" +
                "  ctx.drawImage(imageBitmap, 0, 0);" +
                "  console.log('ImageBitmap received and drawn');" +
                "  imageBitmapComplete = true;" +
                "};" +
                "" +
                "worker.port.postMessage({command: 'create', width: 100, height: 100, color: 'blue'});";

        interact(() -> javaCanvas.executeCode(script));

        // Wait for the worker to create and return ImageBitmap
        waitForJSFlag("imageBitmapComplete", 5000);

        // Verify blue pixel was drawn
        assertPixel(50, 50, 0, 0, 255, 255);
    }

    /**
     * Tests direct MessagePort communication without relying on implicit message
     * passing.
     * Verifies that:
     * 1. MessagePort can be accessed from SharedWorker
     * 2. Messages can be posted directly through port
     * 3. Messages are received and processed correctly
     * 4. Port maintains bidirectional communication
     */
    @Test
    public void testMessagePortCommunication() throws InterruptedException, TimeoutException {
        String script = "var worker = new SharedWorker('test-sharedworker.js');" +
                "var port = worker.port;" +
                "var messageReceived = false;" +
                "" +
                "port.onmessage = function(e) {" +
                "  messageReceived = true;" +
                "  console.log('Received:', e.data);" +
                "};" +
                "" +
                "port.postMessage('test message');";

        javaCanvas.executeCode(script);

        // Wait for message processing using flag
        waitForJSFlag("messageReceived", 5000);

        Scriptable scope = (Scriptable) javaCanvas.getRuntime().getScope();
        Object messageReceivedObj = scope.get("messageReceived", scope);

        // Handle Rhino's NOT_FOUND tag when property doesn't exist
        Boolean messageReceived = false;
        if (messageReceivedObj != Scriptable.NOT_FOUND && messageReceivedObj instanceof Boolean) {
            messageReceived = (Boolean) messageReceivedObj;
        }

        // The worker should echo back the message
        assertTrue(messageReceived,
                "Should receive message through MessagePort");
    }

    /**
     * Tests SharedWorker termination functionality. Verifies that:
     * 1. Workers can be properly terminated
     * 2. Active worker count is tracked correctly
     * 3. terminateAll() properly cleans up all worker instances
     * 4. No workers remain active after termination
     */
    @Test
    public void testSharedWorkerTermination() throws InterruptedException, TimeoutException {
        String script = "var worker = new SharedWorker('test-sharedworker.js');" +
                "var workerStarted = false;" +
                "worker.port.onmessage = function(e) {" +
                "  workerStarted = true;" +
                "};" +
                "worker.port.postMessage('ping');";

        javaCanvas.executeCode(script);

        // Wait for worker to start and respond
        waitForJSFlag("workerStarted", 5000);

        assertTrue(SharedWorker.getActiveWorkerCount() > 0,
                "Worker should be active before termination");

        // Terminate all workers
        SharedWorker.terminateAll();

        assertEquals(0, SharedWorker.getActiveWorkerCount(),
                "No workers should be active after termination");
    }
}
