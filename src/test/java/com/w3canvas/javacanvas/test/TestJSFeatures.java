package com.w3canvas.javacanvas.test;

import com.w3canvas.javacanvas.rt.JavaCanvas;
import org.junit.jupiter.api.Test;

public class TestJSFeatures {
    @Test
    public void testFeatures() {
        JavaCanvas canvas = new JavaCanvas("src/js/development", true);
        canvas.initializeBackend();
        canvas.executeScript("test_features.js");
    }
}
