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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(ApplicationExtension.class)
public class TestCanvas2D extends ApplicationTest {

    private JavaCanvas javaCanvas;
    private Scriptable scope;

    @Start
    public void start(Stage stage) {
    }

    @BeforeEach
    public void setUp() {
        // Use JavaFX backend for this test as it has a complete implementation
        System.setProperty("w3canvas.backend", "javafx");

        javaCanvas = new JavaCanvas(".", true);
        javaCanvas.initializeBackend();

        Context.enter();
        scope = javaCanvas.getRhinoRuntime().getScope();
    }

    @AfterEach
    public void tearDown() {
        Context.exit();
    }

    private HTMLCanvasElement createCanvas() {
        try {
            return (HTMLCanvasElement) javaCanvas.getDocument().jsFunction_createElement("canvas");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void assertPixel(ICanvasRenderingContext2D ctx, int x, int y, int r, int g, int b, int a) throws ExecutionException, InterruptedException {
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
    public void testFillRect() throws ExecutionException, InterruptedException {
        HTMLCanvasElement canvas = createCanvas();
        ICanvasRenderingContext2D ctx = (ICanvasRenderingContext2D) canvas.jsFunction_getContext("2d");

        interact(() -> {
            ctx.clearRect(0, 0, 400, 400);
            ctx.setFillStyle("red");
            ctx.fillRect(10, 10, 50, 50);
        });
        assertPixel(ctx, 20, 20, 255, 0, 0, 255);
        assertPixel(ctx, 5, 5, 0, 0, 0, 0);
    }

    @Test
    public void testStrokeRect() throws ExecutionException, InterruptedException {
        HTMLCanvasElement canvas = createCanvas();
        ICanvasRenderingContext2D ctx = (ICanvasRenderingContext2D) canvas.jsFunction_getContext("2d");

        interact(() -> {
            ctx.clearRect(0, 0, 400, 400);
            ctx.setStrokeStyle("blue");
            ctx.setLineWidth(5);
            ctx.strokeRect(70, 10, 50, 50);
        });
        assertPixel(ctx, 72, 12, 0, 0, 255, 255);
    }

    @Test
    public void testGlobalCompositeOperation() throws ExecutionException, InterruptedException {
        HTMLCanvasElement canvas = createCanvas();
        ICanvasRenderingContext2D ctx = (ICanvasRenderingContext2D) canvas.jsFunction_getContext("2d");

        interact(() -> {
            ctx.clearRect(0, 0, 400, 400);
            ctx.setFillStyle("red");
            ctx.fillRect(10, 10, 20, 20);
            ctx.setGlobalCompositeOperation("copy");
            ctx.setFillStyle("rgba(0, 0, 255, 0.5)"); // semi-transparent blue
            ctx.fillRect(10, 10, 20, 20);
        });
        // The 'copy' operation replaces, so the alpha should be 127 (0.5 * 255 rounded)
        assertPixel(ctx, 15, 15, 0, 0, 255, 127);
    }
}
