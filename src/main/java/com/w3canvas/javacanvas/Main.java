package com.w3canvas.javacanvas;

import com.w3canvas.javacanvas.rt.JavaCanvas;

/**
 * Simple entry point for AOT testing and CLI usage.
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("JavaCanvas Native Demo");
        if (args.length < 1) {
            System.out.println("Usage: javacanvas [--graal] <script.js>");
            // Don't exit, just run a built-in demo behavior for verification
            runDemo();
            return;
        }

        boolean useGraal = false;
        String scriptPath = null;

        for (String arg : args) {
            if (arg.equals("--graal")) {
                useGraal = true;
            } else {
                scriptPath = arg;
            }
        }

        if (scriptPath == null) {
            System.out.println("Usage: javacanvas [--graal] <script.js>");
            return;
        }

        System.out.println("Executing script: " + scriptPath + " (Engine: " + (useGraal ? "GraalJS" : "Rhino") + ")");
        JavaCanvas canvas = new JavaCanvas(".", true, useGraal);
        canvas.initializeBackend();
        canvas.executeScript(scriptPath);
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
                        "console.log('Drew red rectangle on ' + c);");
    }
}
