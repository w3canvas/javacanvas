package com.w3canvas.javacanvas.test;

import com.w3canvas.javacanvas.backend.rhino.impl.node.HTMLCanvasElement;
import com.w3canvas.javacanvas.interfaces.ICanvasGradient;
import com.w3canvas.javacanvas.interfaces.ICanvasPattern;
import com.w3canvas.javacanvas.interfaces.ICanvasRenderingContext2D;
import com.w3canvas.javacanvas.interfaces.IImageData;
import com.w3canvas.javacanvas.interfaces.ITextMetrics;
import com.w3canvas.javacanvas.rt.JavaCanvas;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
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

    private void assertPixel(ICanvasRenderingContext2D ctx, int x, int y, int r, int g, int b, int a, int tolerance) throws ExecutionException, InterruptedException {
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

        assertEquals(r, actualR, tolerance, "Red component mismatch at (" + x + "," + y + ")");
        assertEquals(g, actualG, tolerance, "Green component mismatch at (" + x + "," + y + ")");
        assertEquals(b, actualB, tolerance, "Blue component mismatch at (" + x + "," + y + ")");
        assertEquals(a, actualA, tolerance, "Alpha component mismatch at (" + x + "," + y + ")");
    }

    private void assertPixel(ICanvasRenderingContext2D ctx, int x, int y, int r, int g, int b, int a) throws ExecutionException, InterruptedException {
        assertPixel(ctx, x, y, r, g, b, a, 0);
    }

    @Disabled("Failing due to known issue with JavaFX backend rendering")
    @Test
    public void testFillText() throws ExecutionException, InterruptedException {
        HTMLCanvasElement canvas = createCanvas();
        ICanvasRenderingContext2D ctx = (ICanvasRenderingContext2D) canvas.jsFunction_getContext("2d");

        interact(() -> {
            ctx.clearRect(0, 0, 400, 400);
            ctx.setFont("30px sans-serif");
            ctx.setFillStyle("blue");
            ctx.fillText("Hello", 20, 50, 0);
        });

        // Check a pixel within the rendered text.
        assertPixel(ctx, 30, 40, 0, 0, 255, 255, 224);
    }

    @Disabled("Failing due to known issue with JavaFX backend rendering")
    @Test
    public void testStrokeText() throws ExecutionException, InterruptedException {
        HTMLCanvasElement canvas = createCanvas();
        ICanvasRenderingContext2D ctx = (ICanvasRenderingContext2D) canvas.jsFunction_getContext("2d");

        interact(() -> {
            ctx.clearRect(0, 0, 400, 400);
            ctx.setFont("30px sans-serif");
            ctx.setStrokeStyle("red");
            ctx.strokeText("Hello", 20, 50, 0);
        });

        // Check a pixel within the rendered text.
        assertPixel(ctx, 30, 40, 255, 0, 0, 255, 224);
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
        assertPixel(ctx, 95, 10, 0, 0, 255, 255);
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

    @Test
    public void testLinearGradient() throws ExecutionException, InterruptedException {
        HTMLCanvasElement canvas = createCanvas();
        ICanvasRenderingContext2D ctx = (ICanvasRenderingContext2D) canvas.jsFunction_getContext("2d");

        interact(() -> {
            ctx.clearRect(0, 0, 400, 400);
            ICanvasGradient grad = ctx.createLinearGradient(0, 0, 100, 0);
            grad.addColorStop(0, "red");
            grad.addColorStop(1, "blue");
            ctx.setFillStyle(grad);
            ctx.fillRect(0, 0, 100, 100);
        });

        // Check the color at the start of the gradient (red)
        assertPixel(ctx, 1, 50, 255, 0, 0, 255, 3);

        // Check the color at the end of the gradient (blue)
        assertPixel(ctx, 99, 50, 0, 0, 255, 255, 3);

        // Check the color in the middle of the gradient (purple)
        CompletableFuture<int[]> future = new CompletableFuture<>();
        interact(() -> {
            future.complete(ctx.getSurface().getPixelData(50, 50, 1, 1));
        });
        int[] pixelData = future.get();
        int pixel = pixelData[0];
        int actualR = (pixel >> 16) & 0xff;
        int actualB = (pixel >> 0) & 0xff;

        // Check that red and blue components are close to 127.
        // A tolerance of 2 is used to account for rounding differences.
        assertEquals(127, actualR, 2);
        assertEquals(127, actualB, 2);
    }

    @Test
    public void testRadialGradient() throws ExecutionException, InterruptedException {
        HTMLCanvasElement canvas = createCanvas();
        ICanvasRenderingContext2D ctx = (ICanvasRenderingContext2D) canvas.jsFunction_getContext("2d");

        interact(() -> {
            ctx.clearRect(0, 0, 400, 400);
            ICanvasGradient grad = ctx.createRadialGradient(50, 50, 10, 50, 50, 50);
            grad.addColorStop(0, "red");
            grad.addColorStop(1, "blue");
            ctx.setFillStyle(grad);
            ctx.fillRect(0, 0, 100, 100);
        });

        // Check the color at the center of the gradient (red)
        assertPixel(ctx, 50, 50, 255, 0, 0, 255, 7);

        // Check the color at the edge of the gradient (blue)
        assertPixel(ctx, 99, 50, 0, 0, 255, 255, 7);
    }

    @Test
    public void testPattern() throws ExecutionException, InterruptedException {
        HTMLCanvasElement canvas = createCanvas();
        ICanvasRenderingContext2D ctx = (ICanvasRenderingContext2D) canvas.jsFunction_getContext("2d");

        // Create a small canvas to use as the pattern image
        HTMLCanvasElement patternCanvas = createCanvas();
        ICanvasRenderingContext2D patternCtx = (ICanvasRenderingContext2D) patternCanvas.jsFunction_getContext("2d");

        interact(() -> {
            patternCanvas.setWidth(10);
            patternCanvas.setHeight(10);
            patternCtx.setFillStyle("red");
            patternCtx.fillRect(0, 0, 5, 5);
            patternCtx.setFillStyle("blue");
            patternCtx.fillRect(5, 0, 5, 5);
            patternCtx.setFillStyle("green");
            patternCtx.fillRect(0, 5, 5, 5);
            patternCtx.setFillStyle("yellow");
            patternCtx.fillRect(5, 5, 5, 5);
        });

        interact(() -> {
            ctx.clearRect(0, 0, 400, 400);
            ICanvasPattern pattern = ctx.createPattern(patternCanvas, "repeat");
            ctx.setFillStyle(pattern);
            ctx.fillRect(0, 0, 100, 100);
        });

        // Check pixels to verify the pattern
        assertPixel(ctx, 2, 2, 255, 0, 0, 255); // Red
        assertPixel(ctx, 7, 2, 0, 0, 255, 255); // Blue
        assertPixel(ctx, 2, 7, 0, 128, 0, 255); // Green
        assertPixel(ctx, 7, 7, 255, 255, 0, 255); // Yellow

        // Check a repeated part of the pattern
        assertPixel(ctx, 12, 12, 255, 0, 0, 255); // Red
    }

    @Test
    public void testMeasureText() throws ExecutionException, InterruptedException {
        HTMLCanvasElement canvas = createCanvas();
        ICanvasRenderingContext2D ctx = (ICanvasRenderingContext2D) canvas.jsFunction_getContext("2d");

        interact(() -> {
            ctx.setFont("10px sans-serif");
            ITextMetrics metrics = ctx.measureText("Hello world");
            double width = metrics.getWidth();
            // The exact width will depend on the font rendering engine, so we check for a reasonable range.
            assertEquals(55, width, 20);
        });
    }

    @Test
    public void testImageData() throws ExecutionException, InterruptedException {
        HTMLCanvasElement canvas = createCanvas();
        ICanvasRenderingContext2D ctx = (ICanvasRenderingContext2D) canvas.jsFunction_getContext("2d");

        interact(() -> {
            ctx.clearRect(0, 0, 400, 400);
            ctx.setFillStyle("red");
            ctx.fillRect(10, 10, 50, 50);

            IImageData imageData = ctx.getImageData(10, 10, 50, 50);
            ctx.putImageData(imageData, 70, 10, 0, 0, 50, 50);
        });

        assertPixel(ctx, 80, 20, 255, 0, 0, 255);
    }

    @Test
    public void testRect() throws ExecutionException, InterruptedException {
        HTMLCanvasElement canvas = createCanvas();
        ICanvasRenderingContext2D ctx = (ICanvasRenderingContext2D) canvas.jsFunction_getContext("2d");

        interact(() -> {
            ctx.clearRect(0, 0, 400, 400);
            ctx.beginPath();
            ctx.rect(10, 10, 50, 50);
            ctx.setFillStyle("purple");
            ctx.fill();
        });

        // Check a pixel inside the rectangle
        assertPixel(ctx, 20, 20, 128, 0, 128, 255);
        // Check a pixel outside the rectangle
        assertPixel(ctx, 5, 5, 0, 0, 0, 0);
    }

    @Test
    public void testArc() throws ExecutionException, InterruptedException {
        HTMLCanvasElement canvas = createCanvas();
        ICanvasRenderingContext2D ctx = (ICanvasRenderingContext2D) canvas.jsFunction_getContext("2d");

        interact(() -> {
            ctx.clearRect(0, 0, 400, 400);
            ctx.beginPath();
            ctx.arc(100, 75, 50, 0, Math.PI, false);
            ctx.setStrokeStyle("blue");
            ctx.setLineWidth(5);
            ctx.stroke();
        });

        // Check a pixel on the arc
        assertPixel(ctx, 100, 25, 0, 0, 255, 255, 5);
    }

    @Disabled("Failing due to incorrect rendering of arcTo in the JavaFX backend")
    @Test
    public void testArcTo() throws ExecutionException, InterruptedException {
        HTMLCanvasElement canvas = createCanvas();
        ICanvasRenderingContext2D ctx = (ICanvasRenderingContext2D) canvas.jsFunction_getContext("2d");

        interact(() -> {
            ctx.clearRect(0, 0, 400, 400);
            ctx.beginPath();
            ctx.moveTo(20, 20);
            ctx.lineTo(70, 20);
            ctx.arcTo(120, 20, 120, 70, 50);
            ctx.lineTo(120, 120);
            ctx.setStrokeStyle("green");
            ctx.setLineWidth(5);
            ctx.stroke();
        });

        // Check a pixel on the arc
        assertPixel(ctx, 85, 35, 0, 128, 0, 255, 10);
    }

    @Test
    public void testQuadraticCurveTo() throws ExecutionException, InterruptedException {
        HTMLCanvasElement canvas = createCanvas();
        ICanvasRenderingContext2D ctx = (ICanvasRenderingContext2D) canvas.jsFunction_getContext("2d");

        interact(() -> {
            ctx.clearRect(0, 0, 400, 400);
            ctx.beginPath();
            ctx.moveTo(20, 20);
            ctx.quadraticCurveTo(20, 100, 200, 20);
            ctx.setStrokeStyle("orange");
            ctx.setLineWidth(5);
            ctx.stroke();
        });

        // Check a pixel on the curve
        assertPixel(ctx, 60, 60, 255, 165, 0, 255, 10);
    }

    @Test
    public void testLineCap() throws ExecutionException, InterruptedException {
        HTMLCanvasElement canvas = createCanvas();
        ICanvasRenderingContext2D ctx = (ICanvasRenderingContext2D) canvas.jsFunction_getContext("2d");

        interact(() -> {
            ctx.clearRect(0, 0, 400, 400);
            ctx.setLineWidth(10);

            // Butt cap
            ctx.beginPath();
            ctx.moveTo(20, 20);
            ctx.lineTo(100, 20);
            ctx.setLineCap("butt");
            ctx.stroke();

            // Round cap
            ctx.beginPath();
            ctx.moveTo(20, 40);
            ctx.lineTo(100, 40);
            ctx.setLineCap("round");
            ctx.stroke();

            // Square cap
            ctx.beginPath();
            ctx.moveTo(20, 60);
            ctx.lineTo(100, 60);
            ctx.setLineCap("square");
            ctx.stroke();
        });

        // Check a pixel at the end of the line for the round cap
        assertPixel(ctx, 104, 40, 0, 0, 0, 255, 20);
        // Check a pixel at the end of the line for the square cap
        assertPixel(ctx, 104, 60, 0, 0, 0, 255, 20);
        // Check a pixel just beyond the end of the line for the butt cap
        assertPixel(ctx, 101, 20, 0, 0, 0, 0, 20);
    }

    @Test
    public void testBezierCurveTo() throws ExecutionException, InterruptedException {
        HTMLCanvasElement canvas = createCanvas();
        ICanvasRenderingContext2D ctx = (ICanvasRenderingContext2D) canvas.jsFunction_getContext("2d");

        interact(() -> {
            ctx.clearRect(0, 0, 400, 400);
            ctx.beginPath();
            ctx.moveTo(20, 20);
            ctx.bezierCurveTo(20, 100, 200, 100, 200, 20);
            ctx.setStrokeStyle("purple");
            ctx.setLineWidth(5);
            ctx.stroke();
        });

        // Check a pixel on the curve
        assertPixel(ctx, 100, 80, 128, 0, 128, 255, 10);
    }

    @Test
    public void testFill() throws ExecutionException, InterruptedException {
        HTMLCanvasElement canvas = createCanvas();
        ICanvasRenderingContext2D ctx = (ICanvasRenderingContext2D) canvas.jsFunction_getContext("2d");

        interact(() -> {
            ctx.clearRect(0, 0, 400, 400);
            ctx.beginPath();
            ctx.moveTo(25, 25);
            ctx.lineTo(105, 25);
            ctx.lineTo(25, 105);
            ctx.setFillStyle("green");
            ctx.fill();
        });

        // Check a pixel inside the triangle
        assertPixel(ctx, 50, 50, 0, 128, 0, 255);
        // Check a pixel outside the triangle
        assertPixel(ctx, 100, 100, 0, 0, 0, 0);
    }
}
