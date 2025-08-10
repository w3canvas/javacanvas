package com.w3canvas.javacanvas.test;

import com.w3canvas.javacanvas.backend.rhino.impl.node.HTMLCanvasElement;
import com.w3canvas.javacanvas.interfaces.ICanvasRenderingContext2D;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import javax.swing.SwingUtilities;
import java.util.concurrent.CountDownLatch;
import javafx.embed.swing.JFXPanel;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestJavaFX {

    @BeforeAll
    public static void initJFX() throws InterruptedException {
        System.out.println("Initializing JavaFX...");
        final CountDownLatch latch = new CountDownLatch(1);
        SwingUtilities.invokeLater(() -> {
            new JFXPanel(); // initializes JavaFX environment
            latch.countDown();
        });
        System.out.println("Waiting for JavaFX to initialize...");
        latch.await();
        System.out.println("JavaFX initialized.");
    }

    @Test
    public void testFillRect() {
        System.out.println("Starting testFillRect...");
        System.setProperty("w3canvas.backend", "javafx");
        System.out.println("Creating HTMLCanvasElement...");
        HTMLCanvasElement canvas = new HTMLCanvasElement();
        System.out.println("Getting 2D context...");
        ICanvasRenderingContext2D ctx = (ICanvasRenderingContext2D) canvas.jsFunction_getContext("2d");

        System.out.println("Setting fill style to red...");
        ctx.setFillStyle("red");
        System.out.println("Filling rect...");
        ctx.fillRect(10, 10, 100, 100);

        System.out.println("Getting pixel data...");
        int[] pixelData = ctx.getSurface().getPixelData(15, 15, 1, 1);

        System.out.println("Asserting pixel color...");
        // Check if the pixel is red
        // The format is ARGB, so red is 0xFFFF0000
        assertEquals(0xFFFF0000, pixelData[0]);
        System.out.println("testFillRect finished.");
    }
}
