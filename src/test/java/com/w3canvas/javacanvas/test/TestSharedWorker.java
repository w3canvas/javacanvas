package com.w3canvas.javacanvas.test;

import com.w3canvas.javacanvas.backend.rhino.impl.node.HTMLCanvasElement;
import com.w3canvas.javacanvas.interfaces.ICanvasRenderingContext2D;
import com.w3canvas.javacanvas.js.worker.SharedWorker;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test suite for SharedWorker and MessagePort API.
 * Tests shared worker creation, MessagePort communication, and multi-connection scenarios.
 */
@ExtendWith(ApplicationExtension.class)
public class TestSharedWorker extends ApplicationTest {

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
        javaCanvas.getDocument().addElement("canvas", canvas);
        ScriptableObject.putProperty(scope, "canvas", canvas);
        ctx = (ICanvasRenderingContext2D) canvas.jsFunction_getContext("2d");
    }

    @AfterEach
    public void tearDown() {
        // Clean up all shared workers
        SharedWorker.terminateAll();
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
    public void testSharedWorkerBasicCommunication() throws InterruptedException, TimeoutException {
        interact(() -> ctx.clearRect(0, 0, 400, 400));

        javaCanvas.executeScript("test/test-sharedworker-main.js");

        // Wait for the shared worker to process and return the image data
        sleep(2, TimeUnit.SECONDS);

        // The shared worker should return a counter value and draw it
        // Verify that we got a response (implementation specific to test script)
        assertTrue(SharedWorker.getActiveWorkerCount() > 0, "SharedWorker should be active");
    }

    @Test
    public void testSharedWorkerMultipleConnections() throws InterruptedException, TimeoutException {
        interact(() -> ctx.clearRect(0, 0, 400, 400));

        // Create two connections to the same shared worker
        String script =
            "var worker1 = new SharedWorker('test-sharedworker.js');" +
            "var worker2 = new SharedWorker('test-sharedworker.js');" +
            "var received1 = false;" +
            "var received2 = false;" +
            "" +
            "worker1.port.onmessage = function(e) {" +
            "  console.log('Worker 1 received:', e.data);" +
            "  received1 = true;" +
            "};" +
            "" +
            "worker2.port.onmessage = function(e) {" +
            "  console.log('Worker 2 received:', e.data);" +
            "  received2 = true;" +
            "};" +
            "" +
            "worker1.port.postMessage('hello from connection 1');" +
            "worker2.port.postMessage('hello from connection 2');";

        javaCanvas.executeScript(script);

        // Wait for messages to be processed
        sleep(2, TimeUnit.SECONDS);

        // Should only have one shared worker instance (shared between connections)
        assertEquals(1, SharedWorker.getActiveWorkerCount(),
            "Should have exactly one SharedWorker instance for multiple connections");
    }

    @Test
    public void testSharedWorkerWithImageBitmap() throws ExecutionException, InterruptedException, TimeoutException {
        interact(() -> ctx.clearRect(0, 0, 400, 400));

        String script =
            "var worker = new SharedWorker('test-sharedworker-imagebitmap.js');" +
            "" +
            "worker.port.onmessage = function(e) {" +
            "  var imageBitmap = e.data;" +
            "  ctx.drawImage(imageBitmap, 0, 0);" +
            "  console.log('ImageBitmap received and drawn');" +
            "};" +
            "" +
            "worker.port.postMessage({command: 'create', width: 100, height: 100, color: 'blue'});";

        interact(() -> javaCanvas.executeScript(script));

        // Wait for the worker to create and return ImageBitmap
        sleep(2, TimeUnit.SECONDS);

        // Verify blue pixel was drawn
        assertPixel(50, 50, 0, 0, 255, 255);
    }

    @Test
    public void testMessagePortCommunication() {
        String script =
            "var worker = new SharedWorker('test-sharedworker.js');" +
            "var port = worker.port;" +
            "var messageReceived = false;" +
            "" +
            "port.onmessage = function(e) {" +
            "  messageReceived = true;" +
            "  console.log('Received:', e.data);" +
            "};" +
            "" +
            "port.postMessage('test message');";

        javaCanvas.executeScript(script);

        // Give time for message processing
        sleep(1, TimeUnit.SECONDS);

        Scriptable scope = javaCanvas.getRhinoRuntime().getScope();
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

    @Test
    public void testSharedWorkerTermination() {
        String script =
            "var worker = new SharedWorker('test-sharedworker.js');" +
            "worker.port.postMessage('ping');";

        javaCanvas.executeScript(script);

        // Wait for worker to start
        sleep(1, TimeUnit.SECONDS);

        assertTrue(SharedWorker.getActiveWorkerCount() > 0,
            "Worker should be active before termination");

        // Terminate all workers
        SharedWorker.terminateAll();

        assertEquals(0, SharedWorker.getActiveWorkerCount(),
            "No workers should be active after termination");
    }
}
