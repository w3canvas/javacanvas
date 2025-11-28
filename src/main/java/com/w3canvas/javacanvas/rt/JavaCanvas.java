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

import com.w3canvas.javacanvas.interfaces.IWindowHost;
import com.w3canvas.javacanvas.backend.awt.SwingWindowHost;
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
import com.w3canvas.javacanvas.backend.rhino.impl.node.RhinoCanvasPattern;
import com.w3canvas.javacanvas.backend.rhino.impl.node.DOMMatrix;
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
import org.mozilla.javascript.Scriptable;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.ScriptableObject;

@SuppressWarnings("serial")
public class JavaCanvas {

    private JSRuntime runtime;
    private String basePath;
    private boolean headless;
    private boolean useGraal;
    private PropertiesHolder propertiesHolder;
    private Document document;
    private Object graalDocument;
    private Window window;
    private Object graalWindow;

    public JavaCanvas(String resourcePath, boolean headless) {
        this(resourcePath, headless, false);
    }

    public JavaCanvas(String resourcePath, boolean headless, boolean useGraal) {
        this.headless = headless;
        this.basePath = resourcePath;
        this.useGraal = useGraal;
        this.propertiesHolder = new PropertiesHolder();
    }

    public JSRuntime getRuntime() {
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
            System.err.println("ERROR: Failed to save screenshot to '" + path + "': " + e.getMessage());
        }
    }

    public void executeScript(String scriptPath) {
        try {
            java.io.File scriptFile;
            java.io.File inputFile = new java.io.File(scriptPath);
            if (inputFile.isAbsolute()) {
                scriptFile = inputFile;
            } else {
                scriptFile = new java.io.File(basePath, scriptPath);
            }
            java.io.Reader reader = new java.io.FileReader(scriptFile);

            // Set documentBase property
            runtime.putProperty("documentBase", scriptFile.getParentFile().toURI().toString());

            if (runtime instanceof RhinoRuntime) {
                Context.enter();
                try {
                    runtime.exec(reader, scriptPath);
                } finally {
                    Context.exit();
                }
            } else {
                runtime.exec(reader, scriptPath);
            }
        } catch (Exception e) {
            System.err.println("ERROR: Failed to execute script '" + scriptPath + "': " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Object executeCode(String code) {
        return runtime.exec(code);
    }

    private IWindowHost windowHost;

    // ... (constructors)

    public void init(Object container) {
        initializeBackend(container);
    }

    public void init() {
        init(null);
    }

    public void initializeBackend() {
        initializeBackend(null);
    }

    public void initializeBackend(Object container) {
        if (container instanceof IWindowHost) {
            this.windowHost = (IWindowHost) container;
        } else if (container instanceof RootPaneContainer) {
            this.windowHost = new SwingWindowHost((RootPaneContainer) container);
        }

        if (useGraal) {
            try {
                runtime = (JSRuntime) Class.forName("com.w3canvas.javacanvas.backend.graal.GraalRuntime")
                        .getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException("GraalJS not available", e);
            }
            initializeCommon();
        } else {
            runtime = new RhinoRuntime();
            Context.enter();
            try {
                initializeCommon();
            } finally {
                Context.exit();
            }
        }
    }

    private void initializeCommon() {
        try {
            if (useGraal) {
                this.graalDocument = Class.forName("com.w3canvas.javacanvas.backend.graal.impl.node.GraalDocument")
                        .getDeclaredConstructor().newInstance();
                runtime.putProperty("document", this.graalDocument);
            } else {
                this.document = new Document();
                // Set parent scope BEFORE calling init() because init() creates
                // RhinoFontFaceSet which needs scope
                if (runtime instanceof RhinoRuntime) {
                    Scriptable scope = ((RhinoRuntime) runtime).getScope();
                    this.document.setParentScope(scope);
                    try {
                        Scriptable proto = ScriptableObject.getClassPrototype(scope, "Document");
                        this.document.setPrototype(proto);
                    } catch (Exception e) {
                        System.err.println("Warning: Could not set Document prototype: " + e.getMessage());
                    }
                }
                this.document.init(this.windowHost);
                runtime.putProperty("document", this.document);

                // Store document in RhinoRuntime so it's accessible across Contexts
                if (runtime instanceof RhinoRuntime) {
                    ((RhinoRuntime) runtime).setMainThreadDocument(this.document);
                }
            }

            if (useGraal) {
                int width = 800;
                int height = 600;
                if (!headless && windowHost != null) {
                    width = windowHost.getWidth();
                    height = windowHost.getHeight();
                }
                Class<?> graalWindowClass = Class
                        .forName("com.w3canvas.javacanvas.backend.graal.impl.node.GraalWindow");
                Object gWindow = graalWindowClass.getDeclaredConstructor(int.class, int.class).newInstance(width,
                        height);
                gWindow.getClass()
                        .getMethod("setDocument",
                                Class.forName("com.w3canvas.javacanvas.backend.graal.impl.node.GraalDocument"))
                        .invoke(gWindow, this.graalDocument);
                this.graalWindow = gWindow;
                runtime.putProperty("window", this.graalWindow);
            } else {
                this.window = new Window();
                if (headless || windowHost == null) {
                    this.window.init(800, 600);
                } else {
                    this.window.init(windowHost.getWidth(), windowHost.getHeight());
                }
                this.window.setDocument(document);
                runtime.putProperty("window", this.window);

                // Store window in RhinoRuntime so it's accessible across Contexts
                if (runtime instanceof RhinoRuntime) {
                    ((RhinoRuntime) runtime).setMainThreadWindow(this.window);
                }
            }

            runtime.putProperty("log", new ScriptLogger());
            runtime.putProperty("console", new ScriptLogger());
            runtime.injectWebGPU();

            if (runtime instanceof RhinoRuntime) {
                ((RhinoRuntime) runtime).setSource(basePath);
            }

        } catch (Exception e) {
            System.err.println("ERROR: Failed to initialize backend: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void executeJSCode() throws ReflectiveOperationException {
        Properties properties = propertiesHolder.getProperties();
        List<String> jsClasses = PropertiesHolder.getJSClasses(properties);

        for (String className : jsClasses) {
            Object instance = Class.forName(className).getDeclaredConstructor().newInstance();
            if (instance instanceof Script) {
                // This part is tricky as it depends on Rhino Context.
                // For Graal, we might need a different approach if we support this legacy
                // loading.
                // For now, only support this if it's RhinoRuntime
                if (runtime instanceof RhinoRuntime) {
                    ((Script) instance).exec(Context.getCurrentContext(), ((RhinoRuntime) runtime).getScope());
                }
            }
        }
    }

    protected void readContent(StringBuffer sb) throws FileNotFoundException, IOException {
        // Legacy method, updated to use relative path
        java.io.File testFile = new java.io.File("src/js/development/test.js");
        if (!testFile.exists()) {
             System.err.println("Warning: test.js not found at " + testFile.getAbsolutePath());
             return;
        }

        int i = 1;
        BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(testFile)));

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
