package com.w3canvas.javacanvas;

import com.w3canvas.javacanvas.rt.JavaCanvas;

/**
 * Simple entry point for AOT testing and CLI usage.
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("JavaCanvas Native Demo");
        if (args.length < 1) {
            System.out.println("Usage: javacanvas <script.js>");
            // Don't exit, just run a built-in demo behavior for verification
            runDemo();
            return;
        }

        System.out.println("Executing script: " + args[0]);
        JavaCanvas canvas = new JavaCanvas(".", true);
        canvas.initializeBackend();
        canvas.executeScript(args[0]);
        System.out.println("Execution complete.");
    }

    private static void runDemo() {
        System.out.println("Running internal demo...");
        JavaCanvas canvas = new JavaCanvas(".", true);
        canvas.initializeBackend();
        // Execute a simple script string
        canvas.executeCode("console.log('Hello from Native Canvas!');");
        // We need to verify canvas creation to really test AOT
        canvas.executeCode(
            "var c = document.createElement('canvas');" +
            "c.width = 100; c.height = 100;" +
            "var ctx = c.getContext('2d');" +
            "ctx.fillStyle = 'red';" +
            "ctx.fillRect(10, 10, 50, 50);" +
            "console.log('Drew red rectangle on ' + c);"
        );
    }
}
