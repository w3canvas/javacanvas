package com.w3canvas.javacanvas.test;

import com.w3canvas.javacanvas.backend.awt.AwtFont;
import org.junit.jupiter.api.Test;
import java.awt.Font;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class PureAWTFontTest {

    @Test
    public void testCreateAwtFont() {
        AwtFont font = new AwtFont("sans-serif", 12, "italic", "bold");
        assertEquals("sans-serif", font.getFamily());
        assertEquals(12, font.getSize());
        assertEquals("italic", font.getStyle());
        assertEquals("bold", font.getWeight());
        assertNotNull(font.getFont());
        assertEquals("Dialog", font.getFont().getFamily());
        assertEquals(12, font.getFont().getSize());
        assertEquals(Font.BOLD | Font.ITALIC, font.getFont().getStyle());
    }
}
