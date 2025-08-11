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
    private static JavaCanvas instance;
    private boolean headless;

    public JavaCanvas(String resourcePath, boolean headless) {
        this.headless = headless;
        instance = this;
        basePath = resourcePath;
    }

    public RhinoRuntime getRhinoRuntime() {
        return runtime;
    }

    public static JavaCanvas getInstance() {
        return instance;
    }

    public static void resetForTesting() {
        instance = null;
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
            Node canvasNode = Document.getInstance().jsFunction_getElementById("canvas");
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

        try {
            ScriptableObject.defineClass(runtime.getScope(), Document.class, false, true);
            Document document = RhinoCanvasUtils.getScriptableInstance(Document.class, null);
            document.initInstance((RootPaneContainer) contentPane);
            runtime.defineProperty("document", document);

            ScriptableObject.defineClass(runtime.getScope(), Window.class, false, true);
            Window window = RhinoCanvasUtils.getScriptableInstance(Window.class, null);
            if (headless || contentPane == null) {
                window.initInstance(800, 600); // Default size for headless mode
            } else {
                window.initInstance(contentPane.getWidth(), contentPane.getHeight());
            }
            window.setDocument(document);
            runtime.defineProperty("window", window);

            ScriptableObject.defineClass(runtime.getScope(), Image.class, false, true);
            ScriptableObject.defineClass(runtime.getScope(), HTMLCanvasElement.class, false, true);

            ScriptableObject.defineClass(runtime.getScope(), CanvasRenderingContext2D.class, false, true);
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
        }
    }

    private void executeJSCode() throws ReflectiveOperationException {
        Properties properties = PropertiesHolder.getInstance().getProperties();
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
