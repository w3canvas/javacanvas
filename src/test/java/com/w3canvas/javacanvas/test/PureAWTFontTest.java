package com.w3canvas.javacanvas.test;

import com.w3canvas.javacanvas.backend.awt.AwtFont;
import org.junit.jupiter.api.Test;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.awt.Color;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    public void testFillTextPure() {
        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();

        g2d.setFont(new Font("sans-serif", Font.PLAIN, 30));
        g2d.setColor(Color.BLUE);
        g2d.drawString("Hello", 10, 40);

        boolean bluePixelFound = false;
        for (int x = 10; x < 50; x++) {
            for (int y = 20; y < 50; y++) {
                if (img.getRGB(x, y) == Color.BLUE.getRGB()) {
                    bluePixelFound = true;
                    break;
                }
            }
            if (bluePixelFound) {
                break;
            }
        }
        assertTrue(bluePixelFound, "Did not find a blue pixel in the rendered text.");
    }
}
