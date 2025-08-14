package com.w3canvas.javacanvas.test;

import com.w3canvas.javacanvas.backend.javafx.JavaFXFont;
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
}
