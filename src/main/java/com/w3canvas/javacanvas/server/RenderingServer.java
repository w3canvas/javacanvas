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
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

/**
 * A simple HTTP server that provides a "Canvas-as-a-Service" API.
 * Accepts JavaScript code via POST /render and returns the rendered PNG image.
 * Supports sessions via /create-session and X-Session-ID header.
 */
public class RenderingServer {

    private final HttpServer server;
    private final int port;
    private final Map<String, JavaCanvas> sessions = new ConcurrentHashMap<>();

    public RenderingServer(int port) throws IOException {
        this.port = port;
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        this.server.createContext("/render", new RenderHandler());
        this.server.createContext("/create-session", new CreateSessionHandler());
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

    class CreateSessionHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            if (!"POST".equalsIgnoreCase(t.getRequestMethod())) {
                t.sendResponseHeaders(405, -1);
                return;
            }

            String sessionId = UUID.randomUUID().toString();
            JavaCanvas javaCanvas = new JavaCanvas(".", true);
            javaCanvas.initializeBackend();

            try {
                // Create a default canvas
                HTMLCanvasElement canvas = (HTMLCanvasElement) javaCanvas.getDocument()
                        .jsFunction_createElement("canvas");
                canvas.jsSet_width(800);
                canvas.jsSet_height(600);
                javaCanvas.getDocument().addElement("canvas", canvas);

                // Expose 'canvas' and 'ctx'
                javaCanvas.getRuntime().putProperty("canvas", canvas);
                ICanvasRenderingContext2D ctx = (ICanvasRenderingContext2D) canvas.jsFunction_getContext("2d");
                javaCanvas.getRuntime().putProperty("ctx", ctx);

                sessions.put(sessionId, javaCanvas);
            } catch (Exception e) {
                e.printStackTrace();
                t.sendResponseHeaders(500, -1);
                return;
            }

            String response = "{\"sessionId\": \"" + sessionId + "\"}";
            t.getResponseHeaders().set("Content-Type", "application/json");
            t.sendResponseHeaders(200, response.length());
            try (OutputStream os = t.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }

    class RenderHandler implements HttpHandler {
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

                JavaCanvas javaCanvas;
                String sessionId = t.getRequestHeaders().getFirst("X-Session-ID");

                if (sessionId != null && sessions.containsKey(sessionId)) {
                    javaCanvas = sessions.get(sessionId);
                } else {
                    // Fallback to stateless for backward compatibility
                    javaCanvas = new JavaCanvas(".", true);
                    javaCanvas.initializeBackend();
                    try {
                        HTMLCanvasElement canvas = (HTMLCanvasElement) javaCanvas.getDocument()
                                .jsFunction_createElement("canvas");
                        canvas.jsSet_width(800);
                        canvas.jsSet_height(600);
                        javaCanvas.getDocument().addElement("canvas", canvas);
                        javaCanvas.getRuntime().putProperty("canvas", canvas);
                        ICanvasRenderingContext2D ctx = (ICanvasRenderingContext2D) canvas.jsFunction_getContext("2d");
                        javaCanvas.getRuntime().putProperty("ctx", ctx);
                    } catch (Exception e) {
                        throw new IOException("Failed to create canvas", e);
                    }
                }

                // Execute the script
                javaCanvas.executeCode(script);

                // Extract image (assume the first canvas in the document is the target)
                // In session mode, we use the pre-created canvas.
                // We need to retrieve it from the runtime or document.
                HTMLCanvasElement canvas = (HTMLCanvasElement) javaCanvas.getRuntime().getProperty("canvas");

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
