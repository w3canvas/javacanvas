/*

		JavaCanvas by Jumis, Inc

	Unless otherwise noted:
	All source code is hereby released into public domain and the CC0 license.
	http://creativecommons.org/publicdomain/zero/1.0/
	http://creativecommons.org/licenses/publicdomain/

	Based on Rhino Canvas by Stefan Haustein
	Lead development by Alex Padalka and Charles Pritchard
	with code review and support from Paul Wheaton


*/
package com.w3canvas.javacanvas.rt;

import java.awt.Container;
import javax.swing.RootPaneContainer;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Properties;

import com.w3canvas.javacanvas.backend.rhino.impl.event.JSMouseEvent;
import com.w3canvas.javacanvas.backend.rhino.impl.gradient.LinearCanvasGradient;
import com.w3canvas.javacanvas.backend.rhino.impl.gradient.RadialCanvasGradient;
import com.w3canvas.javacanvas.backend.rhino.impl.gradient.RhinoCanvasGradient;
import com.w3canvas.javacanvas.backend.rhino.impl.node.CanvasPattern;
import com.w3canvas.javacanvas.backend.rhino.impl.node.CanvasPixelArray;
import com.w3canvas.javacanvas.backend.rhino.impl.node.CanvasRenderingContext2D;
import com.w3canvas.javacanvas.backend.rhino.impl.node.Document;
import com.w3canvas.javacanvas.backend.rhino.impl.node.HTMLCanvasElement;
import com.w3canvas.javacanvas.backend.rhino.impl.node.Image;
import com.w3canvas.javacanvas.backend.rhino.impl.node.ImageData;
import com.w3canvas.javacanvas.backend.rhino.impl.node.Node;
import com.w3canvas.javacanvas.backend.rhino.impl.node.StyleHolder;
import com.w3canvas.javacanvas.backend.rhino.impl.node.TextMetrics;
import com.w3canvas.javacanvas.backend.rhino.impl.node.Window;
import com.w3canvas.javacanvas.utils.PropertiesHolder;
import com.w3canvas.javacanvas.utils.RhinoCanvasUtils;
import com.w3canvas.javacanvas.utils.ScriptLogger;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.ScriptableObject;

@SuppressWarnings("serial")
public class JavaCanvas {

    private RhinoRuntime runtime;
    private String basePath;
    private boolean headless;
    private PropertiesHolder propertiesHolder;
    private Document document;
    private Window window;

    public JavaCanvas(String resourcePath, boolean headless) {
        this.headless = headless;
        basePath = resourcePath;
        this.propertiesHolder = new PropertiesHolder();
    }

    public RhinoRuntime getRhinoRuntime() {
        return runtime;
    }

    public Document getDocument() {
        return document;
    }

    public Window getWindow() {
        return window;
    }

    public static void resetForTesting() {
        // This is no longer needed and can be removed once all tests are updated.
    }

    public Container getContentPane() {
        // This method is now a stub for compatibility.
        // In the refactored architecture, the JFrame is managed by JavaCanvasApp.
        // If direct access to the content pane is needed, it should be obtained
        // from the JavaCanvasApp instance.
        return null;
    }

    public void saveScreenshot(String path) {
        try {
            Node canvasNode = this.document.jsFunction_getElementById("canvas");
            if (canvasNode instanceof HTMLCanvasElement) {
                HTMLCanvasElement canvasElement = (HTMLCanvasElement) canvasNode;
                BufferedImage image = canvasElement.getImage();
                javax.imageio.ImageIO.write(image, "png", new java.io.File(path));
                System.out.println("Screenshot saved to " + path);
            } else {
                System.err.println("Could not find canvas element with id 'canvas'");
            }
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    public void executeScript(String scriptPath) {
        try {
            java.io.File scriptFile = new java.io.File(basePath, scriptPath);
            java.io.Reader reader = new java.io.FileReader(scriptFile);
            runtime.getScope().put("documentBase", runtime.getScope(), scriptFile.getParentFile().toURI().toString());
            Context context = Context.enter();
            try {
                runtime.exec(reader, scriptPath);
            } finally {
                Context.exit();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void init(Container contentPane) {
        initializeBackend(contentPane);
    }

    public void init() {
        init(null);
    }

    public void initializeBackend() {
        initializeBackend(null);
    }

    public void initializeBackend(Container contentPane) {
        runtime = new RhinoRuntime();
        Context context = Context.enter();
        try {
            context.putThreadLocal("runtime", runtime);

            ScriptableObject.defineClass(runtime.getScope(), Document.class, false, true);
            this.document = (Document) context.newObject(runtime.getScope(), "Document");
            this.document.init((RootPaneContainer) contentPane);
            runtime.defineProperty("document", this.document);

            ScriptableObject.defineClass(runtime.getScope(), Window.class, false, true);
            this.window = (Window) context.newObject(runtime.getScope(), "Window");
            if (headless || contentPane == null) {
                this.window.init(800, 600); // Default size for headless mode
            } else {
                this.window.init(contentPane.getWidth(), contentPane.getHeight());
            }
            this.window.setDocument(document);
            runtime.defineProperty("window", this.window);

            ScriptableObject.defineClass(runtime.getScope(), Image.class, false, true);
            ScriptableObject.defineClass(runtime.getScope(), HTMLCanvasElement.class, false, true);

            ScriptableObject.defineClass(runtime.getScope(), CanvasRenderingContext2D.class, false, true);
            ScriptableObject.defineClass(runtime.getScope(), RhinoCanvasGradient.class, false, true);
            ScriptableObject.defineClass(runtime.getScope(), LinearCanvasGradient.class, false, true);
            ScriptableObject.defineClass(runtime.getScope(), RadialCanvasGradient.class, false, true);
            ScriptableObject.defineClass(runtime.getScope(), CanvasPattern.class, false, true);
            ScriptableObject.defineClass(runtime.getScope(), TextMetrics.class, false, true);
            ScriptableObject.defineClass(runtime.getScope(), CanvasPixelArray.class, false, true);
            ScriptableObject.defineClass(runtime.getScope(), ImageData.class, false, true);
            ScriptableObject.defineClass(runtime.getScope(), StyleHolder.class, false, true);
            ScriptableObject.defineClass(runtime.getScope(), JSMouseEvent.class, false, true);
            ScriptableObject.defineClass(runtime.getScope(), com.w3canvas.javacanvas.js.worker.Worker.class, false, true);

            runtime.defineProperty("log", new ScriptLogger());
            runtime.defineProperty("console", new ScriptLogger());
            runtime.setSource(basePath);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Context.exit();
        }
    }

    private void executeJSCode() throws ReflectiveOperationException {
        Properties properties = propertiesHolder.getProperties();
        List<String> jsClasses = PropertiesHolder.getJSClasses(properties);

        for (String className : jsClasses) {
            Object instance = Class.forName(className).getDeclaredConstructor().newInstance();
            if (instance instanceof Script) {
                ((Script) instance).exec(Context.getCurrentContext(), runtime.getScope());
            }
        }
    }

    protected void readContent(StringBuffer sb) throws FileNotFoundException, IOException {
        String[] sources = new String[] { "Global.js", "Canvas.js", "Event.js", "Math.js", "Project.js",
                "Project.X_Slide.js", "Raster2D.js", "Raster2D.CLUT.js", "Raster2D.ColorMatrix.js", "Raster2D.Crop.js",
                "Raster2D.GeoMatrix.js", "Raster2D.Histogram.js", "Raster2D.Levels.js", "Vector2D.Font.js",
                "Vector2D.Font.Arial.js", "Vector2D.SVG.js", "Scriptograph.js" };

        int i = 1;
        BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(
                "E:/dev/prj/JavaCanvas/trunk/src/js/development/test.js")));

        while (true) {
            String line = r.readLine();
            if (line == null) {
                break;
            }
            sb.append(line).append('\n');
            i++;
        }
        r.close();
    }
}
