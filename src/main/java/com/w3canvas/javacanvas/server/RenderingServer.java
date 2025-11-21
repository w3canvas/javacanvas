package com.w3canvas.javacanvas.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.w3canvas.javacanvas.backend.rhino.impl.node.HTMLCanvasElement;
import com.w3canvas.javacanvas.interfaces.ICanvasRenderingContext2D;
import com.w3canvas.javacanvas.rt.JavaCanvas;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;

/**
 * A simple HTTP server that provides a "Canvas-as-a-Service" API.
 * Accepts JavaScript code via POST /render and returns the rendered PNG image.
 */
public class RenderingServer {

    private final HttpServer server;
    private final int port;

    public RenderingServer(int port) throws IOException {
        this.port = port;
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        this.server.createContext("/render", new RenderHandler());
        this.server.createContext("/health", t -> {
            String response = "OK";
            t.sendResponseHeaders(200, response.length());
            try (OutputStream os = t.getResponseBody()) {
                os.write(response.getBytes());
            }
        });
        this.server.setExecutor(Executors.newCachedThreadPool());
    }

    public void start() {
        System.out.println("Rendering Server started on port " + port);
        server.start();
    }

    public void stop() {
        server.stop(0);
        System.out.println("Rendering Server stopped");
    }

    public static void main(String[] args) throws IOException {
        int port = 8080;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }
        new RenderingServer(port).start();
    }

    static class RenderHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            if (!"POST".equalsIgnoreCase(t.getRequestMethod())) {
                t.sendResponseHeaders(405, -1);
                return;
            }

            try {
                // Read script from body
                InputStream is = t.getRequestBody();
                String script = new String(is.readAllBytes(), StandardCharsets.UTF_8);

                // Initialize Canvas environment
                // Use headless mode
                JavaCanvas javaCanvas = new JavaCanvas(".", true);
                javaCanvas.initializeBackend();

                // Create a canvas element for the script to use
                // We provide a default 'canvas' variable of 800x600
                HTMLCanvasElement canvas = (HTMLCanvasElement) javaCanvas.getDocument().jsFunction_createElement("canvas");
                canvas.jsSet_width(800);
                canvas.jsSet_height(600);
                javaCanvas.getDocument().addElement("canvas", canvas);

                // Expose 'canvas' and 'ctx' to the script
                org.mozilla.javascript.Scriptable scope = javaCanvas.getRhinoRuntime().getScope();
                org.mozilla.javascript.ScriptableObject.putProperty(scope, "canvas", canvas);
                ICanvasRenderingContext2D ctx = (ICanvasRenderingContext2D) canvas.jsFunction_getContext("2d");
                org.mozilla.javascript.ScriptableObject.putProperty(scope, "ctx", ctx);

                // Execute the script
                javaCanvas.executeCode(script);

                // Extract image
                BufferedImage image = canvas.getImage();

                // Write PNG
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(image, "png", baos);
                byte[] responseBytes = baos.toByteArray();

                // Send response
                t.getResponseHeaders().set("Content-Type", "image/png");
                t.sendResponseHeaders(200, responseBytes.length);
                try (OutputStream os = t.getResponseBody()) {
                    os.write(responseBytes);
                }

            } catch (Exception e) {
                e.printStackTrace();
                String error = "Error executing script: " + e.getMessage();
                t.sendResponseHeaders(500, error.length());
                try (OutputStream os = t.getResponseBody()) {
                    os.write(error.getBytes());
                }
            }
        }
    }
}
