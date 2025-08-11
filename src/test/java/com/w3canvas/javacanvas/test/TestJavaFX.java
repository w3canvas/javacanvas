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

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(ApplicationExtension.class)
public class TestJavaFX extends ApplicationTest {

    private JavaCanvas javaCanvas;

    @Start
    public void start(Stage stage) {
        // The TestFX Application thread starts here.
        // This is required for JavaFX to initialize properly.
    }

    @BeforeEach
    public void setUp() {
        System.setProperty("w3canvas.backend", "javafx");

        // Initialize the canvas in headless mode.
        // This sets up the necessary backend and Rhino environment
        // without creating a visible GUI.
        javaCanvas = new JavaCanvas(".", true);
        javaCanvas.initializeBackend();

        Context.enter();
    }

    @AfterEach
    public void tearDown() {
        Context.exit();
    }

    @Test
    public void testFillRect() throws Exception {
        Scriptable scope = javaCanvas.getRhinoRuntime().getScope();

        HTMLCanvasElement canvas = (HTMLCanvasElement) javaCanvas.getDocument().jsFunction_createElement("canvas");
        ScriptableObject.putProperty(scope, "canvas", canvas);

        ICanvasRenderingContext2D ctx = (ICanvasRenderingContext2D) canvas.jsFunction_getContext("2d");

        // Run drawing operations on the FX Application thread
        interact(() -> {
            ctx.setFillStyle("red");
            ctx.fillRect(10, 10, 100, 100);
        });

        // Get the pixel data from the FX Application thread using a CompletableFuture
        CompletableFuture<int[]> future = new CompletableFuture<>();
        interact(() -> {
            future.complete(ctx.getSurface().getPixelData(15, 15, 1, 1));
        });

        int[] pixelData = future.get();

        assertEquals(0xFFFF0000, pixelData[0], "The pixel at (15,15) should be red.");
    }
}
