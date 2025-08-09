package com.w3canvas.javacanvas.test;

import com.w3canvas.javacanvas.rt.JavaCanvas;

public class TestCanvas2D {
    public static void main(String[] args) {
        try {
            System.out.println("Running Canvas2D tests...");
            JavaCanvas.main(new String[]{"test/test.html"});
            System.out.println("Canvas2D tests ran without crashing.");
        } catch (Exception e) {
            System.err.println("Canvas2D tests failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
