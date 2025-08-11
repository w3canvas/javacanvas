package com.w3canvas.javacanvas.test;

import com.w3canvas.javacanvas.backend.rhino.impl.node.Document;
import com.w3canvas.javacanvas.backend.rhino.impl.node.HTMLCanvasElement;
import com.w3canvas.javacanvas.backend.rhino.impl.node.Window;
import com.w3canvas.javacanvas.interfaces.ICanvasRenderingContext2D;
import com.w3canvas.javacanvas.rt.JavaCanvas;
import com.w3canvas.javacanvas.utils.PropertiesHolder;
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

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Disabled;

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
        JavaCanvas.resetForTesting();
        Document.resetForTesting();
        Window.resetForTesting();
        PropertiesHolder.resetForTesting();

        System.setProperty("w3canvas.backend", "awt");

        String basePath = ".";
        javaCanvas = new JavaCanvas(basePath, true);
        javaCanvas.initializeBackend();

        Context.enter();

        Scriptable scope = javaCanvas.getRhinoRuntime().getScope();
        canvas = com.w3canvas.javacanvas.utils.RhinoCanvasUtils.getScriptableInstance(HTMLCanvasElement.class, null);
        canvas.jsSet_id("canvas");
        Document.getInstance().addElement("canvas", canvas);
        ScriptableObject.putProperty(scope, "canvas", canvas);
        ctx = (ICanvasRenderingContext2D) canvas.jsFunction_getContext("2d");
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
    public void testWorkerDrawing() throws ExecutionException, InterruptedException, TimeoutException {
        interact(() -> ctx.clearRect(0, 0, 400, 400));

        javaCanvas.executeScript("test/test-worker-main.js");

        // We need to wait for the worker to finish and the main thread to draw the image.
        // There is no callback, so we will just sleep for a short time.
        sleep(2, TimeUnit.SECONDS);

        // The worker creates a 100x100 green square and posts it back.
        // The main thread draws it at (0,0).
        assertPixel(50, 50, 0, 128, 0, 255); // Green
    }
}
