package com.w3canvas.javacanvas.test;

import com.w3canvas.css.CSSParser;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.awt.Color;

public class TestCSSParser {

    @Test
    public void testColorParsing() {
        // Test color names
        assertEquals(Color.WHITE, CSSParser.parseColor("white"));
        assertEquals(Color.BLACK, CSSParser.parseColor("black"));
        assertEquals(Color.RED, CSSParser.parseColor("red"));
        assertEquals(Color.GREEN, CSSParser.parseColor("green"));
        assertEquals(Color.BLUE, CSSParser.parseColor("blue"));

        // Test hex codes
        assertEquals(new Color(255, 255, 255), CSSParser.parseColor("#FFFFFF"));
        assertEquals(new Color(255, 0, 0), CSSParser.parseColor("#FF0000"));
        assertEquals(new Color(0, 255, 0), CSSParser.parseColor("#00FF00"));
        assertEquals(new Color(0, 0, 255), CSSParser.parseColor("#0000FF"));
        assertEquals(new Color(17, 34, 51), CSSParser.parseColor("#112233"));


        // Test rgb/rgba
        assertEquals(new Color(255, 0, 0), CSSParser.parseColor("rgb(255,0,0)"));
        assertEquals(new Color(0, 255, 0), CSSParser.parseColor("rgb(0,255,0)"));
        assertEquals(new Color(0, 0, 255), CSSParser.parseColor("rgb(0,0,255)"));
        assertEquals(new Color(255, 0, 0, 128), CSSParser.parseColor("rgba(255,0,0,128)"));

        // Test invalid colors
        assertEquals(Color.BLACK, CSSParser.parseColor("invalid-color"));
        assertEquals(Color.BLACK, CSSParser.parseColor("#invalid"));
        assertEquals(Color.BLACK, CSSParser.parseColor("rgb(invalid)"));
    }
    @Test
    public void testFontParsing() {
        java.util.Map<String, Object> font;

        font = CSSParser.parseFont("12px sans-serif");
        assertEquals("normal", font.get("style"));
        assertEquals("normal", font.get("weight"));
        assertEquals(12.0f, font.get("size"));
        assertEquals("sans-serif", font.get("family"));

        font = CSSParser.parseFont("italic 12px sans-serif");
        assertEquals("italic", font.get("style"));
        assertEquals("normal", font.get("weight"));
        assertEquals(12.0f, font.get("size"));
        assertEquals("sans-serif", font.get("family"));

        font = CSSParser.parseFont("bold 12px sans-serif");
        assertEquals("normal", font.get("style"));
        assertEquals("bold", font.get("weight"));
        assertEquals(12.0f, font.get("size"));
        assertEquals("sans-serif", font.get("family"));

        font = CSSParser.parseFont("italic bold 12px sans-serif");
        assertEquals("italic", font.get("style"));
        assertEquals("bold", font.get("weight"));
        assertEquals(12.0f, font.get("size"));
        assertEquals("sans-serif", font.get("family"));

        font = CSSParser.parseFont("12px 'Times New Roman'");
        assertEquals("normal", font.get("style"));
        assertEquals("normal", font.get("weight"));
        assertEquals(12.0f, font.get("size"));
        assertEquals("'Times New Roman'", font.get("family"));

        font = CSSParser.parseFont("700 12px sans-serif");
        assertEquals("normal", font.get("style"));
        assertEquals("700", font.get("weight"));
        assertEquals(12.0f, font.get("size"));
        assertEquals("sans-serif", font.get("family"));
    }
}
