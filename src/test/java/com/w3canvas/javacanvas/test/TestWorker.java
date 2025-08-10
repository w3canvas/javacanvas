package com.w3canvas.javacanvas.test;

import com.w3canvas.javacanvas.rt.JavaCanvas;

public class TestWorker {
    public static void main(String[] args) {
        try {
            System.out.println("Running Worker tests...");
            JavaCanvas.main(new String[]{"test/test-worker.html"});
            System.out.println("Worker tests ran without crashing.");
        } catch (Exception e) {
            System.err.println("Worker tests failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
