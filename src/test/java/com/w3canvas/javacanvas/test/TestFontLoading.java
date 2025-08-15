package com.w3canvas.javacanvas.test;

import com.w3canvas.javacanvas.backend.rhino.impl.node.HTMLCanvasElement;
import com.w3canvas.javacanvas.interfaces.ICanvasRenderingContext2D;
import com.w3canvas.javacanvas.rt.JavaCanvas;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import fi.iki.elonen.NanoHTTPD;

@ExtendWith(ApplicationExtension.class)
public class TestFontLoading extends ApplicationTest {

    private JavaCanvas javaCanvas;
    private Scriptable scope;
    private WebServer server;

    private static class WebServer extends NanoHTTPD {
        public WebServer() throws IOException {
            super(8080);
            start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        }

        @Override
        public Response serve(IHTTPSession session) {
            try {
                FileInputStream fis = new FileInputStream("fonts/DejaVuSans.ttf");
                return newFixedLengthResponse(Response.Status.OK, "font/ttf", fis, fis.available());
            } catch (IOException ioe) {
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, "Failed to load font");
            }
        }
    }

    @Start
    public void start(Stage stage) {
    }

    @BeforeEach
    public void setUp() throws IOException {
        server = new WebServer();
        System.setProperty("w3canvas.backend", "javafx");

        javaCanvas = new JavaCanvas(".", true);
        javaCanvas.initializeBackend();

        Context.enter();
        scope = javaCanvas.getRhinoRuntime().getScope();
    }

    @AfterEach
    public void tearDown() {
        server.stop();
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

    @Test
    public void testFontFace() throws ExecutionException, InterruptedException {
        HTMLCanvasElement canvas = createCanvas();
        ICanvasRenderingContext2D ctx = (ICanvasRenderingContext2D) canvas.jsFunction_getContext("2d");

        Context.enter();
        try {
            javaCanvas.getRhinoRuntime().getScope().put("ctx", javaCanvas.getRhinoRuntime().getScope(), ctx);
            javaCanvas.getRhinoRuntime().exec(
                "var f = new FontFace('DejaVuSans', 'url(http://localhost:8080/DejaVuSans.ttf)');" +
                "document.fonts.add(f);" +
                "f.load();"
            );
        } finally {
            Context.exit();
        }

        // Wait for the font to load
        javaCanvas.getDocument().jsGet_fonts().getFaces().iterator().next().getLoaded().get();

        interact(() -> {
            Context.enter();
            try {
                ctx.setFont("30px DejaVuSans");
                ctx.setFillStyle("blue");
                ctx.fillText("Hello", 20, 50, 0);
            } finally {
                Context.exit();
            }
        });

        // We can't easily assert the font, but we can assert that something was drawn.
        assertPixel(ctx, 30, 40, 0, 0, 255, 255, 224);
    }
}
