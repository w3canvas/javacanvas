package com.w3canvas.javacanvas.test;

import com.w3canvas.javacanvas.backend.awt.AwtGraphicsBackend;
import com.w3canvas.javacanvas.core.CoreCanvasRenderingContext2D;
import com.w3canvas.javacanvas.interfaces.ICanvasRenderingContext2D;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for filter functionality with actual canvas context
 */
public class TestFilterIntegration {

    private ICanvasRenderingContext2D ctx;

    @BeforeEach
    public void setUp() {
        // Create a canvas context with AWT backend
        AwtGraphicsBackend backend = new AwtGraphicsBackend();
        ctx = new CoreCanvasRenderingContext2D(null, backend, 200, 200);
    }

    @Test
    public void testDefaultFilter() {
        assertEquals("none", ctx.getFilter(), "Default filter should be 'none'");
    }

    @Test
    public void testSetGetFilter() {
        ctx.setFilter("blur(5px)");
        assertEquals("blur(5px)", ctx.getFilter());

        ctx.setFilter("brightness(150%)");
        assertEquals("brightness(150%)", ctx.getFilter());

        ctx.setFilter("blur(3px) brightness(120%) contrast(1.5)");
        assertEquals("blur(3px) brightness(120%) contrast(1.5)", ctx.getFilter());
    }

    @Test
    public void testSetNullFilter() {
        ctx.setFilter(null);
        assertEquals("none", ctx.getFilter(), "Null filter should default to 'none'");
    }

    @Test
    public void testSetEmptyFilter() {
        ctx.setFilter("");
        assertEquals("none", ctx.getFilter(), "Empty filter should default to 'none'");
    }

    @Test
    public void testFilterSaveRestore() {
        // Set initial filter
        ctx.setFilter("blur(5px)");
        assertEquals("blur(5px)", ctx.getFilter());

        // Save state
        ctx.save();

        // Change filter
        ctx.setFilter("brightness(150%)");
        assertEquals("brightness(150%)", ctx.getFilter());

        // Restore state
        ctx.restore();

        // Filter should be back to original
        assertEquals("blur(5px)", ctx.getFilter());
    }

    @Test
    public void testNestedFilterSaveRestore() {
        ctx.setFilter("grayscale(100%)");

        ctx.save();
        ctx.setFilter("sepia(50%)");

        ctx.save();
        ctx.setFilter("blur(3px)");
        assertEquals("blur(3px)", ctx.getFilter());

        ctx.restore();
        assertEquals("sepia(50%)", ctx.getFilter());

        ctx.restore();
        assertEquals("grayscale(100%)", ctx.getFilter());
    }

    @Test
    public void testFilterWithDrawingOperations() {
        // This test verifies that setting filters doesn't cause crashes
        // when used with actual drawing operations

        ctx.setFilter("blur(5px)");
        ctx.fillRect(10, 10, 50, 50);

        ctx.setFilter("brightness(150%)");
        ctx.strokeRect(70, 10, 50, 50);

        ctx.setFilter("grayscale(100%)");
        ctx.beginPath();
        ctx.arc(50, 100, 30, 0, 2 * Math.PI, false);
        ctx.fill();

        ctx.setFilter("none");
        ctx.fillRect(10, 150, 50, 30);

        // If we get here without crashing, the test passes
        assertTrue(true);
    }

    @Test
    public void testMultipleFiltersWithDrawing() {
        ctx.setFilter("blur(2px) brightness(120%) contrast(1.5)");
        ctx.setFillStyle("red");
        ctx.fillRect(10, 10, 100, 100);

        // Verify filter is still set
        assertEquals("blur(2px) brightness(120%) contrast(1.5)", ctx.getFilter());

        // Change to different filters
        ctx.setFilter("sepia(75%) saturate(150%)");
        ctx.setFillStyle("blue");
        ctx.fillRect(120, 10, 100, 100);

        assertEquals("sepia(75%) saturate(150%)", ctx.getFilter());
    }

    @Test
    public void testResetAfterMultipleOperations() {
        // Test that reset() properly resets the filter
        ctx.setFilter("blur(10px)");
        ctx.fillRect(10, 10, 50, 50);

        ctx.reset();

        assertEquals("none", ctx.getFilter(), "Filter should be reset to 'none'");
    }

    @Test
    public void testAllFilterTypes() {
        // Test that all filter types can be set without errors
        String[] filters = {
            "blur(5px)",
            "brightness(150%)",
            "contrast(200%)",
            "grayscale(100%)",
            "sepia(75%)",
            "saturate(150%)",
            "hue-rotate(90deg)",
            "invert(100%)",
            "opacity(50%)",
            "drop-shadow(10px 20px 5px black)"
        };

        for (String filter : filters) {
            ctx.setFilter(filter);
            assertEquals(filter, ctx.getFilter(), "Filter should be set to: " + filter);

            // Draw something to ensure no crashes
            ctx.fillRect(10, 10, 50, 50);
        }
    }
}
