package com.w3canvas.javacanvas.test;

import com.w3canvas.javacanvas.rt.JavaCanvas;
import com.w3canvas.javacanvas.backend.rhino.impl.node.HTMLCanvasElement;
import com.w3canvas.javacanvas.interfaces.ICanvasRenderingContext2D;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.framework.junit5.ApplicationTest;
import javafx.stage.Stage;
import org.mozilla.javascript.Context;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(ApplicationExtension.class)
public class TestModernText extends ApplicationTest {

    private JavaCanvas javaCanvas;

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
        System.setProperty("w3canvas.backend", "javafx");
        javaCanvas = new JavaCanvas(".", true);
        javaCanvas.initializeBackend();
    }

    private HTMLCanvasElement createCanvas() {
        try {
            return (HTMLCanvasElement) javaCanvas.getDocument().jsFunction_createElement("canvas");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void assertPixel(ICanvasRenderingContext2D ctx, int x, int y, int r, int g, int b, int a) throws ExecutionException, InterruptedException {
        assertPixel(ctx, x, y, r, g, b, a, 10);
    }

    private void assertPixel(ICanvasRenderingContext2D ctx, int x, int y, int r, int g, int b, int a, int tolerance) throws ExecutionException, InterruptedException {
        int searchRadius = 15;
        boolean pixelFound = false;

        for (int i = Math.max(0, x - searchRadius); i < Math.min(ctx.getSurface().getWidth(), x + searchRadius); i++) {
            for (int j = Math.max(0, y - searchRadius); j < Math.min(ctx.getSurface().getHeight(), y + searchRadius); j++) {
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
            if (pixelFound) break;
        }
        assertTrue(pixelFound, "Could not find a pixel with the expected color in the vicinity of (" + x + "," + y + ") (searched ±" + searchRadius + ")");
    }

    @Test
    public void testWordSpacing() throws ExecutionException, InterruptedException {
        HTMLCanvasElement canvas = createCanvas();
        ICanvasRenderingContext2D ctx = (ICanvasRenderingContext2D) canvas.jsFunction_getContext("2d");

        interact(() -> {
            Context.enter();
            try {
                ctx.clearRect(0, 0, 400, 400);
                ctx.setFont("20px sans-serif");
                ctx.setFillStyle("black");

                // Normal spacing
                ctx.setWordSpacing(0);
                ctx.fillText("Word1 Word2", 10, 50, 0);

                // Wide spacing
                ctx.setWordSpacing(50);
                ctx.fillText("Word1 Word2", 10, 100, 0);
            } finally {
                Context.exit();
            }
        });

        // Check x=90, y=100 (Gap). Should be transparent.
        assertPixel(ctx, 90, 100, 0, 0, 0, 0);

        // Check x=135, y=100 (Word2 start). Should be black.
        assertPixel(ctx, 135, 100, 0, 0, 0, 255);
    }

    @Test
    public void testLetterSpacing() throws ExecutionException, InterruptedException {
        HTMLCanvasElement canvas = createCanvas();
        ICanvasRenderingContext2D ctx = (ICanvasRenderingContext2D) canvas.jsFunction_getContext("2d");

        interact(() -> {
            Context.enter();
            try {
                ctx.clearRect(0, 0, 400, 400);
                ctx.setFont("20px sans-serif");
                ctx.setFillStyle("black");

                ctx.setLetterSpacing(20);
                ctx.fillText("II", 10, 100, 0);
            } finally {
                Context.exit();
            }
        });

        // Check gap at x=25, y=100. Should be transparent.
        assertPixel(ctx, 25, 100, 0, 0, 0, 0);

        // Check 2nd I at x=35, y=100. Should be black.
        assertPixel(ctx, 35, 100, 0, 0, 0, 255);
    }

    @Test
    public void testDirection() throws ExecutionException, InterruptedException {
        HTMLCanvasElement canvas = createCanvas();
        ICanvasRenderingContext2D ctx = (ICanvasRenderingContext2D) canvas.jsFunction_getContext("2d");

        interact(() -> {
            Context.enter();
            try {
                ctx.clearRect(0, 0, 400, 400);
                ctx.setFont("20px sans-serif");
                ctx.setFillStyle("black");

                ctx.setTextAlign("start");

                // LTR
                ctx.setDirection("ltr");
                ctx.fillText("Test", 200, 50, 0);

                // RTL
                ctx.setDirection("rtl");
                ctx.fillText("Test", 200, 100, 0);
            } finally {
                Context.exit();
            }
        });

        // Check LTR: x=210 should be black. x=190 should be transparent.
        assertPixel(ctx, 210, 50, 0, 0, 0, 255);
        assertPixel(ctx, 190, 50, 0, 0, 0, 0);

        // Check RTL: x=210 should be transparent. x=190 should be black.
        // With RTL + start, alignment should be right-aligned at 200.
        // Text should flow to the left (160 to 200).
        // x=190 (10px left of 200) should be black ("T").
        // x=210 (10px right of 200) should be transparent.
        assertPixel(ctx, 210, 100, 0, 0, 0, 0);
        assertPixel(ctx, 190, 100, 0, 0, 0, 255);
    }
}
