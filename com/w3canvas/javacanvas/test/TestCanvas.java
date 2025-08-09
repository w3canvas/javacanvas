package com.w3canvas.javacanvas.test;

import com.w3canvas.javacanvas.rt.JavaCanvas;
import javax.swing.JFrame;

public class TestCanvas {
    public static void main(String[] args) {
        System.out.println("Running smoke test...");
        try {
            // We can't easily create a JavaCanvas directly because its constructor is private.
            // However, the main method of JavaCanvas does create an instance.
            // For a simple smoke test, we can just call the main method with no arguments.
            // This will at least tell us if the application can initialize itself without crashing.
            com.w3canvas.javacanvas.rt.JavaCanvas.main(new String[0]);
            System.out.println("Smoke test passed: JavaCanvas started without crashing.");
        } catch (Exception e) {
            System.err.println("Smoke test failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
