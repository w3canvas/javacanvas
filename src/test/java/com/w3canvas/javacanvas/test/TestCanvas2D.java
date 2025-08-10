package com.w3canvas.javacanvas.test;

import com.w3canvas.javacanvas.rt.JavaCanvas;
import java.awt.Container;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;

public class TestCanvas2D {
    public static void main(String[] args) {
        // The base path is relative to the root of the project
        String basePath = "test";
        String scriptPath = "test.js";

        // Create the canvas instance
        final JavaCanvas canvas = new JavaCanvas("Test", basePath);
        canvas.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // We need to init the canvas after the window is shown, so we use a WindowListener.
        // This mimics the behavior of the original main method.
        canvas.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                canvas.init();
                // Now that the canvas is initialized, we can run the script.
                // We run this in a new thread to avoid blocking the AWT event dispatch thread.
                new Thread(() -> {
                    canvas.executeScript(scriptPath);
                }).start();
            }
        });

        // Make the canvas visible. This will trigger the windowOpened event.
        // We need to do this on the AWT event dispatch thread.
        javax.swing.SwingUtilities.invokeLater(() -> {
            canvas.setVisible(true);
        });

        // The test will run and eventually call System.exit() from TestUtils.
        // So, the main thread of the test runner can simply exit now.
        // Or we can add a timeout here in case the test hangs.
        // For now, we will just let it run.
    }
}
