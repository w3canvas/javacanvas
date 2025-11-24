package com.w3canvas.javacanvas.test;

import com.w3canvas.javacanvas.rt.JavaCanvas;
import com.w3canvas.javacanvas.backend.rhino.impl.node.Document;
import com.w3canvas.javacanvas.backend.rhino.impl.node.Window;
import com.w3canvas.javacanvas.utils.PropertiesHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestCanvas {

    @Test
    public void testCanvasInitialization() {
        // Test that the JavaCanvas can be initialized in headless mode without
        // crashing.
        JavaCanvas canvas = new JavaCanvas(null, true);
        canvas.initializeBackend();
        assertNotNull(canvas, "JavaCanvas instance should not be null");
        assertNotNull(canvas.getRuntime(), "Runtime should not be null");
    }
}
