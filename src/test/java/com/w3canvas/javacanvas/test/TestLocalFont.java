package com.w3canvas.javacanvas.test;

import com.w3canvas.javacanvas.dom.FontFace;
import com.w3canvas.javacanvas.rt.JavaCanvas;
import org.junit.jupiter.api.Test;
import java.io.FileReader;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestLocalFont {

    @Test
    public void testLocalFontLoading() throws Exception {
        try {
            JavaCanvas canvas = new JavaCanvas("test", true);
            canvas.init();

            System.out.println("Executing script...");
            canvas.getRuntime().exec(new FileReader("test/test-local-font.js"), "test-local-font.js");
            System.out.println("Script executed.");

            com.w3canvas.javacanvas.backend.rhino.impl.font.RhinoFontFaceSet fontFaceSet = canvas.getDocument()
                    .jsGet_fonts();
            assertEquals(1, fontFaceSet.getFaces().size());

            FontFace fontFace = fontFaceSet.getFaces().iterator().next();
            System.out.println("Waiting for font load...");
            try {
                fontFace.getLoaded().get(5, TimeUnit.SECONDS);
            } catch (Exception e) {
                System.out.println("Font load future failed: " + e);
                e.printStackTrace();
            }

            System.out.println("Font status: " + fontFace.getStatus());
            assertEquals("loaded", fontFace.getStatus());
            assertEquals("LocalFont", fontFace.getFamily());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
