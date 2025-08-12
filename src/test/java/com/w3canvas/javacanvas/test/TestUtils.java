package com.w3canvas.javacanvas.test;

import com.w3canvas.javacanvas.backend.rhino.impl.node.CanvasPixelArray;
import com.w3canvas.javacanvas.backend.rhino.impl.node.CanvasRenderingContext2D;
import com.w3canvas.javacanvas.backend.rhino.impl.node.Document;
import com.w3canvas.javacanvas.backend.rhino.impl.node.HTMLCanvasElement;
import com.w3canvas.javacanvas.interfaces.ICanvasPixelArray;
import com.w3canvas.javacanvas.interfaces.IImageData;
import com.w3canvas.javacanvas.backend.rhino.impl.node.Node;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptableObject;
import javax.swing.SwingUtilities;
import java.lang.reflect.InvocationTargetException;

public class TestUtils extends ScriptableObject {

    private boolean failed = false;

    @Override
    public String getClassName() {
        return "TestUtils";
    }

    public void jsFunction_assertPixel(final int x, final int y, final int r, final int g, final int b, final int a) {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    try {
                        Node canvasNode = null;
                        // Node canvasNode = Document.getInstance().jsFunction_getElementById("canvas");
                        if (canvasNode instanceof HTMLCanvasElement) {
                            HTMLCanvasElement canvas = (HTMLCanvasElement) canvasNode;
                            CanvasRenderingContext2D ctx = canvas.getCanvas();
                            if (ctx == null) {
                                System.err.println("Assertion failed: Canvas context is null.");
                                failed = true;
                                return;
                            }

                            IImageData imageData = ctx.getImageData(x, y, 1, 1);
                            ICanvasPixelArray data = imageData.getData();
                            int[] pixels = data.getPixels(0, 0, 1, 1);
                            int pixel = pixels[0];

                            int alpha = (pixel >> 24) & 0xff;
                            int red = (pixel >> 16) & 0xff;
                            int green = (pixel >> 8) & 0xff;
                            int blue = pixel & 0xff;

                            if (red != r || green != g || blue != b || alpha != a) {
                                System.err.println("Assertion failed at pixel (" + x + "," + y + "):");
                                System.err.println("  Expected: rgba(" + r + ", " + g + ", " + b + ", " + a + ")");
                                System.err.println("  Actual:   rgba(" + red + ", " + green + ", " + blue + ", " + alpha + ")");
                                failed = true;
                            } else {
                                 System.out.println("Assertion passed at pixel (" + x + "," + y + ")");
                            }

                        } else {
                            System.err.println("Assertion failed: Could not find canvas element.");
                            failed = true;
                        }
                    } catch (Exception e) {
                        System.err.println("An exception occurred during assertion on EDT:");
                        e.printStackTrace();
                        failed = true;
                    }
                }
            });
        } catch (InterruptedException | InvocationTargetException e) {
            System.err.println("An exception occurred scheduling assertion on EDT:");
            e.printStackTrace();
            failed = true;
        }
    }

    public void jsFunction_testComplete() {
        if (failed) {
            System.err.println("Tests failed.");
            System.exit(1);
        } else {
            System.out.println("All tests passed.");
            System.exit(0);
        }
    }
}
