package com.w3canvas.javacanvas.test;

import com.w3canvas.javacanvas.backend.javafx.JavaFXFont;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(ApplicationExtension.class)
public class PureJavaFXFontTest extends ApplicationTest {

    @Start
    public void start(Stage stage) {
    }

    @Test
    public void testCreateJavaFXFont() {
        interact(() -> {
            JavaFXFont font = new JavaFXFont("sans-serif", 12, "italic", "bold");
            assertEquals("sans-serif", font.getFamily());
            assertEquals(12, font.getSize());
            assertEquals("italic", font.getStyle());
            assertEquals("bold", font.getWeight());
            assertNotNull(font.getFont());
            assertEquals("SansSerif", font.getFont().getFamily());
            assertEquals(12, font.getFont().getSize());
            assertTrue(font.getFont().getStyle().contains("Bold"));
            assertTrue(font.getFont().getStyle().contains("Italic"));
        });
    }

    @Test
    public void testFillTextPure() {
        interact(() -> {
            Canvas canvas = new Canvas(100, 100);
            GraphicsContext gc = canvas.getGraphicsContext2D();

            gc.setFont(new Font("sans-serif", 30));
            gc.setFill(Color.BLUE);
            gc.fillText("Hello", 10, 40);

            WritableImage wim = new WritableImage(100, 100);
            canvas.snapshot(null, wim);

            boolean bluePixelFound = false;
            for (int x = 10; x < 50; x++) {
                for (int y = 20; y < 50; y++) {
                    Color color = wim.getPixelReader().getColor(x, y);
                    if (color.equals(Color.BLUE)) {
                        bluePixelFound = true;
                        break;
                    }
                }
                if (bluePixelFound) {
                    break;
                }
            }
            assertTrue(bluePixelFound, "Did not find a blue pixel in the rendered text.");
        });
    }
}
