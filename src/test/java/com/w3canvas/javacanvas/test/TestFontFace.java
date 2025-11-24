package com.w3canvas.javacanvas.test;

import com.w3canvas.javacanvas.dom.FontFace;
import com.w3canvas.javacanvas.rt.JavaCanvas;
import fi.iki.elonen.NanoHTTPD;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestFontFace {

    private static final int PORT = 8080;
    private static WebServer server;

    @BeforeAll
    public static void setUp() throws IOException {
        server = new WebServer();
        server.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
    }

    @AfterAll
    public static void tearDown() {
        server.stop();
    }

    @Test
    public void testFontFaceLoading() throws Exception {
        JavaCanvas canvas = new JavaCanvas("test", true);
        canvas.init();

        canvas.getRuntime().exec(new FileReader("test/test-font-face.js"), "test-font-face.js");

        com.w3canvas.javacanvas.backend.rhino.impl.font.RhinoFontFaceSet fontFaceSet = canvas.getDocument()
                .jsGet_fonts();
        assertEquals(1, fontFaceSet.getFaces().size());

        FontFace fontFace = fontFaceSet.getFaces().iterator().next();
        fontFace.getLoaded().get(5, TimeUnit.SECONDS); // Wait for the font to load

        assertEquals("loaded", fontFace.getStatus());
    }

    private static class WebServer extends NanoHTTPD {

        public WebServer() {
            super(PORT);
        }

        @Override
        public Response serve(IHTTPSession session) {
            if (session.getUri().equals("/DejaVuSans.ttf")) {
                try {
                    File fontFile = new File("fonts/DejaVuSans.ttf");
                    InputStream is = new FileInputStream(fontFile);
                    return newChunkedResponse(Response.Status.OK, "font/ttf", is);
                } catch (IOException e) {
                    return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain",
                            "Could not load font file.");
                }
            }
            return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "Not Found");
        }
    }
}
