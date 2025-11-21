package com.w3canvas.javacanvas.test;

import com.w3canvas.javacanvas.server.RenderingServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestRenderingServer {

    private RenderingServer server;
    private static final int PORT = 8081; // Use a different port for testing

    @BeforeEach
    public void setUp() throws IOException {
        server = new RenderingServer(PORT);
        server.start();
    }

    @AfterEach
    public void tearDown() {
        server.stop();
    }

    @Test
    public void testHealthCheck() throws IOException {
        URL url = new URL("http://localhost:" + PORT + "/health");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        assertEquals(200, conn.getResponseCode());
        try (InputStream is = conn.getInputStream()) {
            String response = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            assertEquals("OK", response);
        }
    }

    @Test
    public void testRenderRequest() throws IOException {
        URL url = new URL("http://localhost:" + PORT + "/render");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);

        String script = "ctx.fillStyle='red'; ctx.fillRect(0,0,800,600);";
        try (OutputStream os = conn.getOutputStream()) {
            os.write(script.getBytes(StandardCharsets.UTF_8));
        }

        assertEquals(200, conn.getResponseCode());
        assertEquals("image/png", conn.getHeaderField("Content-Type"));

        try (InputStream is = conn.getInputStream()) {
            byte[] imageBytes = is.readAllBytes();
            assertTrue(imageBytes.length > 0, "Should return image data");
            // Check PNG signature (first 8 bytes)
            // 89 50 4E 47 0D 0A 1A 0A
            assertEquals((byte) 0x89, imageBytes[0]);
            assertEquals((byte) 0x50, imageBytes[1]);
            assertEquals((byte) 0x4E, imageBytes[2]);
            assertEquals((byte) 0x47, imageBytes[3]);
        }
    }
}
