package com.w3canvas.javacanvas.test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;
import javafx.scene.text.Font;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(ApplicationExtension.class)
public class TestJavaFXFont extends ApplicationTest {

    @Start
    public void start(Stage stage) {
        // The TestFX Application thread starts here.
        // This is required for JavaFX to initialize properly.
    }

    @Test
    public void testDrawTextWithCustomFont() {
        interact(() -> {
            try {
                // Load the font
                InputStream is = new java.io.FileInputStream("fonts/DejaVuSans.ttf");
                Font font = Font.loadFont(is, 32);
                if (font == null) {
                    throw new RuntimeException("Could not load font");
                }

                // Create a canvas and draw text
                Canvas canvas = new Canvas(200, 200);
                GraphicsContext gc = canvas.getGraphicsContext2D();

                // Fill background with white to have a consistent background color
                gc.setFill(Color.WHITE);
                gc.fillRect(0, 0, 200, 200);

                gc.setFont(font);
                gc.setFill(Color.BLACK);
                gc.fillText("Hello", 50, 100);

                // Take a snapshot
                WritableImage snapshot = new WritableImage(200, 200);
                canvas.snapshot(null, snapshot);

                // Pixel test
                // Check a pixel within the 'H' of "Hello"
                int pixelOnText = snapshot.getPixelReader().getArgb(55, 85);
                // Check a pixel in the background
                int pixelOffText = snapshot.getPixelReader().getArgb(10, 10);

                // Assert that the pixel on the text is black
                assertEquals(0xFF000000, pixelOnText, "Pixel on text should be black");

                // Assert that the pixel off the text is white
                assertEquals(0xFFFFFFFF, pixelOffText, "Pixel off text should be white");

            } catch (java.io.FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
