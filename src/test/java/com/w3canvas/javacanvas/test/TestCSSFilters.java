package com.w3canvas.javacanvas.test;

import com.w3canvas.javacanvas.core.CSSFilterParser;
import com.w3canvas.javacanvas.core.FilterFunction;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test CSS filter parsing and functionality
 */
public class TestCSSFilters {

    @Test
    public void testParseNoneFilter() {
        List<FilterFunction> filters = CSSFilterParser.parse("none");
        assertTrue(filters.isEmpty(), "none should result in empty filter list");
    }

    @Test
    public void testParseBlurFilter() {
        List<FilterFunction> filters = CSSFilterParser.parse("blur(5px)");
        assertEquals(1, filters.size(), "Should parse one filter");

        FilterFunction filter = filters.get(0);
        assertEquals(FilterFunction.FilterType.BLUR, filter.getType());
        assertEquals(5.0, filter.getDoubleParam(0), 0.001);
    }

    @Test
    public void testParseBrightnessFilter() {
        List<FilterFunction> filters = CSSFilterParser.parse("brightness(150%)");
        assertEquals(1, filters.size());

        FilterFunction filter = filters.get(0);
        assertEquals(FilterFunction.FilterType.BRIGHTNESS, filter.getType());
        assertEquals(1.5, filter.getDoubleParam(0), 0.001); // 150% = 1.5
    }

    @Test
    public void testParseContrastFilter() {
        List<FilterFunction> filters = CSSFilterParser.parse("contrast(200%)");
        assertEquals(1, filters.size());

        FilterFunction filter = filters.get(0);
        assertEquals(FilterFunction.FilterType.CONTRAST, filter.getType());
        assertEquals(2.0, filter.getDoubleParam(0), 0.001);
    }

    @Test
    public void testParseGrayscaleFilter() {
        List<FilterFunction> filters = CSSFilterParser.parse("grayscale(100%)");
        assertEquals(1, filters.size());

        FilterFunction filter = filters.get(0);
        assertEquals(FilterFunction.FilterType.GRAYSCALE, filter.getType());
        assertEquals(1.0, filter.getDoubleParam(0), 0.001);
    }

    @Test
    public void testParseSepiaFilter() {
        List<FilterFunction> filters = CSSFilterParser.parse("sepia(75%)");
        assertEquals(1, filters.size());

        FilterFunction filter = filters.get(0);
        assertEquals(FilterFunction.FilterType.SEPIA, filter.getType());
        assertEquals(0.75, filter.getDoubleParam(0), 0.001);
    }

    @Test
    public void testParseSaturateFilter() {
        List<FilterFunction> filters = CSSFilterParser.parse("saturate(150%)");
        assertEquals(1, filters.size());

        FilterFunction filter = filters.get(0);
        assertEquals(FilterFunction.FilterType.SATURATE, filter.getType());
        assertEquals(1.5, filter.getDoubleParam(0), 0.001);
    }

    @Test
    public void testParseHueRotateFilter() {
        List<FilterFunction> filters = CSSFilterParser.parse("hue-rotate(90deg)");
        assertEquals(1, filters.size());

        FilterFunction filter = filters.get(0);
        assertEquals(FilterFunction.FilterType.HUE_ROTATE, filter.getType());
        assertEquals(90.0, filter.getDoubleParam(0), 0.001);
    }

    @Test
    public void testParseHueRotateFilterRad() {
        // Test with radians
        List<FilterFunction> filters = CSSFilterParser.parse("hue-rotate(1.57rad)");
        assertEquals(1, filters.size());

        FilterFunction filter = filters.get(0);
        assertEquals(FilterFunction.FilterType.HUE_ROTATE, filter.getType());
        assertTrue(Math.abs(90.0 - filter.getDoubleParam(0)) < 1.0, "1.57 radians should be approximately 90 degrees");
    }

    @Test
    public void testParseInvertFilter() {
        List<FilterFunction> filters = CSSFilterParser.parse("invert(100%)");
        assertEquals(1, filters.size());

        FilterFunction filter = filters.get(0);
        assertEquals(FilterFunction.FilterType.INVERT, filter.getType());
        assertEquals(1.0, filter.getDoubleParam(0), 0.001);
    }

    @Test
    public void testParseOpacityFilter() {
        List<FilterFunction> filters = CSSFilterParser.parse("opacity(50%)");
        assertEquals(1, filters.size());

        FilterFunction filter = filters.get(0);
        assertEquals(FilterFunction.FilterType.OPACITY, filter.getType());
        assertEquals(0.5, filter.getDoubleParam(0), 0.001);
    }

    @Test
    public void testParseDropShadowFilter() {
        List<FilterFunction> filters = CSSFilterParser.parse("drop-shadow(10px 20px 5px black)");
        assertEquals(1, filters.size());

        FilterFunction filter = filters.get(0);
        assertEquals(FilterFunction.FilterType.DROP_SHADOW, filter.getType());
        assertEquals(10.0, filter.getDoubleParam(0), 0.001); // offset-x
        assertEquals(20.0, filter.getDoubleParam(1), 0.001); // offset-y
        assertEquals(5.0, filter.getDoubleParam(2), 0.001);  // blur-radius
        assertEquals("black", filter.getStringParam(3));     // color
    }

    @Test
    public void testParseMultipleFilters() {
        List<FilterFunction> filters = CSSFilterParser.parse("blur(5px) brightness(150%) contrast(1.5)");
        assertEquals(3, filters.size(), "Should parse three filters");

        // First filter: blur
        FilterFunction blur = filters.get(0);
        assertEquals(FilterFunction.FilterType.BLUR, blur.getType());
        assertEquals(5.0, blur.getDoubleParam(0), 0.001);

        // Second filter: brightness
        FilterFunction brightness = filters.get(1);
        assertEquals(FilterFunction.FilterType.BRIGHTNESS, brightness.getType());
        assertEquals(1.5, brightness.getDoubleParam(0), 0.001);

        // Third filter: contrast
        FilterFunction contrast = filters.get(2);
        assertEquals(FilterFunction.FilterType.CONTRAST, contrast.getType());
        assertEquals(1.5, contrast.getDoubleParam(0), 0.001);
    }

    @Test
    public void testParseNumberWithoutUnit() {
        // Some filters can accept numbers without units
        List<FilterFunction> filters = CSSFilterParser.parse("brightness(1.5)");
        assertEquals(1, filters.size());

        FilterFunction filter = filters.get(0);
        assertEquals(FilterFunction.FilterType.BRIGHTNESS, filter.getType());
        assertEquals(1.5, filter.getDoubleParam(0), 0.001);
    }

    @Test
    public void testParseEmptyString() {
        List<FilterFunction> filters = CSSFilterParser.parse("");
        assertTrue(filters.isEmpty(), "Empty string should result in empty filter list");
    }

    @Test
    public void testParseNullString() {
        List<FilterFunction> filters = CSSFilterParser.parse(null);
        assertTrue(filters.isEmpty(), "Null string should result in empty filter list");
    }

    @Test
    public void testIsValid() {
        assertTrue(CSSFilterParser.isValid("blur(5px)"));
        assertTrue(CSSFilterParser.isValid("brightness(150%)"));
        assertTrue(CSSFilterParser.isValid("blur(5px) brightness(150%)"));
        assertTrue(CSSFilterParser.isValid("none"));
        assertTrue(CSSFilterParser.isValid(""));
        assertTrue(CSSFilterParser.isValid(null));
        assertFalse(CSSFilterParser.isValid("invalid-filter"));
        assertFalse(CSSFilterParser.isValid("random text"));
    }

    @Test
    public void testFilterToString() {
        FilterFunction blur = new FilterFunction(FilterFunction.FilterType.BLUR);
        blur.addParam(5.0);

        String str = blur.toString();
        assertTrue(str.contains("blur"), "toString should contain filter name");
        assertTrue(str.contains("5"), "toString should contain parameter value");
    }
}
