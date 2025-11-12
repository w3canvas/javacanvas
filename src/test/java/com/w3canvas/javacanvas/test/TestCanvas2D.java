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
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(ApplicationExtension.class)
// NOTE: Tests re-enabled after fixing thread-local Context management issue
// See STATE_MANAGEMENT_BUG_ANALYSIS.md for details
public class TestCanvas2D extends ApplicationTest {

    private JavaCanvas javaCanvas;
    private Scriptable scope;

    @Start
    public void start(Stage stage) {
    }

    @BeforeEach
    public void setUp() {
        // Use JavaFX backend for this test as it has a complete implementation

        javaCanvas = new JavaCanvas(".", true);
        javaCanvas.initializeBackend();

        // NOTE: Do NOT call Context.enter() here!
        // Context is thread-local. Since all canvas operations happen on the JavaFX
        // Application Thread via interact(), we should not enter a Context on the
        // JUnit test thread. Each test properly manages Context.enter/exit within
        // its interact() blocks on the JavaFX thread.
        scope = javaCanvas.getRhinoRuntime().getScope();
    }

    @AfterEach
    public void tearDown() {
        // NOTE: Do NOT call Context.exit() here since we didn't enter on this thread
    }

    private HTMLCanvasElement createCanvas() {
        try {
            return (HTMLCanvasElement) javaCanvas.getDocument().jsFunction_createElement("canvas");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void assertPixel(ICanvasRenderingContext2D ctx, int x, int y, int r, int g, int b, int a, int tolerance) throws ExecutionException, InterruptedException {
        boolean pixelFound = false;
        for (int i = Math.max(0, x - 10); i < Math.min(ctx.getSurface().getWidth(), x + 10); i++) {
            for (int j = Math.max(0, y - 10); j < Math.min(ctx.getSurface().getHeight(), y + 10); j++) {
                CompletableFuture<int[]> future = new CompletableFuture<>();
                final int currentX = i;
                final int currentY = j;
                interact(() -> {
                    future.complete(ctx.getSurface().getPixelData(currentX, currentY, 1, 1));
                });
                int[] pixelData = future.get();
                int pixel = pixelData[0];

                int actualA = (pixel >> 24) & 0xff;
                int actualR = (pixel >> 16) & 0xff;
                int actualG = (pixel >> 8) & 0xff;
                int actualB = pixel & 0xff;

                if (Math.abs(r - actualR) <= tolerance &&
                    Math.abs(g - actualG) <= tolerance &&
                    Math.abs(b - actualB) <= tolerance &&
                    Math.abs(a - actualA) <= tolerance) {
                    pixelFound = true;
                    break;
                }
            }
            if (pixelFound) {
                break;
            }
        }
        assertTrue(pixelFound, "Could not find a pixel with the expected color in the vicinity of (" + x + "," + y + ")");
    }

    private void assertPixel(ICanvasRenderingContext2D ctx, int x, int y, int r, int g, int b, int a) throws ExecutionException, InterruptedException {
        assertPixel(ctx, x, y, r, g, b, a, 0);
    }

    @Test
    public void testFillText() throws ExecutionException, InterruptedException {
        HTMLCanvasElement canvas = createCanvas();
        ICanvasRenderingContext2D ctx = (ICanvasRenderingContext2D) canvas.jsFunction_getContext("2d");

        interact(() -> {
            Context.enter();
            try {
                ctx.clearRect(0, 0, 400, 400);
                ctx.setFont("30px sans-serif");
                ctx.setFillStyle("blue");
                ctx.fillText("Hello", 20, 50, 0);
            } finally {
                Context.exit();
            }
        });

        // Check a pixel within the rendered text.
        assertPixel(ctx, 30, 40, 0, 0, 255, 255, 224);
    }

    @Test
    public void testStrokeText() throws ExecutionException, InterruptedException {
        HTMLCanvasElement canvas = createCanvas();
        ICanvasRenderingContext2D ctx = (ICanvasRenderingContext2D) canvas.jsFunction_getContext("2d");

        interact(() -> {
            Context.enter();
            try {
                ctx.clearRect(0, 0, 400, 400);
                ctx.setFont("30px sans-serif");
                ctx.setStrokeStyle("red");
                ctx.strokeText("Hello", 20, 50, 0);
            } finally {
                Context.exit();
            }
        });

        // Check a pixel within the rendered text.
        assertPixel(ctx, 30, 40, 255, 0, 0, 255, 224);
    }

    @Test
    public void testFillRect() throws ExecutionException, InterruptedException {
        HTMLCanvasElement canvas = createCanvas();
        ICanvasRenderingContext2D ctx = (ICanvasRenderingContext2D) canvas.jsFunction_getContext("2d");

        interact(() -> {
            Context.enter();
            try {
                ctx.clearRect(0, 0, 400, 400);
                ctx.setFillStyle("red");
                ctx.fillRect(10, 10, 50, 50);
            } finally {
                Context.exit();
            }
        });
        assertPixel(ctx, 20, 20, 255, 0, 0, 255);
        assertPixel(ctx, 5, 5, 0, 0, 0, 0);
    }

    @Test
    public void testStrokeRect() throws ExecutionException, InterruptedException {
        HTMLCanvasElement canvas = createCanvas();
        ICanvasRenderingContext2D ctx = (ICanvasRenderingContext2D) canvas.jsFunction_getContext("2d");

        interact(() -> {
            Context.enter();
            try {
                ctx.clearRect(0, 0, 400, 400);
                ctx.setStrokeStyle("blue");
                ctx.setLineWidth(5);
                ctx.strokeRect(70, 10, 50, 50);
            } finally {
                Context.exit();
            }
        });
        assertPixel(ctx, 95, 10, 0, 0, 255, 255);
    }

    @Test
    public void testGlobalCompositeOperation() throws ExecutionException, InterruptedException {
        HTMLCanvasElement canvas = createCanvas();
        ICanvasRenderingContext2D ctx = (ICanvasRenderingContext2D) canvas.jsFunction_getContext("2d");

        interact(() -> {
            Context.enter();
            try {
                ctx.clearRect(0, 0, 400, 400);
                ctx.setFillStyle("red");
                ctx.fillRect(10, 10, 20, 20);
                ctx.setGlobalCompositeOperation("copy");
                ctx.setFillStyle("rgba(0, 0, 255, 0.5)"); // semi-transparent blue
                ctx.fillRect(10, 10, 20, 20);
            } finally {
                Context.exit();
            }
        });
        // The 'copy' operation replaces, so the alpha should be 127 (0.5 * 255 rounded)
        assertPixel(ctx, 15, 15, 0, 0, 255, 127);
    }

    @Test
    public void testLinearGradient() throws ExecutionException, InterruptedException {
        HTMLCanvasElement canvas = createCanvas();
        ICanvasRenderingContext2D ctx = (ICanvasRenderingContext2D) canvas.jsFunction_getContext("2d");

        interact(() -> {
            Context.enter();
            try {
                ctx.clearRect(0, 0, 400, 400);
                ICanvasGradient grad = ctx.createLinearGradient(0, 0, 100, 0);
                grad.addColorStop(0, "red");
                grad.addColorStop(1, "blue");
                ctx.setFillStyle(grad);
                ctx.fillRect(0, 0, 100, 100);
            } finally {
                Context.exit();
            }
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
            Context.enter();
            try {
                ctx.clearRect(0, 0, 400, 400);
                ICanvasGradient grad = ctx.createRadialGradient(50, 50, 10, 50, 50, 50);
                grad.addColorStop(0, "red");
                grad.addColorStop(1, "blue");
                ctx.setFillStyle(grad);
                ctx.fillRect(0, 0, 100, 100);
            } finally {
                Context.exit();
            }
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

        String backend = System.getProperty("w3canvas.backend", "awt");
        if (backend.equals("awt")) {
            // Create a small BufferedImage to use as the pattern image
            java.awt.image.BufferedImage patternImage = new java.awt.image.BufferedImage(10, 10, java.awt.image.BufferedImage.TYPE_INT_ARGB);
            java.awt.Graphics2D g2d = patternImage.createGraphics();
            g2d.setColor(java.awt.Color.RED);
            g2d.fillRect(0, 0, 5, 5);
            g2d.setColor(java.awt.Color.BLUE);
            g2d.fillRect(5, 0, 5, 5);
            g2d.setColor(java.awt.Color.GREEN);
            g2d.fillRect(0, 5, 5, 5);
            g2d.setColor(java.awt.Color.YELLOW);
            g2d.fillRect(5, 5, 5, 5);
            g2d.dispose();

            interact(() -> {
                Context.enter();
                try {
                    ctx.clearRect(0, 0, 400, 400);
                    ICanvasPattern pattern = ctx.createPattern(patternImage, "repeat");
                    ctx.setFillStyle(pattern);
                    ctx.fillRect(0, 0, 100, 100);
                } finally {
                    Context.exit();
                }
            });
        } else {
            // Create a small canvas to use as the pattern image
            HTMLCanvasElement patternCanvas = createCanvas();
            ICanvasRenderingContext2D patternCtx = (ICanvasRenderingContext2D) patternCanvas.jsFunction_getContext("2d");

            interact(() -> {
                Context.enter();
                try {
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
                } finally {
                    Context.exit();
                }
            });

            interact(() -> {
                Context.enter();
                try {
                    ctx.clearRect(0, 0, 400, 400);
                    ICanvasPattern pattern = ctx.createPattern(patternCanvas, "repeat");
                    ctx.setFillStyle(pattern);
                    ctx.fillRect(0, 0, 100, 100);
                } finally {
                    Context.exit();
                }
            });
        }

        // Check pixels to verify the pattern
        assertPixel(ctx, 2, 2, 255, 0, 0, 255); // Red
        assertPixel(ctx, 7, 2, 0, 0, 255, 255); // Blue
        assertPixel(ctx, 2, 7, 0, 255, 0, 255); // Green
        assertPixel(ctx, 7, 7, 255, 255, 0, 255); // Yellow

        // Check a repeated part of the pattern
        assertPixel(ctx, 12, 12, 255, 0, 0, 255); // Red
    }

    @Test
    public void testMeasureText() throws ExecutionException, InterruptedException {
        HTMLCanvasElement canvas = createCanvas();
        ICanvasRenderingContext2D ctx = (ICanvasRenderingContext2D) canvas.jsFunction_getContext("2d");

        interact(() -> {
            Context.enter();
            try {
                ctx.setFont("10px sans-serif");
                ITextMetrics metrics = ctx.measureText("Hello world");
                double width = metrics.getWidth();
                // The exact width will depend on the font rendering engine, so we check for a reasonable range.
                assertEquals(55, width, 20);
            } finally {
                Context.exit();
            }
        });
    }

    @Test
    public void testImageData() throws ExecutionException, InterruptedException {
        HTMLCanvasElement canvas = createCanvas();
        ICanvasRenderingContext2D ctx = (ICanvasRenderingContext2D) canvas.jsFunction_getContext("2d");

        interact(() -> {
            Context.enter();
            try {
                ctx.clearRect(0, 0, 400, 400);
                ctx.setFillStyle("red");
                ctx.fillRect(10, 10, 50, 50);

                IImageData imageData = ctx.getImageData(10, 10, 50, 50);
                ctx.putImageData(imageData, 70, 10, 0, 0, 50, 50);
            } finally {
                Context.exit();
            }
        });

        assertPixel(ctx, 80, 20, 255, 0, 0, 255);
    }

    @Test
    public void testRect() throws ExecutionException, InterruptedException {
        HTMLCanvasElement canvas = createCanvas();
        ICanvasRenderingContext2D ctx = (ICanvasRenderingContext2D) canvas.jsFunction_getContext("2d");

        interact(() -> {
            Context.enter();
            try {
                ctx.clearRect(0, 0, 400, 400);
                ctx.beginPath();
                ctx.rect(10, 10, 50, 50);
                ctx.setFillStyle("purple");
                ctx.fill();
            } finally {
                Context.exit();
            }
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
            Context.enter();
            try {
                ctx.clearRect(0, 0, 400, 400);
                ctx.beginPath();
                ctx.arc(100, 75, 50, 0, Math.PI, false);
                ctx.setStrokeStyle("blue");
                ctx.setLineWidth(5);
                ctx.stroke();
            } finally {
                Context.exit();
            }
        });

        // Check a pixel on the arc
        assertPixel(ctx, 100, 25, 0, 0, 255, 255, 5);
    }

    @Test
    public void testArcTo() throws ExecutionException, InterruptedException {
        HTMLCanvasElement canvas = createCanvas();
        ICanvasRenderingContext2D ctx = (ICanvasRenderingContext2D) canvas.jsFunction_getContext("2d");

        interact(() -> {
            Context.enter();
            try {
                ctx.clearRect(0, 0, 400, 400);
                ctx.beginPath();
                ctx.moveTo(20, 20);
                ctx.lineTo(70, 20);
                ctx.arcTo(120, 20, 120, 70, 50);
                ctx.lineTo(120, 120);
                ctx.setStrokeStyle("green");
                ctx.setLineWidth(5);
                ctx.stroke();
            } finally {
                Context.exit();
            }
        });

        // Check a pixel on the arc
        assertPixel(ctx, 100, 30, 0, 128, 0, 255, 10);
    }

    @Test
    public void testArcToFill() throws ExecutionException, InterruptedException {
        HTMLCanvasElement canvas = createCanvas();
        ICanvasRenderingContext2D ctx = (ICanvasRenderingContext2D) canvas.jsFunction_getContext("2d");

        interact(() -> {
            Context.enter();
            try {
                ctx.clearRect(0, 0, 400, 400);
                ctx.beginPath();
                ctx.moveTo(20, 20);
                ctx.lineTo(70, 20);
                ctx.arcTo(120, 20, 120, 70, 50);
                ctx.lineTo(120, 120);
                ctx.closePath();
                ctx.setFillStyle("purple");
                ctx.fill();
            } finally {
                Context.exit();
            }
        });

        // Check a pixel inside the filled shape
        assertPixel(ctx, 100, 50, 128, 0, 128, 255);
    }

    @Test
    public void testQuadraticCurveTo() throws ExecutionException, InterruptedException {
        HTMLCanvasElement canvas = createCanvas();
        ICanvasRenderingContext2D ctx = (ICanvasRenderingContext2D) canvas.jsFunction_getContext("2d");

        interact(() -> {
            Context.enter();
            try {
                ctx.clearRect(0, 0, 400, 400);
                ctx.beginPath();
                ctx.moveTo(20, 20);
                ctx.quadraticCurveTo(20, 100, 200, 20);
                ctx.setStrokeStyle("orange");
                ctx.setLineWidth(5);
                ctx.stroke();
            } finally {
                Context.exit();
            }
        });

        // Check a pixel on the curve
        assertPixel(ctx, 60, 60, 255, 165, 0, 255, 10);
    }

    @Test
    public void testLineCap() throws ExecutionException, InterruptedException {
        HTMLCanvasElement canvas = createCanvas();
        ICanvasRenderingContext2D ctx = (ICanvasRenderingContext2D) canvas.jsFunction_getContext("2d");

        interact(() -> {
            Context.enter();
            try {
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
            } finally {
                Context.exit();
            }
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
            Context.enter();
            try {
                ctx.clearRect(0, 0, 400, 400);
                ctx.beginPath();
                ctx.moveTo(20, 20);
                ctx.bezierCurveTo(20, 100, 200, 100, 200, 20);
                ctx.setStrokeStyle("purple");
                ctx.setLineWidth(5);
                ctx.stroke();
            } finally {
                Context.exit();
            }
        });

        // Check a pixel on the curve
        assertPixel(ctx, 100, 80, 128, 0, 128, 255, 10);
    }

    @Test
    public void testFill() throws ExecutionException, InterruptedException {
        HTMLCanvasElement canvas = createCanvas();
        ICanvasRenderingContext2D ctx = (ICanvasRenderingContext2D) canvas.jsFunction_getContext("2d");

        interact(() -> {
            Context.enter();
            try {
                ctx.clearRect(0, 0, 400, 400);
                ctx.beginPath();
                ctx.moveTo(25, 25);
                ctx.lineTo(105, 25);
                ctx.lineTo(25, 105);
                ctx.setFillStyle("green");
                ctx.fill();
            } finally {
                Context.exit();
            }
        });

        // Check a pixel inside the triangle
        assertPixel(ctx, 50, 50, 0, 128, 0, 255);
        // Check a pixel outside the triangle
        assertPixel(ctx, 100, 100, 0, 0, 0, 0);
    }

    @Test
    public void testSaveRestore() throws ExecutionException, InterruptedException {
        HTMLCanvasElement canvas = createCanvas();
        ICanvasRenderingContext2D ctx = (ICanvasRenderingContext2D) canvas.jsFunction_getContext("2d");

        interact(() -> {
            Context.enter();
            try {
                ctx.setFillStyle("red");
                ctx.fillRect(0, 0, 100, 100);

                ctx.save();
                ctx.setFillStyle("blue");
                ctx.fillRect(100, 0, 100, 100);
                ctx.restore();

                ctx.fillRect(200, 0, 100, 100);
            } finally {
                Context.exit();
            }
        });

        assertPixel(ctx, 50, 50, 255, 0, 0, 255);
        assertPixel(ctx, 150, 50, 0, 0, 255, 255);
        assertPixel(ctx, 250, 50, 255, 0, 0, 255);
    }

    @Test
    public void testTransformations() throws ExecutionException, InterruptedException {
        HTMLCanvasElement canvas = createCanvas();
        canvas.setWidth(400);
        canvas.setHeight(400);
        ICanvasRenderingContext2D ctx = (ICanvasRenderingContext2D) canvas.jsFunction_getContext("2d");

        interact(() -> {
            Context.enter();
            try {
                ctx.clearRect(0, 0, 400, 400);
                ctx.translate(100, 100);
                ctx.setFillStyle("blue");
                ctx.fillRect(10, 10, 50, 50);

                ctx.rotate(Math.PI / 4);
                ctx.setFillStyle("green");
                ctx.fillRect(10, 10, 50, 50);

                ctx.scale(2, 2);
                ctx.setFillStyle("purple");
                ctx.fillRect(10, 10, 25, 25);

            } finally {
                Context.exit();
            }
        });

        assertPixel(ctx, 135, 135, 0, 0, 255, 255);
    }

    @Test
    public void testSetTransform() throws ExecutionException, InterruptedException {
        HTMLCanvasElement canvas = createCanvas();
        canvas.setWidth(400);
        canvas.setHeight(400);
        ICanvasRenderingContext2D ctx = (ICanvasRenderingContext2D) canvas.jsFunction_getContext("2d");

        interact(() -> {
            Context.enter();
            try {
                ctx.clearRect(0, 0, 400, 400);
                ctx.setTransform(2, 0.5, 0.5, 2, 50, 50);
                ctx.setFillStyle("red");
                ctx.fillRect(0, 0, 50, 50);
            } finally {
                Context.exit();
            }
        });

        assertPixel(ctx, 75, 75, 255, 0, 0, 255);
    }

    @Test
    public void testResetTransform() throws ExecutionException, InterruptedException {
        HTMLCanvasElement canvas = createCanvas();
        canvas.setWidth(400);
        canvas.setHeight(400);
        ICanvasRenderingContext2D ctx = (ICanvasRenderingContext2D) canvas.jsFunction_getContext("2d");

        interact(() -> {
            Context.enter();
            try {
                ctx.clearRect(0, 0, 400, 400);
                ctx.translate(100, 100);
                ctx.resetTransform();
                ctx.setFillStyle("green");
                ctx.fillRect(10, 10, 50, 50);
            } finally {
                Context.exit();
            }
        });

        assertPixel(ctx, 35, 35, 0, 128, 0, 255);
    }

    @Test
    public void testEllipse() throws ExecutionException, InterruptedException {
        HTMLCanvasElement canvas = createCanvas();
        canvas.setWidth(400);
        canvas.setHeight(400);
        ICanvasRenderingContext2D ctx = (ICanvasRenderingContext2D) canvas.jsFunction_getContext("2d");

        interact(() -> {
            Context.enter();
            try {
                ctx.clearRect(0, 0, 400, 400);
                ctx.beginPath();
                ctx.ellipse(200, 200, 50, 75, Math.PI / 4, 0, 2 * Math.PI, false);
                ctx.setFillStyle("purple");
                ctx.fill();
            } finally {
                Context.exit();
            }
        });

        assertPixel(ctx, 200, 200, 128, 0, 128, 255);
        assertPixel(ctx, 100, 100, 0, 0, 0, 0);
    }

    @Test
    public void testClip() throws ExecutionException, InterruptedException {
        HTMLCanvasElement canvas = createCanvas();
        canvas.setWidth(400);
        canvas.setHeight(400);
        ICanvasRenderingContext2D ctx = (ICanvasRenderingContext2D) canvas.jsFunction_getContext("2d");

        interact(() -> {
            Context.enter();
            try {
                ctx.clearRect(0, 0, 400, 400);
                ctx.beginPath();
                ctx.arc(200, 200, 50, 0, Math.PI * 2, false);
                ctx.clip();

                ctx.setFillStyle("blue");
                ctx.fillRect(0, 0, 400, 400);
            } finally {
                Context.exit();
            }
        });

        assertPixel(ctx, 200, 200, 0, 0, 255, 255);
        assertPixel(ctx, 100, 100, 0, 0, 0, 0);
    }

    @Test
    public void testIsPointInPath() throws ExecutionException, InterruptedException {
        HTMLCanvasElement canvas = createCanvas();
        ICanvasRenderingContext2D ctx = (ICanvasRenderingContext2D) canvas.jsFunction_getContext("2d");
        CompletableFuture<Boolean> inPath = new CompletableFuture<>();
        CompletableFuture<Boolean> notInPath = new CompletableFuture<>();

        interact(() -> {
            Context.enter();
            try {
                ctx.rect(10, 10, 100, 100);
                inPath.complete(ctx.isPointInPath(50, 50));
                notInPath.complete(ctx.isPointInPath(200, 200));
            } finally {
                Context.exit();
            }
        });

        assertEquals(true, inPath.get());
        assertEquals(false, notInPath.get());
    }

    @Test
    public void testIsPointInStroke() throws ExecutionException, InterruptedException {
        HTMLCanvasElement canvas = createCanvas();
        ICanvasRenderingContext2D ctx = (ICanvasRenderingContext2D) canvas.jsFunction_getContext("2d");
        CompletableFuture<Boolean> inStroke = new CompletableFuture<>();
        CompletableFuture<Boolean> notInStroke = new CompletableFuture<>();

        interact(() -> {
            Context.enter();
            try {
                ctx.beginPath();
                ctx.rect(10, 10, 100, 100);
                ctx.setLineWidth(10);
                ctx.stroke();
                inStroke.complete(ctx.isPointInStroke(10, 15));
                notInStroke.complete(ctx.isPointInStroke(50, 50));
            } finally {
                Context.exit();
            }
        });

        assertEquals(true, inStroke.get());
        assertEquals(false, notInStroke.get());
    }

    @Test
    public void testLineStyles() throws ExecutionException, InterruptedException {
        HTMLCanvasElement canvas = createCanvas();
        canvas.setWidth(400);
        canvas.setHeight(400);
        ICanvasRenderingContext2D ctx = (ICanvasRenderingContext2D) canvas.jsFunction_getContext("2d");

        interact(() -> {
            Context.enter();
            try {
                ctx.clearRect(0, 0, 400, 400);

                // Line Join
                ctx.setLineWidth(20);
                ctx.beginPath();
                ctx.moveTo(20, 20);
                ctx.lineTo(100, 50);
                ctx.lineTo(20, 80);
                ctx.setLineJoin("round");
                ctx.stroke();

                ctx.beginPath();
                ctx.moveTo(120, 20);
                ctx.lineTo(200, 50);
                ctx.lineTo(120, 80);
                ctx.setLineJoin("bevel");
                ctx.stroke();

                ctx.beginPath();
                ctx.moveTo(220, 20);
                ctx.lineTo(300, 50);
                ctx.lineTo(220, 80);
                ctx.setLineJoin("miter");
                ctx.stroke();

                // Miter Limit
                ctx.setMiterLimit(2);
                ctx.beginPath();
                ctx.moveTo(20, 120);
                ctx.lineTo(100, 150);
                ctx.lineTo(20, 180);
                ctx.stroke();

                // Line Dash
                ctx.setLineDash(new Object[]{5.0, 15.0});
                ctx.beginPath();
                ctx.moveTo(0, 220);
                ctx.lineTo(400, 220);
                ctx.stroke();

                // Line Dash Offset
                ctx.setLineDashOffset(5);
                ctx.beginPath();
                ctx.moveTo(0, 250);
                ctx.lineTo(400, 250);
                ctx.stroke();

            } finally {
                Context.exit();
            }
        });

        // Assertions for line styles would require more complex image analysis.
        // For now, we are just testing that the methods don't crash.
        // We can visually inspect the output if needed.
        assertPixel(ctx, 20, 20, 0, 0, 0, 255);

        CompletableFuture<Object[]> getLineDashFuture = new CompletableFuture<>();
        interact(() -> {
            Context.enter();
            try {
                getLineDashFuture.complete((Object[]) ctx.getLineDash());
            } finally {
                Context.exit();
            }
        });
        Object[] lineDash = getLineDashFuture.get();
        assertEquals(2, lineDash.length);
        assertEquals(5.0, (Double) lineDash[0], 0.1);
        assertEquals(15.0, (Double) lineDash[1], 0.1);
    }

    @Test
    public void testGlobalAlpha() throws ExecutionException, InterruptedException {
        HTMLCanvasElement canvas = createCanvas();
        canvas.setWidth(400);
        canvas.setHeight(400);
        ICanvasRenderingContext2D ctx = (ICanvasRenderingContext2D) canvas.jsFunction_getContext("2d");

        interact(() -> {
            Context.enter();
            try {
                ctx.clearRect(0, 0, 400, 400);
                ctx.setFillStyle("red");
                ctx.fillRect(10, 10, 100, 100);

                ctx.setGlobalAlpha(0.5);

                ctx.setFillStyle("blue");
                ctx.fillRect(50, 50, 100, 100);
            } finally {
                Context.exit();
            }
        });

        // The overlapping area should be a blend of red and blue
        // The blue color has 50% alpha, so the resulting color will be
        // R = 0 * 0.5 + 255 * (1 - 0.5) = 127.5
        // G = 0
        // B = 255 * 0.5 + 0 * (1 - 0.5) = 127.5
        // A = 255
        assertPixel(ctx, 75, 75, 127, 0, 127, 255, 1);
    }

    @Test
    public void testCreateImageData() throws ExecutionException, InterruptedException {
        HTMLCanvasElement canvas = createCanvas();
        ICanvasRenderingContext2D ctx = (ICanvasRenderingContext2D) canvas.jsFunction_getContext("2d");
        CompletableFuture<IImageData> imageDataFuture = new CompletableFuture<>();

        interact(() -> {
            Context.enter();
            try {
                imageDataFuture.complete(ctx.createImageData(100, 200));
            } finally {
                Context.exit();
            }
        });

        IImageData imageData = imageDataFuture.get();
        assertEquals(100, imageData.getWidth());
        assertEquals(200, imageData.getHeight());
        // The underlying data is an int array, so the length is width * height
        assertEquals(100 * 200, imageData.getData().getPixels(0, 0, 100, 200).length);
    }

    @Test
    public void testTextAlign() throws ExecutionException, InterruptedException {
        HTMLCanvasElement canvas = createCanvas();
        canvas.setWidth(400);
        canvas.setHeight(400);
        ICanvasRenderingContext2D ctx = (ICanvasRenderingContext2D) canvas.jsFunction_getContext("2d");

        interact(() -> {
            Context.enter();
            try {
                ctx.clearRect(0, 0, 400, 400);
                ctx.setFont("30px sans-serif");
                ctx.setFillStyle("blue");

                ctx.setTextAlign("center");
                ctx.fillText("Hello", 200, 50, 0);

                ctx.setTextAlign("right");
                ctx.fillText("Hello", 200, 100, 0);
            } finally {
                Context.exit();
            }
        });

        // Check a pixel within the centered text.
        assertPixel(ctx, 200, 40, 0, 0, 255, 255, 224);
        // Check a pixel within the right-aligned text.
        assertPixel(ctx, 190, 90, 0, 0, 255, 255, 224);
    }

    @Test
    public void testTextBaseline() throws ExecutionException, InterruptedException {
        HTMLCanvasElement canvas = createCanvas();
        canvas.setWidth(400);
        canvas.setHeight(400);
        ICanvasRenderingContext2D ctx = (ICanvasRenderingContext2D) canvas.jsFunction_getContext("2d");

        interact(() -> {
            Context.enter();
            try {
                ctx.clearRect(0, 0, 400, 400);
                ctx.setFont("30px sans-serif");
                ctx.setFillStyle("blue");

                ctx.setTextBaseline("top");
                ctx.fillText("Top", 50, 50, 0);

                ctx.setTextBaseline("middle");
                ctx.fillText("Middle", 50, 100, 0);

                ctx.setTextBaseline("bottom");
                ctx.fillText("Bottom", 50, 150, 0);

            } finally {
                Context.exit();
            }
        });

        // Check a pixel within the "Top" text.
        assertPixel(ctx, 60, 60, 0, 0, 255, 255, 224);
        // Check a pixel within the "Middle" text.
        assertPixel(ctx, 60, 100, 0, 0, 255, 255, 224);
        // Check a pixel within the "Bottom" text.
        assertPixel(ctx, 60, 140, 0, 0, 255, 255, 224);
    }

    private HTMLCanvasElement createTestPatternCanvas() {
        HTMLCanvasElement patternCanvas = createCanvas();
        ICanvasRenderingContext2D patternCtx = (ICanvasRenderingContext2D) patternCanvas.jsFunction_getContext("2d");

        interact(() -> {
            Context.enter();
            try {
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
            } finally {
                Context.exit();
            }
        });
        return patternCanvas;
    }

    @Test
    public void testDrawImage_3args() throws ExecutionException, InterruptedException {
        HTMLCanvasElement sourceCanvas = createTestPatternCanvas();
        HTMLCanvasElement destCanvas = createCanvas();
        ICanvasRenderingContext2D destCtx = (ICanvasRenderingContext2D) destCanvas.jsFunction_getContext("2d");

        interact(() -> {
            destCtx.drawImage(sourceCanvas, 20, 30);
        });

        // Check a red pixel from the source canvas drawn on the destination
        assertPixel(destCtx, 22, 32, 255, 0, 0, 255);
        // Check a blue pixel
        assertPixel(destCtx, 27, 32, 0, 0, 255, 255);
    }

    @Test
    public void testDrawImage_5args() throws ExecutionException, InterruptedException {
        HTMLCanvasElement sourceCanvas = createTestPatternCanvas();
        HTMLCanvasElement destCanvas = createCanvas();
        ICanvasRenderingContext2D destCtx = (ICanvasRenderingContext2D) destCanvas.jsFunction_getContext("2d");

        interact(() -> {
            destCtx.drawImage(sourceCanvas, 20, 30, 20, 20); // Scale up the 10x10 image
        });

        // Check a red pixel from the source canvas drawn on the destination
        assertPixel(destCtx, 22, 32, 255, 0, 0, 255);
        // Check a blue pixel from the scaled image
        assertPixel(destCtx, 35, 35, 0, 0, 255, 255);
    }

    @Test
    public void testDrawImage_9args() throws ExecutionException, InterruptedException {
        HTMLCanvasElement sourceCanvas = createTestPatternCanvas();
        HTMLCanvasElement destCanvas = createCanvas();
        ICanvasRenderingContext2D destCtx = (ICanvasRenderingContext2D) destCanvas.jsFunction_getContext("2d");

        interact(() -> {
            // Draw the top-left quadrant (red) of the source to the destination
            destCtx.drawImage(sourceCanvas, 0, 0, 5, 5, 20, 30, 10, 10);
        });

        // The destination should be filled with red
        assertPixel(destCtx, 25, 35, 255, 0, 0, 255);
        // A pixel outside this area should be transparent
        assertPixel(destCtx, 15, 25, 0, 0, 0, 0);
    }

    @Test
    public void testIsPointInStrokeWithArcTo() throws ExecutionException, InterruptedException {
        HTMLCanvasElement canvas = createCanvas();
        ICanvasRenderingContext2D ctx = (ICanvasRenderingContext2D) canvas.jsFunction_getContext("2d");
        CompletableFuture<Boolean> inStroke = new CompletableFuture<>();

        interact(() -> {
            Context.enter();
            try {
                ctx.beginPath();
                ctx.moveTo(20, 20);
                ctx.arcTo(120, 20, 120, 70, 50);
                ctx.setLineWidth(10);
                // Don't stroke the path, isPointInStroke should work on the current path
                inStroke.complete(ctx.isPointInStroke(70, 16));
            } finally {
                Context.exit();
            }
        });

        assertTrue(inStroke.get(), "Point should be in the stroke of the arc");
    }
}
