package com.w3canvas.javacanvas.test;

import com.w3canvas.css.CSSParser;
import java.awt.Color;

public class TestCSSParser {
    public static void main(String[] args) {
        System.out.println("Running CSS Parser tests...");
        testColorParsing();
        System.out.println("CSS Parser tests passed!");
    }

    private static void testColorParsing() {
        // Test color names
        assert Color.WHITE.equals(CSSParser.parseColor("white"));
        assert Color.BLACK.equals(CSSParser.parseColor("black"));
        assert Color.RED.equals(CSSParser.parseColor("red"));
        assert Color.GREEN.equals(CSSParser.parseColor("green"));
        assert Color.BLUE.equals(CSSParser.parseColor("blue"));

        // Test hex codes
        assert new Color(255, 255, 255).equals(CSSParser.parseColor("#FFFFFF"));
        assert new Color(255, 0, 0).equals(CSSParser.parseColor("#FF0000"));
        assert new Color(0, 255, 0).equals(CSSParser.parseColor("#00FF00"));
        assert new Color(0, 0, 255).equals(CSSParser.parseColor("#0000FF"));
        assert new Color(17, 34, 51).equals(CSSParser.parseColor("#112233"));


        // Test rgb/rgba
        assert new Color(255, 0, 0).equals(CSSParser.parseColor("rgb(255,0,0)"));
        assert new Color(0, 255, 0).equals(CSSParser.parseColor("rgb(0,255,0)"));
        assert new Color(0, 0, 255).equals(CSSParser.parseColor("rgb(0,0,255)"));
        assert new Color(255, 0, 0, 128).equals(CSSParser.parseColor("rgba(255,0,0,128)"));

        // Test invalid colors
        assert Color.BLACK.equals(CSSParser.parseColor("invalid-color"));
        assert Color.BLACK.equals(CSSParser.parseColor("#invalid"));
        assert Color.BLACK.equals(CSSParser.parseColor("rgb(invalid)"));

        System.out.println("Color parsing tests passed.");
    }
}
